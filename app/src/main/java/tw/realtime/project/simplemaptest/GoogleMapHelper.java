package tw.realtime.project.simplemaptest;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by vexonelite on 2017/9/29.
 */

public class GoogleMapHelper implements OnMapReadyCallback {

    /**
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mGoogleMap;

    /** The zoom level of map's camera. */
    private float mLastZoomLevel;

    /** The center point of map's camera. */
    private LatLng mCameraTarget;

    private boolean isZoomIn;

    private MapReadyDelegate mMapReadyDelegate;
    private MapCameraDelegate mMapCameraDelegate;


    public interface MapReadyDelegate {
        /**
         * Notify that the Google Map has been ready.
         * <p>
         * you can get the camera to the default location and display marker for user Location;
         * If the location is unavailable, it is the time to obtain user's location.
         *
         * @param googleMap
         */
        void onMapReady(GoogleMap googleMap);
    }

    public interface MapCameraDelegate {
        /**
         * Called when camera movement has ended, there are no pending animations and
         * the user has stopped interacting with the map.
         * <p>
         * This is called on the main thread */
        void onCameraIdle();

        /**
         * Called repeatedly as the camera continues to move after an onCameraMoveStarted call.
         * This may be called as often as once every frame and should not perform expensive operations.
         * <p>
         * This is called on the main thread.
         */
        void onCameraMove();

        /**
         * Called when the camera starts moving after it has been idle or
         * when the reason for camera motion has changed. Do not update or animate the camera from within this method.
         * <p>
         * This is called on the main thread.
         *
         * @param reason The reason for the camera change. Possible values:
         *               REASON_GESTURE: User gestures on the map.
         *               REASON_API_ANIMATION: Default animations resulting from user interaction.
         *               REASON_DEVELOPER_ANIMATION: Developer animations.
         */
        void onCameraMoveStarted(int reason);
    }


    public GoogleMapHelper setMapReadyDelegate (MapReadyDelegate delegate) {
        mMapReadyDelegate = delegate;
        return this;
    }

    public GoogleMapHelper setMapCameraDelegate (MapCameraDelegate delegate) {
        mMapCameraDelegate = delegate;
        return this;
    }

    private String getLogTag () {
        return this.getClass().getSimpleName();
    }



    private class MapCameraEventHandler implements
                GoogleMap.OnCameraMoveStartedListener,
                GoogleMap.OnCameraMoveListener,
                GoogleMap.OnCameraIdleListener {

        // Called when camera movement has ended, there are no pending animations and the user has stopped interacting with the map.
        //
        //This is called on the main thread.
        @Override
        public void onCameraIdle() {
            LogWrapper.showLog(Log.INFO, getLogTag(), "onCameraIdle");
            CameraPosition camPosition = mGoogleMap.getCameraPosition();
            mCameraTarget = camPosition.target;
            mLastZoomLevel = camPosition.zoom;
            if (null != mMapCameraDelegate) {
                mMapCameraDelegate.onCameraIdle();
            }
        }

        // Called repeatedly as the camera continues to move after an onCameraMoveStarted call.
        // This may be called as often as once every frame and should not perform expensive operations.
        //
        // This is called on the main thread.
        @Override
        public void onCameraMove() {
            LogWrapper.showLog(Log.INFO, getLogTag(), "onCameraMove");
            if (null != mMapCameraDelegate) {
                mMapCameraDelegate.onCameraMove();
            }
        }

        // Called when the camera starts moving after it has been idle or
        // when the reason for camera motion has changed. Do not update or animate the camera from within this method.
        //
        // This is called on the main thread.
        //
        // Parameters:
        // 		reason 	The reason for the camera change. Possible values:
        // 			REASON_GESTURE: User gestures on the map.
        //			REASON_API_ANIMATION: Default animations resulting from user interaction.
        //			REASON_DEVELOPER_ANIMATION: Developer animations.
        @Override
        public void onCameraMoveStarted(int reason) {
            String text = "";
            switch (reason) {
                case REASON_GESTURE: {
                    text = text + "User gestures on the map";
                    break;
                }
                case REASON_API_ANIMATION: {
                    text = text + "Default animations resulting from user interaction";
                    break;
                }
                case REASON_DEVELOPER_ANIMATION: {
                    text = text + "Developer animations";
                    break;
                }
                default: {
                    text = text + "no reason available";
                    break;
                }
            }
            LogWrapper.showLog(Log.INFO, getLogTag(), "onCameraMoveStarted: " + text);
            if (null != mMapCameraDelegate) {
                mMapCameraDelegate.onCameraMoveStarted(reason);
            }
        }
    }

