package com.example.gripmoney.Home.HomeTransfer

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.Home.RecordNew.DatePickerFragment
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TransferRecord : AppCompatActivity() {
    lateinit var btnUpdateExp : Button
    lateinit var btnDeleteExp : Button
    lateinit var dateEdt: EditText
    lateinit var etTrFrom : EditText
    lateinit var etTrTo : EditText
    lateinit var accIDList: ArrayList<String>
    lateinit var accNameList: ArrayList<String>
    lateinit var accGroupList: ArrayList<String>
    lateinit var accAmountList: ArrayList<String>
    lateinit var selectedFromAccID : String
    lateinit var selectedFromGroup : String
    lateinit var oriFromAccAmount : String
    lateinit var selectedToAccID : String
    lateinit var selectedToGroup : String
    lateinit var oriToAccAmount : String
    lateinit var tempDate:String
    var cal = Calendar.getInstance()
    val db = Firebase.firestore
    var accFromChoice = -1
    var accToChoice = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_record)
        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var actionBar = supportActionBar
        actionBar!!.title="My Transfer"
        btnUpdateExp= findViewById(R.id.btn_transfer_update)
        btnDeleteExp = findViewById(R.id.btn_transfer_delete)
        etTrFrom = findViewById(R.id.et_transfer_from)
        etTrTo = findViewById(R.id.et_transfer_to)
        val etTrAmount: EditText = findViewById(R.id.et_transfer_amount)
        val etTrDate: EditText = findViewById(R.id.et_transfer_date)
        val etTrNote: EditText =findViewById(R.id.et_transfer_note)
        etTrTo.setText(intent.getStringExtra("to"))
        selectedToAccID = intent.getStringExtra("accToID").toString()
        val oriToAccID = intent.getStringExtra("accToID").toString()
        selectedToGroup = intent.getStringExtra("accToGroup").toString()
        val oriToGroup = intent.getStringExtra("accToGroup").toString()
        etTrAmount.setText(intent.getStringExtra("amount"))
        etTrFrom.setText(intent.getStringExtra("from"))
        selectedFromAccID = intent.getStringExtra("accFromID").toString()
        val oriFromAccID = intent.getStringExtra("accFromID").toString()
        selectedFromGroup = intent.getStringExtra("accFromGroup").toString()
        val oriFromGroup = intent.getStringExtra("accFromGroup").toString()
        etTrDate.setText(intent.getStringExtra("date"))
        tempDate= intent.getStringExtra("date").toString()
        etTrNote.setText(intent.getStringExtra("note"))
        dateEdt = findViewById(R.id.et_transfer_date)
        val btnTrFrom:Button = findViewById(R.id.btn_transfer_from)
        val btnTrTo:Button = findViewById(R.id.btn_transfer_to)

        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd-MM-yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                etTrDate.setText(sdf.format(cal.getTime()))
            }
        }
        dateEdt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@TransferRecord,
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

        retrieveAcc(user!!)
        oriFromAccAmount = ""
        oriToAccAmount = ""
        retrieveOriAccAmount(user,selectedFromAccID,selectedFromGroup,selectedToAccID,selectedToGroup)

        btnTrFrom.setOnClickListener {
            showTrFromOptionsDialog()
        }

        btnTrTo.setOnClickListener {
            showTrToOptionsDialog()
        }

        //update the record
        btnUpdateExp.setOnClickListener {
            val trAmount:String = etTrAmount.text.toString()
            val trTo:String = etTrTo.text.toString()
            val trFrom:String = etTrFrom.text.toString()
            val trDate:String = etTrDate.text.toString()
            val trNote:String = etTrNote.text.toString()
            val trId:String = intent.getStringExtra("id").toString()
            if(tempDate != trDate){
                var type="for update"
                deleteTransfer(user.toString(),tempDate,trId,trAmount,oriFromAccID,oriFromGroup,oriToAccID,oriToGroup,type)
            }
            updateTransfer(trAmount,trTo,trFrom,trDate,trNote,user.toString(),trId,oriFromAccID,oriFromGroup,oriToAccID,oriToGroup)}

        //delete the record
        btnDeleteExp.setOnClickListener {
            val trAmount:String = etTrAmount.text.toString()
            val trDate:String = etTrDate.text.toString()
            val trId:String = intent.getStringExtra("id").toString()
            val type:String ="for delete"
            deleteTransfer(user.toString(),trDate,trId,trAmount,oriFromAccID,oriFromGroup,oriToAccID,oriToGroup,type)}
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
    private fun deleteTransfer(user: String, date: String, id:String,amount: String,oriFromAccID:String,oriFromGroup:String,oriToAccID:String,oriToGroup:String, type:String){
        val db =  Firebase.firestore
        db.collection(user).document("transfer").collection(date).document(id)
            .delete()
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                var oriFromAmount = oriFromAccAmount.toDouble() + intent.getStringExtra("amount")!!.toDouble()
                db.collection(user).document("account group").collection(oriFromGroup).document(oriFromAccID)
                    .update("Amount",oriFromAmount.toString())
                    .addOnSuccessListener {
                        var oriToAmount = oriToAccAmount.toDouble() - intent.getStringExtra("amount")!!.toDouble()
                        db.collection(user).document("account group").collection(oriToGroup).document(oriToAccID)
                            .update("Amount",oriToAmount.toString())
                            .addOnSuccessListener {if(type=="for delete")Toast.makeText(this, "This record was successfully deleted!", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener{if(type=="for delete")Toast.makeText(this, "Success to update from account amount but fail to update to account amount", Toast.LENGTH_SHORT).show()}
                    }
                    .addOnFailureListener{Toast.makeText(this, "Success to delete expenses record but fail to update from account amount", Toast.LENGTH_SHORT).show()}

                finish()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error deleting document", e)
                Toast.makeText(this, "This record was failed to delete!", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateTransfer(amount:String, to: String, from:String, date:String, note:String, user:String, id :String,oriFromAccID:String,oriFromGroup:String,oriToAccID:String,oriToGroup:String){
        val db =  Firebase.firestore
        val transfer = hashMapOf(
            "From" to from,
            "AccFromID" to selectedFromAccID,
            "AccFromGroup" to selectedFromGroup,
            "Amount" to amount,
            "To" to to,
            "AccToID" to selectedToAccID,
            "AccToGroup" to selectedToGroup,
            "Date" to date,
            "Note" to note,
        )
        db.collection(user).document("transfer").collection(date).document(id)
            .set(transfer)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $documentReference")
                deleteAccOldTransfer(user,amount,oriFromAccID,oriFromGroup,oriToAccID,oriToGroup)
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(this, "This record was failed to updated!", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteAccOldTransfer(user:String, amount: String,oriFromAccID:String,oriFromGroup:String,oriToAccID:String,oriToGroup:String){
        var oriFromAmountPosition = accIDList.indexOf(oriFromAccID).toString()
        var oriFromAmount = oriFromAccAmount.toDouble() + intent.getStringExtra("amount")!!.toDouble()
        accAmountList[oriFromAmountPosition.toInt()] = oriFromAmount.toString()

        db.collection(user).document("account group").collection(oriFromGroup).document(oriFromAccID)
            .update("Amount",oriFromAmount.toString())
            .addOnSuccessListener {
                //var oriFromAmountPosition = accIDList.indexOfFirst { it == oriFromAccID }
                var oriToAmountPosition = accIDList.indexOf(oriToAccID).toString()
                var oriToAmount = oriToAccAmount.toDouble() - intent.getStringExtra("amount")!!.toDouble()
                accAmountList[oriToAmountPosition.toInt()] = oriToAmount.toString()
                db.collection(user).document("account group").collection(oriToGroup).document(oriToAccID)
                    .update("Amount",oriToAmount.toString())
                    .addOnSuccessListener { addingAccNewTransfer(user,amount,oriFromAmount.toString(), oriFromAccID,oriFromGroup,oriToAmount.toString(), oriToAccID,oriToGroup) }
                    .addOnFailureListener{Toast.makeText(this, "Success to update from account amount but fail to update to account amount", Toast.LENGTH_SHORT).show()}
            }
            .addOnFailureListener{Toast.makeText(this, "Fail to deduct old account amount", Toast.LENGTH_SHORT).show()}
    }

    fun addingAccNewTransfer(user:String, amount:String,oriFromAmount:String, oriFromAccID:String,oriFromGroup:String,oriToAmount:String, oriToAccID:String,oriToGroup:String){
        var newFromAmount = 0.0
        var newToAmount = 0.0
        if(accFromChoice == -1){
            newFromAmount = oriFromAmount.toDouble() - amount.toDouble()
        }else {
            newFromAmount = accAmountList[accFromChoice].toDouble() - amount.toDouble()
        }
        if(accToChoice == -1){
            newToAmount = oriToAmount.toDouble() + amount.toDouble()
        }else {
            newToAmount = accAmountList[accToChoice].toDouble() + amount.toDouble()
        }

        db.collection(user).document("account group").collection(selectedFromGroup).document(selectedFromAccID)
            .update("Amount",newFromAmount.toString())
            .addOnSuccessListener {
                db.collection(user).document("account group").collection(selectedToGroup).document(selectedToAccID)
                    .update("Amount",newToAmount.toString())
                    .addOnSuccessListener { Toast.makeText(this, "Your transfer record was successfully saved!", Toast.LENGTH_SHORT).show()
                                            finish()}
                    .addOnFailureListener{Toast.makeText(this, "Success to update from account amount but fail to update to account amount", Toast.LENGTH_SHORT).show()}
            }
            .addOnFailureListener{Toast.makeText(this, "Fail to deduct old account amount", Toast.LENGTH_SHORT).show()}
    }

    private fun retrieveOriAccAmount(user:String,selectedFromAccID:String,selectedFromGroup:String,selectedToAccID:String,selectedToGroup:String){
        db.collection(user).document("account group").collection(selectedFromGroup).document(selectedFromAccID)
            .get()
            .addOnSuccessListener { document->
                Log.d(ContentValues.TAG, "From Transfer, Original from account amount ${document.id} => ${document.data?.get("Amount")}")
                 oriFromAccAmount = document.data?.get("Amount").toString()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
        Log.d(ContentValues.TAG, "From Transfer, current accOriAmount = " +oriFromAccAmount.toString())

        db.collection(user).document("account group").collection(selectedToGroup).document(selectedToAccID)
            .get()
            .addOnSuccessListener { document->
                Log.d(ContentValues.TAG, "From Transfer, Original to account amount ${document.id} => ${document.data?.get("Amount")}")
                oriToAccAmount = document.data?.get("Amount").toString()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
        Log.d(ContentValues.TAG, "From Transfer, current accToAmount = " + oriToAccAmount)


    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }

    private fun retrieveAcc(user: String) {
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
    }

    fun showTrFromOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(accNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etTrFrom.setText(accNameList[which], TextView.BufferType.EDITABLE)
            selectedFromAccID = accIDList[which]
            selectedFromGroup = accGroupList[which]
            if(selectedFromAccID == intent.getStringExtra("accFromID").toString()){
                accFromChoice = -1
            }else{
                accFromChoice = which
            }
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etTrFrom.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
            selectedFromAccID = accIDList[setChoice]
            selectedFromGroup = accGroupList[setChoice]
            if(selectedFromAccID == intent.getStringExtra("accFromID").toString()){
                accFromChoice = -1
            }else{
                accFromChoice = setChoice
            }
            dialog.dismiss()
        })
        builder.show()
    }

    fun showTrToOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(accNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etTrTo.setText(accNameList[which], TextView.BufferType.EDITABLE)
            selectedToAccID = accIDList[which]
            selectedToGroup = accGroupList[which]
            if(selectedToAccID == intent.getStringExtra("accToID")){
                accToChoice = -1
            }else{
                accToChoice = which
            }
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etTrTo.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
            selectedToAccID = accIDList[setChoice]
            selectedToGroup = accGroupList[setChoice]
            if(selectedToAccID == intent.getStringExtra("accToID")){
                accToChoice = -1
            }else{
                accToChoice = setChoice
            }
            accToChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }
}