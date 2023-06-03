package com.example.gripmoney.Settings.Pin

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.gripmoney.R
import com.google.firebase.firestore.FirebaseFirestore

class SettingChangePin : AppCompatActivity() {
    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_change_pin)

        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference.getString("user","")
        var OldPin = findViewById<EditText>(R.id.etOPin)
        var newPin = findViewById<EditText>(R.id.etNPin)
        var conNewPin = findViewById<EditText>(R.id.etCNPin)
        var btn_save = findViewById<Button>(R.id.btnSave)
        var actionBar = supportActionBar
        actionBar!!.title="Change Pin"

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        btn_save.setOnClickListener{
            val docRef = db.collection(user.toString()).document("pin")
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        var pin = document.data?.getValue("number")
                        if(OldPin.text.toString() == pin.toString()){
                            if(newPin.text.toString().length == 6 && conNewPin.text.toString().length == 6){
                                if(newPin.text.toString() == conNewPin.text.toString()){
                                    var np = hashMapOf("number" to newPin.text.toString())
                                    db.collection(user.toString()).document("pin").set(np)
                                        .addOnSuccessListener { documentReference ->
                                            Toast.makeText(this, "Your pin was successfully changed!", Toast.LENGTH_SHORT).show()
                                            val pref = getSharedPreferences("PIN_SET", Context.MODE_PRIVATE)
                                            val editor = pref?.edit()
                                            editor.apply{
                                                editor?.putString("TEXT_BTN_PIN","Change Pin")
                                            }?.apply()

                                            val prefs = getSharedPreferences("PIN", Context.MODE_PRIVATE)
                                            val editors = prefs.edit()
                                            editors.apply{
                                                putString("STRING_KEY","true")
                                            }.apply()
                                            finish()
                                            //startActivity(Intent(this, HomeFragment::class.java))
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Your pin was failed to changed!", Toast.LENGTH_SHORT).show()
                                        }
                                }else{
                                    Toast.makeText(this, "Error!!! New pin must be identical with confirmed new pin!", Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(this, "Error!!! Pin number must have exactly 6 digit!", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(this, "Error your old pin has enter wrong! Please try again!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "get failed with ", exception)
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }

}