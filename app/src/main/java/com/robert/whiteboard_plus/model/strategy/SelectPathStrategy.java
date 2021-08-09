package com.robert.whiteboard_plus.model.strategy;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.robert.whiteboard_plus.components.pen.SelectPen;
import com.robert.whiteboard_plus.components.movable.DragImageView;
import com.robert.whiteboard_plus.components.movable.PathView;
import com.robert.whiteboard_plus.components.movable.SelectBoxView;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.model.utils.DrawPathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 圈选策略
 */
public class SelectPathStrategy implements PathStrategy {

    public static final String TAG = "SelectPathStrategy";

    private WhiteBoard whiteBoard;

    public SelectPathStrategy(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
    }

    @Override
    public void downPath(Path path, MotionEvent event, float x, float y) {
        // 清空绘制层
        whiteBoard.getRuntimeWhiteBoard().removeAllPath();
        whiteBoard.getRuntimeWhiteBoard().addPath(path, SelectPen.getInstance());
        // 是否触摸圈选框内部
        boolean touchInside = false;
        // 是否有已选中
        boolean isSelected = !whiteBoard.getCacheWhiteBoard().getSelectBox().getRectF().isEmpty();
        if (isSelected)
            touchInside = whiteBoard.getCacheWhiteBoard().getSelectBox().getRectF()
                    .contains(DrawPathUtils.locateX(x, whiteBoard), DrawPathUtils.locateY(y, whiteBoard));

        // 点击选择框以外的区域
        if (isSelected && !touchInside) {
            // 重置选择
            Log.i(TAG, "触摸选择框以外区域，重置圈选");
            whiteBoard.resetSelect();
        }

        // 触摸选择框内的区域，移动框内全部Path
        if (isSelected && touchInside) {
            // 移动 / 缩放过程中隐藏圈选框
            Log.i(TAG, "触摸选择框以内区域，移动");
            whiteBoard.getCacheWhiteBoard().removeView(whiteBoard.getCacheWhiteBoard().getSelectBox());
            // 开始移动圈选内容
            whiteBoard.setMoveSelectOngoing(true);
            DrawPathUtils.onDownPath(path, whiteBoard, x, y);
            return;
        }

        // 开始绘制圈选笔迹
        whiteBoard.setSelectOngoing(true);
        DrawPathUtils.onDownPath(path, whiteBoard, x, y);
    }

    @Override
    public void movePath(Path path, MotionEvent event, float preX, float preY, float curX, float curY) {
        // 计算移动距离
        float distanceX = DrawPathUtils.locateX(curX, whiteBoard) - preX;
        float distanceY = DrawPathUtils.locateY(curY, whiteBoard) - preY;

        // 触摸选择框内，移动框内全部内容
        if (whiteBoard.isMoveSelectOngoing()) {
            // 移动框内Path
            for (PathView s : whiteBoard.getCacheWhiteBoard().getSelected()) {
                s.getPath().offset(distanceX, distanceY);
                s.requestLayout();
            }

            // 移动框内图片
            for (ImageView i : whiteBoard.getCacheWhiteBoard().getSelectedImg()) {
                DragImageView dragImg = (DragImageView) i;
                dragImg.setXPos((int) (dragImg.getXPos() + distanceX));
                dragImg.setYPos((int) (dragImg.getYPos() + distanceY));
                dragImg.requestLayout();
            }

            return;
        }

        // 绘制圈选笔迹
        DrawPathUtils.onMovePath(path, whiteBoard, preX, preY, curX, curY);
    }

