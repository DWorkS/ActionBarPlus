/*
 * Copyright 2013 Chris Banes
 * Copyright 2013 Hari Krishna Dulipudi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.libs.actionbarpulltorefresh;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import dev.dworks.libs.actionbarplus.R;
import dev.dworks.libs.actionbarpulltorefresh.PullToRefreshAttacher.EnvironmentDelegate;
import dev.dworks.libs.actionbarpulltorefresh.PullToRefreshAttacher.HeaderTransformer;
import dev.dworks.libs.actionbarpulltorefresh.PullToRefreshAttacher.ViewDelegate;

/**
 * FIXME
 */
public final class PullToRefreshListAttacher implements View.OnTouchListener {

    /**
     * Default configuration values
     */
    private static final int DEFAULT_HEADER_LAYOUT = R.layout.default_list_header;
    private static final int DEFAULT_FOOTER_LAYOUT = R.layout.default_list_footer;
    private static final int DEFAULT_ANIM_HEADER_IN = R.anim.slide_in;
    private static final int DEFAULT_ANIM_HEADER_OUT = R.anim.slide_out;
    private static final int DEFAULT_ANIM_FOOTER_IN = R.anim.slide_in_bottom;
    private static final int DEFAULT_ANIM_FOOTER_OUT = R.anim.slide_out_bottom;
    private static final float DEFAULT_REFRESH_SCROLL_DISTANCE = 0.5f;
    private static final int DEFAULT_HEADER_THEME = 0;
    
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "PullToRefreshAttacher";

    private View mRefreshableView;
    private ViewDelegate mViewDelegate;

    private final EnvironmentDelegate mEnvironmentDelegate;

    private final View mHeaderView;
    private final View mFooterView;
    private final Animation mHeaderInAnimation, mHeaderOutAnimation;
    private final Animation mFooterInAnimation, mFooterOutAnimation;

    private final int mTouchSlop;
    private final float mRefreshScrollDistance;
    private float mInitialMotionY, mLastMotionY;
    private boolean mIsBeingDragged, mIsRefreshing, mIsHandlingTouchEvent;

    private OnHeaderRefreshListener mHeaderRefreshListener;
    private OnFooterRefreshListener mFooterRefreshListener;
    private final HeaderTransformer mHeaderTransformer;
    private final HeaderTransformer mFooterTransformer;
    
    private boolean mEnabled = true;
    private ListView list;
    private MODE mMode, mCurrentMode;
    
    public enum MODE{
    	None,
    	Header,
    	Footer,
    	Both
    }
    /**
     * FIXME
     * @param activity
     */
    public PullToRefreshListAttacher(Activity activity) {
        this(activity, new ListOptions());
    }

    /**
     * FIXME
     * @param activity
     * @param options
     */
    public PullToRefreshListAttacher(Activity activity, ListOptions options) {
        if (options == null) {
            Log.i(LOG_TAG, "Given null options so using default options.");
            options = new ListOptions();
        }

        // Copy necessary values from options
        mRefreshScrollDistance = options.refreshScrollDistance;

        // EnvironmentDelegate
        mEnvironmentDelegate = options.environmentDelegate != null
                ? options.environmentDelegate
                : new EnvironmentDelegate();

        // Header Transformer
        mHeaderTransformer = options.headerTransformer != null
                ? options.headerTransformer
                : new DefaultHeaderTransformer();
        
        // Footer Transformer
        mFooterTransformer = options.footerTransformer != null
                ? options.footerTransformer
                : new DefaultHeaderTransformer();
        
        // Create animations for use later
        mHeaderInAnimation = AnimationUtils.loadAnimation(activity, options.headerInAnimation);
        mHeaderOutAnimation = AnimationUtils.loadAnimation(activity, options.headerOutAnimation);
        if (mHeaderOutAnimation != null) {
            mHeaderOutAnimation.setAnimationListener(new AnimationCallback());
        }
        
        // Create animations for use later
        mFooterInAnimation = AnimationUtils.loadAnimation(activity, options.footerInAnimation);
        mFooterOutAnimation = AnimationUtils.loadAnimation(activity, options.footerOutAnimation);
        if (mFooterOutAnimation != null) {
        	mFooterOutAnimation.setAnimationListener(new AnimationCallback());
        }

        // Get touch slop for use later
        mTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();

        // Create Header view and then add to Decor View
        mHeaderView = LayoutInflater.from(mEnvironmentDelegate.getContextForInflater(activity))
                .inflate(options.headerLayout, null);
        if (mHeaderView == null) {
            throw new IllegalArgumentException("Must supply valid layout id for header.");
        }
        mHeaderView.setVisibility(View.GONE);
        
        // Create Footer view and then add to Decor View
        mFooterView = LayoutInflater.from(mEnvironmentDelegate.getContextForInflater(activity))
                .inflate(options.footerLayout, null);
        if (mFooterView == null) {
            throw new IllegalArgumentException("Must supply valid layout id for header.");
        }
        mFooterView.setVisibility(View.GONE);

        // Notify transformer
        mHeaderTransformer.onViewCreated(mHeaderView);
        mHeaderTransformer.setTheme(activity, options.headerTheme);
        
        // Notify transformer
        mFooterTransformer.onViewCreated(mFooterView);
        mFooterTransformer.setTheme(activity, options.footerTheme);
    }

