package com.linbo.feature.window.service;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linbo.feature.common.ExecuteDataListener;
import com.linbo.feature.common.FileType;
import com.linbo.feature.common.TestcaseDataListener;
import com.linbo.feature.entity.config.ProjectConfigure;
import com.linbo.feature.manage.FileManager;
import com.linbo.feature.manage.ProcessExecuteEngine;
import com.linbo.feature.runner.core.FileService;
import com.linbo.feature.runner.domain.Record;
import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.TestCase;
import com.linbo.feature.runner.service.impl.RunServiceImpl;
import com.linbo.feature.window.BuildWindow;
import com.linbo.feature.window.MainWindow;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.undo.UndoManager;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MainService extends AbstractService {
    //当前项目的路径和项目名称
    private String path;
    //当前项目的配置文件，包括主类、java可执行文件位置等。
    private ProjectConfigure configure;
    //用于记录当前正在编辑的文件
    private File currentFile;
    //重做管理器，用于编辑框支持撤销和重做操作的
    private UndoManager undoManager;
    //用于记录当前项目是否处于运行状态
    private boolean isProjectRunning = false;

    private java.util.List<Map<Integer, String>> executeEntityList;
    private java.util.List<Map<Integer, String>> testcaseEntityList;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 设定当前项目的名称和路径
     *
     * @param path 路径
     */
    public void setPath(String path) {
        this.path = path.replace("\\", "/");
    }

    /**
     * 获取当前项目的配置
     *
     * @return 项目配置
     */
    public ProjectConfigure getConfigure() {
        return configure;
    }

    /**
     * 加载项目配置文件
     */
    public void loadProjectConfig() {
        File file = new File(path + "/.config");
        if (file.exists()) {
            try (ObjectInputStream stream = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
                configure = (ProjectConfigure) stream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.updateAndSaveConfigure(new ProjectConfigure(new HashMap<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>()));
        }
    }

    /**
     * 更新并保存新的设置
     *
     * @param configure 新的设置
     */
    public void updateAndSaveConfigure(ProjectConfigure configure) {
        JButton button = this.getComponent("main.button.run");
        this.configure = configure;
        try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(Paths.get(path + "/.config")))) {
            stream.writeObject(configure);
            stream.flush();
            if (button != null) {
                if (configure.getProps().isEmpty()) {
//                    button.setEnabled(false);
                    button.setToolTipText("请先完成配置！");
                } else {
                    button.setEnabled(true);
                    button.setToolTipText("点击编译运行");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 运行按钮的行为，包括以下两种行为：
     * - 如果项目处于运行状态，那么点击就会停止项目。
     * - 如果项目没有处于运行状态，那么就会启动项目。
     */
    public void runButtonAction() {
        MainWindow window = (MainWindow) this.getWindow();
        JButton button = this.getComponent("main.button.run");
        JTextArea consoleArea = this.getComponent("main.textarea.console");
        //判断当前项目是否已经开始运行了，分别进行操作
        if (!this.isProjectRunning) {

            //如果项目没有运行，那么需要先编译项目源代码，如果编译成功，那么就可以开始运行项目了
            button.setEnabled(false);
            consoleArea.setText("加载文件...\n");

            new Thread(() -> {
                // 预处理
                List<Record> records = new ArrayList<>();
                final Map<String, String> props = configure.getProps();
                final Map<String, Integer> eMap = configure.getExecuteKeyMap().get("curr");
                for (Map<Integer, String> map : this.executeEntityList) {
                    Record record = new Record();
                    record.setId(Integer.parseInt(map.get(eMap.get(props.get("eid")))));
                    record.setPid(Integer.parseInt(map.get(eMap.get(props.get("pid")))));
                    record.setCode(map.get(eMap.get(props.get("code"))));
                    records.add(record);
                }
                consoleArea.append("执行文件预处理... OK!\n");

                List<TestCase> testCases = new ArrayList<>();
                final Map<String, Integer> tMap = configure.getTestcaseKeyMap().get("curr");
                for (Map<Integer, String> map : this.testcaseEntityList) {
                    TestCase testCase = new TestCase();
                    testCase.setId(Integer.parseInt(map.get(tMap.get(props.get("tTid")))));
                    testCase.setPid(Integer.parseInt(map.get(tMap.get(props.get("tPid")))));
                    testCase.setInput(map.get(tMap.get(props.get("tInput"))));
                    testCase.setExpOutput(map.get(tMap.get(props.get("tExpOutput"))));
                    testCases.add(testCase);
                }
                consoleArea.append("测试用例文件预处理... OK!\n");

                consoleArea.setText("写入文件...\n");
                final FileService fileService = FileService.getInstance();
                try {
                    fileService.writeCode(records);
                    consoleArea.append("执行文件写入... OK!\n");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final Map<Integer, List<TestCase>> testcaseMap = fileService.getTestCases(testCases);
                consoleArea.append("测试用例加载... OK!\n");

                // 编译执行
                final List<Result> exec = new RunServiceImpl().exec(records, testcaseMap);
                if (!exec.isEmpty()) {
                    consoleArea.append("结果写入... OK!\n");
                    window.refreshFileTree();
                }
            }).start();

//            try {
//                log.info("[TEST]: {}", objectMapper.writeValueAsString(records));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }


//            ProcessResult result = ProcessExecuteEngine.buildProject(path);
//            if (result.getExitCode() != 0) {
//                CompileErrorDialog dialog = new CompileErrorDialog(this.getWindow(), result.getOutput());
//                dialog.openDialog();
//                button.setEnabled(true);
//                return;
//            }
            //项目编译完成之后，可能会新增文件，所以需要刷新一下文件树
//            window.refreshFileTree();
            //新开一个线程实时对项目的运行进行监控，并实时将项目的输出内容更新到控制台
//            new Thread(() -> {
//                this.isProjectRunning = true;
//                consoleArea.setText("正在编译项目源代码...编译完成，程序已启动：\n");
//                button.setText("停止");
//                button.setEnabled(true);
//                //准备工作完成之后，就可以正式启动进程了，这里最后会返回执行结果
//                ProcessResult res = ProcessExecuteEngine.startProcess(
//                        path, configure.getJavaCommand(), configure.getMainClass(), consoleArea::append);
//                if (res.getExitCode() != 0)
//                    consoleArea.append(res.getOutput());
//                consoleArea.append("\n进程已结束，退出代码 " + res.getExitCode());
//                button.setText("运行");
//                this.isProjectRunning = false;
//            }).start();
        } else {
            //如果项目正在运行，那么点击按钮就相当于是结束项目运行
//            ProcessExecuteEngine.stopProcess();
            this.isProjectRunning = false;
        }
    }

    /**
     * 构建按钮的行为，很明显，直接构建就完事了
     */
    public void enterBuildWindow() {
//        this.getWindow().dispose();
//        if (configure.getExecuteKeyMap().get("curr").isEmpty() || configure.getTestcaseKeyMap().get("curr").isEmpty()) {
//            return;
//        }
        BuildWindow window = new BuildWindow(path, configure);
        window.openWindow();
    }

    /**
     * 设置按钮的行为，更简单了，直接打开设置面板就完事
     */
//    public void settingButtonAction() {
//        MainWindow window = (MainWindow) this.getWindow();
//        ProjectConfigDialog dialog = new ProjectConfigDialog(window, this, configure);
//        dialog.openDialog();
//    }

    /**
     * 创建一个新的源代码新的文件并生成默认代码
     * mod: 添加代码执行文件
     */
    public void addProjectFile(int type) {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            MainWindow window = (MainWindow) this.getWindow();
            final File selectedFile = chooser.getSelectedFile();
            JTextArea consoleArea = this.getComponent("main.textarea.console");
            postAddProjectFile(selectedFile, type, window, consoleArea, false);
//            this.createFile(filename);
        }
    }

    private void postAddProjectFile(File file, int type, MainWindow window, JTextArea consoleArea, boolean loaded) {
        new Thread(() -> {
            consoleArea.append("正在读取文件...\n");
            if (!loaded) {
                try (FileWriter writer = new FileWriter(path + "/workspace/" + file.getName());
                     FileReader reader = new FileReader(file.getAbsolutePath())) {
                    char[] cbuf = new char[1024];
                    int len;
                    while ((len = reader.read(cbuf)) != -1) {
                        writer.write(cbuf);
                    }
                    writer.flush();
                    window.refreshFileTree();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                configure.getFilenames().add(file.getName());
            }

            if (type == FileType.EXECUTE_FILE) {
                ExecuteDataListener dataListener = new ExecuteDataListener();
                EasyExcel.read(file, dataListener).sheet().doRead();
                this.executeEntityList = dataListener.getRes();
                Map<String, Map<String, Integer>> map = configure.getExecuteKeyMap();
                map.clear();
                map.put("curr", dataListener.getHeadSet());
                consoleArea.append("当前执行文件： " + file.getName() + " [Read Execute File Finished...]\n");
            }
            if (type == FileType.TESTCASE_FILE) {
                TestcaseDataListener dataListener = new TestcaseDataListener();
                EasyExcel.read(file, dataListener).sheet().doRead();
                this.testcaseEntityList = dataListener.getRes();
                Map<String, Map<String, Integer>> map = configure.getTestcaseKeyMap();
                map.clear();
                map.put("curr", dataListener.getHeadSet());
                consoleArea.append("当前测试用例文件： " + file.getName() + "[Read Testcase File Finished...]\n");
            }
            updateAndSaveConfigure(configure);
        }).start();
    }

    public void deleteProjectFile() {
        this.currentFile.delete();
    }

    /**
     * 配置文件树的右键弹出窗口
     *
     * @return MouseAdapter
     */
    public MouseAdapter fileTreeRightClick() {
        JTree fileTree = this.getComponent("main.tree.files");
        JPopupMenu treePopupMenu = this.getComponent("main.popup.tree");
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    treePopupMenu.show(fileTree, e.getX(), e.getY());
            }
        };
    }

    /**
     * 配置编辑框的各项功能
     */
    public void setupEditArea() {
        JTextArea editArea = this.getComponent("main.textarea.edit");
        //当文本内容发生变化时，自动写入到文件中
        editArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                MainService.this.saveFile();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                MainService.this.saveFile();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                MainService.this.saveFile();
            }
        });
        //按下Tab键时，应该输入四个空格，而不是一个Tab缩进（不然太丑）
        editArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 9) {
                    e.consume();
                    editArea.insert("    ", editArea.getCaretPosition());
                }
            }
        });
        //由于默认的文本区域不支持重做和撤销操作，需要使用UndoManager进行配置，这里添加快捷键
        editArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
        editArea.getActionMap().put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) undoManager.redo();
            }
        });
        editArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        editArea.getActionMap().put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) undoManager.undo();
            }
        });
    }

    /**
     * 让控制台输入重定向到进程的系统输入中
     *
     * @return KeyAdapter
     */
    public KeyAdapter inputRedirect() {
        JTextArea consoleArea = this.getComponent("main.textarea.console");
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (isProjectRunning) {
                    String str = String.valueOf(e.getKeyChar());
                    ProcessExecuteEngine.redirectToProcess(str);
                    consoleArea.append(str);
                }
            }
        };
    }

    /**
     * 切换当前编辑的文件，并更新编辑面板中的内容
     *
     * @param path 文件路径
     */
    public void switchEditFile(String path) {
        JTextArea editArea = this.getComponent("main.textarea.edit");
        currentFile = null;
        File file = new File(path);
        if (file.isDirectory()) return;
        editArea.getDocument().removeUndoableEditListener(undoManager);
        if (file.getName().endsWith(".class")) {
            editArea.setText(ProcessExecuteEngine.decompileCode(file.getAbsolutePath()));
            editArea.setEditable(false);
        } else {
            try (FileReader reader = new FileReader(file)) {
                StringBuilder builder = new StringBuilder();
                int len;
                char[] chars = new char[1024];
                while ((len = reader.read(chars)) > 0)
                    builder.append(chars, 0, len);
                editArea.setText(builder.toString());
                editArea.setEditable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editArea.getDocument().addUndoableEditListener((undoManager = new UndoManager()));
        currentFile = file;
    }

    private void deleteFile(String name) {
        if (name == null) return;
        String[] split = name.split("\\.");
        String className = split[split.length - 1];
        String packageName = name.substring(0, name.length() - className.length() - 1);

        File file = new File(path + "/src/" + packageName.replace(".", "/") + "/" + className + ".java");
        if (file.exists() && file.delete()) {
            JOptionPane.showMessageDialog(this.getWindow(), "文件删除成功！");
        } else {
            JOptionPane.showMessageDialog(this.getWindow(), "文件删除失败，文件不存在？");
        }
        MainWindow window = (MainWindow) this.getWindow();
        window.refreshFileTree();
    }

    /**
     * 创建源文件，并生成默认代码
     *
     * @param name 名称
     */
    private void createFile(String name) {
        MainWindow window = (MainWindow) this.getWindow();
        if (name == null) return;
        String[] split = name.split("\\.");
        String className = split[split.length - 1];
        String packageName = name.substring(0, name.length() - className.length() - 1);

        try {
            File dir = new File(path + "/src/" + packageName.replace(".", "/"));
            if (!dir.exists() && !dir.mkdirs()) {
                JOptionPane.showMessageDialog(window, "无法创建文件夹！");
                return;
            }
            File file = new File(path + "/src/" + packageName.replace(".", "/") + "/" + className + ".java");
            if (file.exists() || !file.createNewFile()) {
                JOptionPane.showMessageDialog(window, "无法创建，此文件已存在！");
                return;
            }
            FileWriter writer = new FileWriter(file);
            writer.write(FileManager.defaultCode(className, packageName));
            writer.flush();
            window.refreshFileTree();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存当前编辑框中的内容到当前文件中
     */
    private void saveFile() {
        JTextArea editArea = this.getComponent("main.textarea.edit");
        if (currentFile == null) return;
        try (FileWriter writer = new FileWriter(currentFile)) {
            writer.write(editArea.getText());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置后重启项目后加载文件
     */
    public void loadFiles() {
        Map<String, String> props = configure.getProps();
        MainWindow window = (MainWindow) this.getWindow();
        JTextArea consoleArea = this.getComponent("main.textarea.console");
        if (props.get("executeFile") != null) {
            File executeFile = new File(path + "/workspace/" + props.get("executeFile"));
            postAddProjectFile(executeFile, FileType.EXECUTE_FILE, window, consoleArea, true);
        }
        if (props.get("testcaseFile") != null) {
            File testcaseFile = new File(path + "/workspace/" + props.get("testcaseFile"));
            postAddProjectFile(testcaseFile, FileType.TESTCASE_FILE, window, consoleArea, true);
        }
    }
}
