package dev.dworks.libs.actionbarplus.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Internal base builder that holds common values for all dialog fragment builders.
 *
 * @author Tomas Vondracek
 */
public abstract class BaseDialogBuilder {

	public static String ARG_REQUEST_CODE = "request_code";
	public static String ARG_CANCELABLE_ON_TOUCH_OUTSIDE = "cancelable_oto";
	public static String DEFAULT_TAG = "simple_dialog";
	public static int DEFAULT_REQUEST_CODE = -42;

	protected final Context mContext;
	protected final FragmentManager mFragmentManager;
	protected final Class<? extends BaseDialogFragment> mClass;

	private Fragment mTargetFragment;
	private boolean mCancelable = true;
	private boolean mCancelableOnTouchOutside = true;

	private String mTag = DEFAULT_TAG;
	private int mRequestCode = DEFAULT_REQUEST_CODE;

	public BaseDialogBuilder(Context context, FragmentManager fragmentManager, Class<? extends BaseDialogFragment> clazz) {
		mFragmentManager = fragmentManager;
		mContext = context.getApplicationContext();
		mClass = clazz;
	}

	protected abstract BaseDialogBuilder self();

	protected abstract Bundle prepareArguments();

	public BaseDialogBuilder setCancelable(boolean cancelable) {
		mCancelable = cancelable;
		return self();
	}
	
	public BaseDialogBuilder setCancelableOnTouchOutside(boolean cancelable) {
		mCancelableOnTouchOutside = cancelable;
		if (cancelable) {
			mCancelable = cancelable;
		}
		return self();
	}

	public BaseDialogBuilder setTargetFragment(Fragment fragment, int requestCode) {
		mTargetFragment = fragment;
		mRequestCode = requestCode;
		return self();
	}

	public BaseDialogBuilder setRequestCode(int requestCode) {
		mRequestCode = requestCode;
		return self();
	}

	public BaseDialogBuilder setTag(String tag) {
		mTag = tag;
		return self();
	}


	public DialogFragment show() {
		final Bundle args = prepareArguments();

		final BaseDialogFragment fragment = (BaseDialogFragment) Fragment.instantiate(mContext, mClass.getName(), args);
	
		args.putBoolean(ARG_CANCELABLE_ON_TOUCH_OUTSIDE, mCancelableOnTouchOutside);
		
		if (mTargetFragment != null) {
			fragment.setTargetFragment(mTargetFragment, mRequestCode);
		} else {
			args.putInt(ARG_REQUEST_CODE, mRequestCode);
		}
		fragment.setCancelable(mCancelable);
		fragment.show(mFragmentManager, mTag);
		
		return fragment;
	}
}