    /**
     * Set the view which will be used to initiate refresh requests and a listener to be invoked
     * when a refresh is started. This version of the method will try to find a handler for the
     * view from the built-in view delegates.
     *
     * @param view - View which will be used to initiate refresh requests.
     * @param refreshListener - Listener to be invoked when a refresh is started.
     */
    public void setRefreshableView(ListView view, MODE mode) {
        setRefreshableView(view, mode, null);
    }
    
    public void setHeaderRefreshLister(OnHeaderRefreshListener refreshListener) {
        mHeaderRefreshListener = refreshListener;
    }
    
    public void setFooterRefreshLister(OnFooterRefreshListener refreshListener) {
        mFooterRefreshListener = refreshListener;
    }

    /**
     * Set the view which will be used to initiate refresh requests, along with a delegate which
     * knows how to handle the given view, and a listener to be invoked when a refresh is started.
     *
     * @param view - View which will be used to initiate refresh requests.
     * @param viewDelegate - delegate which knows how to handle <code>view</code>.
     * @param refreshListener - Listener to be invoked when a refresh is started.
     */
    public void setRefreshableView(ListView view, MODE mode, ViewDelegate viewDelegate) {
    	
    	mMode = mode;
		list = (ListView) view;
		
    	switch (mMode) {
		case Header:
			list.addHeaderView(mHeaderView);
			break;
			
		case Footer:
			list.addFooterView(mFooterView);
			break;

		case Both:
    		list.addHeaderView(mHeaderView);
    		list.addFooterView(mFooterView);
			break;

		default:
			break;
		}
    	
        // If we already have a refreshable view, reset it and our state
        if (mRefreshableView != null) {
            mRefreshableView.setOnTouchListener(null);
            setRefreshingHeaderInt(false, false);
            setRefreshingFooterInt(false, false);
        }

        // Check to see if view is null
        if (view == null) {
            Log.i(LOG_TAG, "Refreshable View is null.");
            mViewDelegate = null;
            return;
        }

        // View to detect refreshes for
        mRefreshableView = view;
        mRefreshableView.setOnTouchListener(this);

        // ViewDelegate
        if (viewDelegate == null) {
            viewDelegate = InstanceCreationUtils.getBuiltInViewDelegate(view);
            if (viewDelegate == null) {
                throw new IllegalArgumentException("No view handler found. Please provide one.");
            }
        }
        mViewDelegate = viewDelegate;
    }

    /**
     * Manually set this Attacher's refreshing state. The header will be displayed or hidden as
     * requested.
     * @param refreshing - Whether the attacher should be in a refreshing state,
     */
    public final void setHeaderRefreshing(boolean refreshing) {
        setRefreshingHeaderInt(refreshing, false);
    }
    
    /**
     * Manually set this Attacher's refreshing state. The header will be displayed or hidden as
     * requested.
     * @param refreshing - Whether the attacher should be in a refreshing state,
     */
    public final void setFooterRefreshing(boolean refreshing) {
        setRefreshingFooterInt(refreshing, false);
    }

    /**
     * @return true if this Attacher is currently in a refreshing state.
     */
    public final boolean isRefreshing() {
        return mIsRefreshing;
    }

