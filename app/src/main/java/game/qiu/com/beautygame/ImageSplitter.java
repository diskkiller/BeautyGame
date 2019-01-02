package game.qiu.com.beautygame;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 图片切片类
 * Data：2016/9/11-19:53
 * Blog：www.qiuchengjia.cn
 * Author: qiu
 */
public class ImageSplitter {
    private static Drawable[] lumpDrawables;
    private static int[] positionwrap;

    /**
     * 将图片切成 piece * piece 块
     * @param bitmap the bitmap
     * @param piece  the piece
     * @return the list
     * @author qiu  博客：www.qiuchengjia.cn 时间：2016-09-11
     */
    public static List<ImagePiece> split(Bitmap bitmap, int piece){

        List<ImagePiece> pieces = new ArrayList<ImagePiece>(piece * piece);

        /*int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.e("TAG", "bitmap Width = " + width + " , height = " + height);
        int pieceWidth = Math.max(width, height) / piece;
        int pieceHeight = Math.min(width, height) / piece;
        Log.e("TAG", "piece Width = " + pieceWidth + " , height = " + pieceHeight);
        for (int i = 0; i < piece; i++){
            for (int j = 0; j < piece; j++){
                ImagePiece imagePiece = new ImagePiece();
                imagePiece.index = j + i * piece;

                int xValue = j * pieceWidth;
                int yValue = i * pieceHeight;

                Log.e("TAG", "Value Width = " + xValue + " , height = " + yValue);

                imagePiece.bitmap = Bitmap.createBitmap(bitmap, xValue, yValue,
                            pieceWidth, pieceHeight);


                pieces.add(imagePiece);
            }
        }*/
        int pieceWidth = (int) Math.floor(bitmap.getWidth() / piece);
        int pieceHeight = (int) Math.floor(bitmap.getHeight() / piece);
        Log.e("TAG", "piece Width = " + pieceWidth + " , height = " + pieceHeight);
        lumpDrawables = new Drawable[piece * piece];
        for (int i = 0; i < lumpDrawables.length; i++) {
            int py = i / piece;
            int px = i % piece;

            ImagePiece imagePiece = new ImagePiece();
            imagePiece.index = i;


            lumpDrawables[i] = new BitmapDrawable(Bitmap.createBitmap(bitmap, px * pieceWidth, py
                    * pieceHeight, pieceWidth, pieceHeight));
            imagePiece.mDrawable = lumpDrawables[i];
            pieces.add(imagePiece);
        }

        return pieces;
    }
}
