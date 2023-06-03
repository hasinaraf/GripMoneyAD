package com.example.gripmoney.Home.HomeIncome

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
import java.time.temporal.TemporalField
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList

class IncomeWeekFragment : Fragment() {

    private var total: Double = 0.0
    lateinit var myFragment:View
    lateinit var btnRecordNow : FloatingActionButton
    lateinit var chartButton: ImageView
    lateinit var tv_total: TextView
    lateinit var tv_days: TextView
    lateinit var cardView: CardView
    lateinit var recyclerView: RecyclerView
    lateinit var amounts: ArrayList<String>
    lateinit var categories: ArrayList<String>
    lateinit var dataArrayList: ArrayList<DataIncome>
    lateinit var listAdapter: ListIncomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_income_week, container, false)
        tv_total = myFragment.findViewById(R.id.tv_total)
        recyclerView = myFragment.findViewById(R.id.dailyIncRecyclerView)
        dataArrayList = arrayListOf()
        amounts = arrayListOf()
        categories = arrayListOf()
        tv_days =myFragment.findViewById(R.id.tv_days)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        listAdapter = ListIncomeAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        chartButton = myFragment.findViewById(R.id.chart)
        btnRecordNow = myFragment.findViewById<FloatingActionButton>(R.id.fab_record_now)
        btnRecordNow.setOnClickListener{
            val intent=Intent(activity,RecordNew::class.java)
            startActivity(intent)
        }
        cardView = myFragment.findViewById(R.id.cvFooter)
        chartButton.setOnClickListener {
            val intent=Intent(activity, ChartIncome::class.java)
            intent.putExtra("type","week")
            startActivity(intent)
        }
        cardView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        val week:String = getCurrentDate(1).toString()+" to "+ getCurrentDate(7).toString()
        tv_days.setText(week)
        return myFragment
    }

    @SuppressLint("SimpleDateFormat")
    private fun getIncome(){
        total = 0.0
        val db =  Firebase.firestore
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor: android.content.SharedPreferences.Editor = prefs.edit()
        var num :Long =1

        for(i in 1..7){
            val formattedDate: String = getCurrentDate(num).toString()
            db.collection(user.toString()).document("income").collection(formattedDate).
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
                            val myobject =dc.document.toObject(DataIncome::class.java)
                            myobject.IncomeId = dc.document.id
                            dataArrayList.add(myobject)
                            val amount:String = myobject.Amount.toString()
                            val category = myobject.Category.toString()
                            amounts += amount
                            categories+=category
                            total+=amount.toDouble()
                        }
                        val textTotal:String= String.format("%.2f",total)
                        tv_total.setText("Total: RM"+textTotal)
                        if(amounts.contains("10.0")&&categories.contains("sample")){
                            amounts.remove("10.0")
                            categories.remove("sample")}
                        val gson = Gson()
                        val jsonA: String = gson.toJson(amounts)
                        val jsonC: String = gson.toJson(categories)
                        editor.putString("WeekInAmounts", jsonA)
                        editor.putString("WeekInCategories", jsonC)
                        editor.apply()
                    }

                    listAdapter.notifyDataSetChanged()
                    listAdapter.setonItemClickListener(object: ListIncomeAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val account = dataArrayList[position].Account.toString()
                            val accountID = dataArrayList[position].AccountID.toString()
                            val group = dataArrayList[position].Group.toString()
                            val amount = dataArrayList[position].Amount.toString()
                            val place = dataArrayList[position].Place.toString()
                            val category = dataArrayList[position].Category.toString()
                            val cateID = dataArrayList[position].CateID.toString()
                            val date = dataArrayList[position].Date.toString()
                            val note = dataArrayList[position].Note.toString()
                            val id = dataArrayList[position].IncomeId.toString()
                            val i = Intent(activity, IncomeRecord::class.java)
                            i.putExtra("account",account)
                            i.putExtra("accountID",accountID)
                            i.putExtra("group",group)
                            i.putExtra("amount",amount)
                            i.putExtra("place",place)
                            i.putExtra("category",category)
                            i.putExtra("cateID",cateID)
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
    private fun getCurrentDate(num: Long): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fieldISO: TemporalField = WeekFields.of(Locale.CHINA).dayOfWeek();
            Log.d(ContentValues.TAG, "getCurrentDateTime: greater than O")
            LocalDate.now().with(fieldISO,num).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } else {
            Log.d(ContentValues.TAG, "getCurrentDateTime: less than O")
            val calendar: Calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_WEEK] = num.toInt()
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
        categories = arrayListOf()
        listAdapter = ListIncomeAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        getIncome()
    }

}