    /**
     * @return true if this PullToRefresh is currently enabled (defaults to <code>true</code>)
     */
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Allows the enable/disable of this PullToRefreshAttacher. If disabled when refreshing then
     * the UI is automatically reset.
     *
     * @param enabled - Whether this PullToRefreshAttacher is enabled.
     */
    public void setHeaderEnabled(boolean enabled) {
        mEnabled = enabled;

        if (!enabled) {
            // If we're not enabled, reset any touch handling
            resetTouch();

            // If we're currently refreshing, reset the ptr UI
            if (mIsRefreshing) {
                resetHeader(false);
                resetFooter(false);
            }
        }
    }

    /**
     * Call this when your refresh is complete and this view should reset itself (header view
     * will be hidden).
     *
     * This is the equivalent of calling <code>setRefreshing(false)</code>.
     */
    public final void setHeaderRefreshComplete() {
        setRefreshingHeaderInt(false, false);
    }
    
    /**
     * Call this when your refresh is complete and this view should reset itself (header view
     * will be hidden).
     *
     * This is the equivalent of calling <code>setRefreshing(false)</code>.
     */
    public final void setFooterRefreshComplete() {
        setRefreshingFooterInt(false, false);
    }

    @Override
    public final boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        // If we're not enabled don't handle any touch events
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                // If we're already refreshing or not handling the event, ignore it
                if (mIsRefreshing){
                    return false;
                }
                
                final float y = event.getY();
                if(!mIsHandlingTouchEvent) {
                	if(canRefresh(true) && (mCurrentMode ==  MODE.Header || mCurrentMode ==  MODE.Footer)){
                		mIsHandlingTouchEvent = true;
                		mInitialMotionY = y;
                	}
                	else{
                		return false;
                	}
                }

                if(null != mCurrentMode){
                	switch (mCurrentMode) {
					case Header:
		                // We're not currently being dragged so check to see if the user has scrolled enough
		                if (!mIsBeingDragged && (y - mInitialMotionY) > mTouchSlop) {
		                    mIsBeingDragged = true;
		                    onPullStartedHeader();
		                }
		                if (mIsBeingDragged) {
		                    final float yDx = y - mLastMotionY;

		                    /**
		                     * Check to see if the user is scrolling the right direction (down).
		                     * We allow a small scroll up which is the check against negative touch slop.
		                     */
		                    if (yDx >= -mTouchSlop) {
		                        onPullHeader(y);
		                        // Only record the y motion if the user has scrolled down.
		                        if (yDx > 0f) {
		                            mLastMotionY = y;
		                        }
		                    } else {
		                        resetTouch();
		                    }
		                }
						break;
					case Footer:
		                // We're not currently being dragged so check to see if the user has scrolled enough
		                if (!mIsBeingDragged && (mInitialMotionY - y) > mTouchSlop) {
		                    mIsBeingDragged = true;
		                    onPullStartedFooter();
		                }
		                if (mIsBeingDragged) {
		                    final float yDx = mInitialMotionY - y;

		                    /**
		                     * Check to see if the user is scrolling the right direction (down).
		                     * We allow a small scroll up which is the check against negative touch slop.
		                     */
		                    if (yDx >= -mTouchSlop) {
		                        onPullFooter(y);
		                        // Only record the y motion if the user has scrolled down.
		                        if (yDx > 0f) {
		                            mLastMotionY = y;
		                        }
		                    } else {
		                        resetTouch();
		                    }
		                }
						break;
					default:
						break;
					}
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                // If we're already refreshing, ignore
                if (canRefresh(true)) {
                	if(hasHeaderMode() && mViewDelegate.isScrolledToTop(mRefreshableView)){
                        mIsHandlingTouchEvent = true;
                        mCurrentMode = MODE.Header;
                        mInitialMotionY = event.getY();
                        Log.i("foot", "down header");
                	}
                	else if(hasFooterMode() && mViewDelegate.isScrolledToBottom(mRefreshableView)){
                        mIsHandlingTouchEvent = true;
                        mCurrentMode = MODE.Footer;
                        mInitialMotionY = event.getY();
                        Log.i("foot", "down footer");
                	}

                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{
            	resetTouch();
            	break;
            }
        }

        // Always return false as we only want to observe events
        return false;
    }
    
    private boolean hasHeaderMode(){
    	return mMode == MODE.Header || mMode == MODE.Both;
    }
    
