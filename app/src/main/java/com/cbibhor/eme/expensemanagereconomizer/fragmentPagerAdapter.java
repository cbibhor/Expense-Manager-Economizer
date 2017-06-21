package com.cbibhor.eme.expensemanagereconomizer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bibhor Chauhan on 13-05-2017.
 */

public class fragmentPagerAdapter extends FragmentPagerAdapter {
    private List<String> tabTitles = new ArrayList<>();
    private Context context;

    public fragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(position, tabTitles);
    }

    @Override
    public int getCount() {
        return tabTitles.size();
    }

    public void addFragmentTitle(String month, String year){
        tabTitles.add(month+", "+year);
    }
}
