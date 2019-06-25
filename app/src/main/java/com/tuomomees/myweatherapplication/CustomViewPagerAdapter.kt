package com.tuomomees.myweatherapplication

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.app.FragmentPagerAdapter


class CustomViewPagerAdapter internal constructor(fm: FragmentManager, private val fragmentList: List<Fragment>) :
    FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        // Returns the number of tabs
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {

        return fragmentList[position]
    }

    /*
    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                //return getString(R.string.title_section1).toUpperCase(l);
                return "test";
            case 1:
                //return getString(R.string.title_section2).toUpperCase(l);
                return "test";
            case 2:
                //return getString(R.string.title_section3).toUpperCase(l);
                return "test";
        }
        return null;
    }*/

}