    private boolean hasFooterMode(){
    	return mMode == MODE.Footer || mMode == MODE.Both;
    }

    private void resetTouch() {
        if (mIsBeingDragged) {
            // We were being dragged, but not any more.
            mIsBeingDragged = false;
            onPullHeaderEnded();
            onPullFooterEnded();
        }
        mIsHandlingTouchEvent = false;
        mInitialMotionY = mLastMotionY = 0f;
    }
    
    void onPullStartedHeader() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPullStarted");
        }
        // Show Header
        if (mHeaderInAnimation != null) {
            mHeaderView.startAnimation(mHeaderInAnimation);
        }
        mHeaderView.setVisibility(View.VISIBLE);
    }
    
    void onPullStartedFooter() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPullStarted");
        }
        
        // Show Footer
        if (mHeaderInAnimation != null) {
            mFooterView.startAnimation(mFooterInAnimation);
        }
        mFooterView.setVisibility(View.VISIBLE);
    }

    void onPullHeader(float y) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPull");
        }

        final float pxScrollForRefresh = mRefreshableView.getHeight() * mRefreshScrollDistance;
        final float scrollLength = y - mInitialMotionY;
        if (scrollLength < pxScrollForRefresh) {
            mHeaderTransformer.onPulled(scrollLength / pxScrollForRefresh);
        } else {
            setRefreshingHeaderInt(true, true);
        }
    }
    
    void onPullFooter(float y) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPull");
        }

        final float pxScrollForRefresh = mRefreshableView.getHeight() * mRefreshScrollDistance;
        final float scrollLength = mInitialMotionY - y;
        if (scrollLength < pxScrollForRefresh) {
            mFooterTransformer.onPulled(scrollLength / pxScrollForRefresh);
        } else {
            setRefreshingFooterInt(true, true);
        }
    }
    
    void onPullHeaderEnded() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPullEnded");
        }
        if (!mIsRefreshing) {
            resetHeader(true);
        }
    }
    
    void onPullFooterEnded() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPullEnded");
        }
        if (!mIsRefreshing) {
            resetFooter(true);
        }
    }

    private void setRefreshingHeaderInt(boolean refreshing, boolean fromTouch) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setRefreshingInt: " + refreshing);
        }
        if(hasHeaderMode()){
            // Check to see if we need to do anything
            if (mIsRefreshing == refreshing) {
                return;
            }

            if (refreshing && canRefresh(fromTouch)) {
                startRefreshHeader(fromTouch);
            } else {
                resetHeader(fromTouch);
            }	
        }
    }
    
    private void setRefreshingFooterInt(boolean refreshing, boolean fromTouch) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setRefreshingInt: " + refreshing);
        }
        if(hasFooterMode()){
            // Check to see if we need to do anything
            if (mIsRefreshing == refreshing) {
                return;
            }

            if (refreshing && canRefresh(fromTouch)) {
                startRefreshFooter(fromTouch);
            } else {
                resetFooter(fromTouch);
            }	
        }
    }

    private boolean canRefresh(boolean fromTouch) {
        return !mIsRefreshing && (!fromTouch || mHeaderRefreshListener != null || mFooterRefreshListener != null);
    }
    
    private void resetHeader(boolean fromTouch) {
        // Update isRefreshing state
        mIsRefreshing = false;

        if (mHeaderView.getVisibility() != View.GONE) {
            // Hide Header
            if (mHeaderOutAnimation != null) {
                //mHeaderView.startAnimation(mHeaderOutAnimation);
                mHeaderTransformer.onReset();
                // HeaderTransformer.onReset() is called once the animation has finished
            } else {
                // As we're not animating, hide the header + call the header transformer now
                mHeaderView.setVisibility(View.GONE);
                mHeaderTransformer.onReset();
            }
        }
    }
    
    private void resetFooter(boolean fromTouch) {
        // Update isRefreshing state
        mIsRefreshing = false;

        if (mFooterView.getVisibility() != View.GONE) {
            // Hide Header
            if (mHeaderOutAnimation != null) {
            	//mFooterView.startAnimation(mHeaderOutAnimation);
            	mFooterTransformer.onReset();
                // HeaderTransformer.onReset() is called once the animation has finished
            } else {
                // As we're not animating, hide the header + call the header transformer now
                mFooterView.setVisibility(View.GONE);
                mFooterTransformer.onReset();
            }
        }
    }
    
    private void startRefreshHeader(boolean fromTouch) {
        // Update isRefreshing state
        mIsRefreshing = true;

        // Call OnRefreshListener if this call has originated from a touch event
        if (fromTouch) {
            mHeaderRefreshListener.onHeaderRefreshStarted(mRefreshableView);
        }
        // Call Transformer
        mHeaderTransformer.onRefreshStarted();

        // Make sure header is visible.
        if (mHeaderView.getVisibility() != View.VISIBLE) {
        	if(null != mHeaderInAnimation){
        		mHeaderView.startAnimation(mHeaderInAnimation);
        	}
            mHeaderView.setVisibility(View.VISIBLE);
        }
    }
    
    private void startRefreshFooter(boolean fromTouch) {
        // Update isRefreshing state
        mIsRefreshing = true;

        // Call OnRefreshListener if this call has originated from a touch event
        if (fromTouch) {
        	mFooterRefreshListener.onFooterRefreshStarted(mRefreshableView);
        }
        // Call Transformer
        mFooterTransformer.onRefreshStarted();

        // Make sure header is visible.
        if (mFooterView.getVisibility() != View.VISIBLE) {
        	if(null != mFooterInAnimation){
        		mFooterView.startAnimation(mFooterInAnimation);
        	}
            mFooterView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Simple Listener to listen for any callbacks to Refresh.
     */
    public interface OnHeaderRefreshListener {
        /**
         * Called when the user has initiated a refresh by pulling.
         * @param view - View which the user has started the refresh from.
         */
        public void onHeaderRefreshStarted(View view);
    }
    
    /**
     * Simple Listener to listen for any callbacks to Refresh.
     */
    public interface OnFooterRefreshListener {
        /**
         * Called when the user has initiated a refresh by pulling.
         * @param view - View which the user has started the refresh from.
         */
        public void onFooterRefreshStarted(View view);
    }

    public static final class ListOptions {
        /**
         * EnvironmentDelegate instance which will be used. If null, we will create an instance of
         * the default class.
         */
        public EnvironmentDelegate environmentDelegate = null;

        /**
         * The layout resource ID which should be inflated to be displayed above the Action Bar
         */
        public int headerLayout = DEFAULT_HEADER_LAYOUT;
        
        /**
         * The layout resource ID which should be inflated to be displayed above the Action Bar
         */
        public int footerLayout = DEFAULT_FOOTER_LAYOUT;

        /**
         * The header transformer to be used to transfer the header view. If null, an instance of
         * {@link DefaultHeaderTransformer} will be used.
         */
        public HeaderTransformer headerTransformer = null;

        /**
         * The header transformer to be used to transfer the header view. If null, an instance of
         * {@link DefaultHeaderTransformer} will be used.
         */
        public HeaderTransformer footerTransformer = null;
        
        /**
         * The anim resource ID which should be started when the header is being hidden.
         */
        public int headerOutAnimation = DEFAULT_ANIM_HEADER_OUT;

        /**
         * The anim resource ID which should be started when the header is being shown.
         */
        public int headerInAnimation = DEFAULT_ANIM_HEADER_IN;

        /**
         * The anim resource ID which should be started when the header is being hidden.
         */
        public int footerOutAnimation = DEFAULT_ANIM_FOOTER_OUT;

        /**
         * The anim resource ID which should be started when the header is being shown.
         */
        public int footerInAnimation = DEFAULT_ANIM_FOOTER_IN;
        
        /**
         * The percentage of the refreshable view that needs to be scrolled before a refresh
         * is initiated.
         */
        public float refreshScrollDistance = DEFAULT_REFRESH_SCROLL_DISTANCE;
        
        /**
         * The theme type which should be for the header is being shown.
         */
        public int headerTheme = DEFAULT_HEADER_THEME;
        
        public int footerTheme = DEFAULT_HEADER_THEME;
    }
    
    private class AnimationCallback implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            if (animation == mHeaderOutAnimation) {
                mHeaderTransformer.onReset();
                mFooterTransformer.onReset();
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (animation == mHeaderOutAnimation) {
                mHeaderView.setVisibility(View.GONE);
                mFooterView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * Default Header Transformer.
     */
    public static class DefaultHeaderTransformer extends HeaderTransformer {
        public TextView mHeaderTextView;
        public ProgressBar mHeaderProgressBar;
        private View mHeaderView;
        @Override
        public void onViewCreated(View headerView) {
            // Get ProgressBar and TextView. Also set initial text on TextView
            mHeaderProgressBar = (ProgressBar) headerView.findViewById(R.id.ptr_progress);
            mHeaderTextView = (TextView) headerView.findViewById(R.id.ptr_text);
            mHeaderView = headerView;
            // Call onReset to make sure that the View is consistent
            onReset();
        }

		@Override
		public void onHide() {
            if (mHeaderProgressBar != null) {
            	mHeaderProgressBar.setVisibility(View.GONE);
            }
            if (mHeaderTextView != null) {
                mHeaderTextView.setVisibility(View.GONE);
            }
            mHeaderView.setVisibility(View.GONE);
		}

        @Override
        public void onReset() {
            // Reset Progress Bar
            if (mHeaderProgressBar != null) {
                mHeaderProgressBar.setIndeterminate(false);
            	final int progress = mHeaderProgressBar.getProgress();
                new AsyncTask<Void, Void, Void>(){
                	
					@Override
					protected Void doInBackground(Void... params) {
		            	for (int i = progress; i >= 0; i--) {
		            		try {
		            			publishProgress();
								Thread.sleep(2);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						return null;
					}
					
                	@Override
                	protected void onProgressUpdate(Void... values) {
	            		mHeaderProgressBar.incrementProgressBy(-1);
                		super.onProgressUpdate(values);
                	}
                	
                	@Override
                	protected void onPostExecute(Void result) {
						mHeaderProgressBar.setProgress(0);
						onHide();
                		super.onPostExecute(result);
                	}
                	
                }.execute();
            }

            // Reset Text View
            if (mHeaderTextView != null) {
                mHeaderTextView.setText(R.string.pull_to_refresh_pull_label);
            }
        }

        @Override
        public void onPulled(float percentagePulled) {
            if (mHeaderTextView != null) {
                mHeaderTextView.setVisibility(View.VISIBLE);
            }
            if (mHeaderProgressBar != null) {
                mHeaderProgressBar.setVisibility(View.VISIBLE);
                mHeaderProgressBar.setProgress(Math.round(mHeaderProgressBar.getMax() * percentagePulled));
            }
        }

        @Override
        public void onRefreshStarted() {
            if (mHeaderTextView != null) {
                mHeaderTextView.setText(R.string.pull_to_refresh_refreshing_label);
            }
            if (mHeaderProgressBar != null) {
                mHeaderProgressBar.setIndeterminate(true);
            }
        }
        
		@Override
		public void setTheme(Activity activity, int theme) {
			final int mColorWhite = activity.getResources().getColor(android.R.color.white);
			final int mColorBlack = activity.getResources().getColor(android.R.color.black);
			final int mTextColor = theme == 0 ? mColorWhite : mColorBlack;
			final int mTextBackgroundColor = theme == 0 ? mColorBlack : mColorWhite;
			final int mProgressDrawable = theme == 0 
					? R.drawable.progress_horizontal_center_dark 
					: R.drawable.progress_horizontal_center_light;
			final int mIndeterminateDrawable = R.drawable.progress_indeterminate_horizontal;
			if (mHeaderTextView != null) {
                mHeaderTextView.setHeight(PullToRefreshListAttacher.getActionBarHeight(activity));
                mHeaderTextView.setBackgroundColor(mTextBackgroundColor);
                mHeaderTextView.setTextColor(mTextColor);
            }
			
            if (mHeaderProgressBar != null) {
                mHeaderProgressBar.setProgressDrawable(activity.getResources().getDrawable(mProgressDrawable));
                mHeaderProgressBar.setIndeterminateDrawable(activity.getResources().getDrawable(mIndeterminateDrawable));
            }
		}
    }
	
	private static int getActionBarHeight(Activity activity) {
		int result = 0;
		int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = activity.getResources().getDimensionPixelSize(resourceId);
		}
		resourceId = activity.getResources().getIdentifier("actionBarSize", "attr", null);
		if (resourceId > 0) {
			result = activity.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}