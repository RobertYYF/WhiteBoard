package com.robert.whiteboard_plus.components.movable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 可移动笔迹
 */
public class PathView extends View implements MovableView {

    public static final String TAG = "PathView";

    private Path path;
    private Paint paint;
    private RectF roundBox;

    private int width;
    private int height;

    private Matrix scaleMatrix;

    // 记录view矩形左上角顶点坐标
    private int x;
    private int y;

    public PathView(Context context) {
        super(context);
        init();
    }

    public PathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 利用RectF测量Path外接矩形大小
        roundBox = new RectF();
        path.computeBounds(roundBox, true);

        // 计算整数外接矩形
        computeSize();

        // 设置View的宽高
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(-x, -y);
        canvas.drawPath(path, paint);
        super.onDraw(canvas);
    }

    private void init() {
        path = new Path();
        scaleMatrix = new Matrix();
    }

    /**
     * 计算View所需宽高
     */
    public void computeSize() {
        width = (int) Math.ceil(roundBox.width()) + (int) (5 * paint.getStrokeWidth());
        height = (int) Math.ceil(roundBox.height()) + (int) (5 * paint.getStrokeWidth());
        x = (int) roundBox.centerX() - width / 2;
        y = (int) roundBox.centerY() - height / 2;
    }

    /**
     * Setters
     */
    public void setPath(Path path) {
        this.path = path;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    @Override
    public void setRoundHeight(int height) {
        this.height = height;
    }

    @Override
    public void setRoundWidth(int width) {
        this.width = width;
    }

    @Override
    public void setXPos(int x) {
        this.x = x;
    }

    @Override
    public void setYPos(int y) {
        this.y = y;
    }

    public void setScaleMatrix(Matrix scaleMatrix) {
        this.scaleMatrix = scaleMatrix;
    }

    /**
     * Getters
     */
    public Path getPath() {
        return path;
    }

    public Paint getPaint() {
        return paint;
    }

    public int getRoundHeight() {
        return height;
    }

    public int getXPos() {
        return x;
    }

    public int getYPos() {
        return y;
    }

    public int getRoundWidth() {
        return width;
    }

    public Matrix getScaleMatrix() {
        return scaleMatrix;
    }
}
