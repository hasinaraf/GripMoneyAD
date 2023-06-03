package com.example.gripmoney.Settings.Pin

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat.getSystemService
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.Calendar.CalendarFragment
import com.example.gripmoney.Category.CategoryFragment
import com.example.gripmoney.Help.HelpFragment
import com.example.gripmoney.Home.HomeFragment
import com.example.gripmoney.Login.LoginChoice
import com.example.gripmoney.Premium.PremiumFragment
import com.example.gripmoney.R
import com.example.gripmoney.Settings.SettingFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SettingSetPin : AppCompatActivity() {

    val myFragment = SettingFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_set_pin)

        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        val et_pin = findViewById<EditText>(R.id.etPin)
        var btnSave = findViewById<Button>(R.id.btnSave)

        // calling the action bar
        var actionBar = supportActionBar
        actionBar!!.title="Two Factor Authentication"

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        btnSave.setOnClickListener(){
            val pin:String = et_pin.text.toString()
            if(pin.length == 6){
                setPin(pin, user.toString())
                et_pin.setText("")
            }else{
                Toast.makeText(this, "Error!! Set up pin failed! Pin number must have exactly 6 digit! Please enter again", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

    }

    private fun setPin(pin: String, user: String){
        val p = hashMapOf("number" to pin)
        var db = FirebaseFirestore.getInstance()
        db.collection(user).document("pin").set(p)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Your pin was successfully saved!", Toast.LENGTH_SHORT).show()
                val pref = getSharedPreferences("PIN_SET", Context.MODE_PRIVATE)
                val editor = pref?.edit()
                editor.apply{
                    editor?.putString("TEXT_BTN_PIN","Change Pin")
                }?.apply()

                val prefs = getSharedPreferences("PIN", Context.MODE_PRIVATE)
                val editors = prefs.edit()
                editors.apply{
                    editors?.putString("STRING_KEY","true")
                }.apply()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Your pin was failed to saved!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }

}