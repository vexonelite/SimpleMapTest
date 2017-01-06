package tw.realtime.project.simplemaptest;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public abstract class BaseMapFragment extends BaseFragment {

    /**
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mGoogleMap;

    /** The zoom level of map's camera. */
    private float mLastZoomLevel;

    /** The center point of map's camera. */
    private LatLng mCameraTarget;

    private boolean isZoomIn;



    private class MapCameraEventHandler implements
            GoogleMap.OnCameraMoveStartedListener,
            GoogleMap.OnCameraMoveListener,
            GoogleMap.OnCameraIdleListener {

        // Called when camera movement has ended, there are no pending animations and the user has stopped interacting with the map.
        //
        //This is called on the main thread.
        @Override
        public void onCameraIdle() {
            Log.i(getLogTag(), "onCameraIdle");
            CameraPosition camPosition = mGoogleMap.getCameraPosition();
            mCameraTarget = camPosition.target;
            mLastZoomLevel = camPosition.zoom;
        }

        // Called repeatedly as the camera continues to move after an onCameraMoveStarted call.
        // This may be called as often as once every frame and should not perform expensive operations.
        //
        // This is called on the main thread.
        @Override
        public void onCameraMove() {
            Log.i(getLogTag(), "onCameraMove");
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
            Log.i(getLogTag(), "onCameraMoveStarted: " + text);
        }
    }

    private class MapReadyHandler implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (null == googleMap) {
                Log.e(getLogTag(), "onMapReady: googleMap is null!!");
                return;
            }
            mGoogleMap = googleMap;

            mGoogleMap.setOnMarkerClickListener(new MapMarkerClickHandler());

            mGoogleMap.setOnCameraIdleListener(new MapCameraEventHandler());
            mGoogleMap.setOnCameraMoveListener(new MapCameraEventHandler());
            mGoogleMap.setOnCameraMoveStartedListener(new MapCameraEventHandler());

            // Enable / Disable my location button
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            // Enable / Disable zooming controls
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            // Enable / Disable Compass icon
            mGoogleMap.getUiSettings().setCompassEnabled(true);
            // Enable / Disable Rotate gesture
            mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
            // Enable / Disable zooming functionality
            mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

            isZoomIn = false;
            mCameraTarget = null;
            mLastZoomLevel = -1f;

            onMapAvailableHandler();
            //getCameraToDefaultLocation();
            enableMyLocation();
        }
    }

    private class MapMarkerClickHandler implements OnMarkerClickListener {
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


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @Nullable String permissions[],
                                           @Nullable int[] grantResults) {
        Log.i(getLogTag(), "onRequestPermissionsResult()");

        switch (requestCode) {
            case SystemPermissionUtils.REQUEST_CODE_LOCATION_PERMISSIONS: {
                boolean result = true;
                // If request is cancelled, the result arrays are empty.
                if ( (null != grantResults) && (grantResults.length > 0)) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            result = false;
                            break;
                        }
                    }
                }
                if (result) {
                    // permission was granted, yay!
                    // Do the contacts-related task you need to do.
                    enableMyLocation();
                }
                else {
                    // permission denied, boo!
                    // Disable the functionality that depends on this permission.
                    String message = getString(R.string.lack_of_location_permission);
                    showConfirmDialog(message, new LackOfPermissionsHandler(), new LackOfPermissionsHandler());
                }
                break;
            }
            // other 'case' lines to check for other permissions this app might request
        }
    }

    private class LackOfPermissionsHandler implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            backToPrevious();
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        break;
                }
        }
    }

    protected void setUpMapFragment (int layoutResourceId) {
        try {
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(new MapReadyHandler());

            FragmentManager fragMangr = getChildFragmentManager();
            FragmentTransaction fTransaction = fragMangr.beginTransaction();
            fTransaction.replace(layoutResourceId, mapFragment, "SimpleMap");
            fTransaction.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The callback that gets called when the Google Map is available!
     */
    protected void onMapAvailableHandler () {

    }


    private boolean checkIfMapIsReady() {
        return (null != mGoogleMap);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            SystemPermissionUtils.requestFineLocationPermission(getActivity());
        } else if (mGoogleMap != null) {
            // Access to the location has been granted to the app.
            // Showing / hiding your current location
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void getCameraToDefaultLocation () {
        if ( !checkIfMapIsReady()) {
            return;
        }
        final LatLng taipeiMetroStation = new LatLng(25.047245d, 121.517789d);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(taipeiMetroStation, 15.5f);
        mGoogleMap.moveCamera(update);
    }

    protected boolean getCameraToSpecifiedLocation (LatLng targetPosition) {
        if ( !checkIfMapIsReady()) {
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

    public static class MarkerOptionsBuilder {
        private int mIconResId = Integer.MIN_VALUE;
        private LatLng mPosition;

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
                e.printStackTrace();
                return null;
            }
        }
    }

    protected void addMarkerByPosition (MarkerOptions markerOptions) {
        if (null != markerOptions) {
            mGoogleMap.addMarker(markerOptions);
        }
    }

    protected void taggleMapTraffic () {
        boolean flag = mGoogleMap.isTrafficEnabled();
        mGoogleMap.setTrafficEnabled(!flag);
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













