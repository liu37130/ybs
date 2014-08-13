package com.xysy.ybs.ui.adapter;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xysy.ybs.R;

public class DrawerAdapter extends BaseAdapter {

    private ListView mListView;
    private String[] mContent;
    private Context mContext;

    public DrawerAdapter(Context context, ListView listView, String[] content) {
        mContext = context;
        mListView = listView;
        mContent = content;
    }
    @Override
    public int getCount() {
        return mContent.length;
    }

    @Override
    public String getItem(int position) {
        return mContent[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null ) {
            view = LayoutInflater.from(mContext).inflate(R.layout.drawer_list_item, viewGroup, false);
        }

        TextView tv = (TextView)view.findViewById(R.id.drawer_list_item);
        tv.setText(mContent[position]);


        if (mListView.isItemChecked(position)) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.ybs_transparent_green));
        } else {
            view.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }

        return view;
    }

}
