package com.example.gripmoney.Debt.AddDebtLend

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddLendFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var dateEdt: EditText
    lateinit var myFragment: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_add_lend, container, false)

        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")

        val btnSaveDebt: Button = myFragment.findViewById(R.id.btn_lend_save)
        val etLendAmount: EditText = myFragment.findViewById(R.id.et_lend_amt)
        val etLendName: EditText = myFragment.findViewById(R.id.et_lend_name)
        val etLendDate: EditText = myFragment.findViewById(R.id.et_lend_date)
        val etLendNote: EditText = myFragment.findViewById(R.id.et_lend_note)
        dateEdt = myFragment.findViewById(R.id.et_lend_date)
        dateEdt.setOnClickListener{showDatePickerDialog()}

        //update the record
        btnSaveDebt.setOnClickListener {
            val lendAmount:String = etLendAmount.text.toString()
            val lendName:String = etLendName.text.toString()
            val lendDate:String = etLendDate.text.toString()
            val lendNote:String = etLendNote.text.toString()
            saveLend(lendAmount,lendName,lendDate,lendNote,user.toString())
            etLendAmount.setText("")
            etLendName.setText("")
            etLendDate.setText("")
            etLendNote.setText("")
        }

        return myFragment
    }

    private fun saveLend(amount:String, name: String, date:String, note:String,user:String){
        val db =  Firebase.firestore
        val docName = getNewDocName(user)
        val Debt = hashMapOf(
            "DebtManageID" to docName,
            "Name" to name,
            "Amount" to amount,
            "Date" to date,
            "Note" to note,
        )
        db.collection(user).document("DebtManage").collection("Lend").document(docName)
            .set(Debt)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $documentReference")
                Toast.makeText(this.activity, "This record was successfully updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(this.activity, "This record was failed to updated!", Toast.LENGTH_SHORT).show()
            }
    }

    fun getNewDocName(user: String): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this.requireActivity())
        val newName: String = prefs.getString(user+"DebtManage", "DebtManage.1").toString()

        return newName
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddDebtFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddDebtFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}