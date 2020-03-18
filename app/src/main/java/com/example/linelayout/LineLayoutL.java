package com.example.linelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义线路组件
 */
public class LineLayoutL extends ViewGroup {
    private static final int STARTENDMOD_TOP = 0;
    private static final int STARTENDMOD_CENTER = 1;
    //可以设置的参数变量
    private float tipsNameLayoutHeight = 100;
    private int lineLayoutHeight = 40;
    private int lineViewHeight = 22;
    private NinePatch lineDrawableNoPassNinePatch;
    private NinePatch lineDrawablePassedNinePatch;
    private NinePatch lineDrawablePassingNinePatch;
    private Drawable pointDrawableNoPass;
    private Drawable pointDrawablePassed;
    private Drawable pointDrawablePassing;

    private int stationNameNoPassColor = Color.BLUE;
    private int stationNamePassedColor = Color.RED;
    private int stationNamePassingColor = Color.YELLOW;
    private float stationNameSize = 25;
    private float stationNameSpeed = 0.5f;
    private boolean stationNameBold = true;
    private int stationNameMaxLine = 7;
    private int stopNumber = -1;
    private int stopType = -1;//0是进站 1是出站

    private Drawable startDrawable;
    private Drawable endDrawable;
    private float startEndWidth = 20;
    private float startEndHeight = 20;
    private int startEndMod;
    private float stationWidth = 20;
    private float stationHeight = 20;
    private AnimationDrawable pointAnim;
    private float pointAnimWidth = 20;
    private float pointAnimHeight = 20;
    private int pointAnimCurrentInt = -1;
    private Drawable enterOutDrawable;
    private float enterOutWidth = 40;
    private float enterOutHeight = 40;
    private int enterOutRate = 3;
    //需要使用的局部变量
    private float perWidth;//每个站点所需要占的大小
    private Context context;
    private float[] linePoints;
    private float[] stationPoints;
    private float stationOneTextWidth;
    private List<String> listData = new ArrayList<>();//线路数据
    private Rect rect;



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

    //子组件和相关
    private ImageView iv_next_tip;
    private TextView tv_next_tip;
    private TipsNameView tv_change_message;


