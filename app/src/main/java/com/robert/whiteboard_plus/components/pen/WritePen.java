package com.robert.whiteboard_plus.components.pen;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * 书写笔
 */
public class WritePen extends Paint implements Pen, Cloneable {

    public static final String TAG = "WritePen";

    // 书写笔单例
    private volatile static WritePen writePen;

    public static WritePen getInstance() {
        if (writePen == null) {
            synchronized (WritePen.class) {
                if (writePen == null) {
                    writePen = new WritePen();
                    // 设置默认书写笔
                    writePen.setAntiAlias(true);
                    writePen.setStyle(Style.STROKE);
                    writePen.setStrokeJoin(Join.ROUND);
                    writePen.setStrokeCap(Cap.ROUND);
                    // 默认粗细：8f
                    writePen.setStrokeWidth(8f);
                    // 默认颜色：黑色
                    writePen.setColor(Color.BLACK);
                }
            }
        }
        return writePen;
    }

    @Override
    public void changeSize(float width) {
        writePen.setStrokeWidth(width);
    }

    @Override
    public void changeColor(int color) {
        writePen.setColor(color);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
