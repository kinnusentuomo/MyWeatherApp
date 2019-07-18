package com.tuomomees.myweatherapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class CustomViewPagerAdapter internal constructor(fm: androidx.fragment.app.FragmentManager, private val fragmentList: List<androidx.fragment.app.Fragment>) :
    androidx.fragment.app.FragmentPagerAdapter(fm) {

    /*
    Easy to use viewpager adapter which takes fragments as a list and then loads them as pages
     */

    override fun getCount(): Int {
        // Returns the number of tabs
        return fragmentList.size
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment {

        return fragmentList[position]
    }
}