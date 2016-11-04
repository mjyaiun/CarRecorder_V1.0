package cn.edu.scu.carrecorder.util;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by MrVen on 16/8/20.
 */
public class VideoSize {
    private static int width;
    private static int height;

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public static void getOptimalSize(int w, int h) {
        Camera camera = Camera.open();
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Size size = getOptimalPreviewSize(sizes, w, h);
        width = size.width;
        height = size.height;
        camera.release();
    }

    public static void optimizeCameraDimens(Camera mCamera, Context mContext) {
        final int width = Screens.getScreenWidth(mContext);
        final int height = Screens.getScreenHeight(mContext);

        final List<Camera.Size> mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();


        Camera.Size mPreviewSize;
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            float ratio;
            if(mPreviewSize.height >= mPreviewSize.width)
                ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
            else
                ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
        }
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (w > h)
            targetRatio = (double) w / h;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {

            double ratio = (double) size.width / size.height;
            if(size.height >= size.width)
                ratio = (float) size.height/size.width;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

}
