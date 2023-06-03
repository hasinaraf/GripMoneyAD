package com.example.gripmoney.Home.HomeExpenses

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class HomeExpensesFragmentAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    var fragmentList : ArrayList<Fragment> = ArrayList()
    var fragmenttitle : ArrayList<String> = ArrayList()

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmenttitle[position]
    }

    fun addFragment(fragment: Fragment, title: String){
        fragmentList.add(fragment)
        fragmenttitle.add(title)
    }
}