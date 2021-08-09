package com.robert.whiteboard_plus.components.buttons.listener;

import android.graphics.RectF;
import android.view.View;
import android.widget.Toast;

import com.robert.whiteboard_plus.model.Mode;
import com.robert.whiteboard_plus.whiteboard.CacheWhiteBoard;
import com.robert.whiteboard_plus.whiteboard.RuntimeWhiteBoard;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;

public class SelectOnClickListener implements View.OnClickListener {

    private CacheWhiteBoard storeWhiteBoard;
    private RuntimeWhiteBoard runtimeWhiteBoard;

    private WhiteBoard whiteBoard;

    public SelectOnClickListener(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
        this.storeWhiteBoard = whiteBoard.getCacheWhiteBoard();
        this.runtimeWhiteBoard = whiteBoard.getRuntimeWhiteBoard();
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(), "圈选", Toast.LENGTH_SHORT).show();
//        runtimeWhiteBoard.setMode(Mode.SELECT);
        whiteBoard.setMode(Mode.SELECT);
        // 移除圈选框
        if (!storeWhiteBoard.getSelected().isEmpty() || !storeWhiteBoard.getSelectedImg().isEmpty())
            storeWhiteBoard.removeView(storeWhiteBoard.getSelectBox());
        // 重置选择
        storeWhiteBoard.getSelected().clear();
        storeWhiteBoard.getSelectedImg().clear();
        storeWhiteBoard.getSelectBox().setRectF(new RectF());
        storeWhiteBoard.getSelectBox().invalidate();
        // 重置圈选路径
        runtimeWhiteBoard.removeAllPath();
        runtimeWhiteBoard.invalidate();
    }
}
