package tw.realtime.project.simplemaptest;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;


public abstract class BaseFragment extends Fragment {

    public String getLogTag() {
        return this.getClass().getSimpleName();
    }


    protected void showConfirmDialog (String srcMessage,
                                      DialogInterface.OnClickListener positiveCallback,
                                      DialogInterface.OnClickListener negativeCallback) {
        if (!isAdded()) {
            return;
        }
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) {
            return;
        }
        activity.showConfirmDialog(srcMessage, positiveCallback, negativeCallback);
    }

    protected void backToPrevious () {
        if (isAdded()) {
            Activity activity = getActivity();
            if (null != activity) {
                activity.onBackPressed();
            }
        }
    }
}
