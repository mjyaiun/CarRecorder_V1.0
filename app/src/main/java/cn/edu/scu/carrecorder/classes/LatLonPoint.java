package cn.edu.scu.carrecorder.classes;

import com.amap.api.maps2d.model.LatLng;

import java.io.Serializable;

/**
 * Created by MrVen on 16/11/4.
 */

public class LatLonPoint implements Serializable{
    private double latitude;
    private double longitude;

    public LatLonPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng toAMapLatLonPoint() {
        return new LatLng(latitude, longitude);
    }

}
