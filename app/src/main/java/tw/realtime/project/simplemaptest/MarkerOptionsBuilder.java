package tw.realtime.project.simplemaptest;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by vexonelite on 2017/9/29.
 */

public class MarkerOptionsBuilder {


    private int mIconResId = Integer.MIN_VALUE;
    private LatLng mPosition;


    private String getLogTag() {
        return this.getClass().getSimpleName();
    }


    public MarkerOptionsBuilder setMarkerIconResourceId (int resourceId) {
        mIconResId = resourceId;
        return this;
    }

    public MarkerOptionsBuilder setMarkerPosition (LatLng position) {
        mPosition = position;
        return this;
    }

    public MarkerOptions build () {
        try {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(mPosition);
            if (mIconResId != Integer.MIN_VALUE) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(mIconResId));
            }
            return markerOptions;
        }
        catch (Exception e) {
            LogWrapper.showLog(Log.ERROR, getLogTag(), "Exception on build MarkerOptions", e);
            return null;
        }
    }
}
