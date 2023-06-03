package com.example.gripmoney.Calendar

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.Home.HomeExpenses.ExpensesRecord
import com.example.gripmoney.Home.HomeIncome.IncomeRecord
import com.example.gripmoney.Home.HomeTransfer.TransferRecord
import com.example.gripmoney.Home.RecordNew.RecordNew
import com.example.gripmoney.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class CalendarFragment : Fragment() {
    lateinit var myFragment: View
    lateinit var calendar: CalendarView
    lateinit var recyclerView: RecyclerView
    lateinit var fab: FloatingActionButton
    lateinit var listAdapter: DataListAdapter
    lateinit var dataArrayList: ArrayList<DataDaily>
    lateinit var tvSelectedDay: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment= inflater.inflate(R.layout.fragment_calendar, container, false)
        calendar = myFragment.findViewById<CalendarView>(R.id.calendarView)
        recyclerView = myFragment.findViewById(R.id.dailyAllRecyclerView)
        tvSelectedDay = myFragment.findViewById(R.id.tv_selectedDay)
        fab = myFragment.findViewById(R.id.fab_record_now)
        fab.setOnClickListener {
            val intent = Intent(activity,RecordNew::class.java)
            startActivity(intent)
        }
        dataArrayList = arrayListOf()
        listAdapter = DataListAdapter(dataArrayList)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = listAdapter
        if(tvSelectedDay.text.toString() == "00-00-0000"){
        tvSelectedDay.setText(getCurrentDateTime().toString())}
        return myFragment
    }

    private fun selectDate(){
        calendar.setOnDateChangeListener(
            CalendarView.OnDateChangeListener { view, year, month, day ->
                val day= String.format("%02d",day)
                val month =String.format("%02d",month+1)
                val date = day.toString() + "-" + (month).toString() + "-" + year.toString()
                tvSelectedDay.setText(date)
                getDailyData(date)
            })
    }
    private fun getDailyData(date: String){
        val list= arrayListOf<String>("expenses","income","transfer")
        val db = Firebase.firestore
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
                dataArrayList.clear()
                for(type in list){
        db.collection(user.toString()).document(type).collection(date)
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("Firestore error",error.message.toString())
                        return
                    }
                    for(dc: DocumentChange in value?.documentChanges!!){
                        if(dc.type== DocumentChange.Type.ADDED){
                            val myobject =dc.document.toObject(DataDaily::class.java)
                            myobject.Id = dc.document.id
                            myobject.type = type
                            dataArrayList.add(myobject)
                        }
                    }
                    listAdapter.notifyDataSetChanged()
                    listAdapter.setonItemClickListener(object: DataListAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val account = dataArrayList[position].Account.toString()
                            val amount = dataArrayList[position].Amount.toString()
                            val accountID = dataArrayList[position].AccountID.toString()
                            val group = dataArrayList[position].Group.toString()
                            val place = dataArrayList[position].Place.toString()
                            val category = dataArrayList[position].Category.toString()
                            val cateID = dataArrayList[position].CateID.toString()
                            val date = dataArrayList[position].Date.toString()
                            val note = dataArrayList[position].Note.toString()
                            val id = dataArrayList[position].Id.toString()
                            val from = dataArrayList[position].From.toString()
                            val accFromID = dataArrayList[position].AccFromID.toString()
                            val accFromGroup = dataArrayList[position].AccFromGroup.toString()
                            val to = dataArrayList[position].To.toString()
                            val accToID = dataArrayList[position].AccToID.toString()
                            val accToGroup = dataArrayList[position].AccToGroup.toString()
                            val type = dataArrayList[position].type.toString()
                            val i:Intent
                            if(type=="expenses")
                            i = Intent(activity, ExpensesRecord::class.java)
                            else if(type=="income")
                                i = Intent(activity, IncomeRecord::class.java)
                            else
                                i = Intent(activity, TransferRecord::class.java)
                            i.putExtra("account",account)
                            i.putExtra("amount",amount)
                            i.putExtra("accountID",accountID)
                            i.putExtra("group",group)
                            i.putExtra("place",place)
                            i.putExtra("category",category)
                            i.putExtra("cateID",cateID)
                            i.putExtra("date",date)
                            i.putExtra("note",note)
                            i.putExtra("from",from)
                            i.putExtra("accFromID",accFromID)
                            i.putExtra("accFromGroup",accFromGroup)
                            i.putExtra("to",to)
                            i.putExtra("accToID",accToID)
                            i.putExtra("accToGroup",accToGroup)
                            i.putExtra("id",id)
                            startActivity(i)
                        }
                    })
                }
            })  }

    }

    override fun onResume() {
        super.onResume()
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        dataArrayList = arrayListOf()
        listAdapter = DataListAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        selectDate()
        getDailyData(tvSelectedDay.text.toString())
    }
    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDateTime(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "getCurrentDateTime: greater than O")
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } else {
            Log.d(TAG, "getCurrentDateTime: less than O")
            val SDFormat = SimpleDateFormat("dd-MM-yyyy")
            SDFormat.format(Date())
        }
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        private fun getCurrentDateTime(): String? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "getCurrentDateTime: greater than O")
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            } else {
                Log.d(TAG, "getCurrentDateTime: less than O")
                val SDFormat = SimpleDateFormat("dd-MM-yyyy")
                SDFormat.format(Date())
            }
        }
    }
}