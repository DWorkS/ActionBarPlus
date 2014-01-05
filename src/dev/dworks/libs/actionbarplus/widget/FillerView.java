/*
 * Copyright 2013 Hari Krishna Dulipudi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.libs.actionbarplus.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Simple view to fill space in grid view.
 * 
 * @author Tonic Artos
 */
public class FillerView extends LinearLayout {
	private View mMeasureTarget;

	public FillerView(Context context) {
		super(context);
	}

	public void setMeasureTarget(View lastViewSeen) {
		mMeasureTarget = lastViewSeen;
	}

	public FillerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FillerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(null != mMeasureTarget)
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(
				mMeasureTarget.getMeasuredHeight(), MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}