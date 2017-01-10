package tw.realtime.project.simplemaptest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDialog extends DialogFragment {

    private DialogInterface.OnClickListener mPositiveCallback;
    private DialogInterface.OnClickListener mNegativeCallback;
    public boolean isSingleOption;

    private CharSequence mMessage;
    private CharSequence mPositiveText;
    private CharSequence mNegativeText;


    public ConfirmDialog() {
        this.setCancelable(false);
    }

    public void setCallbacks (DialogInterface.OnClickListener positiveCallback,
                              DialogInterface.OnClickListener negativeCallback) {
        mPositiveCallback = positiveCallback;
        mNegativeCallback = negativeCallback;
    }

    public void setMessage (CharSequence message) {
        mMessage = message;
    }

    public void setSingleOption (boolean flag) {
        isSingleOption = flag;
    }

    public void setPositiveText (CharSequence message) {
        mPositiveText = message;
    }

    public void setNegativeText (CharSequence message) {
        mNegativeText = message;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        if (null == mMessage) {
            mMessage = "";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(mMessage);
        if ( (null != mPositiveText) && (mPositiveText.length() > 0) ) {
            builder.setPositiveButton(mPositiveText, mPositiveCallback);
        }
        else {
            builder.setPositiveButton(android.R.string.ok, mPositiveCallback);
        }
        if (!isSingleOption) {
            if ( (null != mNegativeText) && (mNegativeText.length() > 0) ) {
                builder.setNegativeButton(mNegativeText, mNegativeCallback);
            }
            else {
                builder.setNegativeButton(android.R.string.no, mNegativeCallback);
            }
        }
        return builder.create();
    }
}
