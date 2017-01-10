package tw.realtime.project.simplemaptest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

public class SystemPermissionUtils {

    public static final int REQUEST_CODE_INTERNET_PERMISSION = 101;
    public static final int REQUEST_CODE_CAMERA_PERMISSION = 102;
    public static final int REQUEST_CODE_MICROPHONE_PERMISSION = 103;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 104;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 105;
    public static final int REQUEST_CODE_TAKE_SHOT_PERMISSIONS = 106;
    public static final int REQUEST_CODE_VIDEO_REC_PERMISSIONS = 107;
    public static final int REQUEST_CODE_COARSE_LOCATION_PERMISSION = 108;
    public static final int REQUEST_CODE_FINE_LOCATION_PERMISSION = 109;
    public static final int REQUEST_CODE_LOCATION_PERMISSIONS = 110;


    public static boolean hasInternetPermissionBeenGranted (final Context context) {
        if (null == context) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasCameraPermissionBeenGranted (final Context context) {
        if (null == context) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasMicrophonePermissionBeenGranted (final Context context) {
        if (null == context) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasReadExternalStoragePermissionBeenGranted (final Context context) {
        if (null == context) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasWriteExternalStoragePermissionBeenGranted (final Context context) {
        if (null == context) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasCoarseLocationPermissionBeenGranted (final Context context) {
        if (null == context) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasFineLocationPermissionBeenGranted (final Context context) {
        if (null == context) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasEnoughVideoRecPermission (final Context context) {
        boolean camera = hasCameraPermissionBeenGranted(context);
        boolean mic = hasMicrophonePermissionBeenGranted(context);
        boolean read = hasReadExternalStoragePermissionBeenGranted(context);
        boolean write = hasWriteExternalStoragePermissionBeenGranted(context);
        return (camera && mic && read && write);
    }

    public static boolean hasEnoughTakeShotPermission (final Context context) {
        boolean camera = hasCameraPermissionBeenGranted(context);
        boolean read = hasReadExternalStoragePermissionBeenGranted(context);
        boolean write = hasWriteExternalStoragePermissionBeenGranted(context);
        return (camera && read && write);
    }

    public static boolean hasEnoughLocationPermission (final Context context) {
        boolean fine = hasFineLocationPermissionBeenGranted(context);
        boolean coarse = hasCoarseLocationPermissionBeenGranted(context);
        return (fine && coarse);
    }

    public static void requestInternetPermission (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.INTERNET},
                    REQUEST_CODE_INTERNET_PERMISSION);
        }
    }

    public static void requestCameraPermission (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA_PERMISSION);
        }
    }

    public static void requestMicrophonePermission (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_MICROPHONE_PERMISSION);
        }
    }

    public static void requestReadExternalStoragePermission (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    public static void requestWriteExternalStoragePermission (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    public static void requestCoarseLocationPermission (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_COARSE_LOCATION_PERMISSION);
        }
    }

    public static void requestFineLocationPermission (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_FINE_LOCATION_PERMISSION);
        }
    }

    public static void requestLocationPermissions (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSIONS);
        }
    }


    public static void requestTakeShotPermissions (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_TAKE_SHOT_PERMISSIONS);
        }
    }

    public static void requestVideoRecPermissions (final FragmentActivity activity) {
        if (null != activity) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_VIDEO_REC_PERMISSIONS);
        }
    }


    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_INTERNET_PERMISSION:
            case REQUEST_CODE_CAMERA_PERMISSION:
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION:
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION:
            case REQUEST_CODE_MICROPHONE_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if ( (grantResults.length > 0) &&
                        (grantResults[0] == PackageManager.PERMISSION_GRANTED) ) {
                    // permission was granted, yay!
                    // Do the contacts-related task you need to do.
                }
                else {
                    // permission denied, boo!
                    // Disable the functionality that depends on this permission.

                }
                break;

            case REQUEST_CODE_VIDEO_REC_PERMISSIONS:
            case REQUEST_CODE_TAKE_SHOT_PERMISSIONS:
                boolean result = true;
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
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
                }
                else {
                    // permission denied, boo!
                    // Disable the functionality that depends on this permission.
                }
                break;
        }
    }

}
