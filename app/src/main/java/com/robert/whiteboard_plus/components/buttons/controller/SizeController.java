package com.robert.whiteboard_plus.components.buttons.controller;

import android.widget.SeekBar;

import com.robert.whiteboard_plus.components.pen.WritePen;

/**
 * 负责控制书写笔的粗细
 */
public class SizeController {

    public static final String TAG = "SizeController";

    // 记录书写设置的笔迹粗细，默认大小为8f
    private float size = 8f;
    private SeekBar seekBar;

    public SizeController(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    public void setSize() {
        float curSize = seekBar.getProgress();
        this.size = curSize;
        WritePen.getInstance().changeSize(size);
    }

    public float getSize() {
        return size;
    }

}
