package com.example.xiangpi.dynamicblurdemo.util;

import android.graphics.Bitmap;
import android.os.Environment;

import com.example.xiangpi.dynamicblurdemo.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by xiangpi on 16/8/20.
 */
public class ImageUtils {

    public static final String mSaveDirPath = Environment.getExternalStorageDirectory() + File.separator + "DynamicBlur" + File.separator;

    public static void saveBlurredImage(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        File saveDir = new File(mSaveDirPath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        } else if (saveDir.isDirectory()) {
            File saveFile = new File(mSaveDirPath + "blurred_img_" + System.currentTimeMillis() + ".jpg");
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(saveFile));

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
