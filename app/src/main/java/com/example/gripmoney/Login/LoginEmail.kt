package com.example.gripmoney.Login

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.gripmoney.MainActivity
import com.example.gripmoney.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginEmail : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_email)

        auth = Firebase.auth

        val btnLogin= findViewById<Button>(R.id.btnLogin)
        val etLoginEmail: EditText = findViewById(R.id.etLoginEmail)
        val etLoginPassword: EditText = findViewById(R.id.etLoginPassword)
        val btnOtherLogin: Button = findViewById(R.id.btnOtherLogin)

        btnLogin.setOnClickListener{
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()

            if(email.isEmpty()  || password.isEmpty()){
                Toast.makeText(baseContext, "All the input box should be filled",
                    Toast.LENGTH_LONG).show()
            }else {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            if(auth.currentUser?.isEmailVerified == true){
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success")
                                Toast.makeText(this,"signInWithEmail:success",Toast.LENGTH_LONG).show()
                                val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
                                val editor = sharedPreference.edit()
                                //to get the user uid and pass it to main activity for linking with firebase firestore storage
                                editor.putString("user",auth.uid)
                                editor.apply()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else if (auth.currentUser?.isEmailVerified == false){
                                Log.d(TAG, "Email no verified yet")
                                Toast.makeText(this,"The email does not verified yet.Please check your email address to get the verification link",
                                    Toast.LENGTH_LONG).show()
                            }else{
                                Log.d(TAG, "Cannot get the current user account to test email verification status")
                                Toast.makeText(this,"Something went wrong,please contact the admin",
                                    Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "The provided email or password is wrong"+task.exception,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }

        btnOtherLogin.setOnClickListener{
            startActivity(Intent(this,LoginChoice::class.java))
            finish()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}