package dev.dworks.libs.actionbarplus.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import dev.dworks.libs.actionbarplus.R;

public class Spinner extends SpinnerICS {

	public Spinner(Context context) {
		this(context, null);
	}

	public Spinner(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Spinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpinnerHolo, defStyle, 0);

		CharSequence[] entries = a.getTextArray(R.styleable.SpinnerHolo_android_entries);
		if (entries != null) {
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, entries);
			adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
			setAdapter(adapter);
		}

		a.recycle();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
}
