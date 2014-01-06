package dev.dworks.libs.actionbarplus.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import dev.dworks.libs.actionbarplus.R;

/**
 * Simple progress dialog that shows indeterminate progress bar together with message and dialog title (optional).<br/>
 * <p>
 * To show the dialog, start with {@link #createBuilder(android.content.Context, android.support.v4.app.FragmentManager)}.
 * </p>
 * <p>
 * Dialog can be cancelable - to listen to cancellation, activity or target fragment must implement {@link ISimpleDialogCancelListener}
 * </p>
 *
 * @author Tomas Vondracek
 */
public class ProgressDialogFragment extends BaseDialogFragment {

	protected static String ARG_MESSAGE = "message";
	protected static String ARG_TITLE = "title";

	protected int mRequestCode;

	public static ProgressDialogBuilder createBuilder(Context context, FragmentManager fragmentManager) {
		return new ProgressDialogBuilder(context, fragmentManager);
	}

	@Override
	protected Builder build(Builder builder) {
		final int defaultMessageTextColor = getResources().getColor(R.color.sdl_message_text_dark);
		final TypedArray a = getActivity().getTheme().obtainStyledAttributes(null, R.styleable.DialogStyle, R.attr.sdlDialogStyle, 0);
		final int messageTextColor = a.getColor(R.styleable.DialogStyle_messageTextColor, defaultMessageTextColor);
		a.recycle();

		final LayoutInflater inflater = builder.getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_part_progress, null, false);
		final TextView tvMessage = (TextView) view.findViewById(R.id.sdl__message);
		tvMessage.setText(getArguments().getString(ARG_MESSAGE));
		tvMessage.setTextColor(messageTextColor);

		builder.setView(view);

		builder.setTitle(getArguments().getString(ARG_TITLE));

		return builder;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() == null) {
			throw new IllegalArgumentException("use ProgressDialogBuilder to construct this dialog");
		}
		final Fragment targetFragment = getTargetFragment();
		mRequestCode = targetFragment != null ?
				getTargetRequestCode() : getArguments().getInt(BaseDialogBuilder.ARG_REQUEST_CODE, 0);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		SimpleDialogCancelListener listener = getCancelListener();
		if (listener != null) {
			listener.onCancelled(mRequestCode);
		}
	}

	protected SimpleDialogCancelListener getCancelListener() {
		final Fragment targetFragment = getTargetFragment();
		if (targetFragment != null) {
			if (targetFragment instanceof SimpleDialogCancelListener) {
				return (SimpleDialogCancelListener) targetFragment;
			}
		} else {
			if (getActivity() instanceof SimpleDialogCancelListener) {
				return (SimpleDialogCancelListener) getActivity();
			}
		}
		return null;
	}

	public static class ProgressDialogBuilder extends BaseDialogBuilder {

		private String mTitle;
		private String mMessage;

		protected ProgressDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager, ProgressDialogFragment.class);
		}

		@Override
		protected ProgressDialogBuilder self() {
			return this;
		}

		public ProgressDialogBuilder setTitle(int titleResourceId) {
			mTitle = mContext.getString(titleResourceId);
			return this;
		}


		public ProgressDialogBuilder setTitle(String title) {
			mTitle = title;
			return this;
		}

		public ProgressDialogBuilder setMessage(int messageResourceId) {
			mMessage = mContext.getString(messageResourceId);
			return this;
		}

		public ProgressDialogBuilder setMessage(String message) {
			mMessage = message;
			return this;
		}

		@Override
		protected Bundle prepareArguments() {
			Bundle args = new Bundle();
			args.putString(SimpleDialogFragment.ARG_MESSAGE, mMessage);
			args.putString(SimpleDialogFragment.ARG_TITLE, mTitle);

			return args;
		}
	}
}
