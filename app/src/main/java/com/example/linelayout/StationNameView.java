package com.example.linelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 在线路布局内使用的小组件 竖向的跑马灯类似
 */
public class StationNameView extends View {
    //可设置属性
    private String stationNameString = ""; // 站名
    private int stationNameColor = Color.BLACK; // 文字颜色
    private float stationNameSize = 25; // 文字大小
    private float stationNameSpeed = 0.5f; // 文字速度
    private boolean stationNameBold = true; // 文字加粗
    //局部变量
    private TextPaint mTextPaint;//文字画笔
    private float oneTextWidth;//一个字的宽度
    private float oneTextHeight;//一个字的高度
    private float dy = 0;

    public StationNameView(Context context) {
        super(context);
        init(null, 0);
    }

    public StationNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public StationNameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.StationNameView, defStyle, 0);
        stationNameString = a.getString(
                R.styleable.StationNameView_stationNameString);
        stationNameColor = a.getColor(
                R.styleable.StationNameView_stationNameColor,
                stationNameColor);
        stationNameSize = a.getDimension(
                R.styleable.StationNameView_stationNameSize,
                stationNameSize);
        stationNameSpeed = a.getFloat(
                R.styleable.StationNameView_stationNameSpeed,
                stationNameSpeed);
        stationNameBold = a.getBoolean(
                R.styleable.StationNameView_stationNameBold,
                stationNameBold);
        a.recycle();
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        if (stationNameBold) {
            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            mTextPaint.setTypeface(Typeface.DEFAULT);
        }
        mTextPaint.setTextSize(stationNameSize);
        mTextPaint.setColor(stationNameColor);
        oneTextWidth = mTextPaint.measureText("测");//计算一个字的宽度
        oneTextHeight = mTextPaint.descent() - mTextPaint.ascent();//计算一个字的高度
    }

    private StaticLayout staticLayout;

    private void invalidateTextPaintAndMeasurements() {
        if (stationNameBold) {
            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            mTextPaint.setTypeface(Typeface.DEFAULT);
        }
        mTextPaint.setTextSize(stationNameSize);
        mTextPaint.setColor(stationNameColor);
        oneTextWidth = mTextPaint.measureText("测");//计算一个字的宽度
        oneTextHeight = mTextPaint.descent() - mTextPaint.ascent();//计算一个字的高度
        staticLayout = new StaticLayout(getPaintContent(stationNameString), mTextPaint, (int) Math.ceil(oneTextWidth),
                Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidateTextPaintAndMeasurements();
    }

    private CharSequence getPaintContent(String stationNameString) {
        StringBuilder content = new StringBuilder(stationNameString.subSequence(0, 1));
        if (stationNameString.length() > 1) {
            for (int i = 1; i < stationNameString.length(); i++) {
                content.append("\n").append(stationNameString.subSequence(i, i + 1));
            }
        }
        return content.toString();
    }

    private boolean isUp = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int contentWidth = getWidth();
        int contentHeight = getHeight();
        float allTextHeight = oneTextHeight * stationNameString.length();
        canvas.save();
        if (allTextHeight > contentHeight) {
            canvas.translate(contentWidth / 2f, dy);
        } else {
            canvas.translate(contentWidth / 2f, 0);
        }
        staticLayout.draw(canvas);
        canvas.restore();
        if (allTextHeight > contentHeight) {
            float dis = allTextHeight - contentHeight;
            if (!isUp && dy >= 0) {
                isUp = true;
            }
            if (isUp && dy < -dis) {
                isUp = false;
            }
            if (isUp) {
                dy = dy - stationNameSpeed;
            } else {
                dy = dy + stationNameSpeed;
            }
            invalidate();
        }
    }

    public void setStationNameString(String stationNameString) {
        this.stationNameString = stationNameString;
        invalidate();
    }

    public void setStationNameColor(int stationNameColor) {
        this.stationNameColor = stationNameColor;
        invalidate();
    }

    public void setStationNameSize(float stationNameSize) {
        this.stationNameSize = stationNameSize;
        invalidate();
    }

    public void setStationNameSpeed(float stationNameSpeed) {
        this.stationNameSpeed = stationNameSpeed;
        invalidate();
    }

    public void setStationNameBold(boolean stationNameBold) {
        this.stationNameBold = stationNameBold;
        invalidate();
    }

    public float getOneTextWidth() {
        return oneTextWidth;
    }

    public float getOneTextHeight() {
        return oneTextHeight;
    }
}
