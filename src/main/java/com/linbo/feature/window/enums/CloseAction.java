package com.linbo.feature.window.enums;

import com.linbo.feature.window.AbstractWindow;

import java.awt.*;
import java.util.function.Consumer;

/**
 * 预定义的窗口关闭行为
 */
public enum CloseAction {
    DISPOSE(Window::dispose),    //调用窗口的dispose方法
    EXIT(window -> System.exit(0));   //调用窗口的退出方法

    private final Consumer<AbstractWindow<?>> action;
    CloseAction(Consumer<AbstractWindow<?>> action){
        this.action = action;
    }

    public void doAction(AbstractWindow<?> window){
        action.accept(window);
    }
}
