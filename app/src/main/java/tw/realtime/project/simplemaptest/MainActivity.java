package tw.realtime.project.simplemaptest;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

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
        dialog.show(getSupportFragmentManager(), "error_dialog");
    }

    private void initMapFragment () {
        Fragment next = new SimpleMapFragment();
        FragmentManager tFragManager = getSupportFragmentManager();
        FragmentTransaction tFragTransaction = tFragManager.beginTransaction();
        tFragTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        tFragTransaction.replace(R.id.container, next, next.getClass().getSimpleName());
        try {
            tFragTransaction.commit();
        } catch (Exception e) {
            Log.e("MainActivity", "Exception on FragmentTransaction.commit()");
            e.printStackTrace();
        }
    }
}
