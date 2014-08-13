package com.xysy.ybs.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.xysy.ybs.R;
import com.xysy.ybs.data.DataHelper;
import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.tools.RequestCenter;
import com.xysy.ybs.type.JobInfo;

import java.util.ArrayList;


public class JobAdapter extends CursorAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private boolean multiChoice;
    private ArrayList<String> mItemState;

    public JobAdapter(Context context, boolean multiChoice) {
        super(context, null, false);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.multiChoice = multiChoice;
        if (this.multiChoice) {
            mItemState = new ArrayList<String>();
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return mInflater.inflate(R.layout.job_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = getViewHolder(view);
        if (holder.avatarRequest != null) {
            holder.avatarRequest.cancelRequest();
        }

        holder.jobTitle.setText(cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.JOB_TITLE)));
        holder.company.setText(cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.COMPANY)));
        holder.city.setText(cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.CITY)));
        holder.salary.setText(cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.SALARY)));
        holder.date.setText(cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.DATE)));
        if (cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.AVATAR_URL)) != null) {
            holder.avatarRequest = RequestCenter.getCenter().loadImage(
                    cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.AVATAR_URL)),
                    holder.avatar, R.drawable.default_avatar, R.drawable.default_avatar);
        } else {
            int random = (int)(Math.random() * 5);
            Bitmap avatar = BitmapFactory.decodeResource(mContext.getResources(),
                    getColorfulDefaultAvatar(random));
            holder.avatar.setImageBitmap(avatar);
        }

        if (multiChoice) {
            String url = cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.URL));
            if (mItemState.indexOf(url) != -1) {
                view.setBackgroundColor(mContext.getResources().getColor(R.color.ybs_transparent_green));
            } else {
                view.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
        }

    }

    @Override
    public JobInfo getItem(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return new JobInfo(
                cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.JOB_TITLE)),
                cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.COMPANY)),
                cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.CITY)),
                cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.SALARY)),
                cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.URL)),
                cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.AVATAR_URL)),
                cursor.getString(cursor.getColumnIndex(DataHelper.JobsContract.DATE)));
    }

    private ViewHolder getViewHolder(final View view) {
        ViewHolder holder = (ViewHolder)view.getTag();
        if (holder == null) {
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        return holder;
    }

    public void setSelected(String url, boolean selected) {
        if (multiChoice) {

            if (selected) {
                mItemState.add(url);
            } else {
                mItemState.remove(url);
            }
            notifyDataSetChanged();
        }
    }

    public void clearSelected() {
        if (multiChoice) {
            mItemState.clear();
            notifyDataSetChanged();
        }
    }

    public ArrayList<String> getItemState() {
        if (multiChoice) {
            return mItemState;
        }
        return null;
    }

    private int getColorfulDefaultAvatar(int random) {
        switch (random) {
            case 0:
                return R.drawable.default_avatar_0;
            case 1:
                return R.drawable.default_avatar_1;
            case 2:
                return R.drawable.default_avatar_2;
            case 3:
                return R.drawable.default_avatar_3;
            case 4:
                return R.drawable.default_avatar_4;
            default:
                return R.drawable.default_avatar;
        }
    }

    private class ViewHolder {
        public ImageView avatar;
        public TextView jobTitle;
        public TextView company;
        public TextView city;
        public TextView salary;
        public TextView date;

        public ImageLoader.ImageContainer avatarRequest;

        public ViewHolder(View itemView) {
            avatar = (ImageView)itemView.findViewById(R.id.avatar);
            jobTitle = (TextView)itemView.findViewById(R.id.job_title);
            company = (TextView)itemView.findViewById(R.id.company);
            city = (TextView)itemView.findViewById(R.id.city);
            salary = (TextView)itemView.findViewById(R.id.salary);
            date = (TextView)itemView.findViewById(R.id.date);
        }
    }

}
