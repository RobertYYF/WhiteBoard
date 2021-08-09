package com.robert.whiteboard_plus.model.strategy;

import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.robert.whiteboard_plus.components.pen.WritePen;
import com.robert.whiteboard_plus.components.movable.PathView;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.model.utils.DrawPathUtils;

/**
 * 书写策略
 */
public class WritePathStrategy implements PathStrategy {

    public static final String TAG = "WritePathStrategy";

    private WhiteBoard whiteBoard;

    public WritePathStrategy(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
    }

    @Override
    public void downPath(Path path, MotionEvent event, float x, float y) {
        // 添加笔迹到绘制层
        whiteBoard.getRuntimeWhiteBoard().addPath(path, WritePen.getInstance());
        DrawPathUtils.onDownPath(path, whiteBoard, x, y);
    }

    @Override
    public void movePath(Path path, MotionEvent event, float preX, float preY, float curX, float curY) {
        // 绘制曲线
        DrawPathUtils.onMovePath(path, whiteBoard, preX, preY, curX, curY);
    }

    @Override
    public void upPath(Path path, MotionEvent event, float x, float y) {
        // 将在绘制层的笔迹存到储存层
        PathView newPath = new PathView(whiteBoard.getContext());
        newPath.setPath(path);
        newPath.setPaint(new Paint(WritePen.getInstance()));
        whiteBoard.getCacheWhiteBoard().addView(newPath);
    }

}
