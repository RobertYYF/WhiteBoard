package com.robert.whiteboard_plus.components.pen;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

/**
 * 圈选笔
 */
public class SelectPen extends Paint implements Pen {

    public static final String TAG = "SelectPen";

    // 选择笔单例
    private volatile static SelectPen selectPen;

    public static SelectPen getInstance() {
        if (selectPen == null) {
            synchronized (SelectPen.class) {
                if (selectPen == null) {
                    selectPen = new SelectPen();
                    // 设置圈选笔（虚线）
                    selectPen.setStyle(Style.STROKE);
                    selectPen.setAntiAlias(true);
                    selectPen.setStrokeWidth(5);
                    selectPen.setColor(Color.parseColor("#424343"));
                    selectPen.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));
                }
            }
        }
        return selectPen;
    }


    @Override
    public void changeSize(float width) {
        selectPen.setStrokeWidth(width);
    }

    @Override
    public void changeColor(int color) {
        selectPen.setColor(color);
    }
}
