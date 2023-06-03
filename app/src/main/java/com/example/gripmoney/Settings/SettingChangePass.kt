package com.example.gripmoney.Settings

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.gripmoney.Login.LoginChoice
import com.example.gripmoney.R
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingChangePass : AppCompatActivity() {
    lateinit var etNpass: EditText
    lateinit var etCNpass: EditText
    lateinit var etOpass: EditText
    var auth: FirebaseAuth? = null
    var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_change_pass)
        // calling the action bar
        var actionBar = supportActionBar
        actionBar!!.title="Change Password"

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        etOpass = findViewById<View>(R.id.etOPass) as EditText
        etNpass = findViewById<View>(R.id.etNPass) as EditText
        etCNpass = findViewById<View>(R.id.etCNPass) as EditText
        var btnSave = findViewById<Button>(R.id.btnSave)
        auth = FirebaseAuth.getInstance()

        btnSave.setOnClickListener{
            if(etOpass.text.toString().isNotEmpty() && etNpass.text.toString().isNotEmpty() && etCNpass.text.toString().isNotEmpty()){
                    val user = auth!!.currentUser
                    if (user != null){
                        val credential = EmailAuthProvider.getCredential(user.email!!,etOpass.text.toString())

                        //prompt the user to re-provide their sign-in credentials
                        user?.reauthenticate(credential)
                            ?.addOnCompleteListener {
                            if (it.isSuccessful){
                                if(etNpass.text.toString() == etCNpass.text.toString()){
                                    Toast.makeText(
                                        this,
                                        "Changing password, please wait!!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    user.updatePassword(etNpass!!.text.toString())
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    "Your password has been changed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "For your account security, please log in again to change your password!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }else{
                                    Toast.makeText(this, "Error!! New password must be identical with confirmed new password! Please try again", Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(this, "Error!! Current password entered wrong", Toast.LENGTH_SHORT).show()
                            }
                            }
                    }else{
                        startActivity(Intent(this, LoginChoice::class.java))
                        finish()
                    }
            }else{
                Toast.makeText(this,"Please enter all the fields.", Toast.LENGTH_SHORT).show()
            }
            /*if(etNpass.text.toString() == etCNpass.text.toString()){
                if (user != null) {
                    Toast.makeText(
                        this,
                        "Changing password, please wait!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                    user.updatePassword(etNpass!!.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Your password has been changed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "For your account security, please log in again to change your password!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }else{
                Toast.makeText(this, "Error!! New password must be identical with confirmed new password! Please try again", Toast.LENGTH_SHORT).show()
            }*/
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }

}