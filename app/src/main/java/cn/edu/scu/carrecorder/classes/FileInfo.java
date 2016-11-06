package cn.edu.scu.carrecorder.classes;

/**
 * Created by MrVen on 16/11/6.
 */

public class FileInfo {
    String name;
    long duration;
    String absolutePath;

    public FileInfo(String name, String path, long duration) {
        this.name = name;
        this.absolutePath = path;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public long getDuration() {
        return duration;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
}
