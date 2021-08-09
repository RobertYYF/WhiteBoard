package com.robert.whiteboard_plus.handler;

import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.robert.whiteboard_plus.model.Mode;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.model.utils.DrawPathUtils;

public class TourHandler extends ActionHandler {

    public static final String TAG = "TourHandler";

    private GestureDetector gestureDetector;

    // 手指数量
    private int counter = 0;

    private float startDis;
    private PointF startPoint = new PointF(); // 起始点
    private PointF midPoint; // 中心点
    private MODE mode = MODE.DEFAULT;

    enum MODE {
        DRAG, ZOOM, DEFAULT
    }

    public TourHandler(WhiteBoard whiteBoard) {
        super(whiteBoard);
        gestureDetector = new GestureDetector(whiteBoard.getContext(), new TourGestureDetector());
    }

    @Override
    public void handleAction(MotionEvent event) {
        setTourMode(event);
        boolean isTourMode = whiteBoard.isTourOngoing();
        if (!isTourMode && getSuccessor() != null)
            getSuccessor().handleAction(event);
        else if (isTourMode)
            handle(event);
    }

    /**
     * 根据手指数量以及当前白板模式判断是否开启/关闭漫游模式
     * @param event 待处理的MotionEvent事件
     */
    private void setTourMode(MotionEvent event) {
        // 如果处于多指书写模式，禁用漫游
        if (whiteBoard.isMultiWriteOngoing() && whiteBoard.getMode() == Mode.WRITE) {
            whiteBoard.setTourOngoing(false);
            return;
        }

        // 计算当前屏幕上手指数量，根据手指数量判断是否进入漫游模式
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                counter = 1;
                whiteBoard.setTourOngoing(false);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                counter += 1;
                // 如果手指数量大于3，直接开启漫游
                // 如果手指数量等于2，需判断是否处于圈选状态，若不是则开启漫游模式
                if (counter >= 3 || counter == 2 && getCacheWhiteBoard().getSelected().isEmpty() && getCacheWhiteBoard().getSelectedImg().isEmpty()) {
                    whiteBoard.setTourOngoing(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                counter = 0;
                // 关闭漫游模式
                whiteBoard.setTourOngoing(false);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                counter -= 1;
                break;
        }
    }

    /**
     * 处理MotionEvent，将事件分发给手势处理器
     * @param event 待处理的MotionEvent事件
     */
    private void handle(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "ACTION_DOWN");
                // 清空绘制层
                whiteBoard.getRuntimeWhiteBoard().removeAllPath();
                mode = MODE.DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, "ACTION_POINTER_DOWN");
                // 计算起始距离
                startDis = distance(event);
                // 防止不规则手指触碰
                if (startDis > 10f) {
                    midPoint = rawMid(event);
                    mode = MODE.ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 缩放
                if (mode == MODE.ZOOM) {
                    float endDis = distance(event);//结束距离
                    if (endDis > 10f) {//防止不规则手指触碰
                        float scaleFactor = endDis / startDis;

                        // 选择为空
                        if (getCacheWhiteBoard().getSelected().isEmpty() && getCacheWhiteBoard().getSelectedImg().isEmpty() && whiteBoard.isTourOngoing()) {

                            // 漫游模式下的缩放
                            // 缩放倍数太小或太大，停止缩放
                            if (whiteBoard.getScaleFactor() < 0.3 && scaleFactor < 1)
                                return;
                            else if (whiteBoard.getScaleFactor() > 2 && scaleFactor > 1)
                                return;

                            // 设置缩放中心
                            whiteBoard.setScaleCenterX(midPoint.x);
                            whiteBoard.setScaleCenterY(midPoint.y);

                            // 设置缩放系数
                            whiteBoard.setScaleFactor(whiteBoard.getScaleFactor() * scaleFactor);

                            startDis = endDis;
                            getCacheWhiteBoard().invalidate();


                            return;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                whiteBoard.setTourOngoing(false);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE.DEFAULT;
                break;
        }

    }

    /**
     * 负责处理漫游模式的ACTION_MOVE
     */
    private class TourGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int deltaX = (int) distanceX;
            int deltaY = (int) distanceY;

            // 记录位移量
            whiteBoard.setDeltaX(whiteBoard.getDeltaX() + deltaX);
            whiteBoard.setDeltaY(whiteBoard.getDeltaY() + deltaY);
            // 移动顶层ViewGroup的Canvas
            getCacheWhiteBoard().invalidate();
            return true;
        }
    }

    /**
     * 计算两点间距离
     */
    public float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx*dx+dy*dy);
    }

    /**
     * 计算两个点的中心点（缩放平移修正）
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

    /**
     * 计算两个点的中心点（无修正）
     */
    public PointF rawMid(MotionEvent event){
        float x1 = event.getX(1);
        float x2 = event.getX(0);
        float y1 = event.getY(1);
        float y2 = event.getY(0);
        float midX = (x1 + x2) / 2 + whiteBoard.getDeltaX();
        float midY = (y1 + y2) / 2 + whiteBoard.getDeltaY();
        return new PointF(midX,midY);
    }

}
