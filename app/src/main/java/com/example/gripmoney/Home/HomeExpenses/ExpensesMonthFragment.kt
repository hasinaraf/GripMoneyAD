package com.example.gripmoney.Home.HomeExpenses

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.Home.RecordNew.RecordNew
import com.example.gripmoney.R
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.text.toDouble as textToDouble
import android.preference.PreferenceManager
import android.widget.ImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.EventListener
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import android.content.SharedPreferences as getSharedPreferences


class ExpensesMonthFragment : Fragment() {
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
    lateinit var dataArrayList: ArrayList<DataExpenses>
    lateinit var listAdapter: ListExpensesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_expenses_month, container, false)
        tv_total = myFragment.findViewById(R.id.tv_total)
        recyclerView = myFragment.findViewById(R.id.dailyExpRecyclerView)
        dataArrayList = arrayListOf()
        amounts = arrayListOf()
        categories = arrayListOf()
        tv_days =myFragment.findViewById(R.id.tv_days)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        listAdapter = ListExpensesAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        chartButton = myFragment.findViewById(R.id.chart)
        btnRecordNow = myFragment.findViewById<FloatingActionButton>(R.id.fab_record_now)
        btnRecordNow.setOnClickListener{
            val intent=Intent(activity,RecordNew::class.java)

            startActivity(intent)
        }
        cardView = myFragment.findViewById(R.id.cvFooter)
        chartButton.setOnClickListener {
            val intent=Intent(activity,ChartExpenses::class.java)
            intent.putExtra("type","month")
            startActivity(intent)
        }
        cardView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        val month:String = getCurrentDate(1).toString()+" to "+getCurrentDate(getLastDate()!!).toString()
        tv_days.setText(month)
        return myFragment
    }

    @SuppressLint("SimpleDateFormat")
    private fun getExpenses(){
        total = 0.0
        val db =  Firebase.firestore
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        val prefs: getSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor: android.content.SharedPreferences.Editor = prefs.edit()
        var num :Int =1
        for(i in 1..getLastDate()!!){
            val formattedDate: String = getCurrentDate(num).toString()
            db.collection(user.toString()).document("expenses").collection(formattedDate).
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
                        if(dc.type==DocumentChange.Type.ADDED){
                            val myobject =dc.document.toObject(DataExpenses::class.java)
                            myobject.ExpensesId = dc.document.id
                            dataArrayList.add(myobject)
                            val amount:String = myobject.Amount.toString()
                            val category = myobject.Category.toString()
                            amounts += amount
                            categories+=category
                            total+=amount.textToDouble()
                        }
                        val textTotal:String= String.format("%.2f",total)
                        tv_total.setText("Total: RM"+textTotal)
                        if(amounts.contains("10.0")&&categories.contains("sample")){
                            amounts.remove("10.0")
                            categories.remove("sample")}
                        val gson = Gson()
                        val jsonA: String = gson.toJson(amounts)
                        val jsonC: String = gson.toJson(categories)
                        editor.putString("MonthAmounts", jsonA)
                        editor.putString("MonthCategories", jsonC)
                        editor.apply()
                    }

                    listAdapter.notifyDataSetChanged()
                    listAdapter.setonItemClickListener(object: ListExpensesAdapter.onItemClickListener {
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
                            val id = dataArrayList[position].ExpensesId.toString()
                            val i = Intent(activity, ExpensesRecord::class.java)
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
    private fun getCurrentDate(num: Int): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(ContentValues.TAG, "getCurrentDateTime: greater than O")
            LocalDate.now().withDayOfMonth(num).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } else {
            Log.d(ContentValues.TAG, "getCurrentDateTime: less than O")
            val calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_MONTH] = num
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
             simpleDateFormat.format(calendar.time)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getLastDate(): Int? {
          return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              LocalDate.now().month.length(LocalDate.now().isLeapYear)
          } else {
              val cal = Calendar.getInstance()
              cal.getActualMaximum(Calendar.DATE)
          }
    }

    override fun onResume() {
        super.onResume()
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerView.setHasFixedSize(true)
        dataArrayList = arrayListOf()
        amounts = arrayListOf()
        categories = arrayListOf()
        listAdapter = ListExpensesAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        getExpenses()
    }

}
