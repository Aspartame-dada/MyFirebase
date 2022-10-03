package com.example.myfirebase.MyUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.ImageView;

import com.example.myfirebase.R;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private final int D_RES_ID = R.drawable.defaultimg;
    private ExecutorService executorService;

    public ImageLoader(Context context){
        executorService = Executors.newFixedThreadPool(20);
    }

    public void displayImage(Bitmap bitmap ,ImageView imageView){
        if(bitmap == null){
            return;
        }
        executorService.submit(new ImageRunnable(bitmap,imageView));
       imageView.setImageResource(D_RES_ID);
    }


    class ImageRunnable implements Runnable {
        private ImageView imageView;
        private Bitmap bitmap;


        public ImageRunnable(Bitmap bitmap,ImageView imageView){
            this.imageView = imageView;
            this.bitmap=bitmap;
        }

        @Override
        public void run(){
            Bitmap bmp = null;
            bmp = getCenterBitmap(bitmap);
            ImageViewRunnable imageViewRunnable = new ImageViewRunnable(bmp, imageView);
            Activity activity = (Activity)imageView.getContext();
            activity.runOnUiThread(imageViewRunnable);
        }

    }

    class ImageViewRunnable implements Runnable {
        private Bitmap bitmap ;
        private ImageView imageView;
        private HashMap<ImageView, String> aNull = null;

        public ImageViewRunnable(Bitmap bitmap, ImageView imageView){
            this.bitmap = bitmap;
            this.imageView = imageView;
        }

        public void run(){
            if(bitmap != null){
                imageView.setImageBitmap(bitmap);

            }else{
                imageView.setImageResource(R.drawable.defaultimg);


            }
        }}




    private static Bitmap getCenterBitmap(Bitmap bitmap){
        Log.i("TAG", "getCenterBitmap: -------------------------------"+1);
        if(bitmap == null){
            return null;
        }
        Bitmap thumb_bitmap = null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        if(width >= height){
            thumb_bitmap = Bitmap.createBitmap(bitmap, width/2 - height/2, 0, height, height, matrix, true);
        }else{
            thumb_bitmap = Bitmap.createBitmap(bitmap, 0, height/2 - width/2, width, width, matrix, true);
        }
        return thumb_bitmap;
    }
}
