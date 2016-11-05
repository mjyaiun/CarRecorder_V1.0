package cn.edu.scu.carrecorder.util;

import android.media.CamcorderProfile;
import android.support.v4.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

import cn.edu.scu.carrecorder.classes.Contactor;
import cn.edu.scu.carrecorder.classes.WheelPath;

/**
 * Created by MrVen on 16/8/21.
 */
public class PublicDate {
    public static DrawerLayout drawer;
    public static int defaultDuration = 10*60*1000;   //默认视频录制的最长时长10min
    public static long defaultFileSize = 256*1024*1024;        //默认视频录制的最大文件长度256MB
    public static int defaultQuality = CamcorderProfile.QUALITY_HIGH;   //默认视频质量
    public static int defaultInterval = 5*60*1000;
    public static List<Contactor> contactors;
    public static List<WheelPath> paths = new ArrayList<>();

}
