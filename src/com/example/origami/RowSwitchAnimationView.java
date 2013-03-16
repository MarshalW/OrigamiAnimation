package com.example.origami;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-15
 * Time: 上午11:17
 * To change this template use File | Settings | File Templates.
 */
public class RowSwitchAnimationView extends FrameLayout implements View.OnClickListener, GLSurfaceView.Renderer {

    private LinearLayout contentLayout;

    private View blankView;

    List<ViewItem> viewItems;

    private GLSurfaceView animationView;

    float[] projectionMatrix = new float[16];

    private int duration = 400;

    float factor;

    private ItemsChooseStatus currentStatus;

    private static final int WHAT_CONTENT_VIEW = 1;

    private static final int WHAT_GL_VIEW = 2;

    private static final int DURATION=10;

    private Handler visibleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            View view = null;
            switch (msg.what) {
                case WHAT_CONTENT_VIEW:
                    view = contentLayout;
                    break;
                case WHAT_GL_VIEW:
                    view = animationView;
                    break;
            }
            view.setVisibility(msg.arg1);
        }
    };

    public RowSwitchAnimationView(Context context, int titleHeight, int contentHeight, List<ViewItem> viewItems) {
        super(context);

        ViewItem.titleHeight = titleHeight;
        ViewItem.contentHeight = contentHeight;
        this.viewItems = viewItems;

        int height = titleHeight * viewItems.size() + contentHeight;
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        this.setBackgroundColor(Color.LTGRAY);

        /**
         * 内容布局
         */
        contentLayout = new LinearLayout(context);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        this.addView(contentLayout);

        /**
         * 空的占位view
         */
        blankView = new View(context);
        blankView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, contentHeight));
        contentLayout.addView(blankView);

        int itemIndex = 0;
        for (ViewItem item : viewItems) {
            //设置标题的布局
            ViewGroup titleRootLayout = new FrameLayout(context);
            titleRootLayout.setBackgroundColor(Color.DKGRAY);
            titleRootLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, titleHeight));
            contentLayout.addView(titleRootLayout);

            titleRootLayout.setOnClickListener(this);
            titleRootLayout.setTag(itemIndex);

            //将自定义view加入到标题布局中
            View.inflate(context, item.titleLayoutId, titleRootLayout);

            //设置内容的布局
            ViewGroup contentRootLayout = new FrameLayout(context);
            contentRootLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, contentHeight));
            contentLayout.addView(contentRootLayout);
            contentRootLayout.setVisibility(GONE);

            //将自定义view加入到内容布局中
            View.inflate(context, item.contentLayoutId, contentRootLayout);

            item.setRootLayouts(titleRootLayout, contentRootLayout);
            item.index = itemIndex;
            itemIndex++;
        }

        /**
         * 创建动画view
         */
        animationView = new GLSurfaceView(context);
        animationView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        //使用OpenGL ES 2.0
        animationView.setEGLContextClientVersion(2);

        animationView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        animationView.setZOrderOnTop(true);
        animationView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        animationView.setRenderer(this);
        animationView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        this.addView(animationView);

        /**
         * 得到视图的宽度并截图
         */
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ViewItem.width = getWidth();
                for (ViewItem item : RowSwitchAnimationView.this.viewItems) {
                    item.generateBitmaps();
                }
            }
        });
    }

    @Override
    public void onClick(final View view) {
//        ViewItem chooseItem = null;
        ItemsChooseStatus status = new ItemsChooseStatus();
        status.setOldItemIndex(viewItems);

        status.setCurrentItemIndex((Integer)view.getTag());

        resetAnimationView(status);

        Log.d("origami","--->>>"+status);

        visibleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                _onClick(view);
            }
        },DURATION);
    }

    private void _onClick(View view){
        ViewItem chooseItem = null;

        blankView.setVisibility(GONE);

        for (ViewItem item : viewItems) {
            if (item.titleRootLayout == view) {
                chooseItem = item;
                if (item.contentRootLayout.getVisibility() == VISIBLE) {
                    blankView.setVisibility(VISIBLE);
                }
            }
            //将所有的内容view关闭
            item.contentRootLayout.setVisibility(GONE);
        }
        if (blankView.getVisibility() == GONE) {
            //仅当blankview gone的时候，显示当前选择的内容view
            chooseItem.contentRootLayout.setVisibility(VISIBLE);
        }
    }

    private void resetAnimationView(final ItemsChooseStatus status) {
        ValueAnimator animator = ValueAnimator.ofFloat(status.getStartFloat(), status.getEndFloat());
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                Log.d("origami", "value animator: " + (Float) valueAnimator.getAnimatedValue());
                factor = (Float) valueAnimator.getAnimatedValue();

                if (status.oldItemIndex != -1) {
                    status.tempIndex = status.oldItemIndex;
                } else {
                    status.tempIndex = status.currentItemIndex;
                }

                currentStatus = status;
                animationView.requestRender();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                visibleHandler.sendMessage(Message.obtain(visibleHandler,WHAT_GL_VIEW,VISIBLE,0));
                visibleHandler.sendMessageDelayed(Message.obtain(visibleHandler,WHAT_CONTENT_VIEW,INVISIBLE,0),DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("origami", "items:" + viewItems.size());
                if (status.next()) {
                    resetAnimationView(status);
                } else {
                    visibleHandler.sendMessage(Message.obtain(visibleHandler,WHAT_CONTENT_VIEW,0));
                    visibleHandler.sendMessageDelayed(Message.obtain(visibleHandler,WHAT_GL_VIEW,INVISIBLE,0),DURATION);
                }
            }
        });
        animator.start();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0f, 0f, 0f, 0.0f);

        for (ViewItem item : viewItems) {
            item.init();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        float ratio = width / (float) height;

        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -10f, 10f);
        ViewItem.ratio = ratio;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (currentStatus == null) {//临时做法
            return;
        }

        for (int i = viewItems.size() - 1; i > -1; i--) {
            viewItems.get(i).draw(currentStatus, this);
        }
    }
}

