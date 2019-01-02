package game.qiu.com.beautygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Description: 2048的游戏面板，加入布局文件即可开始游戏
 * Data：2016/9/11-19:48
 * Author: qiu
 */
public class mGamePintuLayout extends RelativeLayout implements View.OnClickListener{
    /**
     * 设置Item的数量n*n；默认为3
     */
    private int mColumn = 3;
    /**
     * 布局的宽度
     */
    private int mWidth;
    /**
     * 布局的padding
     */
    private int mPadding;
    /**
     * 存放所有的Item
     */
    private TextView[] mGamePintuItems;
    /**
     * Item的宽度
     */
    private int mItemWidth;
    /**
     * Item横向与纵向的边距
     */
    private int mMargin = 3;
    /**
     * 拼图的图片
     */
    private Bitmap mBitmap;
    /**
     * 存放切完以后的图片bean
     */
    private List<ImagePiece> mItemBitmaps;

    /**
     * 初始化界面的标记
     */
    private boolean once;
    private int mItemHeight;
    private int mHeight;

    private boolean scaleScreen = true;
    private CheckSuccessListener successListener;

    public mGamePintuLayout(Context context) {
        this(context, null);
    }
    public mGamePintuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造函数，用来初始化
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public mGamePintuLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //把设置的margin值转换为dp
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mMargin, getResources().getDisplayMetrics());
        // 设置Layout的内边距，四边一致，设置为四内边距中的最小值
        mPadding = min(getPaddingLeft(), getPaddingTop(), getPaddingRight(),
                getPaddingBottom());
    }

    /**
     * 用来设置设置自定义的View的宽高，
     * @param widthMeasureSpec  the width measure spec
     * @param heightMeasureSpec the height measure spec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获得游戏布局的边长
        mWidth = Math.max(getMeasuredHeight(), getMeasuredWidth());
        mHeight = Math.min(getMeasuredHeight(), getMeasuredWidth());

        /*if (mBitmap == null)
            mBitmap = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.t5);
        int mWidth = mBitmap.getWidth();
        int mHeight = mBitmap.getHeight();*/


        Log.e("TAG", "scaleScreen Width = " + mWidth + " , height = " + mHeight+"scaleScreen = "+scaleScreen);

        if (!once) {
            initBitmap();
            initItem();
        }
        once = true;
        setMeasuredDimension(mWidth, mHeight);
    }

    /**
     * 初始化bitmap
     */
    private void initBitmap() {
        if (mBitmap == null)
            mBitmap = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.tu_1);
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        Log.e("TAG", "mBitmap Width = " + width + " , height = " + height);
        mItemBitmaps = ImageSplitter.split(mBitmap, mColumn);
        //对图片进行排序
        Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {
            @Override
            public int compare(ImagePiece lhs, ImagePiece rhs) {
                //我们使用random随机比较大小

                Double i = Math.random();

                Log.i("","Math.random():"+ i);
//                return Math.random() > 0.5 ? 1 : -1;
                if(i > 0.5)
                    return 1;
                if(i<0.5)
                    return -1;

                return 0;
            }
        });
    }


    private List<TextView> mItemIndexs = new ArrayList<>();
    /**
     * 初始化每一个item
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    private void initItem() {
        // 获得Item的宽度
        int childWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1))
                / mColumn;
        int childHeight = (mHeight - mPadding * 2 - mMargin * (mColumn - 1))
                / mColumn;


        Log.e("TAG", "child Width = " + childWidth + " , height = " + childHeight);

        mItemWidth = childWidth;
        mItemHeight = childHeight;

        mGamePintuItems = new TextView[mColumn * mColumn];
        mItemIndexs.clear();
        // 放置Item
        for (int i = 0; i < mGamePintuItems.length; i++) {
            TextView item = new TextView(getContext());

            item.setOnClickListener(this);

//            v.setWidth(pieceWidth);
//            v.setHeight(pieceHeight);

            item.setBackgroundDrawable(mItemBitmaps.get(i).mDrawable);
            mGamePintuItems[i] = item;
            item.setId(i + 1);
            item.setTag(i + "_" + mItemBitmaps.get(i).index);
            item.setText(mItemBitmaps.get(i).index+"");
            item.setTextSize(45);
            item.setTextColor(Color.TRANSPARENT);
            mItemIndexs.add(item);

            LayoutParams lp = new LayoutParams(mItemWidth,
                    mItemHeight);
            // 设置横向边距,不是最后一列
            if ((i + 1) % mColumn != 0) {
                lp.rightMargin = mMargin;
            }
            // 如果不是第一列
            if (i % mColumn != 0) {
                lp.addRule(RelativeLayout.RIGHT_OF,//
                        mGamePintuItems[i - 1].getId());
            }
            // 如果不是第一行，//设置纵向边距，非最后一行
            if ((i + 1) > mColumn) {
                lp.topMargin = mMargin;
                lp.addRule(RelativeLayout.BELOW,//
                        mGamePintuItems[i - mColumn].getId());
            }
            addView(item, lp);
        }
    }
    /**
     * 用来得到最小值
     * @param params the params
     * @return the int
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    private int min(int ... params) {
        int minValue = params[0];
        for(int param : params){
            if(minValue>param){
                minValue=param;
            }
        }
        return minValue;
    }

    /**
     * 记录第一次点击的ImageView
     */
    private TextView mFirst;
    /**
     * 记录第二次点击的ImageView
     */
    private TextView mSecond;
    /**
     * 点击事件
     * @param view the view
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    @Override
    public void onClick(View view) {
        Log.d("TAG", "onClick: "+view.getTag());
        // 如果正在执行动画，则屏蔽
        if(isAniming)
            return;
        //如果两次点击的是同一个View
        if(mFirst == view){
            Drawable drawables = mFirst.getBackground();
            drawables.setColorFilter(null);
            mFirst = null;
            return;
        }
        //点击第一个View
        if(mFirst==null){
            mFirst= (TextView) view;
            Drawable drawables = mFirst.getBackground();
            drawables.setColorFilter(Color.parseColor("#55FF0000"), PorterDuff.Mode.SRC_ATOP);
//            mFirst.setColorFilter(Color.parseColor("#55FF0000"));
        }else{//点击第二个View
            mSecond= (TextView) view;
            exchangView();
        }
    }

    /**
     * 动画运行的标志位
     */
    private boolean isAniming;
    /**
     * 动画层
     */
    private RelativeLayout mAnimLayout;
    /**
     * 交换两个Item图片
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    private void exchangView(){
//        mFirst.setColorFilter(null);
        Drawable drawables = mFirst.getBackground();
        drawables.setColorFilter(null);
        Drawable drawables2 = mSecond.getBackground();
        drawables2.setColorFilter(null);
        setUpAnimLayout();
        // 添加FirstView
        TextView first = new TextView(getContext());
        first.setBackgroundDrawable(mItemBitmaps
                .get(getImageIndexByTag((String) mFirst.getTag())).mDrawable);
        LayoutParams lp = new LayoutParams(mItemWidth, mItemHeight);
        lp.leftMargin = mFirst.getLeft() - mPadding;
        lp.topMargin = mFirst.getTop() - mPadding;
        first.setLayoutParams(lp);
        mAnimLayout.addView(first);
        // 添加SecondView
        TextView second = new TextView(getContext());
        second.setBackgroundDrawable(mItemBitmaps
                .get(getImageIndexByTag((String) mSecond.getTag())).mDrawable);
        LayoutParams lp2 = new LayoutParams(mItemWidth, mItemHeight);
        lp2.leftMargin = mSecond.getLeft() - mPadding;
        lp2.topMargin = mSecond.getTop() - mPadding;
        second.setLayoutParams(lp2);
        mAnimLayout.addView(second);

        // 设置动画
        TranslateAnimation anim = new TranslateAnimation(0, mSecond.getLeft()
                - mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
        anim.setDuration(300);
        anim.setFillAfter(true);
        first.startAnimation(anim);

        TranslateAnimation animSecond = new TranslateAnimation(0,
                mFirst.getLeft() - mSecond.getLeft(), 0, mFirst.getTop()
                - mSecond.getTop());
        animSecond.setDuration(300);
        animSecond.setFillAfter(true);
        second.startAnimation(animSecond);
        // 添加动画监听
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isAniming = true;
                mFirst.setVisibility(INVISIBLE);
                mSecond.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String firstTag = (String) mFirst.getTag();
                String secondTag = (String) mSecond.getTag();

                String[] firstParams = firstTag.split("_");
                String[] secondParams = secondTag.split("_");

                mFirst.setBackgroundDrawable(mItemBitmaps.get(Integer
                        .parseInt(secondParams[0])).mDrawable);
                mSecond.setBackgroundDrawable(mItemBitmaps.get(Integer
                        .parseInt(firstParams[0])).mDrawable);

                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);


                mFirst.setText(getIndexByTag((String) mFirst.getTag()) + "");
                mSecond.setText(getIndexByTag((String) mSecond.getTag()) + "");


                mFirst.setVisibility(VISIBLE);
                mSecond.setVisibility(VISIBLE);
                mFirst = mSecond = null;
                mAnimLayout.removeAllViews();
                //checkSuccess();
                isAniming = false;
                //进行游戏胜利判断
                checkSuccess();
            }
        });
    }

    public void setVisible(boolean visible){
        if(visible){
            for (int i = 0; i < mItemIndexs.size(); i++) {
                mItemIndexs.get(i).setTextColor(Color.TRANSPARENT);
            }
        }else{
            for (int i = 0; i < mItemIndexs.size(); i++) {
                mItemIndexs.get(i).setTextColor(Color.RED);
            }
        }

    }
    /*public void setScaleScreen(boolean scaleScreen){
        this.scaleScreen = scaleScreen;
       requestLayout();

    }*/
    public void setScaleScreen(boolean scaleScreen, Bitmap bitmap){
        this.mBitmap = bitmap;
        this.scaleScreen = scaleScreen;
        this.removeAllViews();
        mAnimLayout = null;
        initBitmap();
        initItem();
    }

    /**
     * 缩放图片
     * @return处理后的图片
     */
    public  Bitmap  scaleImage(){
        if (mBitmap == null){
            return null;
        }
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        float scaleWidth = ((float) mWidth/2) / width;
        float scaleHeight = ((float) mHeight/2) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix,true);
        if (mBitmap != null & !mBitmap.isRecycled()){
            mBitmap.recycle();//销毁原图片
            mBitmap = null;
        }
        return newbm;
    }

    /**
     * 进入下一关
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    public void nextLevel() {
        if(mColumn==6) return;
        this.removeAllViews();
        mAnimLayout = null;
        mColumn++;
        initBitmap();
        initItem();
    }
    public void ret_back(int mColumn) {
        this.removeAllViews();
        mAnimLayout = null;
        this.mColumn = mColumn;
        initBitmap();
        initItem();
    }
    /**
     * 设置图片
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        this.removeAllViews();
        mAnimLayout = null;
        initBitmap();
        initItem();
    }
    /**
     * 设置图片
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    public void setBitmap(int bitmapId) {
        mBitmap = BitmapFactory.decodeResource(getResources(),bitmapId);
        this.removeAllViews();
        mAnimLayout = null;
        initBitmap();
        initItem();
    }



    /**
     * 用来判断游戏是否成功
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-12
     */
    private void checkSuccess() {
        boolean isSuccess = true;
        for (int i = 0; i < mGamePintuItems.length; i++) {
            TextView first = mGamePintuItems[i];
            Log.e("TAG", getIndexByTag((String) first.getTag()) + "");
            if (getIndexByTag((String) first.getTag()) != i) {
                isSuccess = false;
            }
        }
        if (isSuccess) {
            Toast.makeText(getContext(), "Success , Level Up !",
                    Toast.LENGTH_LONG).show();
            if(successListener!=null)
                successListener.onSuccess();
//            nextLevel();
        }
    }

    public void setCheckSuccessListener(CheckSuccessListener successListener){
        this.successListener = successListener;
    }


    /**
     * 创建动画层
     */
    private void setUpAnimLayout(){
        if(mAnimLayout==null){
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
    }

    /**
     * 获得存储在mItemBitmaps中存储图片的角标
     * @param tag the tag
     * @return the image index by tag
     */
    private int getImageIndexByTag(String tag) {
        String [] split = tag.split("_");
        return Integer.parseInt(split[0]);
    }

    /**
     * 获得图片的真正索引
     * @param tag the tag
     * @return the index by tag
     */
    private int getIndexByTag(String tag) {
        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }
}