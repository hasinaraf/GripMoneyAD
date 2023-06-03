package com.example.gripmoney.Debt

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.gripmoney.Debt.AddDebtLend.DatePickerFragment
import com.example.gripmoney.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ViewDebtManage : AppCompatActivity() {
    lateinit var db : FirebaseFirestore
    lateinit var userID: String
    lateinit var dateEdt: EditText
    var cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_debt_manage)

        var actionBar = supportActionBar
        actionBar!!.title="Debt Management"
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        db = FirebaseFirestore.getInstance()
        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        userID = sharedPreference?.getString("user","")!!

        val btnUpdateDebtManage: Button = findViewById(R.id.btn_debt_manage_update)
        val btnCompleteDebtManage: Button = findViewById(R.id.btn_debt_manage_complete)
        val tvGroup: TextView = findViewById(R.id.group)
        val etDebtAmount: EditText = findViewById(R.id.et_debt_manage_amt)
        val etDebtName: EditText = findViewById(R.id.et_debt_manage_name)
        val etDebtDate: EditText = findViewById(R.id.et_debt_manage_date)
        val etDebtNote: EditText = findViewById(R.id.et_debt_manage_note)
        dateEdt = findViewById(R.id.et_debt_manage_date)
        val debtManageID = intent.getStringExtra("id")
        val groupType = intent.getStringExtra("group")
        if(groupType == "Lend"){
            tvGroup.setText("Lend To")
        }
        etDebtAmount.setText(intent.getStringExtra("amount"))
        etDebtName.setText(intent.getStringExtra("name"))
        etDebtDate.setText(intent.getStringExtra("date"))
        etDebtNote.setText(intent.getStringExtra("note"))


        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd-MM-yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                etDebtDate.setText(sdf.format(cal.getTime()))
            }
        }

        dateEdt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@ViewDebtManage,
                    dateSetListener,
                    // set DatePickerDialog to point to previous date when it loads up
                    dateEdt.text.substring(6).toInt(),
                    dateEdt.text.substring(3,5).toInt()-1,
                    dateEdt.text.substring(0,2).toInt()).show()
            }
        })

        btnUpdateDebtManage.setOnClickListener {
            val amount = etDebtAmount.text.toString()
            val name = etDebtName.text.toString()
            val date = etDebtDate.text.toString()
            val note = etDebtNote.text.toString()
            updateDebtManageRecord(groupType.toString(),debtManageID.toString(),amount,name,date,note)
        }

        btnCompleteDebtManage.setOnClickListener {

            completeDebtManageRecord(groupType.toString(),debtManageID.toString())
        }

    }

    private fun updateDebtManageRecord(group:String,id:String,amount:String,name:String,date:String,note:String) {
        val Debt = hashMapOf(
            "DebtManageID" to id,
            "Name" to name,
            "Amount" to amount,
            "Date" to date,
            "Note" to note,
        )

        db.collection(userID).document("DebtManage").collection(group).document(id)
            .set(Debt)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "Record successful updated!")
                Toast.makeText(this,"Record successful updated!",Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error updating record", e)
                Toast.makeText(this,"Error updating record",Toast.LENGTH_SHORT).show()
            }

    }

    private fun completeDebtManageRecord(group:String,id:String) {
        db.collection(userID).document("DebtManage").collection(group).document(id)
            .delete()
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "The debt or lend is being paid!")
                Toast.makeText(this,"The debt or lend is being paid!",Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error deleting debt or lend record", e)
                Toast.makeText(this,"Error deleting debt or lend record",Toast.LENGTH_SHORT).show()
            }
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
}