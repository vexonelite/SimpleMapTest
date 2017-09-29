package tw.realtime.project.simplemaptest;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;


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
