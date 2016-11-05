package cn.edu.scu.carrecorder.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.trace.TraceLocation;

import java.io.Serializable;

/**
 * Created by MrVen on 16/11/4.
 */

public class CLatLonPoint implements Serializable, Parcelable{
    private double latitude;
    private double longitude;

    public CLatLonPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected CLatLonPoint(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<CLatLonPoint> CREATOR = new Creator<CLatLonPoint>() {
        @Override
        public CLatLonPoint createFromParcel(Parcel in) {
            return new CLatLonPoint(in);
        }

        @Override
        public CLatLonPoint[] newArray(int size) {
            return new CLatLonPoint[size];
        }
    };

    public TraceLocation toAMapTraceLocation() {
        return new TraceLocation(longitude, latitude, 0,0,0);
    }

    public LatLng toMapPoint () {
        return new LatLng(latitude, longitude);
    }

    public LatLonPoint toLatLonPoint() {
        return new LatLonPoint(latitude, longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
