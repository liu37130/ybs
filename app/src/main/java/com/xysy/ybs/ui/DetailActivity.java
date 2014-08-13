package com.xysy.ybs.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.xysy.ybs.R;
import com.xysy.ybs.data.DataHelper.JobsContract;
import com.xysy.ybs.data.DataProvider;
import com.xysy.ybs.tools.Logger;

public class DetailActivity extends Activity {

    private WebView mWebView;
    private WebChromeClient mChromeClient;
    private WebViewClient mViewClient;
    private ProgressBar mProgressBar;
    private String mUrl;
    private String mStarState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle args = getIntent().getExtras();
        mUrl = args.getString("url");
        new GetJobStarStateTask().execute(mUrl);

        mProgressBar = (ProgressBar)findViewById(R.id.progressbar);
        mWebView = (WebView)findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        mViewClient = new MyWebViewClient();
        mChromeClient = new MyChromeClient();
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(mViewClient);
        mWebView.setWebChromeClient(mChromeClient);
        mWebView.loadUrl(mUrl);

        ImageButton mGoBack = (ImageButton)findViewById(R.id.go_back);
        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.goBack();
            }
        });
        ImageButton mGoForward = (ImageButton)findViewById(R.id.go_forward);
        mGoForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.goForward();
            }
        });
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mStarState != null && mStarState.equals(JobsContract.FAVORITE_JOB)) {
            MenuItem item = menu.findItem(R.id.action_star);
            item.setIcon(R.drawable.ic_action_star);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorites:
                startActivity(new Intent(DetailActivity.this, FavoriteActivity.class));
                return true;
            case R.id.action_star:
                setStarState();
                return true;
            case R.id.action_browser:
                Intent toBrowser = new Intent(Intent.ACTION_VIEW);
                toBrowser.setData(Uri.parse(mUrl));
                startActivity(toBrowser);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void setStarState() {
        if (mStarState.equals(JobsContract.FAVORITE_JOB)) {
            new SetJobStarStateTask().execute(JobsContract.NORMAL_JOB);
        } else if (mStarState.equals(JobsContract.NORMAL_JOB)) {
            new SetJobStarStateTask().execute(JobsContract.FAVORITE_JOB);
        }
    }

    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    public class MyChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 0) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            if (newProgress >= 100) {
                mProgressBar.setVisibility(View.GONE);
            }
            mProgressBar.setProgress(newProgress);
        }
    }

    private class GetJobStarStateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            String url = args[0];
            Cursor cursor = getContentResolver().query(DataProvider.JOBS_URI,
                    new String[] {JobsContract.STAR_STATE}, JobsContract.URL + "=?",
                    new String[] {url}, null);
            cursor.moveToNext();
            return cursor.getString(0);
        }

        @Override
        protected void onPostExecute(String result) {
            mStarState = result;
            invalidateOptionsMenu();
        }
    }

    private class SetJobStarStateTask extends AsyncTask<String, Void,Void> {
        @Override
        protected Void doInBackground(String... args) {
            String state = args[0];
            ContentValues values = new ContentValues();
            values.put(JobsContract.STAR_STATE, state);
            getContentResolver().update(DataProvider.JOBS_URI, values,
                    JobsContract.URL + "=?", new String[] {mUrl});
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.option_successful),
                    Toast.LENGTH_SHORT).show();
            new GetJobStarStateTask().execute(mUrl);
        }
    }

}