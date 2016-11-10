package cn.edu.scu.carrecorder.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by MrVen on 16/11/10.
 */

public class Byte2ImageUtil {

    public static void data2file(byte[] w, String fileName) {// 将二进制数据转换为文件的函数

        FileOutputStream out = null;
        Bitmap bitmap = BitmapFactory.decodeByteArray(w, 0, w.length);

        try {

            out = new FileOutputStream(fileName);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
