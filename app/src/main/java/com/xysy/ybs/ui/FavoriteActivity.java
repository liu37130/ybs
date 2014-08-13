package com.xysy.ybs.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.umeng.analytics.MobclickAgent;
import com.xysy.ybs.R;
import com.xysy.ybs.YApp;
import com.xysy.ybs.data.DataHelper;
import com.xysy.ybs.data.DataHelper.JobsContract;
import com.xysy.ybs.data.DataProvider;
import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.type.JobInfo;
import com.xysy.ybs.ui.adapter.JobAdapter;

import java.util.ArrayList;

public class FavoriteActivity extends Activity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private MultiChoiceCallBack mActionModeCallback;
    private ListView mList;
    private JobAdapter mAdapter;

    /* 异步删除喜欢的工作时，每删除一条都会调用一次MultiChoiceCallBack的onItemCheckedStateChanged()，
     * 会在正在遍历ArrayList时更改List内容，造成ConcurrentModificationException
     * 这个标志位用来屏蔽正在删除时的onItemCheckedStateChanged()调用
     */
    private boolean isDeleting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mActionModeCallback = new MultiChoiceCallBack();

        mList = (ListView)findViewById(R.id.favorite_list);
        mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mAdapter = new JobAdapter(YApp.getContext(), true);
        getLoaderManager().initLoader(1, null, this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent toDetail = new Intent(FavoriteActivity.this, DetailActivity.class);
                toDetail.putExtra("url", mAdapter.getItem(position).getUrl());
                startActivity(toDetail);
            }
        });

        mList.setMultiChoiceModeListener(mActionModeCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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
        return new CursorLoader(YApp.getContext(), DataProvider.JOBS_URI,
                projection, JobsContract.STAR_STATE + "=?",
                new String[] { JobsContract.FAVORITE_JOB },
                DataHelper.JobsContract.ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (data != null && data.getCount() == 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.favorite, new HolderFragment()).commit();
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private class MultiChoiceCallBack implements AbsListView.MultiChoiceModeListener {
        private int selectedNum;

        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
            if (position >= mAdapter.getCount() || isDeleting) {
                return;
            }
            if (checked) {
                mAdapter.setSelected(mAdapter.getItem(position).getUrl(), true);
                selectedNum++;
            } else {
                mAdapter.setSelected(mAdapter.getItem(position).getUrl(), false);
                selectedNum--;
            }
            actionMode.setTitle(getResources().getString(R.string.choice_title_head)
                    + String.valueOf(selectedNum) + getResources().getString(R.string.choice_title_end));
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete:
                    new DeleteFavoriteTask().execute(mAdapter.getItemState());
                    break;
                default:
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mAdapter.clearSelected();
            selectedNum = 0;
        }
    }

    public static class HolderFragment extends Fragment {
        public HolderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.no_favorite_job_holder, viewGroup, false);
        }
    }

    private class DeleteFavoriteTask extends AsyncTask<ArrayList<String>, Void, Void> {
        @Override
        protected Void doInBackground(ArrayList<String>... args) {
            isDeleting = true;
            //ArrayList<String> items = (ArrayList<String>)args[0].clone();
            ContentValues values = new ContentValues();
            values.put(JobsContract.STAR_STATE, JobsContract.NORMAL_JOB);
            ContentResolver resolver = getContentResolver();

            for (String url : args[0]) {
                resolver.update(DataProvider.JOBS_URI,
                        values, JobsContract.URL + "=?", new String[] {url});
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            isDeleting = false;
        }
    }
}
