package com.robert.whiteboard_plus.components.movable;

public interface MovableView {
    /**
     * 获取View的X坐标 (矩形左上角)
     */
    int getXPos();
    /**
     * 获取View的Y坐标 (矩形左上角)
     */
    int getYPos();
    /**
     * 设置View的X坐标 (矩形左上角)
     */
    void setXPos(int x);
    /**
     * 设置View的Y坐标 (矩形左上角)
     */
    void setYPos(int y);
    /**
     * 获取View的所需宽度
     */
    int getRoundWidth();
    /**
     * 设置View的所需宽度
     */
    void setRoundWidth(int width);
    /**
     * 获取View的所需高度
     */
    int getRoundHeight();
    /**
     * 设置View的所需高度
     */
    void setRoundHeight(int height);

}
