package com.example.gripmoney.AccountGroup

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class ViewAccGroup : AppCompatActivity() {
    lateinit var db : FirebaseFirestore
    lateinit var btnAccGroupDetail : Button
    lateinit var etAccGroupDetail : EditText
    lateinit var etAccAmountDetail : EditText
    lateinit var etAccNameDetail : EditText
    lateinit var etAccNoteDetail : EditText
    lateinit var btnUpdateAccount : Button
    lateinit var btnDeleteAccount : Button
    lateinit var accountID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_acc_group)

        // calling the action bar
        var actionBar = supportActionBar
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        db = FirebaseFirestore.getInstance()
        btnAccGroupDetail = findViewById(R.id.btnAccGroupDetail)
        etAccAmountDetail = findViewById(R.id.etAccAmountDetail)
        etAccGroupDetail = findViewById(R.id.etAccGroupDetail)
        etAccNameDetail = findViewById(R.id.etAccNameDetail)
        etAccNoteDetail = findViewById(R.id.etAccNoteDetail)
        btnUpdateAccount = findViewById(R.id.btnUpdateAccount)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)

        etAccAmountDetail.setText(intent.getStringExtra("amount"), TextView.BufferType.EDITABLE)
        etAccNameDetail.setText(intent.getStringExtra("name"), TextView.BufferType.EDITABLE)
        etAccGroupDetail.setText(intent.getStringExtra("group"), TextView.BufferType.EDITABLE)
        etAccNoteDetail.setText(intent.getStringExtra("note"), TextView.BufferType.EDITABLE)
        accountID = intent.getStringExtra("id").toString()

        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userID:String = sharedPreference?.getString("user","")!!


        btnAccGroupDetail.setOnClickListener{
            //showOptionsDialog()
        }

        btnUpdateAccount.setOnClickListener{
            updateAccountDetail(userID)
        }

        btnDeleteAccount.setOnClickListener {
            deleteConfirmation(userID)
        }


    }

    private fun deleteConfirmation(userID:String) {
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account Group")
        builder.setMessage("Are you sure want to delete this account group? the deleted account group detail cannot be retrieve back")
        builder.setPositiveButton("Proceed", DialogInterface.OnClickListener(){ dialog, which->
            deleteAccountGroup(userID)
            dialog.dismiss()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener(){ dialog, which->
            dialog.dismiss()
        })
        builder.show()
    }

    private fun deleteAccountGroup(userID:String) {
        if (accountID == "Accounts.1" || accountID == "Card.1" || accountID == "Cash.1"){
            notifyUser("This is default account group you cannot delete this.")
        }else {
            db.collection(userID).document("account group").collection(etAccGroupDetail.text.toString()).document(intent?.getStringExtra("id")!!)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    notifyUser("The account group has been deleted")
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting document", e)
                    notifyUser("!! Fail to detele the account group !!")
                }
        }

    }

    private fun updateAccountDetail(userID:String) {
        val account = hashMapOf(
            "AccountID" to intent?.getStringExtra("id")!!,
            "Amount" to etAccAmountDetail.text.toString(),
            "Name" to etAccNameDetail.text.toString(),
            "Group" to etAccGroupDetail.text.toString(),
            "Note" to etAccNoteDetail.text.toString()
        )

        db.collection(userID).document("account group").collection(etAccGroupDetail.text.toString()).document(intent?.getStringExtra("id")!!)
            .set(account)
            .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                notifyUser("Account detail successfully updated!")
                finish()
            }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e)
                notifyUser("!! Error to update account detail !!")
            }
    }

    fun showOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(AccGroupFragment.accountCategory, -1, DialogInterface.OnClickListener(){ dialog, which->
            etAccGroupDetail.setText(AccGroupFragment.accountCategory[which], TextView.BufferType.EDITABLE)
            tempChoice = which
        })
        builder.setPositiveButton("Proceed", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etAccGroupDetail.setText(AccGroupFragment.accountCategory[setChoice], TextView.BufferType.EDITABLE)
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
        Toast.makeText(this,str, Toast.LENGTH_SHORT).show()
    }
}