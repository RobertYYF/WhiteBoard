package com.robert.whiteboard_plus.handler;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.robert.whiteboard_plus.model.Mode;
import com.robert.whiteboard_plus.components.movable.DragImageView;
import com.robert.whiteboard_plus.components.movable.PathView;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.model.utils.DrawPathUtils;
import com.robert.whiteboard_plus.model.strategy.PathStrategy;
import com.robert.whiteboard_plus.model.strategy.SelectPathStrategy;

import org.apache.commons.lang3.tuple.MutablePair;

public class SelectHandler extends ActionHandler {

    public static final String TAG = "SelectHandler";

    private PathStrategy selectStrategy;

    private long startTimer;
    private long endTimer;

    private float startDis;
    private PointF startPoint = new PointF(); // 起始点
    private PointF midPoint; // 中心点
    private MODE mode = MODE.DEFAULT;

    enum MODE {
        DRAG, ZOOM, DEFAULT
    }

    public SelectHandler(WhiteBoard whiteBoard) {
        super(whiteBoard);
        selectStrategy = new SelectPathStrategy(whiteBoard);
    }

    @Override
    public void handleAction(MotionEvent event) {
        if (whiteBoard.getMode() == Mode.SELECT && !whiteBoard.isTourOngoing())
            handle(event);
        else if (whiteBoard.isTourOngoing())
            getTourHandler().handleAction(event);
        else if (getSuccessor() != null)
            getSuccessor().handleAction(event);
    }

    /**
     * 处理MotionEvent
     * @param event 待处理的MotionEvent事件
     */
    private void handle(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE.ZOOM) {
                    onScale(event);
                }
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onUp(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(event);
                break;
            default:
                break;
        }
    }

    /**
     * 处理圈选ACTION_DOWN
     */
    private void onDown(MotionEvent event) {
        // 进行一些初始化设定
        // 清空绘制层
        whiteBoard.getRuntimeWhiteBoard().removeAllPath();
        mode = MODE.DRAG;
        // 计时
        startTimer = System.currentTimeMillis();
        // 清空历史追踪记录
        whiteBoard.getWritePaths().clear();
        whiteBoard.getWritePathIds().clear();
        whiteBoard.getHistoryPos().clear();
        // 添加新的路径记录
        whiteBoard.getWritePathIds().add(event.getPointerId(0));
        whiteBoard.getWritePaths().add(new Path());
        whiteBoard.getHistoryPos().add(new MutablePair<>(DrawPathUtils.locateX(event.getX(), whiteBoard),
                DrawPathUtils.locateY(event.getY(), whiteBoard)));
        // 设置起始点
        startPoint.set(DrawPathUtils.locateX(event.getX(), whiteBoard), DrawPathUtils.locateY(event.getY(), whiteBoard));

        // 调用圈选策略的onDown
        whiteBoard.setSelectPath(new Path());
        selectStrategy.downPath(whiteBoard.getSelectPath(), event, event.getX(), event.getY());
    }

    /**
     * 处理圈选ACTION_POINTER_DOWN
     */
    private void onPointerDown(MotionEvent event) {
        endTimer = System.currentTimeMillis();
        if (endTimer - startTimer < 120 && whiteBoard.isSelectOngoing()) {
            Log.i(TAG, "取消圈选，交由漫游处理");
            whiteBoard.setTourOngoing(true);
            getTourHandler().handleAction(event);
            // 清除绘制层选择
            whiteBoard.resetSelect();
            return;
        }

        mode = MODE.ZOOM;
        whiteBoard.setScaleOngoing(true);

        // 计算起始距离 (用于计算缩放比例)
        startDis = distance(event);
        // 防止不规则手指触碰
        if (startDis > 10f) {
            midPoint = mid(event);
        }
    }

    /**
     * 处理圈选ACTION_MOVE
     */
    private void onMove(MotionEvent event) {
        // 更新当前手指的历史位置
        selectStrategy.movePath(whiteBoard.getSelectPath(), event, whiteBoard.getHistoryPos().get(0).left,
                whiteBoard.getHistoryPos().get(0).right, event.getX(), event.getY());
        whiteBoard.getHistoryPos().get(0).left = DrawPathUtils.locateX(event.getX(0), whiteBoard);
        whiteBoard.getHistoryPos().get(0).right = DrawPathUtils.locateY(event.getY(0), whiteBoard);
    }

    /**
     * 处理圈选ACTION_UP
     */
    private void onUp(MotionEvent event) {
        whiteBoard.getHistoryPos().clear();
        selectStrategy.upPath(whiteBoard.getSelectPath(), event, event.getX(), event.getY());
    }

    /**
     * 处理圈选ACTION_POINTER_UP
     */
    private void onPointerUp(MotionEvent event) {
        if (event.getActionIndex() == 0) {
            whiteBoard.getHistoryPos().get(0).left = DrawPathUtils.locateX(event.getX(1), whiteBoard);
            whiteBoard.getHistoryPos().get(0).right = DrawPathUtils.locateY(event.getY(1), whiteBoard);
        }
        mode = MODE.DEFAULT;
    }

    /**
     * 处理圈选ACTION_MOVE中的缩放部分
     */
    private void onScale(MotionEvent event) {
        // 结束距离 (计算缩放比例）
        float endDis = distance(event);
        // 防止不规则手指触碰
        if (endDis > 10f) {
            float scaleFactor = endDis / startDis;
            // 缩放笔迹
            for (PathView s : whiteBoard.getCacheWhiteBoard().getSelected()) {
                RectF rectF = new RectF();
                Matrix scaleMatrix = s.getScaleMatrix();
                s.getPath().computeBounds(rectF, true);
                scaleMatrix.setScale(scaleFactor, scaleFactor, midPoint.x, midPoint.y);
                s.getPath().transform(scaleMatrix);
                s.requestLayout();
            }

            // 缩放图片
            for (ImageView i : whiteBoard.getCacheWhiteBoard().getSelectedImg()) {
                DragImageView dragImg = (DragImageView) i;

                dragImg.setScaleFactor(scaleFactor);
                dragImg.setScaleX(scaleFactor);
                dragImg.setRoundWidth((int) (dragImg.getRoundWidth() * scaleFactor));
                dragImg.setRoundHeight((int) (dragImg.getRoundHeight() * scaleFactor));

                dragImg.setScaleType(ImageView.ScaleType.MATRIX);
                dragImg.setAdjustViewBounds(true);
                dragImg.requestLayout();
            }

            startDis = endDis;
        }

        selectStrategy.movePath(whiteBoard.getSelectPath(), event, whiteBoard.getHistoryPos().get(0).left,
                whiteBoard.getHistoryPos().get(0).right, midPoint.x, midPoint.y);
        whiteBoard.getHistoryPos().get(0).left = DrawPathUtils.locateX(midPoint.x, whiteBoard);
        whiteBoard.getHistoryPos().get(0).right = DrawPathUtils.locateY(midPoint.y, whiteBoard);
    }

    /**
     * 计算两点之间的距离
     */
    public float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx*dx+dy*dy);
    }

    /**
     * 计算两个点的中心点
     */
    public PointF mid(MotionEvent event){
        float x1 = DrawPathUtils.locateX(event.getX(1), whiteBoard);
        float x2 = DrawPathUtils.locateX(event.getX(0), whiteBoard);
        float y1 = DrawPathUtils.locateY(event.getY(1), whiteBoard);
        float y2 = DrawPathUtils.locateY(event.getY(0), whiteBoard);
        float midX = (x1 + x2) / 2;
        float midY = (y1 + y2) / 2;
        return new PointF(midX,midY);
    }

}
