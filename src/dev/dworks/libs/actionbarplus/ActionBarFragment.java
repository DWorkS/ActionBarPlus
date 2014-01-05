package dev.dworks.libs.actionbarplus;

import android.app.Activity;
import android.support.v4.app.FragmentPlus;
import android.support.v7.app.ActionBarActivity;

public class ActionBarFragment extends FragmentPlus{
    private ActionBarActivity mActivity;

    public ActionBarActivity getActionBarActivity() {
        return mActivity;
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof ActionBarActivity)) {
            throw new IllegalStateException(getClass().getSimpleName() + " must be attached to a ActionBarListActivity.");
        }
        mActivity = (ActionBarActivity)activity;

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }
}
