package com.xysy.ybs.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xysy.ybs.R;
import com.xysy.ybs.ui.adapter.DrawerAdapter;

public class DrawerFragment extends Fragment {

    private ListView mListView;
    private DrawerAdapter mAdapter;
    private String[] mCities;
    private OnCitySelectedListener mListener;

    public static DrawerFragment newInstance(String[] cities) {
        DrawerFragment fragment = new DrawerFragment();
        Bundle args = new Bundle();
        args.putStringArray("cities", cities);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCities = getArguments().getStringArray("cities");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_drawer, container, false);
        mListView = (ListView)view.findViewById(R.id.drawer_list);
        mAdapter = new DrawerAdapter(getActivity(), mListView, mCities);
        mListView.setAdapter(mAdapter);
        mListView.setItemChecked(0, true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mListView.setItemChecked(position, true);
                mListener.onCitySelected(position);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCitySelectedListener)activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new ClassCastException(activity.toString()
                    + "must implement OnCitySelectedListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnCitySelectedListener {
        public void onCitySelected(int position);
    }
}
