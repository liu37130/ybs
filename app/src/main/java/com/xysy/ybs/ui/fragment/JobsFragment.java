package com.xysy.ybs.ui.fragment;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.xysy.ybs.R;
import com.xysy.ybs.YApp;
import com.xysy.ybs.data.DataHelper;
import com.xysy.ybs.data.DataHelper.JobsContract;
import com.xysy.ybs.data.DataProvider;
import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.tools.RequestCenter;
import com.xysy.ybs.type.JobInfo;
import com.xysy.ybs.type.JobRequestCarrier;
import com.xysy.ybs.ui.DetailActivity;
import com.xysy.ybs.ui.adapter.JobAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JobsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class JobsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView mJobList;
    private SwipeRefreshLayout mRefreshLayout;
    private ImageButton mRefreshBtn;
    private JobAdapter mAdapter;
    private String mCityName;
    private View mFooter;

    private int mPage = 1;

    private boolean noMoreData;
    private boolean isLoading;
    private boolean netError;

    public static JobsFragment newInstance(String cityName) {
        Logger.i("[JobsFragment] get a new instance");
        JobsFragment fragment = new JobsFragment();
        Bundle args = new Bundle();
        args.putString("cityName", cityName);
        fragment.setArguments(args);
        return fragment;
    }
    public JobsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCityName = getArguments().getString("cityName");
            Logger.i("[JobsFragment] onCreate() city name is " + mCityName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_jobs, container, false);
        mRefreshBtn = (ImageButton)rootView.findViewById(R.id.btn_refresh);
        mRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLoading) {
                    refresh();
                }
            }
        });
        mRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light,
                android.R.color.holo_red_light);
        setSwipeRefreshDistance(130);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFirstPage();
            }
        });
        mFooter = LayoutInflater.from(YApp.getContext())
                .inflate(R.layout.no_more_data_footer, null);
        mFooter.setVisibility(View.GONE);
        mJobList = (ListView)rootView.findViewById(R.id.job_list);
        mJobList.addFooterView(mFooter);
        mJobList.setFooterDividersEnabled(false);
        mAdapter = new JobAdapter(getActivity(), false);
        mJobList.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        mJobList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

                Logger.i("isLoading " + isLoading);
                Logger.i("[JobsFragment] onLoadFinished onScroll list first visible  " + firstVisibleItem
                        + "visible count" + visibleItemCount + "total num " + totalItemCount);
                if (isLoading || noMoreData) {
                    return;
                }

                if (firstVisibleItem + visibleItemCount >= totalItemCount
                        && totalItemCount != 0
                        && totalItemCount != mJobList.getHeaderViewsCount() + mJobList.getFooterViewsCount()
                        && mAdapter.getCount() > 0) {
                    setRefreshing(true);
                    loadNextPage();
                }
            }
        });
        mJobList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                JobInfo job = mAdapter.getItem(position - mJobList.getHeaderViewsCount());
                Intent showDetail = new Intent(getActivity(), DetailActivity.class);
                showDetail.putExtra("url", job.getUrl());
                startActivity(showDetail);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mCityName);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mCityName);
    }

    @Override
    public void onStop() {
        super.onStop();
        RequestCenter.getCenter().cancelAll(this);
        setRefreshing(false);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[] {
                JobsContract.ID,
                JobsContract.JOB_TITLE,
                JobsContract.COMPANY,
                JobsContract.CITY,
                JobsContract.SALARY,
                JobsContract.URL,
                JobsContract.AVATAR_URL,
                JobsContract.DATE
        };
        return new CursorLoader(getActivity().getApplicationContext(), DataProvider.JOBS_URI,
                projection, JobsContract.CITY + " LIKE ? and " + JobsContract.STAR_STATE + "=?",
                new String[] { "%"+ mCityName +"%", JobsContract.NORMAL_JOB},
                DataHelper.JobsContract.ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)  {
        mAdapter.swapCursor(data);
        if (data != null && data.getCount() == 0
                && !noMoreData
                && !isLoading
                && !netError) {
            loadFirstPage();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void loadFirstPage() {
        Logger.i("[JobsFragment] loadFirstPage");
        loadData(1);
    }

    private void loadNextPage() {
        Logger.i("[JobsFragment] loadNextPage");
        loadData(mPage + 1);
    }

    public void refresh() {
        mJobList.smoothScrollToPosition(0);
        loadFirstPage();
    }

    private void setSwipeRefreshDistance(final int distance) {
        ViewTreeObserver observer = mRefreshLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                Float mDistanceToTriggerSync = Math.min(
                        ((View)mRefreshLayout.getParent()).getHeight() * 0.6f,
                        distance * metrics.density);

                try {
                    Field field = SwipeRefreshLayout.class.getDeclaredField("mDistanceToTriggerSync");
                    field.setAccessible(true);
                    field.setFloat(mRefreshLayout, mDistanceToTriggerSync);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mRefreshLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mRefreshLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private void loadData(final int page) {
        setRefreshing(true);
        JobRequestCarrier carrier = new JobRequestCarrier(
                PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_key_job_keywords", ""),
                mCityName, page,
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        setRefreshing(false);
                        switch (msg.what) {
                            case RequestCenter.NORMAL:
                                Bundle args = msg.getData();
                                mPage = args.getInt("page");
                                ArrayList<String> errorList = args.getStringArrayList("errorlist");
                                if (errorList.size() != 0) {
                                    netError = true;
                                    String errorMsg = "";
                                    for (String error : errorList) {
                                        errorMsg = errorMsg + error + " ";
                                    }
                                    errorMsg = errorMsg + getResources().getString(R.string.error_message);
                                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case RequestCenter.NO_MORE_DATA:
                                Logger.i("NO MORE DATA");
                                mFooter.setVisibility(View.VISIBLE);
                                noMoreData = true;
                                break;
                            case RequestCenter.NO_NETWORK:
                                Toast.makeText(getActivity(),
                                        getResources().getString(R.string.no_network),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            default:
                        }
                    }
                });

        RequestCenter.getCenter().getJobInfo(carrier, this);
    }

    private void setRefreshing(boolean state) {
        if (state) {
            isLoading = true;
            if (!mRefreshLayout.isRefreshing()) {
                mRefreshLayout.setRefreshing(true);
            }
        } else {
            isLoading  =false;
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.setRefreshing(false);
            }
        }
    }
}
