package com.example.gripmoney.Debt.AddDebtLend

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.gripmoney.Home.FragmentAdapter
import com.example.gripmoney.R
import com.google.android.material.tabs.TabLayout


class AddDebtManage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_debt)

        var actionBar = supportActionBar
        actionBar!!.title="Debt Management"
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        var viewPager = findViewById<ViewPager>(R.id.debtMngViewPager)
        var tabLayout = findViewById<TabLayout>(R.id.debtMngTab)

        val fragmentAdapter = FragmentAdapter(supportFragmentManager)

        fragmentAdapter.addFragment(AddDebtFragment(),"Debt")
        fragmentAdapter.addFragment(AddLendFragment(),"Lend")

        viewPager.adapter = fragmentAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
