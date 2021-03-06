package com.jald.reserve.widget.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ListView;

import com.jald.reserve.widget.pullrefresh.ILoadingLayout.State;

public class PullToRefreshListView extends PullToRefreshBase<ListView> implements OnScrollListener {

	private ListView mListView;
	private LoadingLayout mLoadMoreFooterLayout;
	private OnScrollListener mScrollListener;

	public PullToRefreshListView(Context context) {
		this(context, null);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setPullLoadEnabled(false);
	}

	@Override
	protected ListView createRefreshableView(Context context, AttributeSet attrs) {
		ListView listView = new ListView(context);
		mListView = listView;
		listView.setOnScrollListener(this);
		return listView;
	}

	public void setHasMoreData(boolean hasMoreData) {
		if (!hasMoreData) {
			if (mLoadMoreFooterLayout != null) {
				mLoadMoreFooterLayout.setState(State.NO_MORE_DATA);
			}
			LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
			if (null != footerLoadingLayout) {
				footerLoadingLayout.setState(State.NO_MORE_DATA);
			}
		}
	}

	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	protected boolean isReadyForPullDown() {
		return isFirstItemVisible();
	}

	@Override
	protected boolean isReadyForPullUp() {
		return isLastItemVisible();
	}

	@Override
	protected void startLoading() {
		super.startLoading();
		if (mLoadMoreFooterLayout != null) {
			mLoadMoreFooterLayout.setState(State.REFRESHING);
		}
	}

	@Override
	public void onPullUpRefreshComplete() {
		super.onPullUpRefreshComplete();
		if (mLoadMoreFooterLayout != null) {
			mLoadMoreFooterLayout.setState(State.RESET);
		}
	}

	@Override
	public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
		if (isScrollLoadEnabled() == scrollLoadEnabled) {
			return;
		}
		super.setScrollLoadEnabled(scrollLoadEnabled);
		if (scrollLoadEnabled) {
			if (mLoadMoreFooterLayout == null) {
				mLoadMoreFooterLayout = new FooterLoadingLayout(getContext());
				mListView.addFooterView(mLoadMoreFooterLayout, null, false);
			}
			mLoadMoreFooterLayout.show(true);
		} else {
			if (mLoadMoreFooterLayout != null) {
				mLoadMoreFooterLayout.show(false);
			}
		}
	}

	@Override
	public LoadingLayout getFooterLoadingLayout() {
		if (isScrollLoadEnabled()) {
			return mLoadMoreFooterLayout;
		}
		return super.getFooterLoadingLayout();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (isScrollLoadEnabled() && hasMoreData()) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				if (isReadyForPullUp()) {
					startLoading();
				}
			}
		}
		if (null != mScrollListener) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (null != mScrollListener) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	protected LoadingLayout createHeaderLoadingLayout(Context context, AttributeSet attrs) {
		return new RotateLoadingLayout(context);
	}

	private boolean hasMoreData() {
		if ((null != mLoadMoreFooterLayout) && (mLoadMoreFooterLayout.getState() == State.NO_MORE_DATA)) {
			return false;
		}
		return true;
	}

	private boolean isFirstItemVisible() {
		final Adapter adapter = mListView.getAdapter();
		if (null == adapter || adapter.isEmpty()) {
			return true;
		}
		int mostTop = (mListView.getChildCount() > 0) ? mListView.getChildAt(0).getTop() : 0;
		if (mostTop >= 0) {
			return true;
		}
		return false;
	}

	private boolean isLastItemVisible() {
		final Adapter adapter = mListView.getAdapter();
		if (null == adapter || adapter.isEmpty()) {
			return true;
		}
		final int lastItemPosition = adapter.getCount() - 1;
		final int lastVisiblePosition = mListView.getLastVisiblePosition();
		if (lastVisiblePosition >= lastItemPosition - 1) {
			final int childIndex = lastVisiblePosition - mListView.getFirstVisiblePosition();
			final int childCount = mListView.getChildCount();
			final int index = Math.min(childIndex, childCount - 1);
			final View lastVisibleChild = mListView.getChildAt(index);
			if (lastVisibleChild != null) {
				return lastVisibleChild.getBottom() <= mListView.getBottom();
			}
		}
		return false;
	}
}
