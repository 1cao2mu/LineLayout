package com.example.linelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义线路组件
 */
public class LineLayout extends ViewGroup {
    //可以设置的参数变量
    private static final int STARTENDMOD_TOP = 0;
    private static final int STARTENDMOD_CENTER = 1;
    private Drawable startDrawable;
    private Drawable endDrawable;
    private float startEndWidth = 20;
    private float startEndHeight = 20;

    private int startEndMod;
    private float stationWidth = 20;
    private float stationHeight = 20;
    private float lineHeight = 20;
    private float lineViewHeight = 30;
    private Drawable pointDrawable;
    private Bitmap lineBitmap;
    private Drawable lineDrawable;
    private Boolean lineBitmapIsNinePatch = false;
    private List<String> listData = new ArrayList<>();//线路数据
    private int linePaintNotPassedColor = Color.BLUE;//默认的未驶过线路颜色
    private int linePaintPassedColor = Color.RED;//默认的已驶过线路颜色
    private int pointPaintNotPassedColor = Color.GRAY;//默认的未驶过站点颜色
    private int pointPaintPassedColor = Color.BLACK;//默认的未驶过站点颜色
    private float offsetTop = 200;//顶部预留多少给站点提示信息
    private int stopNumber = 3;//从零开始
    //需要使用的局部变量
    private Paint linePaintNotPassed;//画未驶过线路的画笔
    private Paint linePaintPassed;//画已驶过线路的画笔
    private Paint pointPaintNotPassed;//画未驶过站点的画笔
    private Paint pointPaintPassed;//画已驶过站点的画笔
    private float perWidth;//每个站点所需要占的大小
    private Context context;
    private float[] linePoints;
    private float[] stationPoints;
    private AnimationDrawable pointAnim;
    private float pointAnimWidth = 20;
    private float pointAnimHeight = 20;
    private int pointAnimCurrentInt = -1;