    // Implementation of OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (null == googleMap) {
            LogWrapper.showLog(Log.WARN, getLogTag(), "onMapReady: googleMap is null!!");
            return;
        }
        LogWrapper.showLog(Log.INFO, getLogTag(), "onMapReady: googleMap is available!!");
        mGoogleMap = googleMap;

        //mGoogleMap.setOnMarkerClickListener(new MapMarkerClickHandler());

        MapCameraEventHandler mapCameraEventHandler = new MapCameraEventHandler();
        mGoogleMap.setOnCameraIdleListener(mapCameraEventHandler);
        mGoogleMap.setOnCameraMoveListener(mapCameraEventHandler);
        mGoogleMap.setOnCameraMoveStartedListener(mapCameraEventHandler);

        // Enable / Disable my location button
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        // Enable / Disable zooming controls
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        // Enable / Disable Compass icon
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        // Enable / Disable Rotate gesture
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
        // Enable / Disable Tilt gesture
        mGoogleMap.getUiSettings().setTiltGesturesEnabled(false);
        // Enable / Disable zooming functionality
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

        isZoomIn = false;
        mCameraTarget = null;
        mLastZoomLevel = -1f;

        if (null != mMapReadyDelegate) {
            mMapReadyDelegate.onMapReady(googleMap);
        }
    }

    private class MapMarkerClickHandler implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            return false;
        }
    }

    private class MyLocationClickCallback implements GoogleMap.OnMyLocationButtonClickListener {
        @Override
        public boolean onMyLocationButtonClick() {
            return false;
        }
    }

    public static LatLng locationToLatLng (Location location) {
        if (null != location) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            return null;
        }
    }

    public boolean isMapReady() {
        return (null != mGoogleMap);
    }

    /**
     * Before you involve the method, you must make sure that
     * user has granted the location permission;
     */
    public void setMyLocationEnabled(boolean flag) {
        if ( !isMapReady()) {
            return;
        }
        try {
            mGoogleMap.setMyLocationEnabled(flag);
        }
        catch (Exception e) {
            LogWrapper.showLog(Log.ERROR, getLogTag(), "Exception on setMyLocationEnabled", e);
        }
    }

    public void toggleMapTraffic () {
        boolean flag = mGoogleMap.isTrafficEnabled();
        mGoogleMap.setTrafficEnabled(!flag);
    }

    public void addMarkersToGoogleMap (List<MarkerOptions> markerOptionsList) {
        if ( !isMapReady()) {
            return;
        }

        if ( (null == markerOptionsList) || (markerOptionsList.isEmpty()) ) {
            LogWrapper.showLog(Log.WARN, getLogTag(), "markerOptionsList is either null or empty!");
            return;
        }

        try {
            for (MarkerOptions markerOptions : markerOptionsList) {
                mGoogleMap.addMarker(markerOptions);
            }
        }
        catch (Exception e) {
            LogWrapper.showLog(Log.ERROR, getLogTag(), "Exception on addMarkersToGoogleMap", e);
        }
    }

    public boolean getCameraToSpecifiedLocation (LatLng targetPosition) {
        if ( !isMapReady()) {
            return false;
        }

        if (null == targetPosition) {
            return false;
        }

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(targetPosition, 15.5f);
        //mMap.animateCamera(update, null);
        mGoogleMap.moveCamera(update);

        return true;
    }

    /*
    protected void setUserMarker (LatLng position) {
        if (mUserMarker == null) {
            mUserMarker = mBuddiiMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bmap_10))
            );
        } else {
            mUserMarker.setPosition(position);
        }
    }

    protected boolean isUserMarker (Marker marker) {
        if (null != mUserMarker) {
            return marker.equals(mUserMarker);
        } else {
            return false;
        }
    }
    */
}
