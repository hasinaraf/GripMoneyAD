package com.example.gripmoney.Home.RecordNew

import android.content.ContentValues
import android.content.ContentValues.TAG
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.gripmoney.AccountGroup.AccDataClass
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecordNewExpensesFragment : Fragment() {
    lateinit var myFragment: View
    lateinit var dateEdt: EditText
    lateinit var btnSaveExp : Button
    lateinit var cardView: CardView
    lateinit var etExAccount : EditText
    lateinit var btnExpensesAcc: Button
    lateinit var etExCategory: EditText
    lateinit var btnExpensesCate: Button
    lateinit var accIDList: ArrayList<String>
    lateinit var accNameList: ArrayList<String>
    lateinit var accGroupList: ArrayList<String>
    lateinit var accAmountList: ArrayList<String>
    lateinit var cateIDList: ArrayList<String>
    lateinit var cateNameList: ArrayList<String>
    var accChoice = 0
    var cateChoice = 0

    val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_record_new_expenses, container, false)
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        cardView = myFragment.findViewById(R.id.cvFooter)
        cardView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        btnSaveExp=  myFragment.findViewById(R.id.btn_expenses_save)
        btnExpensesAcc = myFragment.findViewById(R.id.btn_expenses_acc)
        btnExpensesCate = myFragment.findViewById(R.id.btn_expenses_category)
        etExAccount = myFragment.findViewById(R.id.et_expenses_acc)
        etExCategory = myFragment.findViewById(R.id.et_expenses_category)
        val etExAmount: EditText = myFragment.findViewById(R.id.et_expenses_amount)
        val etExDate: EditText = myFragment.findViewById(R.id.et_expenses_date)
        val etExNote: EditText =myFragment.findViewById(R.id.et_expenses_note)
        val etExPlace: EditText =myFragment.findViewById(R.id.et_expenses_place)
        accIDList = arrayListOf()
        accNameList = arrayListOf()
        accGroupList = arrayListOf()
        accAmountList = arrayListOf()
        cateIDList = arrayListOf()
        cateNameList = arrayListOf()

        retrieveAccOrCate(user!!)

        btnExpensesAcc.setOnClickListener{
            showAccOptionsDialog()
        }

        btnExpensesCate.setOnClickListener{
            showCateOptionsDialog()
        }

        btnSaveExp.setOnClickListener{
            val exAmount:String = etExAmount.text.toString()
            val exAccount:String = etExAccount.text.toString()
            val exCategory:String = etExCategory.text.toString()
            val exDate:String = etExDate.text.toString()
            val exNote:String = etExNote.text.toString()
            val exPlace:String = etExPlace.text.toString()
            //count number of times adding to database
            addExpenses(exAmount,exAccount,exCategory,exDate,exNote,exPlace,user.toString())
            etExAmount.setText("")
            etExAccount.setText("")
            etExCategory.setText("")
            etExDate.setText("")
            etExNote.setText("")
            etExPlace.setText("")
        }
        dateEdt = myFragment.findViewById(R.id.et_expenses_date)
        dateEdt.setOnClickListener{showDatePickerDialog()}

        return myFragment
    }

    private fun addExpenses(amount:String, account: String, category:String, date:String, note:String, place:String, user:String){
        val expenses = hashMapOf(
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
        db.collection(user).document("expenses").collection(date)
            .add(expenses)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                val newAmount:Double = accAmountList[accChoice].toDouble() - amount.toDouble()

                db.collection(user).document("account group").collection(accGroupList[accChoice]).document(accIDList[accChoice])
                    .update("Amount",newAmount.toString())
                    .addOnSuccessListener { Toast.makeText(this.activity, "Your expenses was successfully saved!", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener{Toast.makeText(this.activity, "Success to save expenses record but fail to update account amount", Toast.LENGTH_SHORT).show()}
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
                        Log.d(TAG, "${document.id} => ${document.data}")
                        accIDList.add(document.id)
                        accNameList.add(document.data.get("Name").toString())
                        accGroupList.add(accDoc)
                        accAmountList.add(document.data.get("Amount").toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }

        db.collection(user).document("category").collection("category1")
            .get()
            .addOnSuccessListener { result->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    cateIDList.add(document.id)
                    cateNameList.add(document.get("Name").toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

    }

    fun showAccOptionsDialog() {
        var tempChoice = 1
        var setChoice = 0
        var builder : AlertDialog.Builder = AlertDialog.Builder(this.requireActivity())
        builder.setTitle("Choose the Group of the Account")
        builder.setSingleChoiceItems(accNameList.toTypedArray(), -1, DialogInterface.OnClickListener(){ dialog, which->
            etExAccount.setText(accNameList[which], TextView.BufferType.EDITABLE)
            accChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etExAccount.setText(accNameList[setChoice], TextView.BufferType.EDITABLE)
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
            etExCategory.setText(cateNameList[which], TextView.BufferType.EDITABLE)
            cateChoice = which
            tempChoice = which
        })
        builder.setPositiveButton("Select", DialogInterface.OnClickListener(){ dialog, which->
            setChoice = tempChoice
            dialog.dismiss()
        })
        builder.setNegativeButton("Exit", DialogInterface.OnClickListener(){ dialog, which->
            etExCategory.setText(cateNameList[setChoice], TextView.BufferType.EDITABLE)
            cateChoice = setChoice
            dialog.dismiss()
        })
        builder.show()
    }


}