package com.robert.whiteboard_plus.components;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.robert.whiteboard_plus.components.movable.DragImageView;

/**
 * Glide加载图片时，用该类对图片进行缩放
 * 目前无效
 */
public class RatioScaleTransform extends ImageViewTarget<Bitmap> {

    public static final String TAG = "RatioScaleTransform";

    public RatioScaleTransform(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(@Nullable Bitmap resource) {

        if (resource == null) {
            return;
        }
        view.setImageBitmap(resource);
        view.setImageBitmap(resource);

        // 获取原图的宽高
        int width = resource.getWidth();
        int height = resource.getHeight();

        DragImageView dragImg = (DragImageView) view;
        dragImg.setRoundHeight(height);
        dragImg.setRoundWidth(width);

        // 获取imageView的宽
        int imageViewWidth = 250;

        // 计算缩放比例
        float sy = (float) (imageViewWidth * 0.1) / (float) (width * 0.1);

        // 计算图片等比例放大后的高
        int imageViewHeight = (int) (height * sy);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = imageViewHeight;
        view.setLayoutParams(params);

    }
}
