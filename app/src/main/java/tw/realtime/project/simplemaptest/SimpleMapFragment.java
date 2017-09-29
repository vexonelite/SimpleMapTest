package tw.realtime.project.simplemaptest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by vexonelite on 2017/1/6.
 */

public class SimpleMapFragment extends BaseFragment {

    private LocationHelper mLocationHelper;
    private GoogleMapHelper mGoogleMapHelper;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.base_fragment_simple_map, container, false);
        setUpMapFragment(R.id.googleMapContainer);

        View view = rootView.findViewById(R.id.myPosition);
        if (null != view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setEnabled(false);

                    //getCameraToSpecifiedLocation( locationToLatLng(getCurrentLocation() ) );
                    //taggleMapTraffic();

                    view.setEnabled(true);
                }
            });
        }
        return rootView;
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        if (null == mLocationHelper) {
//            mLocationHelper = new LocationHelper(new MyLocationHelperCallback());
//        }
//        mLocationHelper.initialGoogleApiClient(getActivity());
//    }

    @Override
    public void onResume () {
        super.onResume();

        if ( (null != mGoogleMapHelper) && (mGoogleMapHelper.isMapReady()) && (null != mLocationHelper) ) {
            mLocationHelper.enableGoogleApiClient();
        }
    }

    @Override
    public void onPause () {
        super.onPause();

        if (null != mLocationHelper) {
            mLocationHelper.disableGoogleApiClient();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mLocationHelper) {
            mLocationHelper.disableGoogleApiClient();
            mLocationHelper = null;
        }
    }

//    @Override
//    protected void onUserLocationAvailable(Location userLocation) {
//        addMarkerByPosition(new MarkerOptionsBuilder()
//                .setMarkerPosition(locationToLatLng(userLocation))
//                .build() );
//    }


    private void setUpMapFragment (int layoutResourceId) {
        try {
            MapFragment mapFragment = MapFragment.newInstance();
            mapFragment.getMapAsync(getGoogleMapHelper());

            FragmentManager fragMangr = getFragmentManager();
            FragmentTransaction fTransaction = fragMangr.beginTransaction();
            fTransaction.replace(layoutResourceId, mapFragment, "SimpleMap");
            fTransaction.commit();
        }
        catch (Exception e) {
            LogWrapper.showLog(Log.ERROR, getLogTag(), "Exception on setUpMapFragment", e);
        }
    }

    private GoogleMapHelper getGoogleMapHelper () {
        if (null == mGoogleMapHelper) {
            MyGoogleMapCallback callback = new MyGoogleMapCallback();
            mGoogleMapHelper = new GoogleMapHelper()
                    .setMapCameraDelegate(callback)
                    .setMapReadyDelegate(callback);
        }
        return mGoogleMapHelper;
    }

    private class MyGoogleMapCallback implements
                    GoogleMapHelper.MapCameraDelegate,
                    GoogleMapHelper.MapReadyDelegate,
                    GoogleMap.OnMarkerClickListener,
                    GoogleMap.OnMyLocationButtonClickListener {


        // OnMarkerClickListener
        @Override
        public boolean onMarkerClick(Marker marker) {
            return false;
        }

        // OnMyLocationButtonClickListener
        @Override
        public boolean onMyLocationButtonClick() {
            return false;
        }

        // MapReadyDelegate
        @Override
        public void onMapReady(GoogleMap googleMap) {

            if (null == mLocationHelper) {
                mLocationHelper = new LocationHelper(new MyLocationHelperCallback());
            }
            mLocationHelper.initialGoogleApiClient(getActivity());
            mLocationHelper.enableGoogleApiClient();
            //mLocationHelper.checkLocationSettings();
        }

        // MapCameraDelegate
        @Override
        public void onCameraIdle() {

        }

        // MapCameraDelegate
        @Override
        public void onCameraMove() {

        }

        // MapCameraDelegate
        @Override
        public void onCameraMoveStarted(int reason) {

        }
    }

    private class MyLocationHelperCallback implements LocationHelper.Delegate {
        @Override
        public Activity getAttachedActivity() {
            return getActivity();
        }

        @Override
        public Fragment getFragmentInstance() {
            return SimpleMapFragment.this;
        }

        @Override
        public boolean isFragmentInstance() {
            return true;
        }

        @Override
        public void onLocationChanged(Location location) {
            LogWrapper.showLog(Log.INFO, getLogTag(), "MyLocationHelperCallback#onLocationChanged: " + location);
            if (null != mGoogleMapHelper) {
                mGoogleMapHelper.getCameraToSpecifiedLocation(GoogleMapHelper.locationToLatLng(location) );
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult()");

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case LocationHelper.REQUEST_CHECK_SETTINGS: {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult - User agreed to make required location settings changes.");
                        if (null != mLocationHelper) {
                            if (null == mLocationHelper.getCurrentLocation()) {
                                // If the initial location was never previously requested,
                                // we use FusedLocationApi.getLastLocation() to get it.
                                // If it was previously requested, we store its value in the Bundle and check for it in onCreate().
                                // We do not request it again unless the user specifically requests location updates
                                // by pressing the Start Updates button.
                                //
                                // Because we cache the value of the initial location in the Bundle,
                                // it means that if the user launches the activity,
                                // moves to a new location, and then changes the device orientation,
                                // the original location is displayed as the activity is re-created.
                                // mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                mLocationHelper.makeLocationUpdatesReq(LocationHelper.FAST_REQUEST);
                            } else {
                                mLocationHelper.cancelLocationUpdatesReq();
                                if (null != mGoogleMapHelper) {
                                    mGoogleMapHelper.getCameraToSpecifiedLocation(
                                            GoogleMapHelper.locationToLatLng(mLocationHelper.getCurrentLocation()));
                                }
                            }
                        } else {
                            LogWrapper.showLog(Log.WARN, getLogTag(), "onActivityResult - mLocationHelper is null!");
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult - User chose not to make required location settings changes.");
                        if (null != mLocationHelper) {
                            mLocationHelper.setNotToMakeRequiredLocationSettingsFlag(true);
                        }
                        break;
                }
                break;
            }
            case GoogleMapHelper.CONNECTION_FAILURE_RESOLUTION_REQUEST: {
                LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult - CONNECTION_FAILURE_RESOLUTION_REQUEST");
                switch (resultCode) {
                    case Activity.RESULT_OK :
                        // Try the request again
                        break;
                }
                break;
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @Nullable String permissions[],
                                           @Nullable int[] grantResults) {
        LogWrapper.showLog(Log.INFO, getLogTag(), "onRequestPermissionsResult()");

        switch (requestCode) {
            case SystemPermissionUtils.REQUEST_CODE_FINE_LOCATION_PERMISSION: {
                if (SystemPermissionUtils.verifyGrantResults(grantResults, null)) {
                    // permission was granted, yay!
                    // Do the contacts-related task you need to do.

                    if (null != mLocationHelper) {
                        mLocationHelper.checkLocationSettings();
                    }
                    else {
                        LogWrapper.showLog(Log.WARN, getLogTag(), "onRequestPermissionsResult() - mLocationHelper is null!");
                    }
                }
//                else {
//                    LogWrapper.showLog(Log.WARN, getLogTag(), "onRequestPermissionsResult() - no Permissions granted!");
//                    // permission denied, boo!
//                    // Disable the functionality that depends on this permission.
//                    String message = getString(R.string.lack_of_location_permission);
//                    showAlertDialog(true, "", message, null, null, new LackOfPermissionsHandler(), new LackOfPermissionsHandler());
//                }
                break;
            }
            // other 'case' lines to check for other permissions this app might request
        }
    }

    private class LackOfPermissionsHandler implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
        }
    }
}
