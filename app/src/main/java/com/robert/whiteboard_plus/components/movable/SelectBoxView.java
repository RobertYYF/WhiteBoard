package com.robert.whiteboard_plus.components.movable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 可移动圈选框
 */
public class SelectBoxView extends View implements MovableView {

    public static final String TAG = "SelectBoxView";

    private RectF rectF;
    private Paint paint;

    // 记录view矩形左上角顶点坐标
    private int x;
    private int y;

    private int width;
    private int height;

    public SelectBoxView(Context context) {
        super(context);
        init();
    }

    public SelectBoxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectBoxView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "重新测量选择框");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 设置View的宽高
        computeSize();
        setMeasuredDimension(width, height);
    }

    /**
     * 计算View所需宽高
     */
    public void computeSize() {
        width = (int) Math.ceil(rectF.width()) + (int) (5 * paint.getStrokeWidth());
        height = (int) Math.ceil(rectF.height()) + (int) (5 * paint.getStrokeWidth());
        x = (int) rectF.centerX() - width / 2;
        y = (int) rectF.centerY() - height / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "重绘选择框");
        canvas.translate(-x, -y);
        canvas.drawRect(rectF, paint);
        super.onDraw(canvas);
    }

    /**
     * Setters
     */
    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    @Override
    public void setRoundWidth(int width) {
        this.width = width;
    }

    @Override
    public void setRoundHeight(int height) {
        this.height = height;
    }

    @Override
    public void setXPos(int x) {
        this.x = x;
    }

    @Override
    public void setYPos(int y) {
        this.y = y;
    }

    /**
     * Getters
     */
    public int getXPos() {
        return x;
    }

    public int getYPos() {
        return y;
    }

    public int getRoundHeight() {
        return height;
    }

    public int getRoundWidth() {
        return width;
    }

    public RectF getRectF() {
        return rectF;
    }

}
