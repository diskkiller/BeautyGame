package game.qiu.com.beautygame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private View select_pic, select_next, btn_help, btn_back, btn_scaleScreen;
    private mGamePintuLayout mid_gameview;
    private static int RESULT_LOAD_IMAGE = 1;
    private boolean isVisible = true;
    private boolean scaleScreen = true;
    private ZoomView mZoomView;
    private BitmapFactory.Options bitmapOptions;
    private Uri imgUri;
    private DisplayMetrics screenMetric;
    private Bitmap imgBitmapForFit;
    private Bitmap imgBitmapForScreen;
    private RecyclerView recyclerView;
    private GameAdapter1 adapter;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadGameData();

        init();


    }

    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        /**
         * 创建一个linearlayoutmaneger对象，并将他设置到recyclerview当中。layoutmanager用于指定
         * recyclerview的布局方式，这里是线性布局的意思。可以实现和listview类似的效果。
         *
         */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new SpaceItemDecoration(20));
        adapter = new GameAdapter1();
        recyclerView.setAdapter(adapter);
        adapter.setGameAdapterData(gameDataList);
        adapter.SetOnItemClickListener(new GameAdapter1.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, GameData gameData) {
                mid_gameview.setBitmap(gameData.getImageUrl());
                animationView.cancelAnimation();
                animationView.setVisibility(View.GONE);
            }
        });


        mZoomView = (ZoomView) findViewById(R.id.zoomview);
        mZoomView.setMaxZoom(5.0f);

        mid_gameview = (mGamePintuLayout) findViewById(R.id.id_gameview);

        select_pic = findViewById(R.id.select_pic);
        select_pic.setOnClickListener(new View.OnClickListener() {

                                          @Override
                                          public void onClick(View v) {
                                              Intent i = new Intent(Intent.ACTION_PICK, android
                                                      .provider.MediaStore.Images.Media
                                                      .EXTERNAL_CONTENT_URI);

                                              startActivityForResult(i, RESULT_LOAD_IMAGE);
                                          }
                                      }
        );

        select_next = findViewById(R.id.select_next);
        select_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.cancelAnimation();
                animationView.setVisibility(View.GONE);
                mid_gameview.nextLevel();
            }
        });
        btn_help = findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVisible = !isVisible;
                mid_gameview.setVisible(isVisible);
            }
        });
        btn_scaleScreen = findViewById(R.id.btn_scaleScreen);
        btn_scaleScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scaleScreen)
                    mZoomView.zoomTo(1.0f, 0, 0);
                else
                    mZoomView.zoomTo(0.8f, 0, 0);
                scaleScreen = !scaleScreen;

            }
        });
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mid_gameview.ret_back(3);
            }
        });


        animationView = (LottieAnimationView) findViewById(R.id.lottieView);


        mid_gameview.setCheckSuccessListener(new CheckSuccessListener() {
            @Override
            public void onSuccess() {
                animationView.setVisibility(View.VISIBLE);
                animationView.playAnimation(); //此方法将加载文件并在后台解析动画，并在完成后异步开始呈现
            }

            @Override
            public void onFailure() {

            }
        });

    }

    private List gameDataList = new ArrayList<GameData>();

    public void loadGameData() {

        if (gameDataList != null)
            gameDataList.clear();

        GameData gamdataType1 = new GameData(R.mipmap.tu_1);
        GameData gamdataType2 = new GameData(R.mipmap.tu_2);
        GameData gamdataType3 = new GameData(R.mipmap.tu_3);
        GameData gamdataType4 = new GameData(R.mipmap.tu_4);
        GameData gamdataType5 = new GameData(R.mipmap.tu_5);
        GameData gamdataType6 = new GameData(R.mipmap.tu_6);
        GameData gamdataType7 = new GameData(R.mipmap.tu_7);
        GameData gamdataType8 = new GameData(R.mipmap.tu_8);
        gameDataList.add(gamdataType1);
        gameDataList.add(gamdataType2);
        gameDataList.add(gamdataType3);
        gameDataList.add(gamdataType4);
        gameDataList.add(gamdataType5);
        gameDataList.add(gamdataType6);
        gameDataList.add(gamdataType7);
        gameDataList.add(gamdataType8);

    }


    public DisplayMetrics getScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap b = BitmapFactory.decodeFile(picturePath);
            mid_gameview.setBitmap(b);
            /*if(data != null){
                Uri picUri = data.getData();
                if(picUri != null){
                    this.imgUri = picUri;
                    initSourceBitmap();
                }
            }*/

        }
    }


    /*
     * 初始化需要拼图的图片的bitmap
	 */
    private void initSourceBitmap() {
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(imgUri);
            bitmapOptions.inJustDecodeBounds = true;
            bitmapOptions.inSampleSize = 1;
            // 获取图片的大小
            BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
            int picWidth = bitmapOptions.outWidth;
            int picHeight = bitmapOptions.outHeight;

            boolean rotate = false;
            /*
			 * 如果是横向的图就设置90度旋转
			 */
            if (picWidth > picHeight) {
                int tmpwidth = picWidth;
                picWidth = picHeight;
                picHeight = tmpwidth;
                rotate = true;
            }


            // 将图片进行缩放
            int screenWidth = screenMetric.widthPixels / 10;
            int screenHeight = screenMetric.heightPixels / 10;
            float scaleW = (float) picWidth / screenWidth;
            float scaleH = (float) picHeight / screenHeight;
            float scale = Math.min(scaleW, scaleH);
            if (scale % 10 != 0) {
                scale += 10;
            }
            int s = (int) (scale / 10);
            if (s < 1) {
                s = 1;
            }
            //	Log.e("testp", picWidth+"<<<<<<<<<<<<"+picHeight+">>>>>>"+s);
            bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = false;
            bitmapOptions.inSampleSize = s;
            if (imgBitmapForFit != null && !imgBitmapForFit.isRecycled()) {
                imgBitmapForFit.recycle();
            }
            if (imgBitmapForScreen != null && !imgBitmapForScreen.isRecycled()) {
                imgBitmapForScreen.recycle();
            }

            inputStream.close();
            inputStream = getContentResolver().openInputStream(imgUri);

            int screenImgWidth;
            int screenImgHeight;
            if (rotate) {
                screenImgWidth = screenMetric.heightPixels;
                screenImgHeight = screenMetric.widthPixels;
            } else {
                screenImgWidth = screenMetric.widthPixels;
                screenImgHeight = screenMetric.heightPixels;
            }

            imgBitmapForFit = BitmapFactory.decodeStream(inputStream, new Rect(0, 0,
                            screenImgWidth, screenImgHeight),
                    bitmapOptions);
            inputStream.close();
            if (rotate) {
                Matrix rotateMatrix = new Matrix();
                rotateMatrix.postRotate(90);

                imgBitmapForFit = Bitmap.createBitmap(imgBitmapForFit, 0, 0, imgBitmapForFit
                        .getWidth(), imgBitmapForFit.getHeight(), rotateMatrix, true);
            }
            imgBitmapForScreen = Bitmap.createScaledBitmap(imgBitmapForFit,
                    screenMetric.widthPixels, screenMetric.heightPixels,
                    true);

            int padding = screenMetric.heightPixels - imgBitmapForFit.getHeight();
            //	Log.e("testp", imgBitmapForFit.getWidth()+">>>>"+imgBitmapForFit.getHeight());

            mid_gameview.setBitmap(imgBitmapForScreen);

        } catch (Exception e) {
            Log.e("puzzle.img.getinpustream",
                    "get seelct img inpustream error!", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("render img", "close img inputstream error", e);
                }
            }
        }

    }

}
