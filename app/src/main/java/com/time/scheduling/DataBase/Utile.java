package com.time.scheduling.DataBase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class Utile {

    public static byte[] getbyte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap a = getResizedBitmap(bitmap,300);
        a.compress(Bitmap.CompressFormat.JPEG,50,stream);
        return stream.toByteArray();
    }
    public static Bitmap getImage(byte[] data){
        return BitmapFactory.decodeByteArray(data,0,data.length);
    }


    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }





}
