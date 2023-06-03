package com.example.gripmoney.Home.HomeExpenses

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.Home.RecordNew.DatePickerFragment
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExpensesRecord :AppCompatActivity(){
    lateinit var btnUpdateExp : Button
    lateinit var btnDeleteExp : Button
    lateinit var dateEdt: EditText
    lateinit var etExAccount : EditText
    lateinit var etExCategory: EditText
    lateinit var accIDList: ArrayList<String>
    lateinit var accNameList: ArrayList<String>
    lateinit var accGroupList: ArrayList<String>
    lateinit var accAmountList: ArrayList<String>
    lateinit var cateIDList: ArrayList<String>
    lateinit var cateNameList: ArrayList<String>
    lateinit var selectedAccID : String
    lateinit var selectedGroup : String
    lateinit var selectedCateID : String
    lateinit var accOriAmount : String
    lateinit var tempDate:String
    var cal = Calendar.getInstance()
    var accChoice = -1
    var cateChoice = 0
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expenses_record)
        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var actionBar = supportActionBar
        actionBar!!.title="My Expenses"
        btnUpdateExp= findViewById(R.id.btn_expenses_update)
        btnDeleteExp = findViewById(R.id.btn_expenses_delete)
        val btnExpensesAcc = findViewById<Button>(R.id.btn_expenses_acc)
        val btnExpensesCate = findViewById<Button>(R.id.btn_expenses_category)
        etExAccount = findViewById<EditText>(R.id.et_expenses_acc)
        etExCategory= findViewById(R.id.et_expenses_category)
        val etExAmount:EditText = findViewById(R.id.et_expenses_amount)
        val etExDate:EditText = findViewById(R.id.et_expenses_date)
        val etExNote: EditText =findViewById(R.id.et_expenses_note)
        val etExPlace: EditText =findViewById(R.id.et_expenses_place)
        etExAccount.setText(intent.getStringExtra("account"))
        selectedAccID = intent.getStringExtra("accountID").toString()
        val oriAccID = intent.getStringExtra("accountID").toString()
        selectedGroup = intent.getStringExtra("group").toString()
        val oriGroup = intent.getStringExtra("group").toString()
        etExAmount.setText(intent.getStringExtra("amount"))
        etExCategory.setText(intent.getStringExtra("category"))
        selectedCateID = intent.getStringExtra("cateID").toString()
        etExDate.setText(intent.getStringExtra("date"))
        tempDate= intent.getStringExtra("date").toString()
        etExNote.setText(intent.getStringExtra("note"))
        etExPlace.setText(intent.getStringExtra("place"))
        dateEdt = findViewById(R.id.et_expenses_date)
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd-MM-yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                 etExDate.setText(sdf.format(cal.getTime()))
            }
        }
        dateEdt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@ExpensesRecord,
                    dateSetListener,
                    // set DatePickerDialog to point to previous date when it loads up
                    dateEdt.text.substring(6).toInt(),
                    dateEdt.text.substring(3,5).toInt()-1,
                    dateEdt.text.substring(0,2).toInt()).show()
            }
        })

        accIDList = arrayListOf()
        accNameList = arrayListOf()
        accGroupList = arrayListOf()
        accAmountList = arrayListOf()
        cateIDList = arrayListOf()
        cateNameList = arrayListOf()

       retrieveAccOrCate(user!!)
        retrieveOriAccAmount(user)

        btnExpensesAcc.setOnClickListener{
            showAccOptionsDialog()
        }

        btnExpensesCate.setOnClickListener{
            showCateOptionsDialog()
        }

        //update the record
        btnUpdateExp.setOnClickListener {
            val exAmount:String = etExAmount.text.toString()
            val exAccount:String = etExAccount.text.toString()
            val exCategory:String = etExCategory.text.toString()
            val exDate:String = etExDate.text.toString()
            val exNote:String = etExNote.text.toString()
            val exPlace:String = etExPlace.text.toString()
            val exId:String = intent.getStringExtra("id").toString()
            if(tempDate != exDate){
                var type="for update"
                deleteExpenses(user.toString(),tempDate,exId,exAmount,oriAccID,oriGroup,type)
            }
        updateExpenses(exAmount,exAccount,exCategory,exDate,exNote,exPlace,user.toString(),exId,oriAccID,oriGroup)}
        //delete the record
        btnDeleteExp.setOnClickListener {
            val exAmount:String = etExAmount.text.toString()
            val exDate:String = etExDate.text.toString()
            val exId:String = intent.getStringExtra("id").toString()
            val type:String ="for delete"
        deleteExpenses(user.toString(),exDate,exId,exAmount,oriAccID,oriGroup,type)}
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
}
    private fun deleteExpenses(user: String, date: String, id:String,amount:String,oriAccID: String,oriGroup: String,type:String){
        val db =  Firebase.firestore
      db.collection(user).document("expenses").collection(date).document(id)
            .delete()
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                var oriAmount = intent.getStringExtra("amount")!!.toDouble() + accOriAmount.toDouble()
                db.collection(user).document("account group").collection(oriGroup).document(oriAccID)
                    .update("Amount",oriAmount.toString())
                    .addOnSuccessListener { if(type=="for delete")Toast.makeText(this, "This record was successfully deleted!", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener{if(type=="for delete")Toast.makeText(this, "Success to delete expenses record but fail to update account old amount", Toast.LENGTH_SHORT).show()}
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error deleting document", e)
                Toast.makeText(this, "This record was failed to delete!", Toast.LENGTH_SHORT).show()
            }
    }
   private fun updateExpenses(amount:String, account: String, category:String, date:String, note:String, place:String, user:String, id :String,oriAccID:String,oriGroup:String){
       val db =  Firebase.firestore
       val expenses = hashMapOf(
            "Account" to account,
            "AccountID" to selectedAccID,
            "Group" to selectedGroup,
            "Amount" to amount,
            "Category" to category,
            "CateID" to selectedCateID,
            "Date" to date,
            "Note" to note,
            "Place" to place
       )
       db.collection(user).document("expenses").collection(date).document(id)
           .set(expenses)
           .addOnSuccessListener { documentReference ->
               Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $documentReference")
               removeOldAccExRecord(user,amount,oriAccID,oriGroup)

           }
           .addOnFailureListener { e ->
               Log.w(ContentValues.TAG, "Error adding document", e)
               Toast.makeText(this, "This record was failed to updated!", Toast.LENGTH_SHORT).show()
           }
    }

   private fun removeOldAccExRecord(user:String, amount:String, oriAccID:String,oriGroup:String){
        var oriAmount = intent.getStringExtra("amount")!!.toDouble() + accOriAmount.toDouble()
        db.collection(user).document("account group").collection(oriGroup).document(oriAccID)
            .update("Amount",oriAmount.toString())
            .addOnSuccessListener { addNewAccExRecord(user,amount,oriAmount.toString()) }
            .addOnFailureListener{Toast.makeText(this, "Success to update expenses record but fail to update old account amount", Toast.LENGTH_SHORT).show()}

    }

    private fun addNewAccExRecord(user:String,amount:String,oriAmount:String){
        var newAmount :Double = 0.0
        if (accChoice == -1){
            newAmount = oriAmount.toDouble() - amount.toDouble()
        }else{
             newAmount = accAmountList[accChoice].toDouble() - amount.toDouble()
        }


        db.collection(user).document("account group").collection(selectedGroup).document(selectedAccID)
            .update("Amount",newAmount.toString())
            .addOnSuccessListener {
                Toast.makeText(this, "Your expense record has been updated!!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener{Toast.makeText(this, "Success to update old account amount but fail to update new account amount", Toast.LENGTH_SHORT).show()}
    }

    private fun retrieveOriAccAmount(user:String){
        db.collection(user).document("account group").collection(selectedGroup).document(selectedAccID)
            .get()
            .addOnSuccessListener { document->
                    Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                    accOriAmount = document.data?.get("Amount").toString()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }

    private fun retrieveAccOrCate(user: String) {
        for(accDoc in AccGroupFragment.accountCategory){
            db.collection(user).document("account group").collection(accDoc)
                .get()
                .addOnSuccessListener { result->
                    for (document in result) {
                        Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                        accIDList.add(document.id)
                        accNameList.add(document.data.get("Name").toString())
                        accGroupList.add(accDoc)
                        accAmountList.add(document.data.get("Amount").toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }

        db.collection(user).document("category").collection("category1")
            .get()
            .addOnSuccessListener { result->
                for (document in result) {
                    Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                    cateIDList.add(document.id)
                    cateNameList.add(document.get("Name").toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

    }

    fun showAccOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(accNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etExAccount.setText(accNameList[which], TextView.BufferType.EDITABLE)
            selectedAccID = accIDList[which]
            selectedGroup = accGroupList[which]
            if (selectedAccID == intent.getStringExtra("accountID").toString()){
                accChoice = -1
            }else{
                accChoice = which
            }
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etExAccount.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
            selectedAccID = accIDList[setChoice]
            selectedGroup = accGroupList[setChoice]
            if (selectedAccID == intent.getStringExtra("accountID").toString()){
                accChoice = -1
            }else{
                accChoice = setChoice
            }
            dialog.dismiss()
        })
        builder.show()
    }

    fun showCateOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(cateNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etExCategory.setText(cateNameList[which], TextView.BufferType.EDITABLE)
            selectedCateID = cateIDList[which]
            cateChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etExCategory.setText(cateNameList[setChoice], TextView.BufferType.EDITABLE)
            selectedCateID = cateIDList[setChoice]
            cateChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }
}