package com.robert.whiteboard_plus.handler;

import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

import com.robert.whiteboard_plus.model.Mode;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.model.utils.DrawPathUtils;
import com.robert.whiteboard_plus.model.strategy.ErasePathStrategy;
import com.robert.whiteboard_plus.model.strategy.PathStrategy;

import org.apache.commons.lang3.tuple.MutablePair;

public class EraseHandler extends ActionHandler {

    public static final String TAG = "EraseHandler";

    private PathStrategy eraseStrategy;

    private long startTimer;
    private long endTimer;

    public EraseHandler(WhiteBoard whiteBoard) {
        super(whiteBoard);
        eraseStrategy = new ErasePathStrategy(whiteBoard);
    }

    @Override
    public void handleAction(MotionEvent event) {
        if (whiteBoard.getMode() == Mode.ERASE && !whiteBoard.isTourOngoing()) {
            Log.i(TAG, "Erase处理");
            handle(event);
        }
        else if (whiteBoard.isTourOngoing()) {
            getTourHandler().handleAction(event);
        }
        else if (getSuccessor() != null) {
            getSuccessor().handleAction(event);
        }
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
                boolean result = onPointerDown(event);
                // 交由漫游处理
                if (!result) return;
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onUp(event);
                break;
            default:
                break;
        }
    }

    /**
     * 处理擦除的ACTION_DOWN
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

        // 处理擦除
        whiteBoard.setErasePath(new Path());

        eraseStrategy.downPath(whiteBoard.getErasePath(), event, event.getX(), event.getY());

        return true;
    }

    /**
     * 处理擦除的ACTION_POINTER_DOWN
     */
    private boolean onPointerDown(MotionEvent event) {
        // 结束计时
        endTimer = System.currentTimeMillis();
        if (endTimer - startTimer < 120) {
            Log.i(TAG, "取消擦除，交由漫游处理");
            whiteBoard.setTourOngoing(true);
            getTourHandler().handleAction(event);
            // 清除绘制层擦除
            whiteBoard.resetErase();
            return false;
        }
        return true;
    }

    /**
     * 处理擦除的ACTION_MOVE
     */
    private boolean onMove(MotionEvent event) {
        eraseStrategy.movePath(whiteBoard.getErasePath(), event, whiteBoard.getHistoryPos().get(0).left,
                whiteBoard.getHistoryPos().get(0).right, event.getX(), event.getY());
        // 更新当前手指的历史位置
        whiteBoard.getHistoryPos().get(0).left = DrawPathUtils.locateX(event.getX(), whiteBoard);
        whiteBoard.getHistoryPos().get(0).right = DrawPathUtils.locateY(event.getY(), whiteBoard);
        return true;
    }

    /**
     * 处理擦除的ACTION_UP
     */
    private boolean onUp(MotionEvent event) {
        whiteBoard.getHistoryPos().clear();
        eraseStrategy.upPath(whiteBoard.getErasePath(), event, event.getX(), event.getY());
        return true;
    }
}
