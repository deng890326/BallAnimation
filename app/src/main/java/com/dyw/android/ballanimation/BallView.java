package com.dyw.android.ballanimation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by wei on 2016/7/25.
 */
public class BallView extends View implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private static final float BALL_WIDTH = 50f;
    private static final float SPEED_PER_SEC = 500;
    private ShapeHolder mShapeHolder;
    AnimatorSet mAnimation = null;
    private float mDensity;
    private double mK;
    private int mDirection;
    private boolean mIsAnimating = false;

    private enum CurrentEdge {
        left, top, right, bottom, none
    }
    private CurrentEdge mCurrentEdge = CurrentEdge.none;

    public BallView(Context context) {
        super(context);
        init();
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BallView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mShapeHolder == null) {
            mShapeHolder = addBall();
        }
    }

    private void createK() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int currentX = (int) mShapeHolder.getX();
        int currentY = (int) mShapeHolder.getY();
        double random =  Math.random();
        mK = Math.tan((Math.PI * 4 * random));
        if (random < 0.5) {
            if (currentX == width || currentY == height) {
                mDirection = -1;
            } else {
                mDirection = 1;
            }
        } else {
            if (currentX == 0 || currentY == 0) {
                mDirection = 1;
            } else {
                mDirection = -1;
            }
        }
    }

    private void createAnimator() {
        int currentX = (int) mShapeHolder.getX();
        int currentY = (int) mShapeHolder.getY();

        Point current = new Point(currentX, currentY);
        Point endPoint = resolveEndPoint(current);
        if (endPoint.x == currentX && endPoint.y == currentY) {
            mDirection = -mDirection;
            endPoint = resolveEndPoint(current);
            if (endPoint.x == currentX && endPoint.y == currentY) {
                mK = -mK;
            }
        }
        int desY = endPoint.y;
        int desX = endPoint.x;

        float dx = desX - currentX;
        float dy = desY - currentY;
        float distance = (float) Math.hypot(dx, dy);
        Log.i("dyw", "distance="+ distance);
        long time = (long) (distance / SPEED_PER_SEC * 1000);
        Log.i("dyw", "time="+ time);
        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(mShapeHolder, "x", currentX, desX).setDuration(time);
        xAnimator.addUpdateListener(this);
        ObjectAnimator yAnimator = ObjectAnimator.ofFloat(mShapeHolder, "y", currentY, desY).setDuration(time);
        yAnimator.addUpdateListener(this);
        mAnimation = new AnimatorSet();
        mAnimation.addListener(this);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.playTogether(xAnimator, yAnimator);
    }

    private Point resolveEndPoint(Point current) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int currentX = current.x;
        int currentY = current.y;
        int desY;
        int desX;
        if (mDirection == 1) {
            desX = (int) (width - 2 * BALL_WIDTH);
            desY = (int) (mK * (desX - currentX) + currentY);
            mCurrentEdge = CurrentEdge.right;
        } else {
            desX = 0;
            desY = (int) (mK * (desX - currentX) + currentY);
            mCurrentEdge = CurrentEdge.left;
        }
        if (desY > (height - 2 * BALL_WIDTH)) {
            desY = (int) (height - 2 * BALL_WIDTH);
            desX = (int) ((desY - currentY) / mK + currentX);
            mCurrentEdge = CurrentEdge.bottom;
        } else if (desY < 0) {
            desY = 0;
            desX = (int) ((desY - currentY) / mK + currentX);
            mCurrentEdge = CurrentEdge.top;
        }
        return new Point(desX, desY);
    }

    public void startAnimation() {
        createK();
        createAnimator();
        mAnimation.start();
        mIsAnimating = true;
    }

    public void stopAnimation() {
        mAnimation.cancel();
        mAnimation = null;
        mIsAnimating = false;
    }

    private ShapeHolder addBall() {
        OvalShape circle = new OvalShape();
        circle.resize(BALL_WIDTH * mDensity, BALL_WIDTH * mDensity);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        ShapeHolder shapeHolder = new ShapeHolder(drawable);
        shapeHolder.setX((float) (Math.random() * getMeasuredHeight() / mDensity));
        shapeHolder.setY((float) (Math.random() * getMeasuredWidth() / mDensity));
        int red = (int)(100 + Math.random() * 155);
        int green = (int)(100 + Math.random() * 155);
        int blue = (int)(100 + Math.random() * 155);
        int color = Color.rgb(red, green, blue);
        Paint paint = drawable.getPaint(); //new Paint(Paint.ANTI_ALIAS_FLAG);
        int darkColor = Color.rgb(red/4, green/4, blue/4);
        RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                50f, color, darkColor, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        shapeHolder.setPaint(paint);
        return shapeHolder;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ShapeHolder shapeHolder = mShapeHolder;
        canvas.save();
        canvas.translate(shapeHolder.getX(), shapeHolder.getY());
        shapeHolder.getShape().draw(canvas);
        canvas.restore();
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        mAnimation = null;
        if (mIsAnimating) {
            mK = -mK;
            createAnimator();
            mAnimation.start();
        }
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        invalidate();
    }
}
