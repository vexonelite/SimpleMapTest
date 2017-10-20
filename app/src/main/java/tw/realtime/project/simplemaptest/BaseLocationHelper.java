package tw.realtime.project.simplemaptest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;


public abstract class BaseLocationHelper {

    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     *
     * These settings are the same as the settings for the map.
     * They will in fact give you updates
     * at the maximal rates currently possible.
     */
    public static final LocationRequest FAST_REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    public static final LocationRequest MID_REQUEST = LocationRequest.create()
            .setInterval(60000)			// 60 seconds
            .setFastestInterval(30000)	// 30 seconds
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    public static final LocationRequest SAMPLE_REQUEST = LocationRequest.create()
            .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            //.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    private boolean hasAskedPermission = false;
    private boolean hasCheckedLocationSettings = false;
    private boolean isBeingLocationUpdate = false;

    private LocationSettingsCallback mLocationSettingsCallback;
    private LocationCallback mLocationCallback;
    private StartLocationUpdatesCallback mStartLocationUpdatesCallback;
    private StopLocationUpdatesCallback mStopLocationUpdatesCallback;

    private Delegate mDelegate;


    public interface Delegate {
        void onLocationResult(Location location);
        void onStartLocationUpdates();
    }

    public BaseLocationHelper(Context context, Delegate delegate) {

        resetSetting();

        mDelegate = delegate;

        if (null == mFusedLocationClient) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }
        if (null == mSettingsClient) {
            mSettingsClient = LocationServices.getSettingsClient(context);
        }
        if (null == mLocationSettingsRequest) {
            mLocationSettingsRequest = getLocationSettingsRequest(FAST_REQUEST);
        }
        if (null == mLocationCallback) {
            mLocationCallback = new MyLocationCallback();
        }
        if (null == mLocationSettingsCallback) {
            mLocationSettingsCallback = new LocationSettingsCallback();
        }
        if (null == mStartLocationUpdatesCallback) {
            mStartLocationUpdatesCallback = new StartLocationUpdatesCallback();
        }
        if (null == mStopLocationUpdatesCallback) {
            mStopLocationUpdatesCallback = new StopLocationUpdatesCallback();
        }
    }

    private String getLogTag () {
        return this.getClass().getSimpleName();
    }

    protected abstract Activity getAttachedActivity();

    protected abstract Fragment getFragmentInstance();

    /** True for Fragment; otherwise Activity */
    protected abstract boolean isFragmentInstance();

    public void resetSetting () {
        hasAskedPermission = false;
        hasCheckedLocationSettings = false;
        mCurrentLocation = null;
        mLastUpdateTime = "";
    }

    public void lockHasAskedPermissionFlag () {
        hasAskedPermission = true;
    }

    public void lockHasCheckedLocationSettingsFlag () {
        hasCheckedLocationSettings = true;
    }

    public boolean hasCheckedLocationSettings () {
        return hasCheckedLocationSettings;
    }

    /**
     * If permission has been granted, involve checkLocationSettings() method instantly
     */
    public void permissionCheckAndAskIfNeeded () {

        if (SystemPermissionUtils.hasFineLocationPermissionBeenGranted(getAttachedActivity()) ) {
            LogWrapper.showLog(Log.INFO, getLogTag(), "permissionCheckAndAskIfNeeded() - hasFineLocationPermissionBeenGranted!");
            checkLocationSettings();
        }
        else {
            LogWrapper.showLog(Log.WARN, getLogTag(), "permissionCheckAndAskIfNeeded() - no FineLocationPermissionBeenGranted!");
            if (!hasAskedPermission) {
                if (isFragmentInstance()) {
                    SystemPermissionUtils.requestFineLocationPermission(getFragmentInstance());
                }
                else {
                    SystemPermissionUtils.requestFineLocationPermission(getAttachedActivity());
                }
            }
        }
    }

    public void checkLocationSettings() {
        if (hasCheckedLocationSettings) {
            return;
        }
        LogWrapper.showLog(Log.INFO, getLogTag(), "checkLocationSettings");

        // Begin by checking if the device has the necessary location settings.
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getAttachedActivity(), mLocationSettingsCallback)
                .addOnFailureListener(getAttachedActivity(), mLocationSettingsCallback);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private LocationSettingsRequest getLocationSettingsRequest(LocationRequest locationRequest) {
        return new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .build();
    }

    private class LocationSettingsCallback
            implements  OnSuccessListener<LocationSettingsResponse>, OnFailureListener {

        // OnSuccessListener<LocationSettingsResponse>
        @Override
        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            LogWrapper.showLog(Log.INFO, getLogTag(), "LocationSettingsCallback - OnSuccessListener - "
                    + "All location settings are satisfied.");

            if (null == mCurrentLocation) {
                startLocationUpdates(FAST_REQUEST);
            }
            else {
                stopLocationUpdates();
            }
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            //LogWrapper.showLog(Log.ERROR, getLogTag(), "LocationSettingsCallback - onFailure", e);
            LogWrapper.showLog(Log.ERROR, getLogTag(), "LocationSettingsCallback - onFailure");

            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                    LogWrapper.showLog(Log.INFO, getLogTag(), "LocationSettingsCallback - onFailure[RESOLUTION_REQUIRED]: "
                            + "Location settings are not satisfied. Attempting to upgrade location settings");
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(getAttachedActivity(), REQUEST_CHECK_SETTINGS);
                    }
                    catch (IntentSender.SendIntentException sie) {
                        LogWrapper.showLog(Log.ERROR, getLogTag(), "LocationSettingsCallback[RESOLUTION_REQUIRED]: PendingIntent unable to execute request.", sie);
                    }
                    break;
                }

                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                    LogWrapper.showLog(Log.INFO, getLogTag(), "LocationSettingsCallback - onFailure[SETTINGS_CHANGE_UNAVAILABLE]: "
                            + "Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
                }
            }
        }
    }

    private class MyLocationCallback extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            LogWrapper.showLog(Log.INFO, getLogTag(), "MyLocationCallback#onLocationResult");
            mCurrentLocation = locationResult.getLastLocation();
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

            LogWrapper.showLog(Log.INFO, getLogTag(), "onLocationChanged");

            Location location = locationResult.getLastLocation();
            if (null != location) {
                mCurrentLocation = location;
                if (null != mDelegate) {
                    mDelegate.onLocationResult(location);
                }
                stopLocationUpdates();
            }
            else {
                LogWrapper.showLog(Log.INFO, getLogTag(), "MyLocationCallback#onLocationResult - location is null!");
            }
        }

