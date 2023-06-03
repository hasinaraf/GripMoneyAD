package com.example.gripmoney.Home.HomeIncome;

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
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

public class IncomeRecord :AppCompatActivity() {
    lateinit var btnUpdateExp : Button
    lateinit var btnDeleteExp : Button
    lateinit var dateEdt: EditText
    lateinit var etInAccount: EditText
    lateinit var etInCategory: EditText
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
        setContentView(R.layout.income_record)
        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var actionBar = supportActionBar
        actionBar!!.title="My Income"
        btnUpdateExp= findViewById(R.id.btn_income_update)
        btnDeleteExp = findViewById(R.id.btn_income_delete)
        etInAccount = findViewById(R.id.et_income_acc)
        etInCategory= findViewById(R.id.et_income_category)
        val btnInAccount:Button = findViewById(R.id.btn_income_acc)
        val btnInCategory:Button  = findViewById(R.id.btn_income_category)
        val etInAmount: EditText = findViewById(R.id.et_income_amount)
        val etInDate: EditText = findViewById(R.id.et_income_date)
        val etInNote: EditText =findViewById(R.id.et_income_note)
        val etInPlace: EditText =findViewById(R.id.et_income_place)
        etInAccount.setText(intent.getStringExtra("account"))
        selectedAccID = intent.getStringExtra("accountID").toString()
        val oriAccID = intent.getStringExtra("accountID").toString()
        selectedGroup = intent.getStringExtra("group").toString()
        val oriGroup = intent.getStringExtra("group").toString()
        etInAmount.setText(intent.getStringExtra("amount"))
        etInCategory.setText(intent.getStringExtra("category"))
        selectedCateID = intent.getStringExtra("cateID").toString()
        etInDate.setText(intent.getStringExtra("date"))
        tempDate= intent.getStringExtra("date").toString()
        etInNote.setText(intent.getStringExtra("note"))
        etInPlace.setText(intent.getStringExtra("place"))
        dateEdt = findViewById(R.id.et_income_date)
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd-MM-yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                etInDate.setText(sdf.format(cal.getTime()))
            }
        }
        dateEdt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@IncomeRecord,
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

        btnInAccount.setOnClickListener{
            showAccOptionsDialog()
        }

        btnInCategory.setOnClickListener{
            showCateOptionsDialog()
        }


        //update the record
        btnUpdateExp.setOnClickListener {
            val inAmount:String = etInAmount.text.toString()
            val inAccount:String = etInAccount.text.toString()
            val inCategory:String = etInCategory.text.toString()
            val inDate:String = etInDate.text.toString()
            val inNote:String = etInNote.text.toString()
            val inPlace:String = etInPlace.text.toString()
            val inId:String = intent.getStringExtra("id").toString()
            if(tempDate != inDate){
                var type="for update"
                deleteIncome(user.toString(),tempDate,inId,inAmount,oriAccID,oriGroup,type)
            }
            updateIncome(inAmount,inAccount,inCategory,inDate,inNote,inPlace,user.toString(),inId,oriAccID,oriGroup)}
        //delete the record
        btnDeleteExp.setOnClickListener {
            val inAmount:String = etInAmount.text.toString()
            val inDate:String = etInDate.text.toString()
            val inId:String = intent.getStringExtra("id").toString()
            val type:String ="for delete"
            deleteIncome(user.toString(),inDate,inId,inAmount,oriAccID,oriGroup,type)}
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
    private fun deleteIncome(user: String, date: String, id:String,amount:String,oriAccID: String,oriGroup: String,type:String){
        val db =  Firebase.firestore
        db.collection(user).document("income").collection(date).document(id)
            .delete()
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                var oriAmount = accOriAmount.toDouble() - intent.getStringExtra("amount")!!.toDouble()
                db.collection(user).document("account group").collection(oriGroup).document(oriAccID)
                    .update("Amount",oriAmount.toString())
                    .addOnSuccessListener {if(type=="for delete")Toast.makeText(this, "This record was successfully deleted!", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener{if(type=="for delete")Toast.makeText(this, "Success to delete expenses record but fail to update account old amount", Toast.LENGTH_SHORT).show()}
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error deleting document", e)
                Toast.makeText(this, "This record was failed to delete!", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateIncome(amount:String, account: String, category:String, date:String, note:String, place:String, user:String, id :String,oriAccID:String,oriGroup:String){
        val db =  Firebase.firestore
        val income = hashMapOf(
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
        db.collection(user).document("income").collection(date).document(id)
            .set(income)
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
        var oriAmount = accOriAmount.toDouble() - intent.getStringExtra("amount")!!.toDouble()
        db.collection(user).document("account group").collection(oriGroup).document(oriAccID)
            .update("Amount",oriAmount.toString())
            .addOnSuccessListener { addNewAccExRecord(user,amount,oriAmount.toString()) }
            .addOnFailureListener{Toast.makeText(this, "Success to update expenses record but fail to update old account amount", Toast.LENGTH_SHORT).show()}

    }

    private fun addNewAccExRecord(user:String,amount:String,oriAmount:String){
        var newAmount :Double = 0.0
        if (accChoice == -1){
            newAmount = oriAmount.toDouble() + amount.toDouble()
        }else{
            newAmount = accAmountList[accChoice].toDouble() + amount.toDouble()
        }


        db.collection(user).document("account group").collection(selectedGroup).document(selectedAccID)
            .update("Amount",newAmount.toString())
            .addOnSuccessListener {
                Toast.makeText(this, "Your income record has been updated!!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener{Toast.makeText(this, "Success to update old account amount but fail to update new account amount", Toast.LENGTH_SHORT).show()}
    }

    private fun retrieveOriAccAmount(user:String){
        db.collection(user).document("account group").collection(selectedGroup).document(selectedAccID)
            .get()
            .addOnSuccessListener { document->
                Log.d(ContentValues.TAG, "From Income, Original account group ${document.id} => ${document.data}")
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
            etInAccount.setText(accNameList[which], TextView.BufferType.EDITABLE)
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
            etInAccount.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
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
            etInCategory.setText(cateNameList[which], TextView.BufferType.EDITABLE)
            selectedCateID = cateIDList[which]
            cateChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etInCategory.setText(cateNameList[setChoice], TextView.BufferType.EDITABLE)
            selectedCateID = cateIDList[setChoice]
            cateChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }
}

