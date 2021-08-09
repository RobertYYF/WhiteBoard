package com.robert.whiteboard_plus.whiteboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 绘制层
 * 负责实时显示新绘制的路径
 */
public class RuntimeWhiteBoard extends SurfaceView implements SurfaceHolder.Callback,Runnable {

    public static final String TAG = "RuntimeWhiteBoard";

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    // 状态判断
    private volatile boolean isDrawing;
    // 绘制线程
    Thread drawThread;

    // 绘制列表
    ConcurrentHashMap<Path, Paint> toDraw;

    // 位移量
    int deltaX = 0;
    int deltaY = 0;
    // 缩放倍数
    float scaleFactor = 1f;
    // 缩放中心
    float scaleCenterX = 0;
    float scaleCenterY = 0;

    public RuntimeWhiteBoard(Context context) {
        super(context);
        init();
    }

    public RuntimeWhiteBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RuntimeWhiteBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isDrawing = true;
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isDrawing = false;
        try {
            drawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化绘制层
     */
    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        toDraw = new ConcurrentHashMap<>();
    }

    /**
     * 格式化绘制层
     */
    public void clear() {
        init();
    }

    /**
     * 负责绘制的子线程，每隔10ms刷新
     */
    @Override
    public void run() {
        while (isDrawing) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 25) {
                try {
                    Thread.sleep(25 - (end-start));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 绘制方法
     */
    private void draw() {
        // 在Canvas上绘制
        try {
            mCanvas = mHolder.lockCanvas();
            mCanvas.translate(-deltaX, -deltaY);
            mCanvas.scale(scaleFactor, scaleFactor, scaleCenterX, scaleCenterY);
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            // 绘制笔迹
            drawAllPath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas!=null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     * 添加Path及其绘制所用的Paint到待绘制列表中
     * @param path 要绘制的Path
     * @param paint 该Path绘制所用的Paint
     */
    public void addPath(Path path, Paint paint) {
        toDraw.put(path, paint);
    }

    /**
     * 从待绘制列表中删除特定的Path
     * @param path 要删除的Path
     * @return 返回true：删除成功，返回false：该Path不存在于列表中
     */
    public boolean removePath(Path path) {
        if (toDraw.containsKey(path)) {
            toDraw.remove(path);
            return true;
        }
        return false;
    }

    /**
     * 清空待绘制列表
     */
    public void removeAllPath() {
        toDraw.clear();
    }

    /**
     * 绘制全部Path
     */
    private void drawAllPath() {
        for (Map.Entry<Path, Paint> entry : toDraw.entrySet()) {
            Path path = entry.getKey();
            Paint paint = entry.getValue();
            mCanvas.drawPath(path, paint);
        }
    }

    /**
     * SETTERS
     */
    public void setDeltaX(int deltaX) {
        this.deltaX = deltaX;
    }

    public void setDeltaY(int deltaY) {
        this.deltaY = deltaY;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setScaleCenterX(float scaleCenterX) {
        this.scaleCenterX = scaleCenterX;
    }

    public void setScaleCenterY(float scaleCenterY) {
        this.scaleCenterY = scaleCenterY;
    }

}
