package com.robert.whiteboard_plus.components.pen;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * 擦除笔
 */
public class ErasePen extends Paint implements Pen {

    public static final String TAG = "ErasePen";

    // 擦除笔单例
    private volatile static ErasePen erasePen;

    private ErasePen() {}

    public static ErasePen getInstance() {
        if (erasePen == null) {
            synchronized (ErasePen.class) {
                if (erasePen == null) {
                    erasePen = new ErasePen();
                    // 设置橡皮擦
                    erasePen.setStyle(Style.STROKE);
                    erasePen.setStrokeCap(Cap.ROUND);
                    erasePen.setColor(Color.WHITE);
//                    erasePen.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }
            }
        }
        return erasePen;
    }

    @Override
    public void changeSize(float width) {
        erasePen.setStrokeWidth(width);
    }

    @Override
    public void changeColor(int color) {
        erasePen.setColor(color);
    }
}
