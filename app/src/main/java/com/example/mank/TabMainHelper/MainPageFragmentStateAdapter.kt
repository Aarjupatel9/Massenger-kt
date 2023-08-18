package com.example.mank.TabMainHelper

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mank.R

class MainPageFragmentStateAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun createFragment(position: Int): Fragment {
        if (position == 1) {
            return StatusPageFragment.newInstance(position + 1)
        }
        return if (position == 2) {
            CallsPageFragment.newInstance(position + 1)
        } else PlaceholderFragment.newInstance(position + 1)
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        return mContext.resources.getString(TAB_TITLES[position])
//    }

    override fun getItemCount(): Int {
        // Show 2 total pages.
        return 3
    }

}

//package com.example.mank.TabMainHelper
//
//import android.content.Context
//import androidx.annotation.StringRes
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.FragmentPagerAdapter
//import com.example.mank.R
//
//class MainPageFragmentStateAdapter(private val mContext: Context, fm: FragmentManager?) :
//    FragmentPagerAdapter(
//        fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
//    ) {
//    override fun getItem(position: Int): Fragment {
//        if (position == 1) {
//            return StatusPageFragment.newInstance(position + 1)
//        }
//        return if (position == 2) {
//            CallsPageFragment.newInstance(position + 1)
//        } else PlaceholderFragment.newInstance(position + 1)
//    }
//
//    override fun getPageTitle(position: Int): CharSequence? {
//        return mContext.resources.getString(TAB_TITLES[position])
//    }
//
//    override fun getCount(): Int {
//        // Show 2 total pages.
//        return 3
//    }
//
//    companion object {
//        @StringRes
//        private val TAB_TITLES =
//            intArrayOf(R.string.tab_name_1, R.string.tab_name_2, R.string.tab_name_3)
//    }
//}