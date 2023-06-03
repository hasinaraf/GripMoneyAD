package com.example.gripmoney.Home.RecordNew

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecordNewTransferFragment : Fragment() {

    lateinit var myFragment: View
    lateinit var dateEdt: EditText
    lateinit var btnSaveTrsf : Button
    lateinit var etTrFrom : EditText
    lateinit var etTrTo : EditText
    lateinit var accIDList: ArrayList<String>
    lateinit var accNameList: ArrayList<String>
    lateinit var accGroupList: ArrayList<String>
    lateinit var accAmountList: ArrayList<String>
    val db = Firebase.firestore
    var accFromChoice = 0
    var accToChoice = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_record_new_transfer, container, false)
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")

        btnSaveTrsf=  myFragment.findViewById(R.id.btn_transfer_save)
        etTrFrom = myFragment.findViewById<EditText>(R.id.et_transfer_from)
        val etTrAmount: EditText = myFragment.findViewById(R.id.et_transfer_amount)
        etTrTo = myFragment.findViewById(R.id.et_transfer_to)
        val etTrDate: EditText = myFragment.findViewById(R.id.et_transfer_date)
        val etTrNote: EditText =myFragment.findViewById(R.id.et_transfer_note)
        val btnTrFrom:Button = myFragment.findViewById(R.id.btn_transfer_from)
        val btnTrTo:Button = myFragment.findViewById(R.id.btn_transfer_to)
        accIDList = arrayListOf()
        accNameList = arrayListOf()
        accGroupList = arrayListOf()
        accAmountList = arrayListOf()

        retrieveAcc(user!!)

        btnTrFrom.setOnClickListener {
            showTrFromOptionsDialog()
        }

        btnTrTo.setOnClickListener {
            showTrToOptionsDialog()
        }

        btnSaveTrsf.setOnClickListener{
            val trAmount:String = etTrAmount.text.toString()
            val trFrom:String = etTrFrom.text.toString()
            val trTo:String = etTrTo.text.toString()
            val trDate:String = etTrDate.text.toString()
            val trNote:String = etTrNote.text.toString()

            //count number of times adding to database
            addTransfer(trAmount, trFrom, trTo, trNote, trDate, user.toString())
            etTrAmount.setText("")
            etTrFrom.setText("")
            etTrTo.setText("")
            etTrDate.setText("")
            etTrNote.setText("")

        }
        dateEdt = myFragment.findViewById(R.id.et_transfer_date)
        dateEdt.setOnClickListener{showDatePickerDialog()}

        return myFragment
    }
    private fun addTransfer(amount:String, from:String, to:String, note:String, date:String, user:String){
        val transfer = hashMapOf(
            "From" to from,
            "AccFromID" to accIDList[accFromChoice],
            "AccFromGroup" to accGroupList[accFromChoice],
            "Amount" to amount,
            "To" to to,
            "AccToID" to accIDList[accToChoice],
            "AccToGroup" to accGroupList[accToChoice],
            "Date" to date,
            "Note" to note,
        )
        db.collection(user).document("transfer").collection(date)
            .add(transfer)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                deductFromAmount(user,amount)

            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(this.activity, "Your data was failed to saved!", Toast.LENGTH_SHORT).show()
            }
    }

    fun deductFromAmount(user:String, amount: String){
        val newFromAmount:Double = accAmountList[accFromChoice].toDouble() - amount.toDouble()

        db.collection(user).document("account group").collection(accGroupList[accFromChoice]).document(accIDList[accFromChoice])
            .update("Amount",newFromAmount.toString())
            .addOnSuccessListener { addingToAmount(user,amount) }
            .addOnFailureListener{Toast.makeText(this.activity, "Success to save transfer record but fail to deduct from account amount", Toast.LENGTH_SHORT).show()}
    }

    fun addingToAmount(user:String, amount:String){
        val newToAmount:Double = accAmountList[accToChoice].toDouble() + amount.toDouble()

        db.collection(user).document("account group").collection(accGroupList[accToChoice]).document(accIDList[accToChoice])
            .update("Amount",newToAmount.toString())
            .addOnSuccessListener { Toast.makeText(this.activity, "Your transfer record was successfully saved!", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener{Toast.makeText(this.activity, "Success to save deduct from account amount but fail to adding to account amount", Toast.LENGTH_SHORT).show()}
    }

    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment{day, month, year -> onDateSelected(day, month, year)}
        datePicker.show(childFragmentManager,"datePicker")
    }

    private fun onDateSelected(day:Int, month:Int, year:Int){
        val dy:String = String.format("%02d",day)
        val mon:String = String.format("%02d",month)
        dateEdt.setText("$dy-$mon-$year")
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
        var builder : AlertDialog.Builder = AlertDialog.Builder(this.requireActivity())
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(accNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etTrFrom.setText(accNameList[which], TextView.BufferType.EDITABLE)
            accFromChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etTrFrom.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
            accFromChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }

    fun showTrToOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this.requireActivity())
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(accNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etTrTo.setText(accNameList[which], TextView.BufferType.EDITABLE)
            accToChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etTrTo.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
            accToChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }

}