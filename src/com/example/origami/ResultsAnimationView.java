package com.example.origami;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import static android.opengl.GLES20.*;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-13
 * Time: 上午11:16
 * To change this template use File | Settings | File Templates.
 */
public class ResultsAnimationView extends GLSurfaceView implements GLSurfaceView.Renderer {

    //TODO 通过修改view的visibility（包括glview）显示和消失，取消glview animation(boolean)

    private OrigamiMesh origamiMesh;

    private View contentView;

    private boolean opened;

    private float[] projectionMatrix = new float[16];

    private int duration = 5400;

    public ResultsAnimationView(Context context) {
        super(context);
        init();
    }

    public ResultsAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //使用OpenGL ES 2.0
        this.setEGLContextClientVersion(2);

        this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        this.setZOrderOnTop(true);
        this.getHolder().setFormat(PixelFormat.TRANSPARENT);

        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0f, 0f, 0f, 0.0f);
        origamiMesh = new OrigamiMesh();
//        origamiMesh.setAnimationFromBottom(true);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        float ratio = width / (float) height;
        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -10f, 10f);

        Bitmap bitmap = OrigamiUtils.loadBitmapFromView(this.contentView, this.getWidth(), this.getHeight());
        origamiMesh.setBitmap(bitmap);
        bitmap.recycle();

        float left, top, right, bottom;
        left = -ratio;
        top = 1;
        right = ratio;
        bottom = -1;

        origamiMesh.setOrigamiRect(new RectF(left, top, right, bottom));
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GLES20.GL_COLOR_BUFFER_BIT);

//        if (animation) {
        this.origamiMesh.draw(this.projectionMatrix);
//        }
    }

    public void openResults() {
        if (!this.opened) {

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
//            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setDuration(duration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //设置mesh数据，刷新gl view
                    origamiMesh.setFactor((Float) valueAnimator.getAnimatedValue());
                    requestRender();
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
//                    animation = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    opened = true;
//                    animation = false;
                    contentView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            valueAnimator.start();
        }
    }

    public void closeResults() {
        if (opened) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 0);
//            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setDuration(duration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //设置mesh数据，刷新gl view
                    origamiMesh.setFactor((Float) valueAnimator.getAnimatedValue());
                    requestRender();

//                    if (contentView.getVisibility() != GONE) {
//                        contentView.setVisibility(GONE);
//                    }
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
//                    animation = true;
                    contentView.setVisibility(INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    opened = false;
//                    animation = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            valueAnimator.start();
        }
    }

    public void setContentView(View contentView) {
        this.contentView = contentView;
    }
}
