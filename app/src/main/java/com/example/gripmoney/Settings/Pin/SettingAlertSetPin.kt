package com.example.gripmoney.Settings.Pin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.gripmoney.R

class SettingAlertSetPin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_alert_set_pin)
        // calling the action bar
        var actionBar = supportActionBar
        actionBar!!.title="Alert!!!"

        var btnYes = findViewById<Button>(R.id.btnYes)
        var btnCancel = findViewById<Button>(R.id.btnCancel)

        btnYes.setOnClickListener(){
            val intent= Intent(this, SettingSetPin::class.java)
            startActivity(intent)
            finish()
        }

        btnCancel.setOnClickListener(){
            onSupportNavigateUp()
            finish()
        }

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

}