package com.example.gripmoney.Home.RecordNew

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecordNewIncomeFragment : Fragment() {

    lateinit var myFragment: View
    lateinit var dateEdt: EditText
    lateinit var btnSaveInc : Button
    lateinit var etInAccount: EditText
    lateinit var btnInAccount: Button
    lateinit var etInCategory: EditText
    lateinit var btnInCategory: Button
    lateinit var accIDList: ArrayList<String>
    lateinit var accNameList: ArrayList<String>
    lateinit var accGroupList: ArrayList<String>
    lateinit var accAmountList: ArrayList<String>
    lateinit var cateIDList: ArrayList<String>
    lateinit var cateNameList: ArrayList<String>
    val db = Firebase.firestore
    var accChoice = 0
    var cateChoice = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_record_new_income, container, false)
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")

        btnSaveInc=  myFragment.findViewById(R.id.btn_income_save)
        val etInAmount = myFragment.findViewById<EditText>(R.id.et_income_amount)
        etInAccount = myFragment.findViewById(R.id.et_income_acc)
        etInCategory = myFragment.findViewById(R.id.et_income_category)
        btnInAccount = myFragment.findViewById(R.id.btn_income_acc)
        btnInCategory = myFragment.findViewById(R.id.btn_income_category)
        val etInDate: EditText = myFragment.findViewById(R.id.et_income_date)
        val etInNote: EditText =myFragment.findViewById(R.id.et_income_note)
        val etInPlace: EditText =myFragment.findViewById(R.id.et_income_place)
        accIDList = arrayListOf()
        accNameList = arrayListOf()
        accGroupList = arrayListOf()
        accAmountList = arrayListOf()
        cateIDList = arrayListOf()
        cateNameList = arrayListOf()

        retrieveAccOrCate(user!!)

        btnInAccount.setOnClickListener{
            showAccOptionsDialog()
        }

        btnInCategory.setOnClickListener{
            showCateOptionsDialog()
        }

        btnSaveInc.setOnClickListener{
            val inAmount:String = etInAmount.text.toString()
            val inAccount:String = etInAccount.text.toString()
            val inCategory:String = etInCategory.text.toString()
            val inDate:String = etInDate.text.toString()
            val inNote:String = etInNote.text.toString()
            val inPlace:String = etInPlace.text.toString()
            //count number of times adding to database
            addIncome(inAmount, inAccount, inCategory, inDate, inNote, inPlace, user.toString())

            etInAmount.setText("")
            etInAccount.setText("")
            etInCategory.setText("")
            etInDate.setText("")
            etInNote.setText("")
            etInPlace.setText("")

        }
        dateEdt = myFragment.findViewById(R.id.et_income_date)
        dateEdt.setOnClickListener{showDatePickerDialog()}

        return myFragment
    }
    private fun addIncome(amount:String, account: String, category:String, date:String, note:String, place:String, user:String){
        val income = hashMapOf(
            "Account" to account,
            "AccountID" to accIDList[accChoice],
            "Group" to accGroupList[accChoice],
            "Amount" to amount,
            "Category" to category,
            "CateID" to cateIDList[cateChoice],
            "Date" to date,
            "Note" to note,
            "Place" to place
        )

        db.collection(user).document("income").collection(date)
            .add(income)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                val newAmount:Double = accAmountList[accChoice].toDouble() + amount.toDouble()

                db.collection(user).document("account group").collection(accGroupList[accChoice]).document(accIDList[accChoice])
                    .update("Amount",newAmount.toString())
                    .addOnSuccessListener { Toast.makeText(this.activity, "Your income was successfully saved!", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener{Toast.makeText(this.activity, "Success to save income record but fail to update account amount", Toast.LENGTH_SHORT).show()}
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(this.activity, "Your data was failed to saved!", Toast.LENGTH_SHORT).show()
            }
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
        var builder : AlertDialog.Builder = AlertDialog.Builder(this.requireActivity())
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(accNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etInAccount.setText(accNameList[which], TextView.BufferType.EDITABLE)
            accChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etInAccount.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
            accChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }

    fun showCateOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this.requireActivity())
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(cateNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etInCategory.setText(cateNameList[which], TextView.BufferType.EDITABLE)
            cateChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etInCategory.setText(cateNameList[setChoice], TextView.BufferType.EDITABLE)
            cateChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }


}