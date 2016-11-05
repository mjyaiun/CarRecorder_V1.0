package cn.edu.scu.carrecorder.classes;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by MrVen on 16/11/4.
 */

public class WheelPath implements Serializable{
    String name;
    ArrayList<LatLonPoint> points;

    public WheelPath(String name, ArrayList<LatLonPoint> points) {
        this.name = name;
        this.points = points;
    }

    public void addLatLonPoint(LatLonPoint newPoint) {
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

    public ArrayList<LatLonPoint>  getPoint() {
        return points;
    }
}
