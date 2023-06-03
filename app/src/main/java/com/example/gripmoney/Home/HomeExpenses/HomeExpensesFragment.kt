package com.example.gripmoney.Home.HomeExpenses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.example.gripmoney.Home.FragmentAdapter
import com.example.gripmoney.Home.HomeIncome.HomeIncomeFragment
import com.example.gripmoney.Home.HomeTransfer.HomeTransferFragment
import com.example.gripmoney.R
import com.google.android.material.tabs.TabLayout

class HomeExpensesFragment : Fragment() {

    lateinit var myFragment:View
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_home_expenses, container, false)

        viewPager = myFragment.findViewById(R.id.viewPager) as ViewPager
        tabLayout = myFragment.findViewById(R.id.tablayout) as TabLayout

        return myFragment
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)

    }

    private fun setUpViewPager(viewPager: ViewPager) {
        val fragmentAdapter = FragmentAdapter(childFragmentManager)

        fragmentAdapter.addFragment(ExpensesDayFragment(), "Day")
        fragmentAdapter.addFragment(ExpensesWeekFragment(), "Week")
        fragmentAdapter.addFragment(ExpensesMonthFragment(), "Month")
        fragmentAdapter.addFragment(ExpensesYearFragment(), "Year")

        viewPager.adapter = fragmentAdapter
    }

}