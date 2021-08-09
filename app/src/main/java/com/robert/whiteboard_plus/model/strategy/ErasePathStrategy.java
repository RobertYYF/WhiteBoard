package com.robert.whiteboard_plus.model.strategy;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import com.robert.whiteboard_plus.components.pen.ErasePen;
import com.robert.whiteboard_plus.components.pen.SelectPen;
import com.robert.whiteboard_plus.components.movable.PathView;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.model.utils.DrawPathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 擦除策略
 */
public class ErasePathStrategy implements PathStrategy {

    public static final String TAG = "ErasePathStrategy";

    private WhiteBoard whiteBoard;
    // 橡皮擦内圆画笔
    private Paint inEraserPaint;
    // 橡皮擦外圆画笔
    private Paint outEraserPaint;

    public ErasePathStrategy(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
        this.inEraserPaint = new Paint();
        inEraserPaint.setAntiAlias(false);
        inEraserPaint.setColor(Color.WHITE);
        this.outEraserPaint = new Paint();
        outEraserPaint.setAntiAlias(true);
        outEraserPaint.setStyle(Paint.Style.STROKE);
        outEraserPaint.setStrokeWidth(10);
        outEraserPaint.setColor(Color.BLACK);
    }

    @Override
    public void downPath(Path path, MotionEvent event, float x, float y) {
        whiteBoard.getRuntimeWhiteBoard().removeAllPath();

        // 显示橡皮擦路径
        whiteBoard.getRuntimeWhiteBoard().addPath(path, ErasePen.getInstance());
        DrawPathUtils.onDownPath(path, whiteBoard, x, y);

        // 显示橡皮擦
        // 根据触摸面积改变橡皮擦大小
        float defaultSize = (event.getSize() * 10000) / 2 + 0.5f;
        int adjustSize = (int) (defaultSize * whiteBoard.getDensity() + 0.5f);
        int width = (int) (adjustSize / whiteBoard.getScaleFactor());
        int xPos = (int) DrawPathUtils.locateX(x, whiteBoard) - width / 2;
        int yPos = (int) DrawPathUtils.locateY(y, whiteBoard) - width / 2;

        // 在绘制层显示橡皮擦
        whiteBoard.setEraserOutDot(new Path());
        whiteBoard.setEraserInDot(new Path());

        int circleRadius = width / 2;
        whiteBoard.getEraserInDot().addCircle(xPos + circleRadius, yPos + circleRadius, circleRadius - 10, Path.Direction.CW);
        whiteBoard.getRuntimeWhiteBoard().addPath(whiteBoard.getEraserInDot(), inEraserPaint);
        whiteBoard.getEraserOutDot().addCircle(xPos + circleRadius, yPos + circleRadius, circleRadius - 10, Path.Direction.CW);
        whiteBoard.getRuntimeWhiteBoard().addPath(whiteBoard.getEraserOutDot(), outEraserPaint);

        // 更改橡皮擦笔迹粗细
        ErasePen.getInstance().changeSize(width - 20);
    }

    @Override
    public void movePath(Path path, MotionEvent event, float preX, float preY, float curX, float curY) {
        float distanceX = DrawPathUtils.locateX(curX, whiteBoard) - preX;
        float distanceY = DrawPathUtils.locateY(curY, whiteBoard) - preY;

        // 移动绘制层橡皮擦圆点
        whiteBoard.getEraserInDot().offset(distanceX, distanceY);
        whiteBoard.getEraserOutDot().offset(distanceX, distanceY);

        // 重新显示圆点（防止覆盖）
        whiteBoard.getRuntimeWhiteBoard().removePath(whiteBoard.getEraserOutDot());
        whiteBoard.getRuntimeWhiteBoard().removePath(whiteBoard.getEraserInDot());
        whiteBoard.getRuntimeWhiteBoard().addPath(whiteBoard.getEraserInDot(), inEraserPaint);
        whiteBoard.getRuntimeWhiteBoard().addPath(whiteBoard.getEraserOutDot(), outEraserPaint);


        DrawPathUtils.onMovePath(path, whiteBoard, preX, preY, curX, curY);
    }

    @Override
    public void upPath(Path path, MotionEvent event, float x, float y) {

        // 移除绘制层圆点
        whiteBoard.getRuntimeWhiteBoard().removePath(whiteBoard.getEraserOutDot());
        whiteBoard.getRuntimeWhiteBoard().removePath(whiteBoard.getEraserInDot());

        // 储存将要删除的笔迹
        List<View> toDelete = new ArrayList<>();

        // 判断与擦除路径相交的笔迹
        for (int i = 0; i < whiteBoard.getCacheWhiteBoard().getChildCount(); i++) {
            Path tmp = new Path();

            if (whiteBoard.getCacheWhiteBoard().getChildAt(i).getClass() == PathView.class) {
                PathView current = (PathView) whiteBoard.getCacheWhiteBoard().getChildAt(i);
                if (current.getPaint() != SelectPen.getInstance()) {
                    tmp.op(current.getPath(), whiteBoard.getErasePath(), Path.Op.INTERSECT);
                    if (!tmp.isEmpty()) {
                        toDelete.add(whiteBoard.getCacheWhiteBoard().getChildAt(i));
                    }
                }
            }
        }

        // 清除笔迹
        for (View v : toDelete) {
            whiteBoard.getCacheWhiteBoard().removeView(v);
        }

        // 清除擦除笔迹
        whiteBoard.getRuntimeWhiteBoard().removeAllPath();
    }

}