//        public void onLocationAvailability(LocationAvailability locationAvailability) {
//
//        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    public void startLocationUpdates(LocationRequest locationRequest) {
        if (isBeingLocationUpdate) {
            LogWrapper.showLog(Log.WARN, getLogTag(), "startLocationUpdates - no need");
            return;
        }

        isBeingLocationUpdate = true;
        LogWrapper.showLog(Log.WARN, getLogTag(), "startLocationUpdates");

        //noinspection MissingPermission
        mFusedLocationClient
                .requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
                .addOnSuccessListener(getAttachedActivity(), mStartLocationUpdatesCallback)
                .addOnFailureListener(getAttachedActivity(), mStartLocationUpdatesCallback)
                .addOnCompleteListener(getAttachedActivity(), mStartLocationUpdatesCallback);
    }

    private class StartLocationUpdatesCallback implements
                OnSuccessListener<Void>, OnFailureListener, OnCompleteListener<Void> {

        @Override
        public void onSuccess(Void aVoid) {
            LogWrapper.showLog(Log.INFO, getLogTag(), "StartLocationUpdatesCallback#onSuccess");
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            LogWrapper.showLog(Log.ERROR, getLogTag(), "StartLocationUpdatesCallback#onFailure", e);
        }

        @Override
        public void onComplete(@NonNull Task<Void> task) {
            LogWrapper.showLog(Log.INFO, getLogTag(), "StartLocationUpdatesCallback#onComplete");
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    public void stopLocationUpdates() {
        if (!isBeingLocationUpdate) {
            LogWrapper.showLog(Log.WARN, getLogTag(), "stopLocationUpdates - no need");
            return;
        }
        LogWrapper.showLog(Log.INFO, getLogTag(), "stopLocationUpdates");

        // It is a good practice to remove location requests when the activity
        // is in a paused or stopped state. Doing so helps battery performance and
        // is especially recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnSuccessListener(getAttachedActivity(), mStopLocationUpdatesCallback)
                .addOnFailureListener(getAttachedActivity(), mStopLocationUpdatesCallback)
                .addOnCompleteListener(getAttachedActivity(), mStopLocationUpdatesCallback);
    }

    private class StopLocationUpdatesCallback implements
                OnSuccessListener<Void>, OnFailureListener, OnCompleteListener<Void> {

        @Override
        public void onSuccess(Void aVoid) {
            LogWrapper.showLog(Log.INFO, getLogTag(), "StopLocationUpdatesCallback#onSuccess");
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            LogWrapper.showLog(Log.ERROR, getLogTag(), "StopLocationUpdatesCallback#onFailure", e);
        }

        @Override
        public void onComplete(@NonNull Task<Void> task) {
            LogWrapper.showLog(Log.INFO, getLogTag(), "StopLocationUpdatesCallback#onComplete");
            isBeingLocationUpdate = false;
        }
    }

    public Location getCurrentLocation () {
        return mCurrentLocation;
    }

    public static LatLng locationToLatLng (Location location) {
        if (null != location) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            return null;
        }
    }

    public static Location newInstance (double latitude, double longitude) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        //location.distanceTo()
        //Location.distanceBetween();
        return location;
    }

    public static String getDistanceBetweenInKm (@NonNull Location location1,
                                                 @NonNull Location location2,
                                                 int numOfFractionDigits) {
        int defaultFractionDigits = 2;
        if (numOfFractionDigits > 0) {
            defaultFractionDigits = numOfFractionDigits;
        }
        float distanceInMeter = location1.distanceTo(location2);
        float distanceInKm = distanceInMeter / 1000f;
        NumberFormat nf = NumberFormat.getInstance(); // get instance
        nf.setMaximumFractionDigits(defaultFractionDigits); // set decimal places
        nf.setMinimumFractionDigits(defaultFractionDigits);
        return nf.format(distanceInKm);
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult()");
//
//        switch (requestCode) {
//            // Check for the integer request code originally supplied to startResolutionForResult().
//            case LocationHelper.REQUEST_CHECK_SETTINGS: {
//                switch (resultCode) {
//                    case Activity.RESULT_OK:
//                        LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult - User agreed to make required location settings changes.");
//                        if (null != mLocationHelper) {
//                            if (null == mLocationHelper.getCurrentLocation()) {
//                                // If the initial location was never previously requested,
//                                // we use FusedLocationApi.getLastLocation() to get it.
//                                // If it was previously requested, we store its value in the Bundle and check for it in onCreate().
//                                // We do not request it again unless the user specifically requests location updates
//                                // by pressing the Start Updates button.
//                                //
//                                // Because we cache the value of the initial location in the Bundle,
//                                // it means that if the user launches the activity,
//                                // moves to a new location, and then changes the device orientation,
//                                // the original location is displayed as the activity is re-created.
//                                // mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                                mLocationHelper.makeLocationUpdatesReq(LocationHelper.FAST_REQUEST);
//                            } else {
//                                mLocationHelper.cancelLocationUpdatesReq();
//                            }
//                        } else {
//                            LogWrapper.showLog(Log.WARN, getLogTag(), "onActivityResult - mLocationHelper is null!");
//                        }
//                        break;
//
//                    case Activity.RESULT_CANCELED:
//                        LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult - User chose not to make required location settings changes.");
//
//                        if (null != mLocationHelper) {
//                            mLocationHelper.lockNotToMakeRequiredLocationSettingsFlag();
//                        }
//                        issueApiRequestIfNeeded();
//                        break;
//                }
//                break;
//            }
//            case GoogleMapHelper.CONNECTION_FAILURE_RESOLUTION_REQUEST: {
//                LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult - CONNECTION_FAILURE_RESOLUTION_REQUEST");
//                switch (resultCode) {
//                    case Activity.RESULT_OK :
//                        // Try the request again
//                        break;
//                }
//                break;
//            }
//            default: {
//                super.onActivityResult(requestCode, resultCode, data);
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @Nullable String permissions[],
//                                           @Nullable int[] grantResults) {
//        LogWrapper.showLog(Log.INFO, getLogTag(), "onRequestPermissionsResult()");
//
//        switch (requestCode) {
//            case SystemPermissionUtils.REQUEST_CODE_FINE_LOCATION_PERMISSION: {
//
//                if (SystemPermissionUtils.verifyGrantResults(grantResults, null)) {
//                    // permission was granted, yay!
//                    // Do the contacts-related task you need to do.
//                    if (null != mLocationHelper) {
//                        if (mLocationHelper.isNotToMakeRequiredLocationSettingsFlag()) {
//                            issueApiRequestIfNeeded();
//                        }
//                        else {
//                            mLocationHelper.checkLocationSettings();
//                        }
//                    }
//                    else {
//                        LogWrapper.showLog(Log.WARN, getLogTag(), "onRequestPermissionsResult() - mLocationHelper is null!");
//                    }
//                }
//                else {
//                    LogWrapper.showLog(Log.WARN, getLogTag(), "onRequestPermissionsResult() - no Permissions granted!");
//                    // permission denied, boo!
//                    // Disable the functionality that depends on this permission.
////                    String message = getString(R.string.lack_of_location_permission);
////                    showAlertDialog(true, "", message, null, null, new LackOfPermissionsHandler(), new LackOfPermissionsHandler());
//
//                    if (null != mLocationHelper) {
//                        mLocationHelper.lockHasAskedPermissionFlag();
//                    }
//                    issueApiRequestIfNeeded();
//                }
//                break;
//            }
//            // other 'case' lines to check for other permissions this app might request
//        }
//    }
//
//    public class LocationHelper extends BaseLocationHelper {
//
//        public LocationHelper(Delegate delegate) {
//            super(delegate);
//        }
//
//        @Override
//        protected Activity getAttachedActivity() {
//            return null;
//        }
//
//        @Override
//        protected Fragment getFragmentInstance() {
//            return null;
//        }
//
//        @Override
//        protected boolean isFragmentInstance() {
//            return false;
//        }
//    }
}