    public LineLayoutL(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public LineLayoutL(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public LineLayoutL(Context context, AttributeSet attrs, int defStyle) {
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
        perWidth = (contentWidth - stationOneTextWidth) / (listData.size() - 1);
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
                left = (int) (paddingLeft + j * perWidth);
                right = left + childMeasureWidth;
                top = (int) tipsNameLayoutHeight + lineLayoutHeight + paddingTop;
                bottom = top + childMeasureHeight;
                //确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
                child.layout(left, top, right, bottom);
            } else {
                child.layout((int) (paddingLeft + startEndWidth), paddingTop, (int) (contentWidth + paddingLeft - startEndWidth), (int) tipsNameLayoutHeight + paddingTop);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算出所有的childView的宽和高
        // 计算出所有的childView的宽和高
        final int size = getChildCount();
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (child instanceof StationNameView) {
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                } else {
                    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                    int widthMeasureSpec_t = MeasureSpec.makeMeasureSpec((int) (widthSize - startEndWidth - startEndWidth), MeasureSpec.EXACTLY);
                    measureChild(child, widthMeasureSpec_t, heightMeasureSpec);
                }
            }
        }
        //测量并保存layout的宽高(使用getDefaultSize时，wrap_content和match_perent都是填充屏幕)
        //稍后会重新写这个方法，能达到wrap_content的效果
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }


    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LineLayoutL, defStyle, 0);

        if (a.hasValue(R.styleable.LineLayoutL_enterOutDrawable)) {
            enterOutDrawable = a.getDrawable(
                    R.styleable.LineLayoutL_enterOutDrawable);
        }

        if (a.hasValue(R.styleable.LineLayoutL_pointAnim)) {
            Drawable drawable = a.getDrawable(
                    R.styleable.LineLayoutL_pointAnim);
            if (drawable instanceof AnimationDrawable) {
                pointAnim = (AnimationDrawable) drawable;
                if (pointAnim.getNumberOfFrames() > 0) {
                    pointAnimCurrentInt = 0;
                }
            }
        }
        if (a.hasValue(R.styleable.LineLayoutL_startDrawable)) {
            startDrawable = a.getDrawable(
                    R.styleable.LineLayoutL_startDrawable);
        }
        if (a.hasValue(R.styleable.LineLayoutL_endDrawable)) {
            endDrawable = a.getDrawable(
                    R.styleable.LineLayoutL_endDrawable);
        }

        enterOutWidth = a.getDimension(R.styleable.LineLayoutL_enterOutWidth, enterOutWidth);
        enterOutHeight = a.getDimension(R.styleable.LineLayoutL_enterOutHeight, enterOutHeight);
        startEndWidth = a.getDimension(R.styleable.LineLayoutL_startEndWidth, startEndWidth);
        startEndHeight = a.getDimension(R.styleable.LineLayoutL_startEndHeight, startEndHeight);
        startEndMod = a.getInt(R.styleable.LineLayoutL_startEndMod, startEndMod);
        enterOutRate = a.getInt(R.styleable.LineLayoutL_enterOutRate, enterOutRate);

        if (a.hasValue(R.styleable.LineLayoutL_pointDrawableNoPassL)) {
            pointDrawableNoPass = a.getDrawable(
                    R.styleable.LineLayoutL_pointDrawableNoPassL);
        } else {
            pointDrawableNoPass = getResources().getDrawable(R.drawable.point_blue);
        }
        if (a.hasValue(R.styleable.LineLayoutL_lineDrawablePassedL)) {
            pointDrawablePassed = a.getDrawable(
                    R.styleable.LineLayoutL_lineDrawablePassedL);
        } else {
            pointDrawablePassed = getResources().getDrawable(R.drawable.point_red);
        }
        if (a.hasValue(R.styleable.LineLayoutL_pointDrawablePassingL)) {
            pointDrawablePassing = a.getDrawable(
                    R.styleable.LineLayoutL_pointDrawablePassingL);
        } else {
            pointDrawablePassing = getResources().getDrawable(R.drawable.point_yellow);
        }

        int lineDrawableNoPassId = a.getResourceId(
                R.styleable.LineLayoutL_lineDrawableNoPassL, R.drawable.default_r_line_view_img_no_pass);
        Bitmap lineDrawableNoPassBitmap = BitmapFactory.decodeResource(getResources(), lineDrawableNoPassId);
        lineDrawableNoPassNinePatch = new NinePatch(lineDrawableNoPassBitmap, lineDrawableNoPassBitmap.getNinePatchChunk(), null);
        int lineDrawablePassedId = a.getResourceId(
                R.styleable.LineLayoutL_lineDrawablePassedL, R.drawable.default_r_line_view_img_passed);
        Bitmap lineDrawablePassedBitmap = BitmapFactory.decodeResource(getResources(), lineDrawablePassedId);
        lineDrawablePassedNinePatch = new NinePatch(lineDrawablePassedBitmap, lineDrawablePassedBitmap.getNinePatchChunk(), null);
        int lineDrawablePassingId = a.getResourceId(
                R.styleable.LineLayoutL_lineDrawablePassingL, R.drawable.default_r_line_view_img_passing);
        Bitmap lineDrawablePassingBitmap = BitmapFactory.decodeResource(getResources(), lineDrawablePassingId);
        lineDrawablePassingNinePatch = new NinePatch(lineDrawablePassingBitmap, lineDrawablePassingBitmap.getNinePatchChunk(), null);
        rect = new Rect();
        a.recycle();
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
        float lineCenter = tipsNameLayoutHeight + paddingTop + lineLayoutHeight / 2f;
        float paddingLeftLeft = paddingLeft + stationOneTextWidth / 2f;
        for (int i = 0; i < listData.size(); i++) {
            if (i != listData.size() - 1) {
                linePoints[i * 4] = paddingLeftLeft + (i * perWidth);
                linePoints[i * 4 + 1] = lineCenter;
                linePoints[i * 4 + 2] = paddingLeftLeft + ((i + 1.0f) * perWidth);
                linePoints[i * 4 + 3] = lineCenter;
            }
            stationPoints[i * 2] = paddingLeftLeft + i * perWidth;
            stationPoints[i * 2 + 1] = lineCenter;
        }
        for (int i = 0; i < listData.size() - 1; i++) {
            NinePatch lineNinePatch;
            if (i < stopNumber) {
                lineNinePatch = lineDrawablePassedNinePatch;
            } else {
                lineNinePatch = lineDrawableNoPassNinePatch;
            }
            rect.left = (int) (linePoints[i * 4]);
            rect.top = (int) (linePoints[i * 4 + 1] - lineViewHeight / 2f);
            rect.right = (int) (linePoints[i * 4 + 2]);
            rect.bottom = (int) (linePoints[i * 4 + 3] + lineViewHeight / 2f);
            lineNinePatch.draw(canvas, rect);
        }

        for (int i = 0; i < listData.size(); i++) {
            Drawable pointDrawable;
            if (i < stopNumber) {
                pointDrawable = pointDrawablePassed;
            } else if (i == stopNumber) {
                pointDrawable = pointDrawablePassing;
            } else {
                pointDrawable = pointDrawableNoPass;
            }
            if (i == 0 || i == listData.size() - 1) {
                Drawable drawable = startDrawable;
                if (i == listData.size() - 1) {
                    drawable = endDrawable;
                }
                if (startEndMod == STARTENDMOD_CENTER) {
                    drawable.setBounds((int) (stationPoints[i * 2] - startEndHeight / 2), (int) (lineCenter - startEndHeight / 2),
                            (int) (stationPoints[i * 2] + startEndHeight / 2), (int) (lineCenter + startEndHeight / 2));
                    drawable.draw(canvas);
                    continue;
                } else if (startEndMod == STARTENDMOD_TOP) {
                    drawable.setBounds((int) (stationPoints[i * 2] - startEndHeight / 2), (int) (lineCenter - lineViewHeight / 2 - startEndHeight),
                            (int) (stationPoints[i * 2] + startEndHeight / 2), (int) (lineCenter - lineViewHeight / 2));
                    drawable.draw(canvas);
                }
            }
            if (stopNumber == i) {
                if (pointAnim != null && pointAnimCurrentInt != -1) {
                    Drawable drawable = pointAnim.getFrame(pointAnimCurrentInt);
                    drawable.setBounds((int) (stationPoints[i * 2] - pointAnimWidth / 2), (int) (lineCenter - pointAnimHeight / 2),
                            (int) (stationPoints[i * 2] + pointAnimWidth / 2), (int) (lineCenter + pointAnimHeight / 2));
                    drawable.draw(canvas);
                    if (!handler.hasMessages(0)) {
                        handler.sendEmptyMessageDelayed(0, pointAnim.getDuration(pointAnimCurrentInt));
                    }
                }
                continue;
            }
            pointDrawable.setBounds((int) (stationPoints[i * 2] - stationWidth / 2), (int) (lineCenter - stationHeight / 2),
                    (int) (stationPoints[i * 2] + stationWidth / 2), (int) (lineCenter + stationHeight / 2));
            pointDrawable.draw(canvas);
        }
        if (enterOutDrawable != null) {
            if (stopNumber != -1&&stopType==1) {
                enterOutDrawable.setBounds((int) (stationPoints[(stopNumber-1) * 2] - enterOutWidth / 2 + enterOutOffset), (int) (lineCenter - enterOutHeight / 2),
                        (int) (stationPoints[(stopNumber-1) * 2] + enterOutWidth / 2 + enterOutOffset), (int) (lineCenter + enterOutHeight / 2));
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
        int listSize = listData.size();
        for (int i = 0; i < listData.size(); i++) {
            String content = listData.get(i);
            StationNameView stationNameView = new StationNameView(context);
            stationNameView.setStationNameSize(stationNameSize);
            stationNameView.setStationNameBold(stationNameBold);
            stationNameView.setStationNameSpeed(stationNameSpeed);
            int nameColor;
            if (i < stopNumber) {
                nameColor = stationNamePassedColor;
            } else if (i == stopNumber) {
                nameColor = stationNamePassingColor;
            } else {
                nameColor = stationNameNoPassColor;
            }
            stationNameView.setStationNameColor(nameColor);
            stationOneTextWidth = stationNameView.getOneTextWidth();
            float oneTextHeight = stationNameView.getOneTextHeight();
            int lineNum = content.length() > stationNameMaxLine ? stationNameMaxLine : content.length();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) stationOneTextWidth, (int) (oneTextHeight * lineNum) + 1);
            stationNameView.setLayoutParams(layoutParams);
            stationNameView.setStationNameString(listData.get(i));
            addView(stationNameView);
        }
        linePoints = new float[(listData.size() - 1) * 4];
        stationPoints = new float[listData.size() * 2];
        View view = View.inflate(context, R.layout.view_next_station_tips, null);
        iv_next_tip = view.findViewById(R.id.iv_next_tip);
        tv_next_tip = view.findViewById(R.id.tv_next_tip);
        tv_change_message = view.findViewById(R.id.tv_change_message);
        if (stopNumber != -1) {
            view.setVisibility(View.VISIBLE);
            int s = stopNumber;
            if (stopNumber == listSize) {
                s = 0;
            }
            tv_next_tip.setText(stopType == 0 ? "当前站:" + listData.get(s) : "下一站:" + listData.get(s));
        } else {
            view.setVisibility(View.INVISIBLE);
        }
        iv_next_tip.setVisibility(View.GONE);
        addView(view);
    }

    public ImageView getIv_next_tip() {
        return iv_next_tip;
    }

    public TextView getTv_next_tip() {
        return tv_next_tip;
    }

    public TipsNameView getTv_change_message() {
        return tv_change_message;
    }

    public void setStopNumber(int stopNumber, int stopType) {
        this.stopType = stopType;
        if (stopType == 1) {
            this.stopNumber = stopNumber + 1;
        } else {
            this.stopNumber = stopNumber;
        }
        handleListData();
    }


}
