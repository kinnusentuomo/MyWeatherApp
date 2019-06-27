package com.tuomomees.myweatherapplication

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
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
}