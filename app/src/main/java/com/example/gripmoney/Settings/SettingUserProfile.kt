package com.example.gripmoney.Settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.R
import com.example.gripmoney.Settings.Pin.SettingAlertSetPin
import com.example.gripmoney.Settings.Pin.SettingChangePin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class SettingUserProfile : AppCompatActivity() {

    lateinit var etgender: EditText

    override fun onResume() {
        super.onResume()

        var db = FirebaseFirestore.getInstance()
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var tvHI = findViewById<TextView>(R.id.tvHi)
        val firebaseAuth = FirebaseAuth.getInstance().currentUser
        var tvUname = findViewById<TextView>(R.id.tvUname)
        var tvUemail = findViewById<TextView>(R.id.tvUemail)
        var tvUage = findViewById<TextView>(R.id.tvUage)
        var tvUgender = findViewById<TextView>(R.id.tvUgender)
        var tvSubscribe = findViewById<TextView>(R.id.tvUpremium)

        val docRef = db.collection(user.toString()).document("userProfile")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("name").toString() != "-") {
                    tvHI.text = "Hi, "+ document.data?.getValue("name").toString()
                    tvUname.text = document.data?.getValue("name").toString()
                    tvUemail.text = firebaseAuth?.email.toString()
                    Log.d(ContentValues.TAG, "Document Username data: ${document.data}")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
                if (document.data?.getValue("age").toString() != "-") {
                    tvUage.text = document.data?.getValue("age").toString()
                    Log.d(ContentValues.TAG, "Document Age data: ${document.data}")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
                if (document.data?.getValue("gender").toString() != "-") {
                    tvUgender.text = document.data?.getValue("gender").toString()
                    Log.d(ContentValues.TAG, "Document Gender data: ${document.data}")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

        val doc = db.collection(user.toString()).document("subscribe")
        doc.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("premium").toString() != "-") {
                    tvSubscribe.text = "Premium"
                    Log.d(ContentValues.TAG, "Document subscribe premium data: ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_user_profile)

        var db = FirebaseFirestore.getInstance()
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var tvHI = findViewById<TextView>(R.id.tvHi)
        var tvUname = findViewById<TextView>(R.id.tvUname)
        var tvUemail = findViewById<TextView>(R.id.tvUemail)
        var tvUage = findViewById<TextView>(R.id.tvUage)
        var tvUgender = findViewById<TextView>(R.id.tvUgender)
        var tvSubscribe = findViewById<TextView>(R.id.tvUpremium)
        var ibEdit = findViewById<Button>(R.id.ibEdit)
        val firebaseAuth = FirebaseAuth.getInstance().currentUser
        var actionBar = supportActionBar
        actionBar!!.title="User Profile"

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val docRef = db.collection(user.toString()).document("userProfile")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("name").toString() != "-") {
                    tvHI.text = "Hi, "+ document.data?.getValue("name").toString()
                    tvUname.text = document.data?.getValue("name").toString()
                    tvUemail.text = firebaseAuth?.email.toString()
                    Log.d(ContentValues.TAG, "Document Username data: ${document.data}")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
                if (document.data?.getValue("age").toString() != "-") {
                    tvUage.text = document.data?.getValue("age").toString()
                    Log.d(ContentValues.TAG, "Document Age data: ${document.data}")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
                if (document.data?.getValue("gender").toString() != "-") {
                    tvUgender.text = document.data?.getValue("gender").toString()
                    Log.d(ContentValues.TAG, "Document Gender data: ${document.data}")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

        val doc = db.collection(user.toString()).document("subscribe")
        doc.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("premium").toString() != "-") {
                    tvSubscribe.text = "Premium"
                    Log.d(ContentValues.TAG, "Document subscribe premium data: ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

        ibEdit.setOnClickListener{
            showDialog()
        }

    }

    private fun showDialog(){
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.setting_edit_profile)
        var db = FirebaseFirestore.getInstance()
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var tvHI = findViewById<TextView>(R.id.tvHi)
        var tvUname = findViewById<TextView>(R.id.tvUname)
        var tvUemail = findViewById<TextView>(R.id.tvUemail)
        var tvUage = findViewById<TextView>(R.id.tvUage)
        var tvUgender = findViewById<TextView>(R.id.tvUgender)
        val firebaseAuth = FirebaseAuth.getInstance().currentUser
        var etUsername = dialog.findViewById<TextView>(R.id.etUname)
        var etAge = dialog.findViewById<TextView>(R.id.etUage)
        etgender = dialog.findViewById<EditText>(R.id.etUgender)
        var btnGender = dialog.findViewById<Button>(R.id.btnUgender)
        var btnSave = dialog.findViewById<Button>(R.id.btnSave)

        val doc = db.collection(user.toString()).document("userProfile")
        doc.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("name").toString() != "-") {
                    etUsername.hint = document.data?.getValue("name").toString()
                    etAge.hint = document.data?.getValue("age").toString()
                    etgender.hint = document.data?.getValue("gender").toString()

                    Log.d(ContentValues.TAG, "Document Username data: ${document.data}")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }

        btnGender.setOnClickListener {
            showGenderDialog()
        }

        btnSave.setOnClickListener{
            if(etUsername.text.toString() != "" && etAge.text.toString() != "" && etgender.text.toString() != ""){
                var up = hashMapOf("name" to etUsername.text.toString(), "age" to etAge.text.toString(), "gender" to etgender.text.toString())
                db.collection(user.toString()).document("userProfile").set(up)
                dialog.dismiss()
                Toast.makeText(this, "Your profile has been successfully updated", Toast.LENGTH_SHORT).show()

                val docRef = db.collection(user.toString()).document("userProfile")
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document.data?.getValue("name").toString() != "-") {
                            tvHI.text = "Hi, "+ document.data?.getValue("name").toString()
                            tvUname.text = document.data?.getValue("name").toString()
                            tvUemail.text = firebaseAuth?.email.toString()
                            Log.d(ContentValues.TAG, "Document Username data: ${document.data}")
                        } else {
                            Log.d(ContentValues.TAG, "No such document")
                        }
                        if (document.data?.getValue("age").toString() != "-") {
                            tvUage.text = document.data?.getValue("age").toString()
                            Log.d(ContentValues.TAG, "Document Age data: ${document.data}")
                        } else {
                            Log.d(ContentValues.TAG, "No such document")
                        }
                        if (document.data?.getValue("gender").toString() != "-") {
                            tvUgender.text = document.data?.getValue("gender").toString()
                            Log.d(ContentValues.TAG, "Document Gender data: ${document.data}")
                        } else {
                            Log.d(ContentValues.TAG, "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "get failed with ", exception)
                    }

            }else{
                Toast.makeText(this, "Error! All fields must be filled!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showGenderDialog(){
        var tempChoice = 1
        var setChoice = 0
        var builder : androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Pick a gender")
        builder.setSingleChoiceItems(SettingFragment.gender, -1, DialogInterface.OnClickListener(){ dialog, which->
            etgender.setText(SettingFragment.gender[which], TextView.BufferType.EDITABLE)
            tempChoice = which
        })
        builder.setPositiveButton("Select",DialogInterface.OnClickListener(){dialog,which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit",DialogInterface.OnClickListener(){dialog,which->
            etgender.setText(SettingFragment.gender[setChoice], TextView.BufferType.EDITABLE)
            dialog.dismiss()
        })
        builder.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }
}