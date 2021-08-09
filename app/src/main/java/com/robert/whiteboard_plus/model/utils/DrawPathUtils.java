package com.robert.whiteboard_plus.model.utils;

import android.graphics.Path;

import com.robert.whiteboard_plus.whiteboard.WhiteBoard;

public class DrawPathUtils {

    public static final String TAG = "DrawPathUtils";

    /**
     * 根据漫游缩放、位移重新定位x坐标
     * @param x 需要重新定位的x坐标
     * @param whiteBoard 白板
     * @return 重新定位后的x坐标
     */
    public static float locateX(float x, WhiteBoard whiteBoard) {
        return (x + whiteBoard.getDeltaX() - whiteBoard.getScaleCenterX())
                / whiteBoard.getScaleFactor() + whiteBoard.getScaleCenterX();
    }

    /**
     * 根据漫游缩放、位移重新定位y坐标
     * @param y 需要重新定位的y坐标
     * @param whiteBoard 白板
     * @return 重新定位后的y坐标
     */
    public static float locateY(float y, WhiteBoard whiteBoard) {
        return (y + whiteBoard.getDeltaY() - whiteBoard.getScaleCenterY())
                / whiteBoard.getScaleFactor() + whiteBoard.getScaleCenterY();
    }

    /**
     * 移动Path的落笔点到待修改的x，y坐标
     * @param path 笔迹
     * @param whiteBoard 白板
     * @param x 原始x坐标
     * @param y 原始y坐标
     */
    public static void onDownPath(Path path, WhiteBoard whiteBoard, float x, float y) {
        float movePointX = locateX(x, whiteBoard);
        float movePointY = locateY(y, whiteBoard);
        path.moveTo(movePointX, movePointY);
    }

    /**
     * Path连线，根据提供的x,y和其历史位置做连线
     * @param path 笔迹
     * @param whiteBoard 白板
     * @param preX 历史x坐标
     * @param preY 历史y坐标
     * @param curX 当前x坐标
     * @param curY 当前y坐标
     */
    public static void onMovePath(Path path, WhiteBoard whiteBoard, float preX, float preY, float curX, float curY) {
        // 根据白板漫游缩放、偏移量修正x,y坐标
        float correctX = locateX(curX, whiteBoard);
        float correctY = locateY(curY, whiteBoard);

        // 计算贝塞尔曲线控制点
        float endPointX = (correctX + preX) / 2;
        float endPointY = (correctY + preY) / 2;

        path.quadTo(preX, preY, endPointX, endPointY);
    }

    public static void onUpPath(Path path, WhiteBoard whiteBoard, float x, float y) {
    }

}
