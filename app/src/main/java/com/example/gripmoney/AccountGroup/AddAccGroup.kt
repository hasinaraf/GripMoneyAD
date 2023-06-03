package com.example.gripmoney.AccountGroup

import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.gripmoney.MainActivity
import com.example.gripmoney.R
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class AddAccGroup : AppCompatActivity() {

    lateinit var db : FirebaseFirestore
    lateinit var btnAccGroup : Button
    lateinit var etAccGroup : EditText
    lateinit var etAccAmount : EditText
    lateinit var etAccName : EditText
    lateinit var etAccNote : EditText
    lateinit var btnSaveNewAccount : Button

    companion object{
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, AddAccGroup::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_acc_group)

        // calling the action bar
        var actionBar = supportActionBar
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        db = FirebaseFirestore.getInstance()
        btnAccGroup = findViewById(R.id.btnAccGroup)
        etAccAmount = findViewById(R.id.etAccAmount)
        etAccGroup = findViewById(R.id.etAccGroup)
        etAccName = findViewById(R.id.etAccName)
        etAccNote = findViewById(R.id.etAccNote)
        btnSaveNewAccount = findViewById(R.id.btnSaveNewAccount)

        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userID:String = sharedPreference?.getString("user","")!!

        btnAccGroup.setOnClickListener{
            showOptionsDialog()
        }

        btnSaveNewAccount.setOnClickListener{

            val newDocName = getNewDocName(etAccGroup.text.toString())


            val account = hashMapOf(
                "AccountID" to newDocName,
                "Amount" to etAccAmount.text.toString(),
                "Name" to etAccName.text.toString(),
                "Group" to etAccGroup.text.toString(),
                "Note" to etAccNote.text.toString()
            )

            db.collection(userID).document("account group").collection(etAccGroup.text.toString()).document(newDocName)
                .set(account)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!")
                    notifyUser("DocumentSnapshot successfully written!")
                    finish()
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)
                    notifyUser("Error writing document!")
                }

        }

    }

    fun getNewDocName(group: String): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val newName: String = prefs.getString("Account Group " + group, group+".1").toString()

        return newName
    }


    fun showOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(AccGroupFragment.accountCategory, -1, DialogInterface.OnClickListener(){dialog,which->
            etAccGroup.setText(AccGroupFragment.accountCategory[which], TextView.BufferType.EDITABLE)
            tempChoice = which
        })
        builder.setPositiveButton("Select",DialogInterface.OnClickListener(){dialog,which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit",DialogInterface.OnClickListener(){dialog,which->
            etAccGroup.setText(AccGroupFragment.accountCategory[setChoice], TextView.BufferType.EDITABLE)
            dialog.dismiss()
        })
        builder.show()
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

    fun notifyUser(str:String){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show()
    }
}