package com.lt.viewpagerindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by lt on 2018/4/20.
 */
public class ImagePagerIndicator extends LinearLayout {

    private static final String TAG = "ImagePagerIndicator";
    /**
     * 指示器图片和容器高度比率最大值（为了更好的适配，防止图片过大）
     */
    public static final float DEFAULT_IMAGE_HEIGHT_RADIO = 1/4F;
    /**
     * 默认可见tab的数量
     */
    public static final int DEFAULT_TAB_VISABLE_COUNT = 3;
    /**
     * 选中与没有选中的颜色
     */
    private int mHigh_light_color;
    private int mNumal_color;
    /**
     * 指示器的宽高
     */
    private int mIndicatorHeight;
    private int mIndicatorWidth;
    /**
     * tab标题的宽度
     */
    private int mTabWidth;
    private Paint mPaint;
    /**
     * 指示器初始时的偏移量
     */
    private int mStartTranslateX;
    /**
     * 指示器跟随移动的偏移量
     */
    private int mTranslateX ;
    /**
     * 可见tab标题的数量
     */
    private int mTabVisiableCount = DEFAULT_TAB_VISABLE_COUNT;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;

    /**
     * 指示器图片
     */
    private Bitmap mImageBitmap;
    /**
     * 指定图片指示器和容器的最大高度比率
     */
    private float mImageHeightRadio;


    public ImagePagerIndicator(Context context) {
        this(context,null);
    }

