package com.example.gripmoney.Home.HomeTransfer

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.Home.RecordNew.RecordNew
import com.example.gripmoney.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TransferYearFragment : Fragment() {

    private var total: Double = 0.0
    lateinit var myFragment:View
    lateinit var btnRecordNow : FloatingActionButton
    lateinit var chartButton: ImageView
    lateinit var tv_total: TextView
    lateinit var tv_days: TextView
    lateinit var cardView: CardView
    lateinit var recyclerView: RecyclerView
    lateinit var amounts: ArrayList<String>
    lateinit var transaction: ArrayList<String>
    lateinit var dataArrayList: ArrayList<DataTransfer>
    lateinit var listAdapter: ListTransferAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_transfer_year, container, false)
        tv_total = myFragment.findViewById(R.id.tv_total)
        recyclerView = myFragment.findViewById(R.id.dailyTrfRecyclerView)
        dataArrayList = arrayListOf()
        amounts = arrayListOf()
        transaction = arrayListOf()
        tv_days =myFragment.findViewById(R.id.tv_days)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        listAdapter = ListTransferAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        chartButton = myFragment.findViewById(R.id.chart)
        btnRecordNow = myFragment.findViewById<FloatingActionButton>(R.id.fab_record_now)
        btnRecordNow.setOnClickListener{
            val intent=Intent(activity,RecordNew::class.java)
            startActivity(intent)
        }
        cardView = myFragment.findViewById(R.id.cvFooter)
        chartButton.setOnClickListener {
            val intent=Intent(activity, ChartTransfer::class.java)
            intent.putExtra("type","year")
            startActivity(intent)
        }
        cardView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        val year:String = "Year "+getCurrentDate(1).toString().substring(6)
        tv_days.setText(year)
        return myFragment
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTransfer(){
        total = 0.0
        val db =  Firebase.firestore
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor: android.content.SharedPreferences.Editor = prefs.edit()
        val collections = db.collection(user.toString()).document("Transfer")
        var num :Int =1
        for(i in 1..365){
            val formattedDate: String = getCurrentDate(num).toString()
            db.collection(user.toString()).document("transfer").collection(formattedDate).
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
                        editor.clear()
                        if(dc.type== DocumentChange.Type.ADDED){
                            val myobject =dc.document.toObject(DataTransfer::class.java)
                            myobject.TransferId = dc.document.id
                            dataArrayList.add(myobject)
                            val amount:String = myobject.Amount.toString()
                            val to = myobject.To.toString()
                            val from = myobject.From.toString()
                            amounts += amount
                            transaction+=(from+" to "+to)
                            total+=amount.toDouble()
                        }
                        val textTotal:String= String.format("%.2f",total)
                        tv_total.setText("Total: RM"+textTotal)
                        if(amounts.contains("10.0")&&transaction.contains("sample")){
                            amounts.remove("10.0")
                            transaction.remove("sample")}
                        val gson = Gson()
                        val jsonA: String = gson.toJson(amounts)
                        val jsonC: String = gson.toJson(transaction)
                        editor.putString("YearTrAmounts", jsonA)
                        editor.putString("YearTrTransaction", jsonC)
                        editor.apply()
                    }

                    listAdapter.notifyDataSetChanged()
                    listAdapter.setonItemClickListener(object: ListTransferAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val from = dataArrayList[position].From.toString()
                            val accFromID = dataArrayList[position].AccFromID.toString()
                            val accFromGroup = dataArrayList[position].AccFromGroup.toString()
                            val amount = dataArrayList[position].Amount.toString()
                            val to = dataArrayList[position].To.toString()
                            val accToID = dataArrayList[position].AccToID.toString()
                            val accToGroup = dataArrayList[position].AccToGroup.toString()
                            val date = dataArrayList[position].Date.toString()
                            val note = dataArrayList[position].Note.toString()
                            val id = dataArrayList[position].TransferId.toString()
                            val i = Intent(activity, TransferRecord::class.java)
                            i.putExtra("from",from)
                            i.putExtra("accFromID",accFromID)
                            i.putExtra("accFromGroup",accFromGroup)
                            i.putExtra("amount",amount)
                            i.putExtra("to",to)
                            i.putExtra("accToID",accToID)
                            i.putExtra("accToGroup",accToGroup)
                            i.putExtra("date",date)
                            i.putExtra("note",note)
                            i.putExtra("id",id)
                            startActivity(i)
                        }
                    })

                }

            }
            )
            num+=1
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(num: Int): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(ContentValues.TAG, "getCurrentDateTime: greater than O")
            LocalDate.now().withDayOfYear(num).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } else {
            Log.d(ContentValues.TAG, "getCurrentDateTime: less than O")
            val calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_YEAR] = num
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
            simpleDateFormat.format(calendar.time)
        }
    }

    override fun onResume() {
        super.onResume()
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        dataArrayList = arrayListOf()
        amounts = arrayListOf()
        transaction = arrayListOf()
        listAdapter = ListTransferAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        getTransfer()
    }

}