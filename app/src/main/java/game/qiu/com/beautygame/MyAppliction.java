package game.qiu.com.beautygame;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

// 全局应用
public class MyAppliction extends Application {
    private static Context mContext;
    private static Thread mMainThread;
    private static long mMainTreadId;
    private static Looper mMainLooper;
    private static Handler mHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        // 上下文
        mContext = getApplicationContext();

        // 主线程
        mMainThread = Thread.currentThread();

        // 主线程Id
        mMainTreadId = android.os.Process.myTid();

        // tid thread
        // uid user
        // pid process
        // 主线程Looper对象
        mMainLooper = getMainLooper();

        // 定义一个handler

        mHandler = new Handler();

    }




    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }


    public static Handler getHandler() {
        return mHandler;
    }

    public static Context getContext() {
        return mContext;
    }

    public static Thread getMainThread() {
        return mMainThread;
    }

    public static long getMainTreadId() {
        return mMainTreadId;
    }

    public static Looper getMainThreadLooper() {
        return mMainLooper;
    }





    public static MyAppliction INSTANCE;

    /**
     * 提供系统调用的构造函数，
     */
    public MyAppliction() {
        INSTANCE = this;
    }

    /**
     * 获得application实例
     *
     * @return
     */
    public static MyAppliction getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MyAppliction();
        }
        return INSTANCE;
    }



    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
