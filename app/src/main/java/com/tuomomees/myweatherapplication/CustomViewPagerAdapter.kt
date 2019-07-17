package com.tuomomees.myweatherapplication

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class CustomViewPagerAdapter internal constructor(fm: FragmentManager, private val fragmentList: List<Fragment>) :
    FragmentPagerAdapter(fm) {

    /*
    Easy to use viewpager adapter which takes fragments as a list and then loads them as pages
     */

    override fun getCount(): Int {
        // Returns the number of tabs
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {

        return fragmentList[position]
    }
}