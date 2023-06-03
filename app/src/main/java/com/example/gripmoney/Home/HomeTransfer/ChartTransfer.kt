package com.example.gripmoney.Home.HomeTransfer

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.example.gripmoney.R
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type

class ChartTransfer : AppCompatActivity() {
    lateinit var chart: AnyChartView
    lateinit var amounts: ArrayList<String>
    lateinit var transaction: ArrayList<String>
    lateinit var typeAmo:String
    lateinit var typeTrf:String
    lateinit var choose:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_transfer)
        var actionBar = supportActionBar
        actionBar!!.title="My Transaction Chart"
        amounts = arrayListOf()
        transaction = arrayListOf()
        chart = findViewById(R.id.ac_tr_pc)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        val editor: android.content.SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        choose = intent.getStringExtra("type").toString()
        if(choose == "day"){
            typeAmo = "DayTrAmounts"
            typeTrf = "DayTrTransaction"
        }else if(choose == "week"){
            typeAmo = "WeekTrAmounts"
            typeTrf = "WeekTrTransaction"
        }else if(choose == "month"){
            typeAmo = "MonthTrAmounts"
            typeTrf = "MonthTrTransaction"
        }else {
            typeAmo = "YearTrAmounts"
            typeTrf = "YearTrTransaction"
        }
        if((prefs.getString(typeAmo,null)!=null)){
            val jsonA: String? = prefs.getString(typeAmo, null)
            val jsonC: String? = prefs.getString(typeTrf, null)
            val type: Type = object : TypeToken<ArrayList<String?>?>() {}.getType()
            val a:ArrayList<String> = gson.fromJson(jsonA, type)
            val c:ArrayList<String> = gson.fromJson(jsonC, type)
            configChartView(a, c,choose)
            val editor: android.content.SharedPreferences.Editor = prefs.edit()
            editor.putString(typeAmo, null)
            editor.putString(typeTrf, null)
            editor.clear()
        } else {
            amounts.clear()
            transaction.clear()
            amounts.add("10.0")
            transaction.add("sample")
            configChartView(amounts, transaction, choose)
            editor.clear()
        }
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
    fun configChartView(amounts:ArrayList<String>, transaction:ArrayList<String>, type:String){
        val pie : Pie = AnyChart.pie()
        val dataPieChart: MutableList<DataEntry> = mutableListOf()
        dataPieChart.clear()
        for (index in amounts.indices){
            dataPieChart.add(ValueDataEntry(transaction.elementAt(index),amounts.elementAt(index).toDouble()))
        }
        pie.data(dataPieChart)
        pie.title("Transaction per ${type} Overview")
        chart.setChart(pie)
    }
    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }
}