    private Drawable enterOutDrawable;
    private float enterOutWidth = 40;
    private float enterOutHeight = 40;
    private int enterOutRate = 3;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                pointAnimCurrentInt++;
                if (pointAnim == null) {
                    pointAnimCurrentInt = -1;
                }
                if (pointAnimCurrentInt > pointAnim.getNumberOfFrames() - 1) {
                    pointAnimCurrentInt = 0;
                }
                postInvalidate();
            }
            return true;
        }
    });


    public LineLayout(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public LineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public LineLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs, defStyle);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e("onLayout", getWidth() + " " + getHeight());
        int totalWidth = getWidth();
        int totalHeight = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = totalWidth - paddingLeft - paddingRight;
        int contentHeight = totalHeight - paddingTop - paddingBottom;
        perWidth = contentWidth / (listData.size());
        final int count = getChildCount();
        int left, top, right, bottom;
        for (int i = 0, j = -1; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof StationNameView) {
                j++;
                //注意此处不能使用getWidth和getHeight，这两个方法必须在onLayout执行完，才能正确获取宽高
                int childMeasureWidth = child.getMeasuredWidth();
                int childMeasureHeight = child.getMeasuredHeight();
                //如果一行没有排满，继续往右排列
                left = (int) (paddingLeft + (j + 0.5f) * perWidth - childMeasureWidth / 2f);
                right = left + childMeasureWidth;
                top = (int) offsetTop + 30;
                bottom = top + childMeasureHeight;
                //确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
                child.layout(left, top, right, bottom);
            } else {
                child.layout(paddingLeft, 0, contentWidth + paddingLeft, (int) offsetTop);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //测量并保存layout的宽高(使用getDefaultSize时，wrap_content和match_perent都是填充屏幕)
        //稍后会重新写这个方法，能达到wrap_content的效果
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    private NinePatch ninePatch;
    private Rect rect;

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LineLayout, defStyle, 0);

        if (a.hasValue(R.styleable.LineLayout_enterOutDrawable)) {
            enterOutDrawable = a.getDrawable(
                    R.styleable.LineLayout_enterOutDrawable);
        }

        if (a.hasValue(R.styleable.LineLayout_pointAnim)) {
            Drawable drawable = a.getDrawable(
                    R.styleable.LineLayout_pointAnim);
            if (drawable instanceof AnimationDrawable) {
                pointAnim = (AnimationDrawable) drawable;
                if (pointAnim.getNumberOfFrames() > 0) {
                    pointAnimCurrentInt = 0;
                }
            }
        }
        if (a.hasValue(R.styleable.LineLayout_startDrawable)) {
            startDrawable = a.getDrawable(
                    R.styleable.LineLayout_startDrawable);
        }
        if (a.hasValue(R.styleable.LineLayout_endDrawable)) {
            endDrawable = a.getDrawable(
                    R.styleable.LineLayout_endDrawable);
        }

        enterOutWidth = a.getDimension(R.styleable.LineLayout_enterOutWidth, enterOutWidth);
        enterOutHeight = a.getDimension(R.styleable.LineLayout_enterOutHeight, enterOutHeight);
        startEndWidth = a.getDimension(R.styleable.LineLayout_startEndWidth, startEndWidth);
        startEndHeight = a.getDimension(R.styleable.LineLayout_startEndHeight, startEndHeight);
        startEndMod = a.getInt(R.styleable.LineLayout_startEndMod, startEndMod);
        enterOutRate = a.getInt(R.styleable.LineLayout_enterOutRate, enterOutRate);


        if (a.hasValue(R.styleable.LineLayout_pointDrawable)) {
            pointDrawable = a.getDrawable(
                    R.styleable.LineLayout_pointDrawable);
            if (pointDrawable != null)
                pointDrawable.setCallback(this);
        }
        if (a.hasValue(R.styleable.LineLayout_lineDrawable)) {
            lineDrawable = a.getDrawable(
                    R.styleable.LineLayout_lineDrawable);
            if (lineDrawable != null) {
                lineDrawable.setCallback(this);
            }
            lineBitmapIsNinePatch = lineDrawable != null && lineDrawable instanceof NinePatchDrawable;
            if (lineBitmapIsNinePatch) {
                int lineDrawableId = a.getResourceId(
                        R.styleable.LineLayout_lineDrawable, R.drawable.ic_launcher_2);
                lineBitmap = BitmapFactory.decodeResource(getResources(), lineDrawableId);
                ninePatch = new NinePatch(lineBitmap, lineBitmap.getNinePatchChunk(), null);
            }
            rect = new Rect();
        }

        a.recycle();
        initData();
    }

    private void initData() {
        linePoints = new float[(listData.size() + 1) * 4];
        stationPoints = new float[listData.size() * 2];
        linePaintNotPassed = new Paint();
        linePaintNotPassed.setColor(linePaintNotPassedColor);
        linePaintNotPassed.setFlags(Paint.ANTI_ALIAS_FLAG);
        linePaintNotPassed.setStrokeWidth(lineHeight);
        linePaintPassed = new Paint();
        linePaintPassed.setColor(linePaintPassedColor);
        linePaintPassed.setFlags(Paint.ANTI_ALIAS_FLAG);
        linePaintPassed.setStrokeWidth(lineHeight);
        pointPaintNotPassed = new Paint();
        pointPaintNotPassed.setColor(pointPaintNotPassedColor);
        pointPaintNotPassed.setFlags(Paint.ANTI_ALIAS_FLAG);
        pointPaintNotPassed.setStrokeWidth(20);
        pointPaintPassed = new Paint();
        pointPaintPassed.setColor(pointPaintPassedColor);
        pointPaintPassed.setFlags(Paint.ANTI_ALIAS_FLAG);
        pointPaintPassed.setStrokeWidth(20);
    }


    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("onDraw", getWidth() + " " + getHeight());
        //计算高度、宽度、padding和内容高宽
        int totalWidth = getWidth();
        int totalHeight = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = totalWidth - paddingLeft - paddingRight;
        int contentHeight = totalHeight - paddingTop - paddingBottom;
        perWidth = contentWidth / (listData.size());
        for (int i = 0; i < listData.size(); i++) {
            if (i == 0) {
                linePoints[i * 4] = paddingLeft;
                linePoints[i * 4 + 1] = offsetTop;
                linePoints[i * 4 + 2] = paddingLeft + (i + 0.5f) * perWidth;
                linePoints[i * 4 + 3] = offsetTop;
            }
            int j = i + 1;
            linePoints[j * 4] = paddingLeft + ((i + 0.5f) * perWidth);
            linePoints[j * 4 + 1] = offsetTop;
            if (i == listData.size() - 1) {
                linePoints[j * 4 + 2] = paddingLeft + ((i + 1.0f) * perWidth);
            } else {
                linePoints[j * 4 + 2] = paddingLeft + ((i + 1.5f) * perWidth);
            }
            linePoints[j * 4 + 3] = offsetTop;
            stationPoints[i * 2] = paddingLeft + (i + 0.5f) * perWidth;
            stationPoints[i * 2 + 1] = offsetTop;
        }
        for (int i = 0; i < listData.size() - 1; i++) {
            int j = i + 1;
            rect.left = (int) (linePoints[j * 4]);
            rect.top = (int) (linePoints[j * 4 + 1] - 10);
            rect.right = (int) (linePoints[j * 4 + 2]);
            rect.bottom = (int) (linePoints[j * 4 + 3] + 10);
            if (lineBitmapIsNinePatch) {
                ninePatch.draw(canvas, rect);
            } else {
                lineDrawable.setBounds(rect);
                lineDrawable.draw(canvas);
            }
        }

        for (int i = 0; i < listData.size(); i++) {
            if (i == 0 || i == listData.size() - 1) {
                Drawable drawable = startDrawable;
                if (i == listData.size() - 1) {
                    drawable = endDrawable;
                }
                if (startEndMod == STARTENDMOD_CENTER) {
                    drawable.setBounds((int) (stationPoints[i * 2] - startEndHeight / 2), (int) (offsetTop - startEndHeight / 2),
                            (int) (stationPoints[i * 2] + startEndHeight / 2), (int) (offsetTop + startEndHeight / 2));
                    drawable.draw(canvas);
                    continue;
                } else if (startEndMod == STARTENDMOD_TOP) {
                    drawable.setBounds((int) (stationPoints[i * 2] - startEndHeight / 2), (int) (offsetTop - lineViewHeight / 2 - startEndHeight),
                            (int) (stationPoints[i * 2] + startEndHeight / 2), (int) (offsetTop - lineViewHeight / 2));
                    drawable.draw(canvas);
                }
            }
            if (stopNumber == i) {
                if (pointAnim != null && pointAnimCurrentInt != -1) {
                    Drawable drawable = pointAnim.getFrame(pointAnimCurrentInt);
                    drawable.setBounds((int) (stationPoints[i * 2] - pointAnimWidth / 2), (int) (offsetTop - pointAnimHeight / 2),
                            (int) (stationPoints[i * 2] + pointAnimWidth / 2), (int) (offsetTop + pointAnimHeight / 2));
                    drawable.draw(canvas);
                    if (!handler.hasMessages(0)) {
                        handler.sendEmptyMessageDelayed(0, pointAnim.getDuration(pointAnimCurrentInt));
                    }
                }

                continue;
            }
            pointDrawable.setBounds((int) (stationPoints[i * 2] - stationWidth / 2), (int) (offsetTop - stationHeight / 2),
                    (int) (stationPoints[i * 2] + stationWidth / 2), (int) (offsetTop + stationHeight / 2));
            pointDrawable.draw(canvas);
        }
        if (enterOutDrawable != null) {
            if (stopNumber != -1) {
                enterOutDrawable.setBounds((int) (stationPoints[stopNumber * 2] - enterOutWidth / 2 + enterOutOffset), (int) (offsetTop - enterOutHeight / 2),
                        (int) (stationPoints[stopNumber * 2] + enterOutWidth / 2 + enterOutOffset), (int) (offsetTop + enterOutHeight / 2));
                enterOutDrawable.draw(canvas);
                enterOutOffset = enterOutOffset + enterOutRate;
                if (enterOutOffset > perWidth) {
                    enterOutOffset = 0;
                }
                invalidate();
            }
        }
    }

    private float enterOutOffset = 0;


    public void setListData(List<String> listData) {
        Log.e("setListData", getWidth() + " " + getHeight());
        this.listData = listData;
        handleListData();
    }

    private void handleListData() {
        Log.e("handleListData", getWidth() + " " + getHeight());
        removeAllViews();
        for (int i = 0; i < listData.size(); i++) {
            StationNameView stationNameView = new StationNameView(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(30, 60);
            stationNameView.setLayoutParams(layoutParams);
            stationNameView.setStationNameSize(30);
            stationNameView.setStationNameString(listData.get(i));
            addView(stationNameView);
        }
        linePoints = new float[(listData.size() + 1) * 4];
        stationPoints = new float[listData.size() * 2];
        View view = View.inflate(context, R.layout.view_next_station_tips, null);
        addView(view);
    }


}
