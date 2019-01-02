package game.qiu.com.beautygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Zooming view.
 */
public class ZoomView extends FrameLayout {

    public boolean isEnableTouchMove() {
        return enableTouchMove;
    }

    public void setEnableTouchMove(boolean enableTouchMove) {
        this.enableTouchMove = enableTouchMove;
    }

    private boolean enableTouchMove = false;//是否允许手势放大

    public ZoomView(Context context) {
        super(context);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Zooming view listener interface.
     *
     * @author karooolek
     */
    public interface ZoomViewListener {

        void onZoomStarted(float zoom, float zoomx, float zoomy);

        void onZooming(float zoom, float zoomx, float zoomy);

        void onZoomEnded(float zoom, float zoomx, float zoomy);
    }

    // zooming
    float zoom = 0.8f;
    float maxZoom = 2.0f;
    float smoothZoom = 0.8f;
    float mDefaultZoom = 0.8f;
    float zoomX, zoomY;
    float smoothZoomX, smoothZoomY;
    private boolean scrolling; // NOPMD by karooolek on 29.06.11 11:45

    // minimap variables
    private boolean showMinimap = false;
    private int miniMapColor = Color.BLACK;
    private int miniMapHeight = -1;
    private String miniMapCaption;
    private float miniMapCaptionSize = 10.0f;
    private int miniMapCaptionColor = Color.WHITE;

    // touching variables
    private long lastTapTime;
    private float touchStartX, touchStartY;
    private float touchLastX, touchLastY;
    private float startd;
    private boolean pinching;
    private float lastd;
    private float lastdx1, lastdy1;
    private float lastdx2, lastdy2;

    // drawing
    private final Matrix m = new Matrix();
    private final Paint p = new Paint();

    // listener
    ZoomViewListener listener;

    private Bitmap ch;

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    /**
     * 设置最大缩放比例
     *
     * @param maxZoom
     */
    public void setMaxZoom(final float maxZoom) {
        if (maxZoom < mDefaultZoom) {
            return;
        }

        this.maxZoom = maxZoom;
    }

    public void setMiniMapEnabled(final boolean showMiniMap) {
        this.showMinimap = showMiniMap;
    }

    public boolean isMiniMapEnabled() {
        return showMinimap;
    }

    public void setMiniMapHeight(final int miniMapHeight) {
        if (miniMapHeight < 0) {
            return;
        }
        this.miniMapHeight = miniMapHeight;
    }

    public int getMiniMapHeight() {
        return miniMapHeight;
    }

    public void setMiniMapColor(final int color) {
        miniMapColor = color;
    }

    public int getMiniMapColor() {
        return miniMapColor;
    }

    public String getMiniMapCaption() {
        return miniMapCaption;
    }

    public void setMiniMapCaption(final String miniMapCaption) {
        this.miniMapCaption = miniMapCaption;
    }

    public float getMiniMapCaptionSize() {
        return miniMapCaptionSize;
    }

    public void setMiniMapCaptionSize(final float size) {
        miniMapCaptionSize = size;
    }

    public int getMiniMapCaptionColor() {
        return miniMapCaptionColor;
    }

    public void setMiniMapCaptionColor(final int color) {
        miniMapCaptionColor = color;
    }

    public void zoomTo(final float zoom, final float x, final float y) {
        this.zoom = Math.min(zoom, maxZoom);
        zoomX = x;
        zoomY = y;
        smoothZoomTo(this.zoom, x, y);
    }

    /**
     * @param zoom zoom >= 1  传入的zoom >= 1;导致只能放大不能缩小
     * @param x
     * @param y
     */
    public void smoothZoomTo(final float zoom, final float x, final float y) {
        smoothZoom = clamp(mDefaultZoom, zoom, maxZoom);  //获取一个zoom的比例，>= 1
        smoothZoomX = x;
        smoothZoomY = y;
        if (listener != null) {
            listener.onZoomStarted(smoothZoom, x, y);
        }
    }

    public ZoomViewListener getListener() {
        return listener;
    }

    public void setListner(final ZoomViewListener listener) {
        this.listener = listener;
    }

    public float getZoomFocusX() {
        return zoomX * zoom;
    }

    public float getZoomFocusY() {
        return zoomY * zoom;
    }

    /**
     * 事件分发：单手指和双指
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        // single touch
        if (ev.getPointerCount() == 1) {
            processSingleTouchEvent(ev);   //目前单手指点击事件注释
        }

        if(enableTouchMove){

            // // double touch
            if (ev.getPointerCount() == 2) {
                processDoubleTouchEvent(ev);
            }

        }
        // redraw
        getRootView().invalidate();
        invalidate();

        return true;
    }

    /**
     * 单手指点击时间
     *
     * @param ev
     */
    private void processSingleTouchEvent(final MotionEvent ev) {

        final float x = ev.getX();
        final float y = ev.getY();

        final float w = miniMapHeight * (float) getWidth() / getHeight();
        final float h = miniMapHeight;
        final boolean touchingMiniMap = x >= 10.0f && x <= 10.0f + w && y >= 10.0f && y <= 10.0f
                + h;

        if (showMinimap && smoothZoom > mDefaultZoom && touchingMiniMap) {
            processSingleTouchOnMinimap(ev);

            //L.debug(JiaZoomView.class.getSimpleName(), " OnMinimap(ev);");
        } else {
            processSingleTouchOutsideMinimap(ev);
            //L.debug(JiaZoomView.class.getSimpleName(), " OutsideMinimap(ev);");
        }
    }

    /**
     * @param ev
     */
    private void processSingleTouchOnMinimap(final MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();

        final float w = miniMapHeight * (float) getWidth() / getHeight();
        final float h = miniMapHeight;
        final float zx = (x - 10.0f) / w * getWidth();
        final float zy = (y - 10.0f) / h * getHeight();
        smoothZoomTo(smoothZoom, zx, zy);
    }

    /**
     * up中包含 一指双击的处理
     *
     * @param ev
     */
    private void processSingleTouchOutsideMinimap(final MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        float lx = x - touchStartX;
        float ly = y - touchStartY;
        final float l = (float) Math.hypot(lx, ly);
        float dx = x - touchLastX;
        float dy = y - touchLastY;
        touchLastX = x;
        touchLastY = y;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = x;
                touchStartY = y;
                touchLastX = x;
                touchLastY = y;
                dx = 0;
                dy = 0;
                lx = 0;
                ly = 0;
                scrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (scrolling || (smoothZoom > mDefaultZoom && l > 30.0f)) {
                    if (!scrolling) {
                        scrolling = true;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                    }
                    smoothZoomX -= dx / zoom;
                    smoothZoomY -= dy / zoom;
                    return;
                }
                break;

            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP:

                // tap
                if (l < 30.0f) {
                    // check double tap  // 一指双击的处理
                   /* if (System.currentTimeMillis() - lastTapTime < 500) {
                        if (smoothZoom == 1.0f) {
                            smoothZoomTo(2.0f, x, y);
                        } else {
                            smoothZoomTo(1.0f, getWidth() / 2.0f, getHeight() / 2.0f);
                        }
                        lastTapTime = 0;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return;
                    }*/

                    lastTapTime = System.currentTimeMillis();

                    performClick();
                }
                break;

            default:
                break;
        }

        ev.setLocation(zoomX + (x - 0.5f * getWidth()) / zoom, zoomY + (y - 0.5f * getHeight()) /
                zoom);

        ev.getX();
        ev.getY();

        super.dispatchTouchEvent(ev);
    }

