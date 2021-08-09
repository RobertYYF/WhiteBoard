package com.robert.whiteboard_plus.components.buttons.controller;

import android.graphics.Color;
import android.util.Log;

import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.robert.whiteboard_plus.components.pen.WritePen;

/**
 * 负责控制书写笔的颜色
 */
public class ColorController {

    public static final String TAG = "ColorController";

    // 记录书写设置的笔迹颜色，默认为黑色
    private int color = Color.BLACK;
    private ColorPickerView colorPickerView;

    public ColorController(ColorPickerView colorPickerView) {
        this.colorPickerView = colorPickerView;
    }

    public void setColor() {
        Log.i("颜色更改为", " ：" + colorPickerView.getColor());
        int curColor = colorPickerView.getColor();
        this.color = curColor;
        WritePen.getInstance().changeColor(color);
    }

    public int getColor() {
        return color;
    }


}