    @Override
    public void upPath(Path path, MotionEvent event, float x, float y) {

        whiteBoard.getCacheWhiteBoard().getSelectPath().setPath(new Path(whiteBoard.getSelectPath()));

        // 结束圈选移动
        if (whiteBoard.isMoveSelectOngoing()) {
            // 重新显示圈选框
            Log.i(TAG, "重新显示圈选框");
            whiteBoard.getCacheWhiteBoard().getSelectBox().setRectF(calculateBox(whiteBoard.getCacheWhiteBoard().getSelected(), whiteBoard.getCacheWhiteBoard().getSelectedImg()));
            whiteBoard.getCacheWhiteBoard().addView(whiteBoard.getCacheWhiteBoard().getSelectBox());
            whiteBoard.setMoveSelectOngoing(false);

            return;
        }

        whiteBoard.getCacheWhiteBoard().getSelectPath().getPath().close();
        whiteBoard.setSelectOngoing(false);

        // 判断相交 / 包含的笔迹
        List<PathView> toSelect = new ArrayList<>();
        List<ImageView> toSelectImg = new ArrayList<>();

        // 圈选路径的外接矩形
        Rect pathRect = new Rect();
        RectF pathRectF = new RectF();
        whiteBoard.getCacheWhiteBoard().getSelectPath().getPath().computeBounds(pathRectF, true);
        pathRectF.round(pathRect);

        // 判断与擦除路径相交的笔迹
        for (int i = 0; i < whiteBoard.getCacheWhiteBoard().getChildCount(); i++) {
            Path tmp = new Path();

            // 与图片相交
            if (whiteBoard.getCacheWhiteBoard().getChildAt(i).getClass() == DragImageView.class) {
                DragImageView img = (DragImageView) whiteBoard.getCacheWhiteBoard().getChildAt(i);

                // imgRect 用于表示图片的外接矩形
                Rect imgRect = new Rect();
                imgRect.left = (int) Math.ceil(img.getXPos());
                imgRect.top = (int) Math.ceil(img.getYPos());
                imgRect.right = imgRect.left + img.getRoundWidth();
                imgRect.bottom = imgRect.top + img.getRoundHeight();

                if (imgRect.intersect(pathRect)) {
                    whiteBoard.getCacheWhiteBoard().getSelectedImg().add(img);
                    toSelectImg.add(img);
                }
            }

            // 与笔迹相交
            else if (whiteBoard.getCacheWhiteBoard().getChildAt(i).getClass() != SelectBoxView.class) {
                PathView current = (PathView) whiteBoard.getCacheWhiteBoard().getChildAt(i);
                if (current.getPaint() != SelectPen.getInstance()) {
                    tmp.op(current.getPath(), whiteBoard.getCacheWhiteBoard().getSelectPath().getPath(), Path.Op.INTERSECT);
                    if (!tmp.isEmpty()) {
                        toSelect.add((PathView) whiteBoard.getCacheWhiteBoard().getChildAt(i));
                        whiteBoard.getCacheWhiteBoard().getSelected().add(current);
                    }
                }
            }
        }

        // 选中为空
        if (toSelect.isEmpty() && toSelectImg.isEmpty()) {
            // 重置选择框
            whiteBoard.getCacheWhiteBoard().getSelectBox().setRectF(new RectF());
            whiteBoard.getCacheWhiteBoard().getSelectBox().requestLayout();
            // 重置圈选路径
            whiteBoard.getRuntimeWhiteBoard().removeAllPath();
            whiteBoard.getRuntimeWhiteBoard().invalidate();
            return;
        }

        // 组合图片、笔迹
        RectF combo = calculateBox(toSelect, toSelectImg);
        // 显示圈选框
        whiteBoard.getCacheWhiteBoard().getSelectBox().setRectF(combo);
        whiteBoard.getCacheWhiteBoard().addView(whiteBoard.getCacheWhiteBoard().getSelectBox());

        // 重置圈选路径
        whiteBoard.getCacheWhiteBoard().getSelectPath().setPath(new Path());
        // 结束圈选
        whiteBoard.setSelectOngoing(false);
        // 清空绘制层
        whiteBoard.getRuntimeWhiteBoard().removeAllPath();
        whiteBoard.getRuntimeWhiteBoard().invalidate();

    }

    /**
     * 根据圈选中的笔迹和图片计算出圈选框
     * @param toSelect 圈选中的笔迹
     * @param toSelectImg 圈选中的图片
     * @return 圈选框
     */
    public static RectF calculateBox(List<PathView> toSelect, List<ImageView> toSelectImg) {
        RectF pathBound = new RectF();
        if (!toSelect.isEmpty()) {
            Path comboPath = new Path(toSelect.get(0).getPath());
            for (PathView s : toSelect) {
                comboPath.op(comboPath, new Path(s.getPath()), Path.Op.UNION);
            }
            comboPath.computeBounds(pathBound, true);
        }

        // 组合图片
        Rect imgBound = new Rect();
        if (!toSelectImg.isEmpty()) {
            for (ImageView i : toSelectImg) {
                DragImageView img = (DragImageView) i;
                Rect tmpRect = new Rect();
                tmpRect.left = (int) Math.ceil(img.getXPos());
                tmpRect.top = (int) Math.ceil(img.getYPos());
                tmpRect.right = tmpRect.left + img.getRoundWidth();
                tmpRect.bottom = tmpRect.top + img.getRoundHeight();
                imgBound.union(tmpRect);
            }
        }

        // 组合图片 + 笔迹
        Rect combo = new Rect();
        pathBound.round(combo);
        combo.union(imgBound);

        combo.top -= 20;
        combo.bottom += 20;
        combo.left -= 20;
        combo.right += 20;

        return new RectF(combo);
    }

}
