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

import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import dev.dworks.libs.actionbarplus.R;
import dev.dworks.libs.actionbarplus.widget.PinnedSectionGridView.PinnedSectionGridAdapter;

public class SimpleSectionedGridAdapter extends BaseAdapter implements PinnedSectionGridAdapter{
	protected static final int TYPE_FILLER = 0;
	protected static final int TYPE_HEADER = 1;
	protected static final int TYPE_HEADER_FILLER = 2;
    private boolean mValid = true;
    private int mSectionResourceId;
    private LayoutInflater mLayoutInflater;
    private ListAdapter mBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<Section>();
	private Context mContext;
//	private View mLastHeaderViewSeen;
	private View mLastViewSeen;
	private int mNumColumns;
	private int mWidth;
	private int mColumnWidth;
	private int mHorizontalSpacing;
	private int mStrechMode;
	private int requestedColumnWidth;
	private int requestedHorizontalSpacing;
	private GridView mGridView;

    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;
        int type = 0;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public CharSequence getTitle() {
            return title;
        }
    }

    public SimpleSectionedGridAdapter(Context context, int sectionResourceId, BaseAdapter baseAdapter) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSectionResourceId = sectionResourceId;
        mBaseAdapter = baseAdapter;
        mContext = context;
        mBaseAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mValid = !mBaseAdapter.isEmpty();
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                mValid = false;
                notifyDataSetInvalidated();
            }
        });
    }
    
    public void setGridView(GridView gridView){
    	if(!(gridView instanceof PinnedSectionGridView)){
    		throw new IllegalArgumentException("Does your grid view extends PinnedSectionGridView?");
    	}
    	mGridView = gridView;
    	mStrechMode = gridView.getStretchMode();
    	mWidth = gridView.getWidth() - (mGridView.getPaddingLeft() + mGridView.getPaddingRight());
        mNumColumns = ((PinnedSectionGridView)gridView).getNumColumns();
        requestedColumnWidth = ((PinnedSectionGridView)gridView).getColumnWidth();
    	requestedHorizontalSpacing = ((PinnedSectionGridView)gridView).getHorizontalSpacing();
    }
    
    private int getHeaderSize(){
    	if(mWidth != mGridView.getWidth()){
	    	mStrechMode = mGridView.getStretchMode();
	    	mWidth = mGridView.getWidth() - (mGridView.getPaddingLeft() + mGridView.getPaddingRight());
	        mNumColumns = ((PinnedSectionGridView)mGridView).getNumColumns();
	        requestedColumnWidth = ((PinnedSectionGridView)mGridView).getColumnWidth();
	    	requestedHorizontalSpacing = ((PinnedSectionGridView)mGridView).getHorizontalSpacing();
    	}
    	
        int spaceLeftOver = mWidth - (mNumColumns * requestedColumnWidth) -
                ((mNumColumns - 1) * requestedHorizontalSpacing);
        switch (mStrechMode) {
        case GridView.NO_STRETCH:            // Nobody stretches
        	mWidth -= spaceLeftOver;
            mColumnWidth = requestedColumnWidth;
            mHorizontalSpacing = requestedHorizontalSpacing;
            break;

        case GridView.STRETCH_COLUMN_WIDTH:
            mColumnWidth = requestedColumnWidth + spaceLeftOver / mNumColumns;
            mHorizontalSpacing = requestedHorizontalSpacing;
            break;

        case GridView.STRETCH_SPACING:
            mColumnWidth = requestedColumnWidth;
            if (mNumColumns > 1) {
                mHorizontalSpacing = requestedHorizontalSpacing + 
                    spaceLeftOver / (mNumColumns - 1);
            } else {
                mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
            }
            break;

        case GridView.STRETCH_SPACING_UNIFORM:
            mColumnWidth = requestedColumnWidth;
            mHorizontalSpacing = requestedHorizontalSpacing;
        	mWidth = mWidth - spaceLeftOver + (2 * mHorizontalSpacing);
            break;
        }
		return mWidth + ((mNumColumns - 1) * (mColumnWidth + mHorizontalSpacing)) ;
    }

    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (int i = 0; i < sections.length; i++) {
			Section section = sections[i];
    		Section sectionAdd;
 
        	for (int j = 0; j < mNumColumns - 1; j++) {
        		sectionAdd = new Section(section.firstPosition, section.title);
        		sectionAdd.type = TYPE_HEADER_FILLER;
        		sectionAdd.sectionedPosition = sectionAdd.firstPosition + offset;
                mSections.append(sectionAdd.sectionedPosition, sectionAdd);
                ++offset;
			}
    		
    		sectionAdd = new Section(section.firstPosition, section.title);
    		sectionAdd.type = TYPE_HEADER;
    		sectionAdd.sectionedPosition = sectionAdd.firstPosition + offset;
            mSections.append(sectionAdd.sectionedPosition, sectionAdd);
            ++offset;
        	
            if(i+1 < sections.length){
            	int nextPos = sections[i+1].firstPosition;
            	int itemsCount = nextPos - section.firstPosition;
            	int dummyCount = mNumColumns - (itemsCount % mNumColumns);
            	if(mNumColumns != dummyCount){
	            	for (int j = 0 ;j < dummyCount; j++) {
	                	sectionAdd = new Section(section.firstPosition, section.title);
	            		sectionAdd.type = TYPE_FILLER;
	            		sectionAdd.sectionedPosition = nextPos + offset;
	            		mSections.append(sectionAdd.sectionedPosition, sectionAdd);
	            		++offset;
					}
            	}
            }
		}

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return ListView.INVALID_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }

    @Override
    public int getCount() {
        return (mValid ? mBaseAdapter.getCount() + mSections.size() : 0);
    }

    @Override
    public Object getItem(int position) {
        return isSectionHeaderPosition(position)
                ? mSections.get(position)
                : mBaseAdapter.getItem(sectionedPositionToPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? getViewTypeCount() - 1
                : mBaseAdapter.getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableConditionalExpression
        return isSectionHeaderPosition(position)
                ? false
                : mBaseAdapter.isEnabled(sectionedPositionToPosition(position));
    }

    @Override
    public int getViewTypeCount() {
        return mBaseAdapter.getViewTypeCount() + 1; // the section headings
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return mBaseAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mBaseAdapter.isEmpty();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isSectionHeaderPosition(position)) {
        	HeaderLayout header;
        	TextView view;
        	if(null == convertView){
        		convertView = mLayoutInflater.inflate(mSectionResourceId, parent, false);
        	}
        	else{
        		if(null == convertView.findViewById(R.id.header_layout)){
        			convertView = mLayoutInflater.inflate(mSectionResourceId, parent, false);	
        		}
        	}
			switch (mSections.get(position).type) {
			case TYPE_HEADER:
				header = (HeaderLayout) convertView.findViewById(R.id.header_layout);
				view = (TextView) convertView.findViewById(R.id.header);
	            view.setText(mSections.get(position).title);
	            header.setHeaderWidth(getHeaderSize());
	            //view.setBackgroundColor(Color.BLUE);
	            break;
			case TYPE_HEADER_FILLER:
				header = (HeaderLayout) convertView.findViewById(R.id.header_layout);
				view = (TextView) convertView.findViewById(R.id.header);
	            view.setText("");
	            header.setHeaderWidth(0);
				break;
			default:
				convertView = getFillerView(convertView, parent, mLastViewSeen);
				break;
			}
        } else {
            convertView = mBaseAdapter.getView(sectionedPositionToPosition(position), convertView, parent);
        	mLastViewSeen = convertView; 
        }
        return convertView;
    }
    
    private FillerView getFillerView(View convertView, ViewGroup parent, View lastViewSeen) {
        FillerView fillerView = null;
        if (fillerView == null) {
            fillerView = new FillerView(mContext);
        }
        fillerView.setMeasureTarget(lastViewSeen);
        return fillerView;
    }

	@Override
	public boolean isItemViewTypePinned(int position) {
		Section section = mSections.get(position); 
		return isSectionHeaderPosition(position) && section.type == TYPE_HEADER;
	}

	public static class ViewHolder {
		@SuppressWarnings("unchecked")
		public static <T extends View> T get(View view, int id) {
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
			if (viewHolder == null) {
				viewHolder = new SparseArray<View>();
				view.setTag(viewHolder);
			}
			View childView = viewHolder.get(id);
			if (childView == null) {
				childView = view.findViewById(id);
				viewHolder.put(id, childView);
			}
			return (T) childView;
		}
	}
}