    public ImagePagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ImagePagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setPathEffect(new CornerPathEffect(3)); // 设置画笔平滑圆角，不然看起来尖锐
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
        // 获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImagePagerIndicator);
        mHigh_light_color = typedArray.getColor(R.styleable.ImagePagerIndicator_tab_select_color, Color.RED);
        mNumal_color = typedArray.getColor(R.styleable.ImagePagerIndicator_tab_unselect_color, Color.GRAY);
        mTabVisiableCount = typedArray.getInteger(R.styleable.ImagePagerIndicator_tab_visiable_count, DEFAULT_TAB_VISABLE_COUNT);
        mImageHeightRadio = typedArray.getFloat(R.styleable.ImagePagerIndicator_image_radio_height, DEFAULT_IMAGE_HEIGHT_RADIO);
        mIndicatorHeight = (int) typedArray.getDimension(R.styleable.ImagePagerIndicator_indicator_height,dp2px(6));
        if(mTabVisiableCount <1){
            mTabVisiableCount = 1; // 指定最小可见数量为1
        }
        Drawable drawable = typedArray.getDrawable(R.styleable.ImagePagerIndicator_tab_indicator);
        // Drawable转Bitmap
        if(drawable instanceof BitmapDrawable) {
            mImageBitmap = ((BitmapDrawable)drawable).getBitmap();
        }else if(drawable instanceof ColorDrawable){
            mImageBitmap = Bitmap.createBitmap(2,mIndicatorHeight,
                    Bitmap.Config.ARGB_8888);
            mImageBitmap.eraseColor(((ColorDrawable) drawable).getColor());//填充颜色
        }
        typedArray.recycle();
    }

    /**
     * 每次view的尺寸发生变化时都会回调
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG,"onSizeChanged:w"+w+"h"+h+"oldw"+oldw+"oldh"+oldh);
        mTabWidth = w/mTabVisiableCount;
        mIndicatorHeight = mImageBitmap.getHeight();
        mIndicatorWidth = mImageBitmap.getWidth();
        if(mIndicatorWidth > mTabWidth || mIndicatorWidth == 2){
            mIndicatorWidth = mTabWidth;
        }
        int maxIndicatorHeight = (int) (h* mImageHeightRadio);
        if(mIndicatorHeight > maxIndicatorHeight){
            mIndicatorHeight = maxIndicatorHeight;
        }
        Log.i(TAG,"mIndicatorHeight"+mIndicatorHeight);
        mStartTranslateX = mTabWidth/2 - mIndicatorWidth/2;
        changeTabWidth();
    }

    /**
     * 代码设置tab的标题，及文字大小(单位为sp）
     * @param titles
     * @param textSize
     */
    public void setTabTitles(String[] titles,float textSize){
        if(titles == null || titles.length ==0){
            return;
        }
        // 动态添加Title，这里将使得布局设置的tab标题全部移除
        removeAllViews();
        for(String title : titles){
            TextView textView = new TextView(getContext());
            textView.setText(title);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
            LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            addView(textView,layoutParams);
        }
    }

    /**
     * 代码改变tab标题的布局宽度，防止布局中为不同的title设置不同的宽度
     */
    private void changeTabWidth() {
        int childCount = getChildCount();
        if(childCount == 0){
            return;
        }
        for(int i=0;i<childCount;i++){
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            layoutParams.weight = 0;
            layoutParams.width = getWidth()/mTabVisiableCount;
            child.setLayoutParams(layoutParams);
        }
    }

    /**
     * 设置ViewPager，实现绑定
     * @param viewPager
     * @param pos
     */
    public void setViewPager(ViewPager viewPager, final int pos){
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(mOnPageChangeListener != null){
                    mOnPageChangeListener.onPageScrolled(position,positionOffset,positionOffsetPixels);
                }
                Log.i("onPageScrolled():","positionOffset:"+positionOffset);
                ImagePagerIndicator.this.scroll(position,positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                if(mOnPageChangeListener != null){
                    mOnPageChangeListener.onPageSelected(position);
                }
                resetTextColor();
                highlightTextColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(mOnPageChangeListener != null){
                    mOnPageChangeListener.onPageScrollStateChanged(state);
                }
            }
        });
        mViewPager.setCurrentItem(pos);
        resetTextColor();
        highlightTextColor(pos);
        setTabClickListener();
    }

    /**
     * 设置tab的点击事件
     */
    private void setTabClickListener(){
        for(int i=0;i<getChildCount();i++){
            final int j = i;
            getChildAt(i).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewPager.setCurrentItem(j);
                }
            });
        }
    }

    /**
     * 重置文本颜色
     */
    private void resetTextColor(){
        for(int i=0;i<getChildCount();i++){
            View view = getChildAt(i);
            if(view instanceof TextView){
                ((TextView) view).setTextColor(mNumal_color);
            }
        }
    }

    /**
     * 高亮文本
     * @param pos
     */
    private void highlightTextColor(int pos){
        View view = getChildAt(pos);
        if(view instanceof TextView){
            ((TextView) view).setTextColor(mHigh_light_color);
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        // 平移画布
        canvas.translate(mStartTranslateX +mTranslateX,getHeight()-mIndicatorHeight);
        // 设置图片的裁剪区域，为null则不裁剪
        Rect src = new Rect(0,0,mIndicatorWidth,mIndicatorHeight);
        // 设置图片为画布中显示的区域，由于将画布平移了，这里和图片的裁剪区域一致
        Rect dest = new Rect(0,0,mIndicatorWidth,mIndicatorHeight);
        // 绘制图片
        canvas.drawBitmap(mImageBitmap,src,dest,mPaint);
        canvas.restore();
    }

    /**
     * 实现指示器和布局容器的联动
     * @param position
     * @param offset
     */
    private void scroll(int position, float offset) {
        // 实现指示器的滚动
        mTranslateX = (int) (mTabWidth*(offset+position));
        invalidate();
        // 实现容器联动
        Log.i(TAG,position+"%"+mTabVisiableCount+":"+position%mTabVisiableCount);
        // 什么时候容器需要滚动？
        if(offset > 0 && getChildCount() > mTabVisiableCount && position > (mTabVisiableCount -2)){
            this.scrollTo((position - mTabVisiableCount + 1) * mTabWidth + (int) (offset * mTabWidth), 0);
        }
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    /**
     * dp转 px.
     * @param value the value
     * @return the int
     */
    public int dp2px(float value) {
        final float scale = getContext().getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }
}
