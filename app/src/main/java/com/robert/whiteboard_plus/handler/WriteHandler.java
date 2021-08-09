package com.robert.whiteboard_plus.handler;

import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

import com.robert.whiteboard_plus.model.Mode;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.model.utils.DrawPathUtils;
import com.robert.whiteboard_plus.model.strategy.PathStrategy;
import com.robert.whiteboard_plus.model.strategy.WritePathStrategy;

import org.apache.commons.lang3.tuple.MutablePair;

public class WriteHandler extends ActionHandler {

    public static final String TAG = "WriteHandler";

    private PathStrategy writeStrategy;

    private long startTimer;
    private long endTimer;

    public WriteHandler(WhiteBoard whiteBoard) {
        super(whiteBoard);
        writeStrategy = new WritePathStrategy(whiteBoard);
    }

    @Override
    public void handleAction(MotionEvent event) {
        if (whiteBoard.getMode() == Mode.WRITE && !whiteBoard.isTourOngoing())
            handle(event);
        else if (whiteBoard.isTourOngoing())
            getTourHandler().handleAction(event);
        else if (getSuccessor() != null)
            getSuccessor().handleAction(event);
    }

    /**
     * 处理MotionEvent，将事件分发给手势处理器和ACTION_UP方法
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
     * 处理书写模式的ACTION_DOWN
     * @param event 待处理事件
     * @return 处理完成与否
     */
    private boolean onDown(MotionEvent event) {
        // 清空绘制层
        whiteBoard.getRuntimeWhiteBoard().removeAllPath();

        // 开始计时
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

        writeStrategy.downPath(whiteBoard.getWritePaths().get(0), event, event.getX(), event.getY());

        return true;
    }

    /**
     * 处理书写模式的ACTION_POINTER_DOWN，仅在多指书写模式下处理
     * @param event 待处理事件
     * @return 处理完成与否
     */
    private boolean onPointerDown(MotionEvent event) {

        if (!whiteBoard.isMultiWriteOngoing()) {
            // 结束计时
            endTimer = System.currentTimeMillis();
            if (endTimer - startTimer < 120) {
                Log.i(TAG, "取消书写，交由漫游处理");
                whiteBoard.setTourOngoing(true);
                getTourHandler().handleAction(event);

                // 重置绘制层书写
                whiteBoard.resetWrite();
                return true;
            }
        }

        if (whiteBoard.isMultiWriteOngoing()) {
            // 获取新加入的手指index
            int actionIndex = event.getActionIndex();
            // 记录该手指对应的PointerId
            whiteBoard.getWritePathIds().add(event.getPointerId(actionIndex));
            Path newPath = new Path();
            whiteBoard.getWritePaths().add(newPath);
            int index = whiteBoard.getWritePathIds().indexOf(event.getPointerId(event.getActionIndex()));
            // 记录当前手指位置
            whiteBoard.getHistoryPos().add(new MutablePair<>(DrawPathUtils.locateX(event.getX(index), whiteBoard),
                    DrawPathUtils.locateY(event.getY(index), whiteBoard)));
            // 调用downPath移动画笔
            writeStrategy.downPath(newPath, event, event.getX(index), event.getY(index));
        }
        return true;
    }

    /**
     * 处理书写模式的ACTION_MOVE
     * @param event 待处理事件
     * @return 处理完成与否
     */
    private boolean onMove(MotionEvent event) {
        for (int i = 0; i < whiteBoard.getWritePaths().size(); i++) {
            DrawPathUtils.onMovePath(whiteBoard.getWritePaths().get(i), whiteBoard,
                    whiteBoard.getHistoryPos().get(i).left,
                    whiteBoard.getHistoryPos().get(i).right,
                    event.getX(i), event.getY(i));
            // 更新当前手指的历史位置
            whiteBoard.getHistoryPos().get(i).left = DrawPathUtils.locateX(event.getX(i), whiteBoard);
            whiteBoard.getHistoryPos().get(i).right = DrawPathUtils.locateY(event.getY(i), whiteBoard);
        }
        return true;
    }

    /**
     * 处理书写模式的ACTION_UP
     * @param event 待处理事件
     * @return 处理完成与否
     */
    private boolean onUp(MotionEvent event) {
        int index = whiteBoard.getWritePathIds().indexOf(event.getPointerId(event.getActionIndex()));

        if (index != 0) {
            // 移除对该手指的追踪记录
            whiteBoard.getWritePaths().clear();
            whiteBoard.getWritePathIds().clear();
            whiteBoard.getHistoryPos().clear();
            // 清空绘制层
            whiteBoard.getRuntimeWhiteBoard().removeAllPath();
            return true;
        }

        Path targetPath = whiteBoard.getWritePaths().get(index);
        writeStrategy.upPath(targetPath, event, event.getX(index), event.getY(index));
        // 移除对该手指的追踪记录
        whiteBoard.getWritePaths().clear();
        whiteBoard.getWritePathIds().clear();
        whiteBoard.getHistoryPos().clear();

        return true;
    }

    /**
     * 处理书写模式的ACTION_POINTER_UP，仅在多指书写模式下处理
     * @param event 待处理事件
     * @return 处理完成与否
     */
    private boolean onPointerUp(MotionEvent event) {

        int index = whiteBoard.getWritePathIds().indexOf(event.getPointerId(event.getActionIndex()));
        boolean multiWriteState = whiteBoard.isMultiWriteOngoing();

        // 单指书写时用多指，先抬起第一根手指
        if (!multiWriteState && index == 0) {
            Path targetPath = whiteBoard.getWritePaths().get(index);
            writeStrategy.upPath(targetPath, event, event.getX(index), event.getY(index));
            // 移除对该手指的追踪记录
            whiteBoard.getWritePaths().remove(targetPath);
            whiteBoard.getWritePathIds().remove(index);
            whiteBoard.getHistoryPos().remove(index);
            // 清空绘制层
            whiteBoard.getRuntimeWhiteBoard().removeAllPath();
            return true;
        }

        if (multiWriteState) {
            Path targetPath = whiteBoard.getWritePaths().get(index);
            writeStrategy.upPath(targetPath, event, event.getX(index), event.getY(index));
            // 移除对该手指的追踪记录
            whiteBoard.getWritePaths().remove(targetPath);
            whiteBoard.getWritePathIds().remove(index);
            whiteBoard.getHistoryPos().remove(index);
        }
        return true;
    }

}
