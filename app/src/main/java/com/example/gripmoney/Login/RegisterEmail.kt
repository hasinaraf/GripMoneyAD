package com.example.gripmoney.Login

import android.content.ContentValues
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
import com.example.gripmoney.AccountGroup.AccDataClass
import com.example.gripmoney.Category.CateDataClass
import com.example.gripmoney.Category.CategoryFragment
import com.example.gripmoney.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterEmail : AppCompatActivity() {
    //done by teoh
    lateinit var auth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_email)

        auth = Firebase.auth
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)


        val btnSignUp= findViewById<Button>(R.id.btnSignUp)
        val etSignUpName = findViewById<EditText>(R.id.etSignUpName)
        val etSignUpEmail:EditText = findViewById(R.id.etSignUpEmail)
        val etSignUpPassword:EditText = findViewById(R.id.etSignUpPassword)
        val etSignUpConfPassword:EditText = findViewById(R.id.etSignUpConfPassword)
        val btnOtherLogin: Button = findViewById(R.id.btnOtherLogin)

        btnSignUp.setOnClickListener{

            val name:String = etSignUpName.text.toString()
            val email:String = etSignUpEmail.text.toString()
            val password:String = etSignUpPassword.text.toString()
            val confPassword:String = etSignUpConfPassword.text.toString()

            val editor = sharedPreference.edit()
            editor.putString("username",name)
            editor.apply()

            if(name.isEmpty() || email.isEmpty()  || password.isEmpty() || confPassword.isEmpty()){
                Toast.makeText(baseContext, "All the input box should be filled",
                    Toast.LENGTH_LONG).show()
            }else{
                checkIdenticalPassword(name,email,password,confPassword)
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

    fun checkIdenticalPassword(uName: String,email:String,password:String,confPassword:String){
        if(password == confPassword){
            checkPasswordRequirement(uName,email,password)
        }else{
            Log.w(TAG, "Password and Confirm Password not identical")
            Toast.makeText(baseContext, "Password and Confirm Password not identical",
                Toast.LENGTH_LONG).show()
        }
    }

    fun checkPasswordRequirement(uName: String, email: String, password: String) {
        if(password.length in 8..20){
            accountCreation(uName,email,password)
        } else{
            Log.w(TAG, "Password length not in the range from 8 to 20")
            Toast.makeText(baseContext, "Password length not in the range from 8 to 20",
                Toast.LENGTH_LONG).show()
        }
    }

    fun accountCreation(uName: String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    val uid = user!!.uid
                    user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                baseContext,
                                "The verification link has to send to your email address. Please click the link to verify yourself",
                                Toast.LENGTH_LONG
                            ).show()
                            var auth = Firebase.auth
                            val user = auth.currentUser
                            val email = user!!.email
                            val index = email?.lastIndexOf('@')
                            val substring = email?.subSequence(0, index!!.toInt())

                            val uName = hashMapOf("name" to uName, "age" to "-", "gender" to "-")
                            var db = FirebaseFirestore.getInstance()
                            db.collection(uid).document("userProfile").set(uName)
                            val pin = hashMapOf("number" to "0")
                            db.collection(uid).document("pin").set(pin)
                            for(acc in arrayOf("Accounts","Card","Cash")){
                                val account = hashMapOf(
                                    "AccountID" to "$acc.1",
                                    "Amount" to "100",
                                    "Name" to acc,
                                    "Group" to acc,
                                    "Note" to "This is default account"
                                )
                                db.collection(uid).document("account group").collection(acc).document("$acc.1").set(account)
                            }
                            val categoryName = arrayOf("Food","Gift","Transportation")
                            for(num in 1..3){
                                val category = hashMapOf(
                                    "CateID" to "category.$num",
                                    "Name" to categoryName[num-1],
                                    "Description" to "This is a default category",
                                    "Icon" to CategoryFragment.iconList[num-1],
                                    "Color" to CategoryFragment.colorList[num-1]
                                )
                                db.collection(uid).document("category").collection("category1").document("category.$num").set(category)
                            }
                            val sub = hashMapOf("premium" to "-")
                            db.collection(uid).document("subscribe").set(sub)
                            val userType = hashMapOf("type" to "email")
                            db.collection(uid).document("userType").set(userType)
                            auth.signOut()
                            val intent = Intent(this, LoginChoice::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                baseContext, "Fail to send out verification link." + task.exception,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Fail to sign up account" + task.exception,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun notifyUser(str:String){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show()
    }

}