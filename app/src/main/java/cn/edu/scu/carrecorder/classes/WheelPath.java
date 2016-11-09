package cn.edu.scu.carrecorder.classes;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by MrVen on 16/11/4.
 */

public class WheelPath implements Serializable{
    String name;
    ArrayList<CLatLonPoint> points;

    public WheelPath(String name, ArrayList<CLatLonPoint> points) {
        this.name = name;
        this.points = points;
    }

    public void addLatLonPoint(CLatLonPoint newPoint) {
        points.add(newPoint);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void clear() {
        points.clear();
    }

    public ArrayList<CLatLonPoint>  getPoint() {
        return points;
    }

    public int size() {
        return points.size();
    }
}
