package tw.realtime.project.simplemaptest;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;


public class BaseLocationFragment extends BaseMapFragment {

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    private LocationEventHandler mLocationEventHandler;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    //private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * These settings are the same as the settings for the map.
     * They will in fact give you updates
     * at the maximal rates currently possible.
     */
    private static final LocationRequest FAST_REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    //.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private static final LocationRequest MID_REQUEST = LocationRequest.create()
            .setInterval(60000)			// 60 seconds
            .setFastestInterval(30000)	// 30 seconds
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        //Log.d(getMyIdentifiedTag(), "onViewCreated");
        super.onViewCreated(rootView, savedInstanceState);

        initialGoogleApiClient();
    }

        @Override
    public void onResume () {
        super.onResume();
        enableGoogleApiClient();
    }

    @Override
    public void onPause() {
        super.onPause();
        disableGoogleApiClient();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disableGoogleApiClient();
        mGoogleApiClient = null;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(getLogTag(), "onActivityResult()");

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(getLogTag(), "User agreed to make required location settings changes.");
                        if (null == mCurrentLocation) {
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
                            makeLocationUpdatesReq(FAST_REQUEST);
                        }
                        else {
                            cancelLocationUpdatesReq();
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.i(getLogTag(), "User chose not to make required location settings changes.");
                        break;
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
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
                    checkLocationSettings();
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


    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    private void checkLocationSettings() {
        Log.i(getLogTag(), "checkLocationSettings");
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        getLocationSettingsRequest());
        result.setResultCallback(getLocationEventHandler());
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private LocationSettingsRequest getLocationSettingsRequest () {
        return new LocationSettingsRequest.Builder()
                .addLocationRequest(FAST_REQUEST)
                .build();
    }


    private LocationEventHandler getLocationEventHandler () {
        if (null == mLocationEventHandler) {
            mLocationEventHandler = new LocationEventHandler();
        }
        return mLocationEventHandler;
    }

    private class LocationEventHandler implements
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    LocationListener,
            ResultCallback<LocationSettingsResult> {

        /**
         * Runs when a GoogleApiClient object successfully connects.
         */
        @Override
        public void onConnected(Bundle bundle) {
            Log.i(getLogTag(), "onConnected");

            if (SystemPermissionUtils.hasEnoughLocationPermission(getContext()) ) {
                Log.i(getLogTag(), "onConnected() - has enough location permission!");
                checkLocationSettings();
            }
            else {
                SystemPermissionUtils.requestLocationPermissions(getActivity());
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.i(getLogTag(), "Connection suspended");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
            // onConnectionFailed.
            Log.i(getLogTag(), "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        }

        /**
         * Callback that fires when the location changes.
         */
        @Override
        public void onLocationChanged(Location location) {
            Log.i(getLogTag(), "onLocationChanged");

            if (null != location) {
                if (null == mCurrentLocation) {
                    mCurrentLocation = location;
                    cancelLocationUpdatesReq();

                    getCameraToSpecifiedLocation(locationToLatLng(location));

                    onUserLocationAvailable(location);
                }
            }
        }

        @Override
        public void onResult (LocationSettingsResult locationSettingsResult) {
            Log.i(getLogTag(), "LocationEventHandler - onResult");

            final Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i(getLogTag(), "All location settings are satisfied.");

                    if (null == mCurrentLocation) {
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
                        makeLocationUpdatesReq(FAST_REQUEST);
                    }
                    else {
                        cancelLocationUpdatesReq();
                    }
                    break;

                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i(getLogTag(), "Location settings are not satisfied. Show the user a dialog to" +
                            "upgrade location settings ");

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                    }
                    catch (Exception e) {
                        Log.e(getLogTag(), "PendingIntent unable to execute request.");
                        e.printStackTrace();
                    }
                    break;

                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i(getLogTag(), "Location settings are inadequate, and cannot be fixed here. Dialog " +
                            "not created.");
                    break;
            }
        }
    }


    private void initialGoogleApiClient () {
        Log.i(getLogTag(), "initialGoogleApiClient");
        if (null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(getLocationEventHandler())
                    .addOnConnectionFailedListener(getLocationEventHandler())
                    .build();
        }
    }

    private void enableGoogleApiClient () {
        Log.i(getLogTag(), "enableGoogleApiClient");
        if ( (null != mGoogleApiClient) && (!mGoogleApiClient.isConnected()) ) {
            mGoogleApiClient.connect();
            Log.i(getLogTag(), "enableGoogleApiClient - client.connect");
        }
    }

    private void disableGoogleApiClient () {
        Log.i(getLogTag(), "disableGoogleApiClient");
        if ( (null != mGoogleApiClient) && (mGoogleApiClient.isConnected()) ) {
            cancelLocationUpdatesReq();
            mGoogleApiClient.disconnect();
            mGoogleApiClient.unregisterConnectionCallbacks(getLocationEventHandler());
            mGoogleApiClient.unregisterConnectionFailedListener(getLocationEventHandler());
            Log.i(getLogTag(), "disableGoogleApiClient - client.disconnect");
        }
    }

    private void makeLocationUpdatesReq (LocationRequest locReq) {
        Log.i(getLogTag(), "makeLocationUpdatesReq");
        if ( (null != mGoogleApiClient) && (mGoogleApiClient.isConnected()) ) {
            try {
                PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient,
                        locReq,
                        getLocationEventHandler());
                pendingResult.setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                Log.i(getLogTag(), "makeLocationUpdatesReq - ResultCallback - status code: "
                                        + status.getStatusCode() + ", message: " + status.getStatusMessage());
                                mRequestingLocationUpdates = true;
                            }
                        });
                Log.i(getLogTag(), "makeLocationUpdatesReq - FusedLocationApi.requestLocationUpdates");
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void cancelLocationUpdatesReq () {
        Log.i(getLogTag(), "cancelLocationUpdatesReq");
        if ( (null != mGoogleApiClient) && (mGoogleApiClient.isConnected()) ) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,
                    getLocationEventHandler())
            .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    Log.i(getLogTag(), "cancelLocationUpdatesReq - ResultCallback - status code: "
                            + status.getStatusCode() + ", message: " + status.getStatusMessage());
                    mRequestingLocationUpdates = false;
                }
            });
            Log.i(getLogTag(), "cancelLocationUpdatesReq - FusedLocationApi.removeLocationUpdates");
        }
    }

    protected void onUserLocationAvailable (Location userLocation) {

    }

    protected Location getCurrentLocation () {
        return mCurrentLocation;
    }

    public static LatLng locationToLatLng (Location location) {
        if (null != location) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            return null;
        }
    }
}
