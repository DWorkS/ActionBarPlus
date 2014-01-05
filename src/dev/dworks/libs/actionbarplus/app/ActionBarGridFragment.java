package dev.dworks.libs.actionbarplus.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import dev.dworks.libs.actionbarplus.ActionBarFragment;
import dev.dworks.libs.actionbarplus.R;

public class ActionBarGridFragment extends ActionBarFragment {
	private ListAdapter mAdapter;
    private CharSequence mEmptyText;
    private View mEmptyView;
    final private Handler mHandler = new Handler();
    private GridView mGrid;
    private View mGridContainer;
    private boolean mGridShown;
    final private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((GridView) parent, v, position, id);
        }
    };
    private View mProgressContainer;
    final private Runnable mRequestFocus = new Runnable() {
        @Override
        public void run() {
            mGrid.focusableViewAvailable(mGrid);
        }
    };
    private TextView mStandardEmptyView;

    private void ensureGrid() {
        if (mGrid != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof GridView) {
            mGrid = (GridView) root;
        } else {
            mStandardEmptyView = (TextView) root
                    .findViewById(R.id.internalEmpty);
            if (mStandardEmptyView == null) {
                mEmptyView = root.findViewById(android.R.id.empty);
            } else {
                mStandardEmptyView.setVisibility(View.GONE);
            }
            mProgressContainer = root.findViewById(R.id.progressContainer);
            mGridContainer = root.findViewById(R.id.listContainer);
            View rawGridView = root.findViewById(R.id.grid);
            if (rawGridView == null) {
                throw new RuntimeException(
                        "Your content must have a GridView whose id attribute is "
                                + "'R.id.grid'");
            }
            else{
            	try {
                	@SuppressWarnings("unused")
                	GridView grid = (GridView) rawGridView;	
				} catch (Exception e) {
		               throw new RuntimeException(
		                        "Content has view with id attribute 'R.id.grid' "
		                                + "that is not a GridView class");
				}
            }
            mGrid = (GridView) rawGridView;
            if (mEmptyView != null) {
                mGrid.setEmptyView(mEmptyView);
            } else if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
                mGrid.setEmptyView(mStandardEmptyView);
            }
        }
        mGridShown = true;
        mGrid.setOnItemClickListener(mOnClickListener);
        if (mAdapter != null) {
            ListAdapter adapter = mAdapter;
            mAdapter = null;
            setGridAdapter(adapter);
        } else {
            if (mProgressContainer != null) {
                setGridShown(false, false);
            }
        }
        mHandler.post(mRequestFocus);
    }

    protected View getEmptyView() {
        return mEmptyView;
    }

    public ListAdapter getGridAdapter() {
        return mAdapter;
    }

    public GridView getGridView() {
        ensureGrid();
        return mGrid;
    }

    public long getSelectedItemId() {
        ensureGrid();
        return mGrid.getSelectedItemId();
    }

    public int getSelectedItemPosition() {
        ensureGrid();
        return mGrid.getSelectedItemPosition();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.abp_grid_content, container, false);
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mGrid = null;
        mGridShown = false;
        mEmptyView = mProgressContainer = mGridContainer = null;
        mStandardEmptyView = null;
        super.onDestroyView();
    }

    public void onListItemClick(GridView l, View v, int position, long id) {
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	ensureGrid();
    }

    public void setEmptyText(CharSequence text) {
        ensureGrid();
        if (mStandardEmptyView == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        mStandardEmptyView.setText(text);
        if (mEmptyText == null) {
            mGrid.setEmptyView(mStandardEmptyView);
        }
        mEmptyText = text;
    }

    public void setGridAdapter(ListAdapter adapter) {
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mGrid != null) {
            mGrid.setAdapter(adapter);
            if (!mGridShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter. It is now time to show it.
                setGridShown(true, getView().getWindowToken() != null);
            }
        }
    }

    public void setGridShown(boolean shown) {
        setGridShown(shown, true);
    }

    private void setGridShown(boolean shown, boolean animate) {
        ensureGrid();
        if (mProgressContainer == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        if (mGridShown == shown) {
            return;
        }
        mGridShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mGridContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mGridContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mGridContainer.setVisibility(View.VISIBLE);
        } else {
        	if(null != mStandardEmptyView){
        		mStandardEmptyView.setText("");
        	}
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mGridContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mGridContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mGridContainer.setVisibility(View.GONE);
        }
    }

    public void setGridShownNoAnimation(boolean shown) {
        setGridShown(shown, false);
    }

    public void setSelection(int position) {
        ensureGrid();
        mGrid.setSelection(position);
    }
}
