package com.robert.whiteboard_plus.components.movable;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

/**
 * 可移动图片
 */
public class DragImageView extends androidx.appcompat.widget.AppCompatImageView implements MovableView {

    public static final String TAG = "DragImageView";

    private int xPos = 0;
    private int yPos = 0;
    private int width;
    private int height;

    private float scaleFactor = 1;

    public DragImageView(Context context) {
        super(context);
    }

    public DragImageView(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
    }

    public DragImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(-xPos, -yPos);
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public int getXPos() {
        return xPos;
    }

    @Override
    public int getYPos() {
        return yPos;
    }

    @Override
    public void setXPos(int x) {
        xPos = x;
    }

    @Override
    public void setYPos(int y) {
        yPos = y;
    }

    public int getRoundWidth() {
        return width;
    }

    public void setRoundWidth(int width) {
        this.width = width;
    }

    public int getRoundHeight() {
        return height;
    }

    public void setRoundHeight(int height) {
        this.height = height;
    }
}
