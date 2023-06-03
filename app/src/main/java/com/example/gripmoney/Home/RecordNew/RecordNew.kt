package com.example.gripmoney.Home.RecordNew

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.viewpager.widget.ViewPager
import com.example.gripmoney.Home.FragmentAdapter
import com.example.gripmoney.R
import com.google.android.material.tabs.TabLayout

class RecordNew : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_new)

        var viewPager:ViewPager = findViewById(R.id.viewPager) as ViewPager
        var tablayout:TabLayout = findViewById(R.id.tablayout) as TabLayout
        // calling the action bar
        var actionBar = supportActionBar
        actionBar!!.title="Record New"

        val fragmentAdapter = FragmentAdapter(supportFragmentManager)
        fragmentAdapter.addFragment(RecordNewExpensesFragment(),"EXPENSES")
        fragmentAdapter.addFragment(RecordNewIncomeFragment(),"INCOME")
        fragmentAdapter.addFragment(RecordNewTransferFragment(),"TRANSFER")

        viewPager.adapter = fragmentAdapter
        tablayout.setupWithViewPager(viewPager)

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
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