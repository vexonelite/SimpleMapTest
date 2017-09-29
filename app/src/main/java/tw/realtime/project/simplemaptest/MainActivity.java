package tw.realtime.project.simplemaptest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private String getLogTag () {
        return this.getClass().getSimpleName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMapFragment();
    }

    public void showConfirmDialog (String srcMessage,
                                   DialogInterface.OnClickListener positiveCallback,
                                   DialogInterface.OnClickListener negativeCallback) {

        SpannableString message = null;
        if( (srcMessage != null) && (srcMessage.length() > 0) ) {
            message = new SpannableString(srcMessage);
        } else {
            message = new SpannableString("");
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setCallbacks(positiveCallback, negativeCallback);
        dialog.setMessage(message);
        dialog.setSingleOption(true);
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void initMapFragment () {
        Fragment next = new SimpleMapFragment();
        FragmentManager tFragManager = getFragmentManager();
        FragmentTransaction tFragTransaction = tFragManager.beginTransaction();
        tFragTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        tFragTransaction.replace(R.id.fragmentContainer, next, next.getClass().getSimpleName());
        try {
            tFragTransaction.commit();
        }
        catch (Exception e) {
            LogWrapper.showLog(Log.ERROR, getLogTag(), "Exception on FragmentTransaction.commit()", e);
        }
    }

    // Override onActivityResult() method and redirect to Fragment#onActivityResult()
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult() - requestCode: " + requestCode);

        switch (requestCode) {
            case GoogleMapHelper.CONNECTION_FAILURE_RESOLUTION_REQUEST :
            case LocationHelper.REQUEST_CHECK_SETTINGS:
                Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (null != fragment) {
                    LogWrapper.showLog(Log.INFO, getLogTag(), "onActivityResult() - fragment: " + fragment.getClass().getSimpleName());
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
