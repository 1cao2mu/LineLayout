package com.example.linelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * 在线路布局内使用的小组件 竖向的跑马灯类似
 */
public class TipsNameView extends View {
    public static final String TAG = "TipsNameView";
    private String tipsNameString = "A因为基线上方为负，所以ascent和top的值都是负数，而且top要大于ascent，原因是要为符号留出位置。B"; // 站名
    private int tipsNameColor = Color.RED; // 文字颜色
    private float tipsNameSize = 35; // 文字大小

    private TextPaint mTextPaint;//文字画笔
    private float oneTextWidth;//一个字的宽度
    private float allTextWidth;//所有字的宽度
    private float oneTextHeight;//一个字的高度
    private boolean isScorll = false;

    public TipsNameView(Context context) {
        super(context);
        init(null, 0);
    }

    public TipsNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TipsNameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TipsNameView, defStyle, 0);
        if (a.hasValue(R.styleable.TipsNameView_tipsName))
            tipsNameString = a.getString(
                    R.styleable.TipsNameView_tipsName);
        tipsNameColor = a.getColor(
                R.styleable.TipsNameView_tipsNameColor,
                tipsNameColor);
        tipsNameSize = a.getDimension(
                R.styleable.TipsNameView_tipsNameSize,
                tipsNameSize);
        a.recycle();

        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
    }


    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(tipsNameSize);
        mTextPaint.setColor(tipsNameColor);
        oneTextWidth = mTextPaint.measureText("测");//计算一个字的宽度
        allTextWidth = mTextPaint.measureText(tipsNameString);//计算所有字的宽度
        oneTextHeight = mTextPaint.descent() - mTextPaint.ascent();//计算一个字的高度
        Log.e(TAG, "ascent: " + mTextPaint.ascent());
        Log.e(TAG, "descent: " + mTextPaint.descent());
        Log.e(TAG, "oneTextHeight: " + oneTextHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidateTextPaintAndMeasurements();
        // 获得它的父容器为它设置的测量模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.e(TAG, "onMeasure widthMode: " + (widthMode == MeasureSpec.AT_MOST));
        Log.e(TAG, "onMeasure heightMode: " + heightMode);
        Log.e(TAG, "onMeasure widthSize: " + widthSize);
        Log.e(TAG, "onMeasure heightSize: " + heightSize);
        if (allTextWidth > widthSize) {
            isScorll = true;
            dx = (int) (3*oneTextWidth);
        } else {
            isScorll = false;
            dx=0;
        }
        int needWidth = (int) allTextWidth;
        if (widthMode == MeasureSpec.AT_MOST && allTextWidth > widthSize) {
            needWidth = widthSize;
        }
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : needWidth, heightMode == MeasureSpec.EXACTLY ? heightSize : (int) oneTextHeight);
    }


    private int dx = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = getWidth();
        int contentHeight = getHeight();
        int needWidth = (int) (oneTextWidth * tipsNameString.length());
        if (isScorll) {
            canvas.drawText(tipsNameString,
                    dx,
                    contentHeight - (int) mTextPaint.descent(),
                    mTextPaint);
            if (dx <= contentWidth - allTextWidth-(int) (3*oneTextWidth)) {
                dx = (int) (3*oneTextWidth);
            } else {
                dx--;
            }
            invalidate();
        } else {
            canvas.drawText(tipsNameString,
                    0,
                    contentHeight - (int) mTextPaint.descent(),
                    mTextPaint);
        }
    }
}
