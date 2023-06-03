package com.example.gripmoney.Settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gripmoney.Login.LoginChoice
import com.example.gripmoney.R
import com.example.gripmoney.Settings.Pin.SettingAlertSetPin
import com.example.gripmoney.Settings.Pin.SettingChangePin
import com.example.gripmoney.Settings.Pin.SettingSetPin
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern
import java.util.regex.Matcher


class SettingFragment : Fragment() {

    lateinit var myFragment: View
    var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null

    override fun onResume() {
        super.onResume()
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var db = FirebaseFirestore.getInstance()
        var tvHI = myFragment.findViewById<TextView>(R.id.tvHi)
        val docRef = db.collection(user.toString()).document("pin")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var btn = myFragment.findViewById<Button>(R.id.btnCpin)
                    if(document.data?.getValue("number").toString() != "0"){
                        btn.text = "Change Pin"
                    }else{
                        btn.text = "Set Pin"
                    }
                    Log.d(TAG, "Document Pin data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        val ref = db.collection(user.toString()).document("userProfile")
        ref.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("name").toString() != "-") {
                    tvHI.text = "Hi, " + document.data?.getValue("name").toString()
                    Log.d(ContentValues.TAG, "Document Username data: ${document.data}")
                }
            }

        val doc = db.collection(user.toString()).document("userType")
        doc.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("type").toString() == "google") {
                    var btnChgPass = myFragment.findViewById<Button>(R.id.btnCpass)
                    var btnChgPin = myFragment.findViewById<Button>(R.id.btnCpin)
                    btnChgPass.visibility = View.GONE
                    btnChgPin.setBackgroundColor(Color.parseColor("#FBBC05"))
                    Log.d(ContentValues.TAG, "Document userType data: ${document.data}")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_setting, container, false)

        var btnUP = myFragment.findViewById<Button>(R.id.btnUP)
        var btnChgPass = myFragment.findViewById<Button>(R.id.btnCpass)
        var btnChgPin = myFragment.findViewById<Button>(R.id.btnCpin)
        var btnDltAcc = myFragment.findViewById<Button>(R.id.btnDltAcc)
        var db = FirebaseFirestore.getInstance()
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var progressBar = myFragment.findViewById<ProgressBar>(R.id.progressBar)
        var tvHI = myFragment.findViewById<TextView>(R.id.tvHi)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth!!.currentUser

        val doc = db.collection(user.toString()).document("userType")
        doc.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("type").toString() == "google") {
                    btnChgPass.visibility = View.GONE
                    btnChgPin.setBackgroundColor(Color.parseColor("#FBBC05"))
                    Log.d(ContentValues.TAG, "Document userType data: ${document.data}")
                }
            }

        val ref = db.collection(user.toString()).document("userProfile")
        ref.get()
            .addOnSuccessListener { document ->
                //email user
                if (document.data?.getValue("name").toString() != "-") {
                    tvHI.text = "Hi, " + document.data?.getValue("name").toString()
                    btnChgPass.setOnClickListener{
                        val intent= Intent(activity, SettingChangePass::class.java)
                        startActivity(intent)
                    }
                    Log.d(ContentValues.TAG, "Document Username data: ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

        val docRef = db.collection(user.toString()).document("pin")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    btnChgPin.setOnClickListener(){
                        var btn = myFragment.findViewById<Button>(R.id.btnCpin)
                        if(document.data?.getValue("number").toString() == "0" && btn.text == "Set Pin"){
                            val intent= Intent(activity, SettingAlertSetPin::class.java)
                            startActivity(intent)
                        }else{
                            val intent= Intent(activity, SettingChangePin::class.java)
                            startActivity(intent)
                        }
                    }
                    Log.d(TAG, "Document Pin 1 data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        btnUP.setOnClickListener{
            val intent= Intent(activity, SettingUserProfile::class.java)
            startActivity(intent)
        }

        btnChgPass.setOnClickListener{
            val intent= Intent(activity, SettingChangePass::class.java)
            startActivity(intent)
        }

        btnDltAcc.setOnClickListener{
            var dialog: AlertDialog.Builder = AlertDialog.Builder(this.activity)
            dialog.setTitle("Are you sure?")
            dialog.setMessage("Deleting this account will result in completely removing " +
                    "your account from the system and you won't be able to access the app")
            dialog.setPositiveButton("Delete"){
                    dialog, which ->
                progressBar.setVisibility(View.VISIBLE)
                firebaseUser!!.delete().addOnCompleteListener { task ->
                    progressBar.setVisibility(View.GONE)
                    if (task.isSuccessful) {
                        Toast.makeText(this.activity, "Account Delete", Toast.LENGTH_SHORT).show()
                        val pref = this.activity?.getSharedPreferences("PIN_SET", Context.MODE_PRIVATE)
                        val editor = pref?.edit()
                        editor.apply{
                            editor?.putString("TEXT_BTN_PIN","Set Pin")
                        }?.apply()
                        val intent = Intent(activity, LoginChoice::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        //Toast.makeText(this.activity, task.exception!!.message, Toast.LENGTH_SHORT).show()
                        showCustomDialog(task.exception!!.message)
                    }
                }
            }
            dialog.setNegativeButton("Dismiss") { dialog, which -> dialog.dismiss() }
            val alertDialog = dialog.create()
            alertDialog.show()
            //activity?.finish()
        }

        return myFragment
    }

    companion object{
        var gender = arrayOf("Female","Male")
    }

    private fun showCustomDialog(msg: String?){
        var dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.setting_custom_dialog)
        var tvcd = dialog.findViewById<TextView>(R.id.tv_cd2)
        tvcd.text = msg
        var btnCancel = dialog.findViewById<Button>(R.id.cancelOK)
        btnCancel.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

}
