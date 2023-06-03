package com.example.gripmoney.AccountGroup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.Login.LoginChoice
import com.example.gripmoney.MainActivity
import com.example.gripmoney.Premium.PremiumPay
import com.example.gripmoney.R
import com.example.gripmoney.Settings.Pin.SettingSetPin
import com.example.gripmoney.Settings.SettingChangePass
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccGroupFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var param1: String? = null
    private var param2: String? = null
    private var db:FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var myFragment:View
    lateinit var recyclerView: RecyclerView
    lateinit var accList: ArrayList<AccDataClass>
    lateinit var totalAmount: TextView
    lateinit var ttl: String
    lateinit var adapter: AccRecyclerAdapter
    lateinit var intent: Intent
    lateinit var btnHide: ImageButton
    lateinit var btnUnhide:ImageButton

    private lateinit var callbackActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        accList = arrayListOf()
        adapter = AccRecyclerAdapter(accList)
        recyclerView.adapter = adapter
        getAccountGroup()
        loadState()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        callbackActivity = (activity as MainActivity)
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_acc_group, container, false)

        totalAmount = myFragment.findViewById(R.id.totalAmount)

        recyclerView = myFragment.findViewById(R.id.recyclerView)

        intent = Intent(activity, ViewAccGroup::class.java)

        btnHide = myFragment.findViewById(R.id.ibtnHide)
        btnUnhide = myFragment.findViewById(R.id.ibtnUnHide)

        var btnAddAcc = myFragment.findViewById<ImageButton>(R.id.btnAddAcc)
        var db = FirebaseFirestore.getInstance()
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")

        btnAddAcc.setOnClickListener{
            startActivity(AddAccGroup.newIntent(callbackActivity))
        }

        btnHide.setOnClickListener{
            val ref = db.collection(user.toString()).document("pin")
            ref.get()
                .addOnSuccessListener { document ->
                    if (document.data?.getValue("number").toString() == "0") {
                        var dialog: AlertDialog.Builder = AlertDialog.Builder(this.activity)
                        dialog.setTitle("Error!!")
                        dialog.setMessage("Pin have not been set up. To enable this feature, please set up your pin in setting.")
                        dialog.setPositiveButton("Set Up Now"){
                                dialog, which ->
                            val intent = Intent(activity, SettingSetPin::class.java)
                            startActivity(intent)
                        }
                        dialog.setNegativeButton("Dismiss") { dialog, which -> dialog.dismiss() }
                        val alertDialog = dialog.create()
                        alertDialog.show()
                        Log.d(ContentValues.TAG, "Document pin data: ${document.data}")
                    }else{
                        showCustomDialog()
                        Log.d(ContentValues.TAG, "Document Username data: ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "get failed with ", exception)
                }
        }

        btnUnhide.setOnClickListener {
            showDialog()
        }

        return myFragment
    }

    private fun getAccountGroup() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(callbackActivity)
        val editor = prefs.edit()

        val pref = this.activity?.getSharedPreferences("HIDE_STATE", Context.MODE_PRIVATE)
        val visibility = pref?.getBoolean("visibility",false)

        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userID:String = sharedPreference?.getString("user","")!!

        var total: Double = 0.00

        for(category in accountCategory){
            db.collection(userID).document("account group").collection(category).
            addSnapshotListener(object:EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if(error!=null){
                        Log.e("Firestore error",error.message.toString())
                        return
                    }

                    for(dc: DocumentChange in value?.documentChanges!!){
                        if(dc.type==DocumentChange.Type.ADDED){
                            val myobject =dc.document.toObject(AccDataClass::class.java)
                            if(myobject.Name != ""){
                                accList.add(myobject)
                            }
                            val amount = myobject.Amount
                            total += amount.toDouble()
                            ttl = String.format("%.2f", total)

                            val name = dc.document.id
                            //intent.putExtra("docID",dc.document.id)
                            val substring: String =
                                name.subSequence(name.indexOf(".")+1, name.length).toString()
                            val newIndex = substring.toInt() + 1
                            val newName = name.subSequence(0, name.indexOf(".")+1).toString() + newIndex

                            Log.d(TAG, "New document name -> $newName")
                            editor.putString("Account Group " + category,newName)
                        }
                        else if (dc.type==DocumentChange.Type.REMOVED){
                            Log.d(TAG, "Removed data on firestore: ${dc.document.data.getValue("AccountID")}")
                            val element = accList.filter{s -> s.AccountID == "${dc.document.data.getValue("AccountID")}"}
                            Log.d(TAG, "Removed data on array list: $element")
                        }
                        var tempid = dc.document.id
                        Log.d(TAG, "New Doc Name -> $tempid")
                        val editors = pref?.edit()
                        if(visibility == true){
                            totalAmount.text = "****.**"
                            editors?.putString("amount", "****.**")
                            btnHide.visibility = View.INVISIBLE
                            btnUnhide.visibility = View.VISIBLE
                        }else{
                            totalAmount.text = String.format("%.2f", total)
                            editors?.putString("amount", total.toString())
                            btnHide.visibility = View.VISIBLE
                            btnUnhide.visibility = View.INVISIBLE
                        }
                        editors?.apply()
                    }
                    editor.apply()

                    adapter?.notifyDataSetChanged()
                    adapter.setonItemClickListener(object: AccRecyclerAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val id = accList[position].AccountID
                            val amount = accList[position].Amount
                            val name = accList[position].Name
                            val group = accList[position].Group
                            val note = accList[position].Note
                            val image = accList[position].Image
                            intent.putExtra("id",id)
                            intent.putExtra("amount",amount)
                            intent.putExtra("name",name)
                            intent.putExtra("group",group)
                            intent.putExtra("note",note)
                            intent.putExtra("image",image)
                            startActivity(intent)
                        }
                    })
                }
            })
        }
    }

    private fun showCustomDialog(){
        var dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.setting_check_pin)
        var db = FirebaseFirestore.getInstance()
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var pin = dialog.findViewById<EditText>(R.id.etUpin)
        var btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        btnHide = myFragment.findViewById(R.id.ibtnHide)
        btnUnhide = myFragment.findViewById(R.id.ibtnUnHide)

        btnConfirm.setOnClickListener {
            if(pin.text.toString() != ""){
                val doc = db.collection(user.toString()).document("pin")
                doc.get()
                    .addOnSuccessListener { document ->
                        if (pin.text.toString() == document.data?.getValue("number").toString()) {
                            Toast.makeText(this.activity, "Your data has been hide!", Toast.LENGTH_SHORT).show()
                            totalAmount.text = "****.**"
                            btnHide.visibility = View.INVISIBLE
                            btnUnhide.visibility = View.VISIBLE
                            val pref = this.activity?.getSharedPreferences("HIDE_STATE", Context.MODE_PRIVATE)
                            val editor = pref?.edit()
                            editor?.putBoolean("visibility", true)
                            editor?.apply()
                            adapter?.notifyDataSetChanged()
                            dialog.dismiss()
                            Log.d(ContentValues.TAG, "Document pin data: ${document.data}")
                        } else {
                            Toast.makeText(this.activity, "Wrong pin! Please try again", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            Log.d(ContentValues.TAG, "No such document")
                        }
                    }
            }else{
                Toast.makeText(this.activity, "Error! Pin cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showDialog(){
        var dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.setting_check_pin)
        var db = FirebaseFirestore.getInstance()
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var pin = dialog.findViewById<EditText>(R.id.etUpin)
        var btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        btnHide = myFragment.findViewById(R.id.ibtnHide)
        btnUnhide = myFragment.findViewById(R.id.ibtnUnHide)

        btnConfirm.setOnClickListener {
            if(pin.text.toString() != ""){
                val doc = db.collection(user.toString()).document("pin")
                doc.get()
                    .addOnSuccessListener { document ->
                        if (pin.text.toString() == document.data?.getValue("number").toString()) {
                            Toast.makeText(this.activity, "Your data has been Unhide!", Toast.LENGTH_SHORT).show()
                            totalAmount.text = ttl
                            btnHide.visibility = View.VISIBLE
                            btnUnhide.visibility = View.INVISIBLE
                            val pref = this.activity?.getSharedPreferences("HIDE_STATE", Context.MODE_PRIVATE)
                            val editor = pref?.edit()
                            editor?.putBoolean("visibility", false)
                            editor?.apply()
                            adapter?.notifyDataSetChanged()
                            dialog.dismiss()
                            Log.d(ContentValues.TAG, "Document pin data: ${document.data}")
                        } else {
                            Toast.makeText(this.activity, "Wrong pin! Please try again", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            Log.d(ContentValues.TAG, "No such document")
                        }
                    }
            }else{
                Toast.makeText(this.activity, "Error! Pin cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun loadState(){
        val pref = this.activity?.getSharedPreferences("HIDE_STATE", Context.MODE_PRIVATE)
        val hideAmount = pref?.getString("amount","0.00")
        val visibility = pref?.getBoolean("visibility",false)
        totalAmount.text = hideAmount
        if(visibility == true){
            btnHide.visibility = View.INVISIBLE
            btnUnhide.visibility = View.VISIBLE
        }else{
            btnHide.visibility = View.VISIBLE
            btnUnhide.visibility = View.INVISIBLE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccGroupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccGroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        var accountCategory = arrayOf("Accounts","Card","Cash","Debit Card","Investment")
    }

    fun notifyUser(str:String){
        Toast.makeText(callbackActivity,str,Toast.LENGTH_LONG).show()
    }
}