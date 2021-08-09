package com.robert.whiteboard_plus.whiteboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.robert.whiteboard_plus.R;
import com.robert.whiteboard_plus.model.Mode;
import com.robert.whiteboard_plus.handler.ActionHandler;
import com.robert.whiteboard_plus.handler.EraseHandler;
import com.robert.whiteboard_plus.handler.SelectHandler;
import com.robert.whiteboard_plus.handler.TourHandler;
import com.robert.whiteboard_plus.handler.WriteHandler;
import com.robert.whiteboard_plus.components.buttons.listener.ClearOnClickListener;
import com.robert.whiteboard_plus.components.buttons.listener.EraseOnClickListener;
import com.robert.whiteboard_plus.components.buttons.listener.InsertOnClickListener;
import com.robert.whiteboard_plus.components.buttons.listener.SelectOnClickListener;
import com.robert.whiteboard_plus.components.buttons.listener.WriteOnClickListener;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WhiteBoard extends FrameLayout {

    public static final String TAG = "WhiteBoard";

    // 绘制层
    private RuntimeWhiteBoard runtimeWhiteBoard;
    // 储存显示层
    private CacheWhiteBoard cacheWhiteBoard;

    // 按键
    AppCompatImageButton writeBtn;
    AppCompatImageButton eraseBtn;
    AppCompatImageButton selectBtn;
    AppCompatImageButton clearBtn;
    AppCompatImageButton insertBtn;

    // 触摸事件处理责任链
    ActionHandler writeHandler;
    ActionHandler eraseHandler;
    ActionHandler selectHandler;
    ActionHandler tourHandler;
    // 责任链头部
    ActionHandler headHandler = null;

    // 白板模式
    Mode mode = Mode.WRITE;
    // 缩放状态
    boolean scaleOngoing = false;
    // 圈选状态
    boolean selectOngoing = false;
    // 漫游状态
    boolean tourOngoing = false;
    // 移动圈选内容状态
    boolean moveSelectOngoing = false;
    // 多指书写状态
    boolean multiWriteOngoing = false;

    // 位移量
    int deltaX = 0;
    int deltaY = 0;
    // 缩放倍数
    float scaleFactor = 1f;
    // 缩放中心
    float scaleCenterX = 0;
    float scaleCenterY = 0;

    // 擦除路径记录
    Path erasePath = null;
    // 圈选路径记录
    Path selectPath = null;
    // 书写路径记录
    List<Path> writePaths = new CopyOnWriteArrayList<>();
    List<Integer> writePathIds = new CopyOnWriteArrayList<>();
    List<MutablePair<Float, Float>> historyPos = new CopyOnWriteArrayList<>();

    // 橡皮擦圆点
    Path eraserOutDot = null;
    Path eraserInDot = null;

    private float density;

    public WhiteBoard(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.white_board, this);
        init();
    }

    public WhiteBoard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.white_board, this);
        init();
    }

    public WhiteBoard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.white_board, this);
        init();
    }

    private void init() {
        initBoards();
        initButtons();
        assembleHandlerChain();
        initScreenSize();
    }

    /**
     * 初始化实时绘制层和显示层
     */
    private void initBoards() {
        runtimeWhiteBoard = (RuntimeWhiteBoard) findViewById(R.id.runtime_whiteboard);
        cacheWhiteBoard = (CacheWhiteBoard) findViewById(R.id.cache_whiteboard);
    }

    /**
     * 初始化按键
     */
    private void initButtons() {
        // 初始化按键
        writeBtn = (AppCompatImageButton) findViewById(R.id.write_btn);
        eraseBtn = (AppCompatImageButton) findViewById(R.id.erase_btn);
        selectBtn = (AppCompatImageButton) findViewById(R.id.select_btn);
        clearBtn = (AppCompatImageButton) findViewById(R.id.clear_btn);
        insertBtn = (AppCompatImageButton) findViewById(R.id.insert_btn);
        // 设置按键事件监听
        writeBtn.setOnClickListener(new WriteOnClickListener(this));
        writeBtn.setOnLongClickListener(new WriteOnClickListener(this));
        eraseBtn.setOnClickListener(new EraseOnClickListener(this));
        selectBtn.setOnClickListener(new SelectOnClickListener(this));
        insertBtn.setOnClickListener(new InsertOnClickListener(this));
        clearBtn.setOnClickListener(new ClearOnClickListener(this));
    }

    /**
     * 组装触摸事件责任链
     */
    private void assembleHandlerChain() {
        writeHandler = new WriteHandler(this);
        eraseHandler = new EraseHandler(this);
        selectHandler = new SelectHandler(this);
        tourHandler = new TourHandler(this);

        // 书写 -> 擦除 -> 圈选
        //  |       |     |
        //  v       v     v
        // 漫游     漫游   漫游
        writeHandler.setSuccessor(eraseHandler);
        eraseHandler.setSuccessor(selectHandler);
        writeHandler.setTourHandler(tourHandler);
        eraseHandler.setTourHandler(tourHandler);
        selectHandler.setTourHandler(tourHandler);

        // 设置责任链头部
        headHandler = writeHandler;
    }

    // 获取屏幕密度
    private void initScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
        Log.i(TAG, "屏幕密度" + density);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 交由责任链处理
        headHandler.handleAction(event);
        return true;
    }

    /**
     * 重置书写
     */
    public void resetWrite() {
        getWritePaths().clear();
        getWritePathIds().clear();
        getHistoryPos().clear();
        runtimeWhiteBoard.removeAllPath();
        runtimeWhiteBoard.invalidate();
    }

    /**
     * 重置擦除
     */
    public void resetErase() {
        erasePath.reset();
        runtimeWhiteBoard.removeAllPath();
        runtimeWhiteBoard.invalidate();
    }

    /**
     * 重置圈选
     */
    public void resetSelect() {
        // 重置选择框
        cacheWhiteBoard.getSelectBox().setRectF(new RectF());
        cacheWhiteBoard.getSelectBox().invalidate();
        // 重置已选路径
        selectPath.reset();
        cacheWhiteBoard.getSelected().clear();
        cacheWhiteBoard.getSelectedImg().clear();
        // 移除选择框和选择路径
        runtimeWhiteBoard.removeAllPath();
        runtimeWhiteBoard.invalidate();
        cacheWhiteBoard.removeView(cacheWhiteBoard.getSelectBox());
        setSelectOngoing(true);
    }

    /**
     * Setters
     */
    public void setScaleOngoing(boolean scaleOngoing) {
        this.scaleOngoing = scaleOngoing;
    }

    public void setTourOngoing(boolean tourOngoing) {
        this.tourOngoing = tourOngoing;
    }

    public void setSelectOngoing(boolean selectOngoing) {
        this.selectOngoing = selectOngoing;
    }

    public void setMoveSelectOngoing(boolean moveSelectOngoing) { this.moveSelectOngoing = moveSelectOngoing; }

    public void setMultiWriteOngoing(boolean multiWriteOngoing) { this.multiWriteOngoing = multiWriteOngoing; }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        runtimeWhiteBoard.setScaleFactor(scaleFactor);
        cacheWhiteBoard.setScaleFactor(scaleFactor);
    }

    public void setScaleCenterX(float scaleCenterX) {
        this.scaleCenterX = scaleCenterX;
        runtimeWhiteBoard.setScaleCenterX(scaleCenterX);
        cacheWhiteBoard.setScaleCenterX(scaleCenterX);
    }

    public void setScaleCenterY(float scaleCenterY) {
        this.scaleCenterY = scaleCenterY;
        runtimeWhiteBoard.setScaleCenterY(scaleCenterY);
        cacheWhiteBoard.setScaleCenterY(scaleCenterY);
    }

    public void setDeltaX(int deltaX) {
        this.deltaX = deltaX;
        runtimeWhiteBoard.setDeltaX(deltaX);
        cacheWhiteBoard.setDeltaX(deltaX);
    }

    public void setDeltaY(int deltaY) {
        this.deltaY = deltaY;
        runtimeWhiteBoard.setDeltaY(deltaY);
        cacheWhiteBoard.setDeltaY(deltaY);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setErasePath(Path erasePath) {
        this.erasePath = erasePath;
    }

    public void setSelectPath(Path selectPath) {
        this.selectPath = selectPath;
    }

    public void setEraserOutDot(Path eraserOutDot) { this.eraserOutDot = eraserOutDot; }

    public void setEraserInDot(Path eraserInDot) { this.eraserInDot = eraserInDot; }

    /**
     * Getters
     */
    public RuntimeWhiteBoard getRuntimeWhiteBoard() {
        return runtimeWhiteBoard;
    }

    public CacheWhiteBoard getCacheWhiteBoard() {
        return cacheWhiteBoard;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public boolean isScaleOngoing() {
        return scaleOngoing;
    }

    public boolean isTourOngoing() {
        return tourOngoing;
    }

    public boolean isSelectOngoing() { return selectOngoing; }

    public boolean isMoveSelectOngoing() { return moveSelectOngoing; }

    public boolean isMultiWriteOngoing() { return multiWriteOngoing; }

    public Mode getMode() {
        return mode;
    }

    public float getScaleCenterX() {
        return scaleCenterX;
    }

    public float getScaleCenterY() {
        return scaleCenterY;
    }

    public Path getErasePath() {
        return erasePath;
    }

    public Path getSelectPath() {
        return selectPath;
    }

    public List<Path> getWritePaths() { return writePaths; }

    public List<Integer> getWritePathIds() { return writePathIds; }

    public Path getEraserOutDot() { return eraserOutDot; }

    public Path getEraserInDot() { return eraserInDot; }

    public List<MutablePair<Float, Float>> getHistoryPos() { return historyPos; }

    public float getDensity() {
        return density;
    }
}
