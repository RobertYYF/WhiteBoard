package com.robert.whiteboard_plus.model.strategy;

import android.graphics.Path;
import android.view.MotionEvent;

public interface PathStrategy {
    /**
     * 处理笔迹onDown事件
     */
    void downPath(Path path, MotionEvent event, float x, float y);
    /**
     * 处理笔迹onMove事件
     */
    void movePath(Path path, MotionEvent event, float preX, float preY, float curX, float curY);
    /**
     * 处理笔迹onUp事件
     */
    void upPath(Path path, MotionEvent event, float x, float y);
}
