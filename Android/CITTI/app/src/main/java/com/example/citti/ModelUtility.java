package com.example.citti;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ModelUtility {

    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    private static final int IMG_SIZE_X = 128;
    private static final int IMG_SIZE_Y = 128;

    public static MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static int getNumBytesPerChannel() {
        // a 32bit float value requires 4 bytes
        return 4;
    }

    public static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * IMG_SIZE_X
                                * IMG_SIZE_Y
                                * DIM_PIXEL_SIZE
                                * getNumBytesPerChannel());
        imgData.order(ByteOrder.nativeOrder());
        imgData.rewind();

        int[] intValues = new int[IMG_SIZE_X * IMG_SIZE_Y];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        long startTime = SystemClock.uptimeMillis();

        // Convert the image to floating point.
        int pixel = 0;

        for (int i = 0; i < IMG_SIZE_X; ++i) {
            for (int j = 0; j < IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];

                imgData.putFloat(((val>> 16) & 0xFF) / 255.f);
                imgData.putFloat(((val>> 8) & 0xFF) / 255.f);
                imgData.putFloat((val & 0xFF) / 255.f);
            }
        }

        long endTime = SystemClock.uptimeMillis();
        Log.d("BYTEBUFFER", "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
        return imgData;
    }


}