class ItemsChooseStatus {
    int oldItemIndex = -1, currentItemIndex = -1, tempIndex;
    float currentBottom = -1;


    void setOldItemIndex(List<ViewItem> viewItems) {
        oldItemIndex = getVisibilityIndex(viewItems);
    }

//    void setCurrentItemIndex(List<ViewItem> viewItems) {
//        currentItemIndex = getVisibilityIndex(viewItems);
//    }
    void setCurrentItemIndex(int currentIndex){
        this.currentItemIndex=currentIndex;
        if(currentIndex==oldItemIndex){
            this.currentItemIndex=-1;
        }
    }

    int getVisibilityIndex(List<ViewItem> viewItems) {
        int index = -1;
        int i = 0;
        for (ViewItem item : viewItems) {
            if (item.contentRootLayout.getVisibility() == View.VISIBLE) {
                index = i;
                break;
            }
            i++;
        }

        return index;
    }

    float getStartFloat() {
        return oldItemIndex == -1 ? 0 : 1;
    }

    float getEndFloat() {
        return oldItemIndex == -1 ? 1 : 0;
    }

    boolean next() {
        if (oldItemIndex == -1 || currentItemIndex == -1) {
            return false;
        }
        oldItemIndex = -1;
        return true;
    }

    @Override
    public String toString() {
        return "ItemsChooseStatus{" +
                "currentItemIndex=" + currentItemIndex +
                ", oldItemIndex=" + oldItemIndex +
                '}';
    }

    int getCurrentIndex() {
        return currentItemIndex != -1 ? currentItemIndex : oldItemIndex;
    }

}

class ViewItem {
    static int width, titleHeight, contentHeight;
    int index, titleLayoutId, contentLayoutId;
    ViewGroup titleRootLayout, contentRootLayout;
    OrigamiMesh origamiMesh;
    TitleMesh titleMesh;
    Bitmap titleBitmap, contentBitmap;

//    static float currentBottom=-1;

    //临时
    float titleH = 0.5f, contentH = 1f;
    static float ratio;

    public ViewItem(int titleLayoutId, int contentLayoutId) {
        this.titleLayoutId = titleLayoutId;
        this.contentLayoutId = contentLayoutId;
    }

    public void setRootLayouts(ViewGroup titleRootLayout, ViewGroup contentRootLayout) {
        this.titleRootLayout = titleRootLayout;
        this.contentRootLayout = contentRootLayout;
    }

    void generateBitmaps() {
        //设置折纸mesh，并设置bitmap
        Bitmap texture = OrigamiUtils.loadBitmapFromView(contentRootLayout, width, contentHeight);
        contentBitmap = texture;

        //设置标题mesh，并设置bitmap
        texture = OrigamiUtils.loadBitmapFromView(titleRootLayout, width, titleRootLayout.getHeight());
        titleBitmap = texture;

        OrigamiUtils.saveBitmap(titleBitmap, "title_" + index + ".png");
    }

    /**
     * 必须通过gl线程调用
     */
    void init() {
        origamiMesh = new OrigamiMesh();
        titleMesh = new TitleMesh();
    }

    void draw(ItemsChooseStatus status, RowSwitchAnimationView view) {
        float left, top, right, bottom;
        top = bottom = 0;
        left = -ratio;
        right = ratio;

        if (index == status.tempIndex) {
            origamiMesh.setAnimationFromBottom(true);
            origamiMesh.setBitmap(contentBitmap);
            origamiMesh.setFactor(view.factor);

            bottom = -1 + titleH * (view.viewItems.size() - (index + 1));
            top = bottom + contentH;

            origamiMesh.setOrigamiRect(new RectF(left, top, right, bottom));

            origamiMesh.draw(view.projectionMatrix);

            status.currentBottom = origamiMesh.getTopLeftPoint().y;
        }

        //设置当前标题的左上顶点

        if (index <= status.tempIndex) {
            top = status.currentBottom + titleH;
            bottom = status.currentBottom;

        } else {
            //TODO 这里逻辑太混乱了，要重构，否则只能支持2个item
            top = -0.5f;
            bottom = -1f;
        }

        status.currentBottom = top;

        titleMesh.setRect(new RectF(left, top, right, bottom));
        titleMesh.setTexture(this.titleBitmap);

        titleMesh.draw(view.projectionMatrix);
    }
}



