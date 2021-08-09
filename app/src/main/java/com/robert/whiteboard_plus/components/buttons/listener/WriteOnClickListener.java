package com.robert.whiteboard_plus.components.buttons.listener;

import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.robert.whiteboard_plus.R;
import com.robert.whiteboard_plus.model.Mode;
import com.robert.whiteboard_plus.whiteboard.CacheWhiteBoard;
import com.robert.whiteboard_plus.whiteboard.RuntimeWhiteBoard;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;
import com.robert.whiteboard_plus.components.buttons.controller.ColorController;
import com.robert.whiteboard_plus.components.buttons.controller.SizeController;

public class WriteOnClickListener implements View.OnClickListener, View.OnLongClickListener  {

    private WhiteBoard whiteBoard;
    private CacheWhiteBoard storeWhiteBoard;
    private RuntimeWhiteBoard runtimeWhiteBoard;

    private ColorController colorController;
    private SizeController sizeController;

    private View popView;
    private PopupWindow popupWindow;

    private ColorPickerView colorPickerView;
    private SeekBar seekBar;
    private Switch multiWriteSwitch;

    public WriteOnClickListener(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
        this.storeWhiteBoard = whiteBoard.getCacheWhiteBoard();
        this.runtimeWhiteBoard = whiteBoard.getRuntimeWhiteBoard();
        initPopWindow();
        initController();
    }

    private void initPopWindow() {
        popView = LayoutInflater.from(runtimeWhiteBoard.getContext()).inflate(R.layout.popout_option, null);
        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 更新粗细，颜色
                colorController.setColor();
                sizeController.setSize();
                // 更新书写模式
                if (multiWriteSwitch.isChecked())
                    whiteBoard.setMultiWriteOngoing(true);
                else
                    whiteBoard.setMultiWriteOngoing(false);
            }
        });
    }

    private void initController() {
        colorPickerView = popView.findViewById(R.id.color_picker_view);
        seekBar = popView.findViewById(R.id.seekBar);
        multiWriteSwitch = popView.findViewById(R.id.multi_write_switch);
        colorController = new ColorController(colorPickerView);
        sizeController = new SizeController(seekBar);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(), "书写", Toast.LENGTH_SHORT).show();
        switchMode();
    }

    @Override
    public boolean onLongClick(View v) {
        switchMode();
        showPopWindow(v);
        // 重置圈选路径
        runtimeWhiteBoard.removeAllPath();
        runtimeWhiteBoard.invalidate();
        return true;
    }

    private void switchMode() {
        whiteBoard.setMode(Mode.WRITE);
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

    private void showPopWindow(View v) {
        colorPickerView.setColor(colorController.getColor());
        seekBar.setProgress((int) sizeController.getSize());

        int popupWidth = popView.getMeasuredWidth();
        int popupHeight = popView.getMeasuredHeight();

        int[] location = new int[2];
        v.getLocationOnScreen(location);
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, (location[0] + v.getWidth() / 2) - popupWidth / 2, location[1] - popupHeight - 15);
        popupWindow.update();
    }


}
