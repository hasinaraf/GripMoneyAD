package com.example.gripmoney.Home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.example.gripmoney.Home.HomeExpenses.HomeExpensesFragment
import com.example.gripmoney.Home.HomeIncome.HomeIncomeFragment
import com.example.gripmoney.R
import com.example.gripmoney.Home.HomeTransfer.HomeTransferFragment
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    lateinit var myFragment:View
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_home, container, false)

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

        fragmentAdapter.addFragment(HomeExpensesFragment(), "EXPENSES")
        fragmentAdapter.addFragment(HomeIncomeFragment(), "INCOME")
        fragmentAdapter.addFragment(HomeTransferFragment(), "TRANSFER")

        viewPager.adapter = fragmentAdapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}