    /**
     * 双手指触发事件
     *
     * @param ev
     */
    private void processDoubleTouchEvent(final MotionEvent ev) {
        final float x1 = ev.getX(0);
        final float dx1 = x1 - lastdx1;
        lastdx1 = x1;
        final float y1 = ev.getY(0);
        final float dy1 = y1 - lastdy1;
        lastdy1 = y1;

        final float x2 = ev.getX(1);
        final float dx2 = x2 - lastdx2;
        lastdx2 = x2;
        final float y2 = ev.getY(1);
        final float dy2 = y2 - lastdy2;
        lastdy2 = y2;

        // pointers distance  开平方，求两个点的距离
        final float d = (float) Math.hypot(x2 - x1, y2 - y1);
        final float dd = d - lastd;
        lastd = d;
        Log.i("Main", "lastd:::" + lastd);
        final float ld = Math.abs(d - startd);

        //tan值  正切值
        Math.atan2(y2 - y1, x2 - x1);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startd = d;
                pinching = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (pinching || ld > 30.0f) { //两点之间的直线距离大于30px
                    pinching = true;
                    final float dxk = 0.5f * (dx1 + dx2);   //中点坐标
                    final float dyk = 0.5f * (dy1 + dy2);

                    //只能放大，无法缩小，原因在这
                    smoothZoomTo(Math.max(mDefaultZoom, zoom * d / (d - dd)), zoomX - dxk / zoom, zoomY -
                            dyk / zoom);
                }

                break;

            case MotionEvent.ACTION_UP:
            default:
                pinching = false;
                break;
        }

