package com.example.gripmoney.Debt

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.Debt.AddDebtLend.AddDebtManage
import com.example.gripmoney.MainActivity
import com.example.gripmoney.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DebtManageFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var myFragment:View
    lateinit var btnRecordNow : FloatingActionButton
    lateinit var debtTotal: TextView
    lateinit var lendTotal: TextView
    lateinit var assetTotal: TextView
    lateinit var debtRecyclerView : RecyclerView
    lateinit var lendRecyclerView : RecyclerView
    lateinit var debtAdapter: DebtRecyclerAdapter
    lateinit var lendAdapter: DebtRecyclerAdapter
    lateinit var debtList : ArrayList<DebtManageDataClass>
    lateinit var lendList : ArrayList<DebtManageDataClass>
    var debtTotalAmount = 0.00
    var lendTotalAmount = 0.00

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

        val defaultTotal = "0.00"
        debtTotal.text = defaultTotal
        lendTotal.text = defaultTotal
        assetTotal.text = defaultTotal
        debtTotalAmount = 0.00
        lendTotalAmount = 0.00
        debtRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        lendRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        debtRecyclerView.setHasFixedSize(true)
        lendRecyclerView.setHasFixedSize(true)
        debtList = arrayListOf()
        lendList = arrayListOf()
        debtAdapter = DebtRecyclerAdapter(debtList)
        lendAdapter = DebtRecyclerAdapter(lendList)
        debtRecyclerView.adapter = debtAdapter
        lendRecyclerView.adapter = lendAdapter
        getDebtList()
        getLendList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        callbackActivity = (activity as MainActivity)
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_debt_management, container, false)

        btnRecordNow = myFragment.findViewById(R.id.Debt_Float_Btn)
        debtTotal = myFragment.findViewById(R.id.tv_debt_total)
        lendTotal = myFragment.findViewById(R.id.tv_lend_total)
        assetTotal = myFragment.findViewById(R.id.tv_asset_total)

        val defaultTotal = "0.00"
        debtTotal.text = defaultTotal
        lendTotal.text = defaultTotal
        assetTotal.text = defaultTotal
        debtRecyclerView = myFragment.findViewById(R.id.debtRecyclerView)
        lendRecyclerView = myFragment.findViewById(R.id.lendRecyclerView)

        btnRecordNow.setOnClickListener{
            val intent= Intent(this.requireActivity(), AddDebtManage::class.java)
            startActivity(intent)
        }

        return myFragment
    }

    private fun getDebtList() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(callbackActivity)
        val editor = prefs.edit()

        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userID:String = sharedPreference?.getString("user","")!!

        var total: Double = 0.00


        db.collection(userID).document("DebtManage").collection("Debt").
        addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if(error!=null){
                    Log.e("Firestore error",error.message.toString())
                    return
                }

                for(dc: DocumentChange in value?.documentChanges!!){
                    if(dc.type== DocumentChange.Type.ADDED){
                        val myobject =dc.document.toObject(DebtManageDataClass::class.java)
                        if(myobject.Name != ""){
                            debtList.add(myobject)
                        }
                        val amount = myobject.Amount
                        total += amount.toDouble()

                        val name = dc.document.id
                        //intent.putExtra("docID",dc.document.id)
                        val substring: String =
                            name.subSequence(name.indexOf(".")+1, name.length).toString()
                        val newIndex = substring.toInt() + 1
                        val newName = name.subSequence(0, name.indexOf(".")+1).toString() + newIndex

                        Log.d(ContentValues.TAG, "New document name -> $newName")
                        editor.putString(userID+"DebtManage",newName)
                    }
                    else if (dc.type== DocumentChange.Type.REMOVED){
                        Log.d(ContentValues.TAG, "Removed data on firestore: ${dc.document.data.getValue("DebtManageID")}")
                        val element = debtList.filter{s -> s.DebtManageID == "${dc.document.data.getValue("DebtManageID")}"}
                        Log.d(ContentValues.TAG, "Removed data on array list: $element")
                    }
                    var tempid = dc.document.id
                    Log.d(ContentValues.TAG, "New Doc Name -> $tempid")
                    debtTotal.text = String.format("%.2f", total)
                    debtTotalAmount = total
                    val tempAssetTotal = debtTotalAmount + lendTotalAmount
                    assetTotal.text = String.format("%.2f", tempAssetTotal)
                }
                editor.apply()

                debtAdapter?.notifyDataSetChanged()
                debtAdapter.setonItemClickListener(object: DebtRecyclerAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val group = "Debt"
                        val id = debtList[position].DebtManageID
                        val amount = debtList[position].Amount
                        val name = debtList[position].Name
                        val date = debtList[position].Date
                        val note = debtList[position].Note
                        val i = Intent(activity, ViewDebtManage::class.java)
                        i.putExtra("group",group)
                        i.putExtra("id",id)
                        i.putExtra("amount",amount)
                        i.putExtra("name",name)
                        i.putExtra("date",date)
                        i.putExtra("note",note)
                        startActivity(i)
                    }
                })
            }
        })
    }

    private fun getLendList() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(callbackActivity)
        val editor = prefs.edit()

        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userID:String = sharedPreference?.getString("user","")!!

        var total: Double = 0.00


        db.collection(userID).document("DebtManage").collection("Lend").
        addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if(error!=null){
                    Log.e("Firestore error",error.message.toString())
                    return
                }

                for(dc: DocumentChange in value?.documentChanges!!){
                    if(dc.type== DocumentChange.Type.ADDED){
                        val myobject =dc.document.toObject(DebtManageDataClass::class.java)
                        if(myobject.Name != ""){
                            lendList.add(myobject)
                        }
                        val amount = myobject.Amount
                        total += amount.toDouble()

                        val name = dc.document.id
                        //intent.putExtra("docID",dc.document.id)
                        val substring: String =
                            name.subSequence(name.indexOf(".")+1, name.length).toString()
                        val newIndex = substring.toInt() + 1
                        val newName = name.subSequence(0, name.indexOf(".")+1).toString() + newIndex

                        Log.d(ContentValues.TAG, "New document name -> $newName")
                        editor.putString(userID+"DebtManage",newName)
                    }
                    else if (dc.type== DocumentChange.Type.REMOVED){
                        Log.d(ContentValues.TAG, "Removed data on firestore: ${dc.document.data.getValue("DebtManageID")}")
                        val element = lendList.filter{s -> s.DebtManageID == "${dc.document.data.getValue("DebtManageID")}"}
                        Log.d(ContentValues.TAG, "Removed data on array list: $element")
                    }
                    var tempid = dc.document.id
                    Log.d(ContentValues.TAG, "New Doc Name -> $tempid")
                    lendTotal.text = String.format("%.2f", total)
                    lendTotalAmount = total
                    val tempAssetTotal = debtTotalAmount + lendTotalAmount
                    assetTotal.text = String.format("%.2f", tempAssetTotal)
                }
                editor.apply()

                lendAdapter?.notifyDataSetChanged()
                lendAdapter.setonItemClickListener(object: DebtRecyclerAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val group = "Lend"
                        val id = lendList[position].DebtManageID
                        val amount = lendList[position].Amount
                        val name = lendList[position].Name
                        val date = lendList[position].Date
                        val note = lendList[position].Note
                        val i = Intent(activity, ViewDebtManage::class.java)
                        i.putExtra("group",group)
                        i.putExtra("id",id)
                        i.putExtra("amount",amount)
                        i.putExtra("name",name)
                        i.putExtra("date",date)
                        i.putExtra("note",note)
                        startActivity(i)
                    }
                })
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DebtManageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}