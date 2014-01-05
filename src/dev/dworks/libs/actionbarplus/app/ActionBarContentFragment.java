package dev.dworks.libs.actionbarplus.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import dev.dworks.libs.actionbarplus.ActionBarFragment;
import dev.dworks.libs.actionbarplus.R;

public class ActionBarContentFragment extends ActionBarFragment {
    private CharSequence mEmptyText;
    private View mEmptyView;
    final private Handler mHandler = new Handler();
    private FrameLayout mContent;
    private View mContentContainer;
    private boolean mContentShown;
    private View mProgressContainer;
    final private Runnable mRequestFocus = new Runnable() {
        @Override
        public void run() {
            mContent.focusableViewAvailable(mContent);
        }
    };
    private TextView mStandardEmptyView;

    private void ensureContent() {
        if (mContent != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        //if (root instanceof View) {
        //    mContent = (FrameLayout) root;
        //} else {
            mStandardEmptyView = (TextView) root
                    .findViewById(R.id.internalEmpty);
            if (mStandardEmptyView == null) {
                mEmptyView = root.findViewById(android.R.id.empty);
            } else {
                mStandardEmptyView.setVisibility(View.GONE);
            }
            mProgressContainer = root.findViewById(R.id.progressContainer);
            mContentContainer = root.findViewById(R.id.listContainer);
            View rawContentView = root.findViewById(android.R.id.content);
            if (rawContentView == null) {
                throw new RuntimeException(
                        "Your content must have a FrameLayout whose id attribute is "
                                + "'android.R.id.content'");
            }
            mContent = (FrameLayout) rawContentView;
            if (mEmptyView != null) {
                //mContent.setEmptyView(mEmptyView);
            } else if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
                //mContent.setEmptyView(mStandardEmptyView);
            }
        //}
        mContentShown = true;
		if (mProgressContainer != null) {
			setContentShown(false, false);
		}
        mHandler.post(mRequestFocus);
    }

    protected View getEmptyView() {
        return mEmptyView;
    }

    public FrameLayout getContentView() {
        ensureContent();
        return mContent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.abp_view_content, container, false);
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mContent = null;
        mContentShown = false;
        mEmptyView = mProgressContainer = mContentContainer = null;
        mStandardEmptyView = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	ensureContent();
    }

    public void setEmptyText(CharSequence text) {
        ensureContent();
        if (mStandardEmptyView == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        mStandardEmptyView.setText(text);
        if (mEmptyText == null) {
            //mContent.setEmptyView(mStandardEmptyView);
        }
        mEmptyText = text;
    }

    public void setContentShown(boolean shown) {
        setContentShown(shown, true);
    }

    private void setContentShown(boolean shown, boolean animate) {
        ensureContent();
        if (mProgressContainer == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        if (mContentShown == shown) {
            return;
        }
        mContentShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mContentContainer.setVisibility(View.VISIBLE);
        } else {
        	if(null != mStandardEmptyView){
        		mStandardEmptyView.setText("");
        	}
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
        }
    }
    
    public void setEmptyShown(boolean shown) {
        ensureContent();
        if (mProgressContainer == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        if (mContentShown == shown) {
            return;
        }
        mContentShown = shown;
        if (shown) {
        	if(null != mStandardEmptyView){
                if (mEmptyText != null) {
            		mStandardEmptyView.setText(mEmptyText);
                }
        	}
            mProgressContainer.clearAnimation();
            mContentContainer.clearAnimation();
            mProgressContainer.setVisibility(View.GONE);
            mContentContainer.setVisibility(View.VISIBLE);
            mContent.setVisibility(View.GONE);
            mStandardEmptyView.setVisibility(View.VISIBLE);
        } else {
        	if(null != mStandardEmptyView){
        		mStandardEmptyView.setText("");
        	}
            setContentShown(false);
        }
    }

    public void setContentShownNoAnimation(boolean shown) {
        setContentShown(shown, false);
    }
}