        ev.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(ev);
    }

    /**
     * 如果
     *
     * @param min   1.0
     * @param value
     * @param max
     * @return
     */
    private float clamp(final float min, final float value, final float max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * 得到一个数
     *
     * @param a
     * @param b b>=1
     * @param k k = 0.2
     * @return
     */
    private float lerp(final float a, final float b, final float k) {
        return a + (b - a) * k;
    }

    /**
     * java.lang.Math.signum(double d) 如果参数大于零返回1.0，如果参数小于零返回-1，如果参数为0，则返回signum函数的参数为零。特殊情况：
     * 如果参数是NaN，那么结果为NaN.  NaN是Not a Number的缩写.在Matlab中，NaN是一个预定义的常量，表示"不明确的数值结果"。
     * 如果参数是正零或负零，那么结果是一样的参数.
     *
     * @param a
     * @param b
     * @param k 0.05
     * @return true;  Math.abs(b - a) >= k   a+k
     * false; Math.abs(b - a) < k    b
     */
    private float bias(final float a, final float b, final float k) {
        return Math.abs(b - a) >= k ? a + k * Math.signum(b - a) : b;
    }

    /**
     * 绘制自己的孩子
     * <p/>
     * dispatchDraw()主要是分发给子组件进行绘制，我们通常定制组件的时候重写的是onDraw()方法。
     * 值得注意的是ViewGroup容器组件的绘制，当它没有背景时直接调用的是dispatchDraw()方法,
     * 而绕过了draw()方法，当它有背景的时候就调用draw()方法，而draw()方法里包含了dispatchDraw()方法的调用。
     * 因此要在ViewGroup上绘制东西的时候往往重写的是dispatchDraw()方法而不是onDraw()方法，
     * 或者自定制一个Drawable，重写它的draw(Canvas c)和 getIntrinsicWidth()，getIntrinsicHeight()方法，
     * 然后设为背景。
     *
     * @param canvas
     */
    @Override
    protected void dispatchDraw(final Canvas canvas) {
        // do zoom
        zoom = lerp(bias(zoom, smoothZoom, 0.05f), smoothZoom, 0.2f); //得到一个zoom值
        smoothZoomX = clamp(0.5f * getWidth() / smoothZoom, smoothZoomX, getWidth() - 0.5f *
                getWidth() / smoothZoom);
        smoothZoomY = clamp(0.5f * getHeight() / smoothZoom, smoothZoomY, getHeight() - 0.5f *
                getHeight() / smoothZoom);

        zoomX = lerp(bias(zoomX, smoothZoomX, 0.1f), smoothZoomX, 0.35f);
        zoomY = lerp(bias(zoomY, smoothZoomY, 0.1f), smoothZoomY, 0.35f);
        if (zoom != smoothZoom && listener != null) {
            listener.onZooming(zoom, zoomX, zoomY);
        }

        final boolean animating = Math.abs(zoom - smoothZoom) > 0.0000001f
                || Math.abs(zoomX - smoothZoomX) > 0.0000001f || Math.abs(zoomY - smoothZoomY) >
                0.0000001f;

        // nothing to draw
        if (getChildCount() == 0) {
            return;
        }

        // prepare matrix
        m.setTranslate(0.5f * getWidth(), 0.5f * getHeight());
        m.preScale(zoom, zoom);
        m.preTranslate(-clamp(0.5f * getWidth() / zoom, zoomX, getWidth() - 0.5f * getWidth() /
                        zoom),
                -clamp(0.5f * getHeight() / zoom, zoomY, getHeight() - 0.5f * getHeight() / zoom));

        // get view
        final View v = getChildAt(0);
        m.preTranslate(v.getLeft(), v.getTop());

        // get drawing cache if available
        if (animating && ch == null && isAnimationCacheEnabled()) {
            v.setDrawingCacheEnabled(true);
            ch = v.getDrawingCache();
        }

        // draw using cache while animating
        if (animating && isAnimationCacheEnabled() && ch != null) {
            p.setColor(0xffffffff);
            canvas.drawBitmap(ch, m, p);
        } else { // zoomed or cache unavailable
            ch = null;
            canvas.save();
            canvas.concat(m);
            v.draw(canvas);
            canvas.restore();
        }

        // draw minimap
        if (showMinimap) {
            if (miniMapHeight < 0) {
                miniMapHeight = getHeight() / 4;
            }

            canvas.translate(10.0f, 10.0f);

            p.setColor(0x80000000 | 0x00ffffff & miniMapColor);
            final float w = miniMapHeight * (float) getWidth() / getHeight();
            final float h = miniMapHeight;
            canvas.drawRect(0.0f, 0.0f, w, h, p);

            if (miniMapCaption != null && miniMapCaption.length() > 0) {
                p.setTextSize(miniMapCaptionSize);
                p.setColor(miniMapCaptionColor);
                p.setAntiAlias(true);
                canvas.drawText(miniMapCaption, 10.0f, 10.0f + miniMapCaptionSize, p);
                p.setAntiAlias(false);
            }

            p.setColor(0x80000000 | 0x00ffffff & miniMapColor);
            final float dx = w * zoomX / getWidth();
            final float dy = h * zoomY / getHeight();
            canvas.drawRect(dx - 0.5f * w / zoom, dy - 0.5f * h / zoom, dx + 0.5f * w / zoom, dy
                    + 0.5f * h / zoom, p);

            canvas.translate(-10.0f, -10.0f);
        }

        // redraw
        // if (animating) {
        getRootView().invalidate();
        invalidate();
        // }
    }
}
