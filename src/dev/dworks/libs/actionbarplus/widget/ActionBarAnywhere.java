package dev.dworks.libs.actionbarplus.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import dev.dworks.libs.actionbarplus.R;

public class ActionBarAnywhere extends LinearLayout {
	private Context mContext;

	public ActionBarAnywhere(Context context) {
		super(context);
		init(context, null, 0);
	}

	public ActionBarAnywhere(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public ActionBarAnywhere(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
		
		LayoutInflater.from(context).inflate(R.layout.abp_view, this, true);
/*		if(isInEditMode()){
			return;
		}*/
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}