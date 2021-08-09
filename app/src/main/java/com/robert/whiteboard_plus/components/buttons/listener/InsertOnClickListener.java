package com.robert.whiteboard_plus.components.buttons.listener;

import android.content.Context;
import android.graphics.RectF;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.robert.whiteboard_plus.whiteboard.CacheWhiteBoard;
import com.robert.whiteboard_plus.components.movable.DragImageView;
import com.robert.whiteboard_plus.components.RatioScaleTransform;
import com.robert.whiteboard_plus.whiteboard.RuntimeWhiteBoard;
import com.robert.whiteboard_plus.whiteboard.WhiteBoard;

public class InsertOnClickListener implements View.OnClickListener {

    private CacheWhiteBoard storeWhiteBoard;
    private RuntimeWhiteBoard runtimeWhiteBoard;

    private WhiteBoard whiteBoard;

    ActivityResultLauncher<String> mGetContent;

    public InsertOnClickListener(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
        this.storeWhiteBoard = whiteBoard.getCacheWhiteBoard();
        this.runtimeWhiteBoard = whiteBoard.getRuntimeWhiteBoard();
        this.mGetContent = ((ActivityResultCaller) storeWhiteBoard.getContext()).registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {

                if (result == null) return;

                // 加载本地图片
                DragImageView imageView = new DragImageView(storeWhiteBoard.getContext());
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                imageView.setAdjustViewBounds(true);
                Glide.with(storeWhiteBoard.getContext())
                        .asBitmap()
                        .load(result)
                        .thumbnail(0.5f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new RatioScaleTransform(imageView));

                float relocateX = (whiteBoard.getDeltaX() - whiteBoard.getScaleCenterX())
                        / whiteBoard.getScaleFactor() + whiteBoard.getScaleCenterX();
                float relocateY = (whiteBoard.getDeltaY() - whiteBoard.getScaleCenterY())
                        / whiteBoard.getScaleFactor() + whiteBoard.getScaleCenterY();

                WindowManager wm = (WindowManager) whiteBoard.getContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics outMetrics = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(outMetrics);
                int screenWidth = outMetrics.widthPixels;
                int screenHeight = outMetrics.heightPixels;

                imageView.setXPos((int) relocateX + screenWidth / 4);
                imageView.setYPos((int) relocateY + screenHeight / 4);

                // 限定加载的图片宽高为500 * 500
                storeWhiteBoard.addView(imageView, 500, 500);

            }
        });

    }

    @Override
    public void onClick(View v) {
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

        // 开启相册选择图片
        pickPhoto();
    }

    private void pickPhoto() {
        mGetContent.launch("image/*");
    }

}
