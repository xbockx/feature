package com.linbo.feature.window.dialog;

import com.linbo.feature.window.AbstractWindow;

import javax.swing.*;
import java.awt.*;

/**
 * 编译失败对话框
 */
public class CompileErrorDialog extends AbstractDialog {
    public CompileErrorDialog(AbstractWindow parent, String text) {
        super(parent, "编译失败", new Dimension(600, 300));
        this.setLayout(new BorderLayout());
        JTextArea area = new JTextArea(text);
        this.addComponent(new JScrollPane(area), pane -> area.setEditable(false));

    }

    @Override
    protected void initDialogContent() {}
}
