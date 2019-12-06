package com.cs407.team15.redstone.ui.publicboard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cs407.team15.redstone.R;

public class PbTabsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES =
            new int[] { R.string.pb_tab_text_1, R.string.pb_tab_text_2};
    private final Context mContext;
    private String location;

    public PbTabsPagerAdapter(Context context, FragmentManager fm, String location) {
        super(fm);
        mContext = context;
        this.location = location;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("area", location);
        switch (position) {
            case 0:
                PostingFragment postingFragment = new PostingFragment();
                postingFragment.setArguments(bundle);
                return postingFragment;
            case 1:
                PbAdsFragment adsfragment = new PbAdsFragment();
                adsfragment.setArguments(bundle);
                return adsfragment;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }

}
