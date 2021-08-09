package com.robert.whiteboard_plus.whiteboard;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.robert.whiteboard_plus.components.pen.ErasePen;
import com.robert.whiteboard_plus.components.pen.SelectPen;
import com.robert.whiteboard_plus.components.movable.MovableView;
import com.robert.whiteboard_plus.components.movable.PathView;
import com.robert.whiteboard_plus.components.movable.SelectBoxView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 显示层
 * 负责储存、显示已绘制的笔迹、图片、圈选框等
 */
public class CacheWhiteBoard extends FrameLayout {

    public static final String TAG = "WhiteBoardLayout";

    // 橡皮擦路径
    private PathView erasePath;
    // 圈选路径
    private PathView selectPath;
    // 圈选框
    private SelectBoxView selectBox;
    // 圈选中的笔迹
    private List<PathView> selected;
    // 圈选中的图片
    private List<ImageView> selectedImg;

    // 位移量
    int deltaX = 0;
    int deltaY = 0;
    // 缩放倍数
    float scaleFactor = 1f;
    // 缩放中心
    float scaleCenterX = 0;
    float scaleCenterY = 0;

    public CacheWhiteBoard(@NonNull Context context) {
        super(context);
        setWillNotDraw(false);
        init();
    }

    public CacheWhiteBoard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        init();
    }

    public CacheWhiteBoard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 根据子View宽高和定位绘制全部子View
        for (int i=0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            MovableView movableView = (MovableView) childView;

            if (movableView.getRoundHeight() == 0 || movableView.getRoundHeight() == 0) {
                childView.layout(left, top, right, bottom);
            } else {
                int l = movableView.getXPos();
                int t = movableView.getYPos();
                childView.layout(l ,t, l + movableView.getRoundWidth(), t + movableView.getRoundHeight());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "重绘CacheWhiteBoard Canvas");
        canvas.translate(-deltaX, -deltaY);
        canvas.scale(scaleFactor, scaleFactor, scaleCenterX, scaleCenterY);
    }

    /**
     * 初始化显示层
     */
    private void init() {
        // 初始化擦除路径
        erasePath = new PathView(getContext());
        erasePath.setPaint(ErasePen.getInstance());
        // 初始化圈选路径
        selectPath = new PathView(getContext());
        selectPath.setPaint(SelectPen.getInstance());
        // 初始化圈选框
        selectBox = new SelectBoxView(getContext());
        selectBox.setPaint(SelectPen.getInstance());
        // 初始圈选列表为空
        selected = new CopyOnWriteArrayList<>();
        selectedImg = new CopyOnWriteArrayList<>();
    }


    /**
     * Getters
     */
    public PathView getSelectPath() {
        return selectPath;
    }

    public List<PathView> getSelected() {
        return selected;
    }

    public SelectBoxView getSelectBox() {
        return selectBox;
    }

    public List<ImageView> getSelectedImg() {
        return selectedImg;
    }

    /**
     * Setters
     */

    public void setDeltaX(int deltaX) {
        this.deltaX = deltaX;
    }

    public void setDeltaY(int deltaY) {
        this.deltaY = deltaY;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setScaleCenterX(float scaleCenterX) {
        this.scaleCenterX = scaleCenterX;
    }

    public void setScaleCenterY(float scaleCenterY) {
        this.scaleCenterY = scaleCenterY;
    }
}
