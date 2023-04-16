package com.linbo.feature.window;

import com.linbo.feature.entity.config.ProjectConfigure;
import com.linbo.feature.window.enums.CloseAction;
import com.linbo.feature.window.service.BuildService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

public class BuildWindow extends AbstractWindow <BuildService>{

    private final String path;

    private DefaultMutableTreeNode root;

    //当前项目的配置文件，包括主类、java可执行文件位置等。
    private ProjectConfigure configure;

    private final Dimension comboBoxDim = new Dimension(200, 25);
    private final Dimension labelDim = new Dimension(100, 25);

    private java.util.List<String> executeKeys;
    private java.util.List<String> testcaseKeys;


    public BuildWindow(String path, ProjectConfigure configure) {
        super("配置", new Dimension(500, 600), true, BuildService.class);
        //设定路径和项目名称，然后开始配置窗口内容
        this.path = path;
        //窗口关闭不能直接退出程序，因为要回到欢迎界面
        this.setDefaultCloseAction(CloseAction.DISPOSE);
        //为业务层设定当前项目的路径
        service.setPath(path);
        //然后是加载当前项目的配置，项目的配置不同会影响组件的某些显示状态
//        service.loadProjectConfig();
        this.configure = configure;
        this.executeKeys = new ArrayList<>(configure.getExecuteKeyMap().get("curr").keySet());
        this.testcaseKeys = new ArrayList<>(configure.getTestcaseKeyMap().get("curr").keySet());
        //最后再初始化窗口内容
        this.initWindowContent();
    }

    @Override
    protected void initWindowContent() {

        JPanel main = new JPanel(new BorderLayout());

        JPanel content = new JPanel(new GridLayout(9,1));

        final Map<String, String> existProps = configure.getProps();

//        System.out.println(configure.getFilenames().toArray(new String[0]));

        // 代码 ...
        JPanel codePan = new JPanel();
        JLabel codeLabel = new JLabel("执行文件");
        codeLabel.setFont(new Font("微软雅黑",Font.PLAIN,15));
        JComboBox codeComboBox = new JComboBox();
        for (String a: configure.getFilenames().toArray(new String[0])) {
            codeComboBox.addItem(a);
        }
        codeLabel.setPreferredSize(labelDim);
        codeComboBox.setPreferredSize(comboBoxDim);
        codeComboBox.setSelectedItem(existProps.get("executeFile"));
        codePan.add(codeLabel);
        codePan.add(codeComboBox);
        codeComboBox.addItemListener(e -> {
            if (e.getStateChange() == 1) {
                System.out.println("test: " + codeComboBox.getSelectedItem().toString());
            }
        });
        content.add(codePan);

        // 关联 ID ...
        JComboBox eidCB = createComboBox(executeKeys, existProps.get("eid"));
        content.add(createListPanel("执行ID", eidCB));

        JComboBox pidCB = createComboBox(executeKeys, existProps.get("pid"));
        content.add(createListPanel("题目ID", pidCB));

        JComboBox codeCB = createComboBox(executeKeys, existProps.get("code"));
        content.add(createListPanel("代码", codeCB));

        // 测试用例 ...
        JComboBox testcaseCB = createComboBox(configure.getFilenames(), existProps.get("testcaseFile"));
        content.add(createListPanel("测试用例", testcaseCB));

        JComboBox tcidCB = createComboBox(testcaseKeys, existProps.get("tTid"));
        content.add(createListPanel("测试用例ID", tcidCB));

        JComboBox tPidCB = createComboBox(testcaseKeys, existProps.get("tPid"));
        content.add(createListPanel("题目ID", tPidCB));

        JComboBox tInputCB = createComboBox(testcaseKeys, existProps.get("tInput"));
        content.add(createListPanel("输入", tInputCB));

        JComboBox tExpOutputCB = createComboBox(testcaseKeys, existProps.get("tExpOutput"));
        content.add(createListPanel("预期输出", tExpOutputCB));


        JPanel btnPan = new JPanel();
        JButton buildBtn = new JButton("构建");
        JButton cancelBtn = new JButton("取消");
        btnPan.add(buildBtn);
        btnPan.add(cancelBtn);

        buildBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Map<String, String> props = configure.getProps();
                props.clear();
                props.put("executeFile", codeComboBox.getSelectedItem().toString());
                props.put("eid", eidCB.getSelectedItem().toString());
                props.put("pid", pidCB.getSelectedItem().toString());
                props.put("code", codeCB.getSelectedItem().toString());

                props.put("testcaseFile", testcaseCB.getSelectedItem().toString());
                props.put("tTid", tcidCB.getSelectedItem().toString());
                props.put("tPid", tPidCB.getSelectedItem().toString());
                props.put("tInput", tInputCB.getSelectedItem().toString());
                props.put("tExpOutput", tExpOutputCB.getSelectedItem().toString());
                service.updateAndSaveConfigure(configure);
                closeWindow();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //取消按钮，直接退出程序
                closeWindow();
            }
        });

        main.add(content,BorderLayout.CENTER);
        main.add(btnPan,BorderLayout.SOUTH);

        this.add(main);
    }

    private JComboBox createComboBox(java.util.List<String> arr, Object selectedItem) {
        JComboBox comboBox = new JComboBox();
        for (String a: arr) {
            comboBox.addItem(a);
        }
        comboBox.setSelectedItem(selectedItem);
        comboBox.setPreferredSize(comboBoxDim);
        return comboBox;
    }

    private JPanel createListPanel(String label, JComboBox comboBox) {
        JPanel pan = new JPanel();
        JLabel title = new JLabel(label);
        title.setFont(new Font("微软雅黑",Font.PLAIN,15));
        title.setPreferredSize(labelDim);
        pan.add(title);
        pan.add(comboBox);
        return pan;
    }

    @Override
    protected boolean onClose() {
//        //关闭之前如果还有运行的项目没有结束，一定要结束掉
//        ProcessExecuteEngine.stopProcess();
//        //然后回到初始界面
//        WelcomeWindow window = new WelcomeWindow();
//        window.openWindow();
        return true;
    }
}
