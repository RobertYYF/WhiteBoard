package com.robert.whiteboard_plus.handler;

import android.view.MotionEvent;

import com.robert.whiteboard_plus.whiteboard.CacheWhiteBoard;
import com.robert.whiteboard_plus.whiteboard.RuntimeWhiteBoard;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;

public abstract class ActionHandler {

    /**
     * 白板
     */
    protected WhiteBoard whiteBoard;
    /**
     * 责任链下一位
     */
    protected ActionHandler successor;
    /**
     * 漫游处理，书写、擦除、圈选过程中若能触发多指漫游，会重置状态，将触摸事件转交给漫游处理
     */
    protected ActionHandler tourHandler;

    public ActionHandler(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
    }

    public ActionHandler getSuccessor() {
        return successor;
    }

    public void setSuccessor(ActionHandler successor) {
        this.successor = successor;
    }

    public ActionHandler getTourHandler() { return tourHandler; }

    public void setTourHandler(ActionHandler tourHandler) { this.tourHandler = tourHandler; }

    public RuntimeWhiteBoard getRuntimeWhiteBoard() { return whiteBoard.getRuntimeWhiteBoard(); }

    public CacheWhiteBoard getCacheWhiteBoard() {
        return whiteBoard.getCacheWhiteBoard();
    }

    /**
     * 抽象方法，处理MotionEvent的具体实现
     * @param event 将要处理的MotionEvent
     */
    public abstract void handleAction(MotionEvent event);

}
