package com.example.gripmoney.Home.HomeIncome

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

class ChartIncome : AppCompatActivity() {
    lateinit var chart: AnyChartView
    lateinit var amounts: ArrayList<String>
    lateinit var categories: ArrayList<String>
    lateinit var typeAmo:String
    lateinit var typeCat:String
    lateinit var choose:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_income)
        var actionBar = supportActionBar
        actionBar!!.title="My Income Chart"
        amounts = arrayListOf()
        categories = arrayListOf()
        chart = findViewById(R.id.ac_id_pc)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        val editor: android.content.SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        choose = intent.getStringExtra("type").toString()
        if(choose == "day"){
            typeAmo = "DayInAmounts"
            typeCat = "DayInCategories"
        }else if(choose == "week"){
            typeAmo = "WeekInAmounts"
            typeCat = "WeekInCategories"
        }else if(choose == "month"){
            typeAmo = "MonthInAmounts"
            typeCat = "MonthInCategories"
        }else {
            typeAmo = "YearInAmounts"
            typeCat = "YearInCategories"
        }
        if((prefs.getString(typeAmo,null)!=null)){
            val jsonA: String? = prefs.getString(typeAmo, null)
            val jsonC: String? = prefs.getString(typeCat, null)
            val type: Type = object : TypeToken<ArrayList<String?>?>() {}.getType()
            val a:ArrayList<String> = gson.fromJson(jsonA, type)
            val c:ArrayList<String> = gson.fromJson(jsonC, type)
            configChartView(a, c,choose)
            val editor: android.content.SharedPreferences.Editor = prefs.edit()
            editor.putString(typeAmo, null)
            editor.putString(typeCat, null)
            editor.clear()
        } else {
            amounts.clear()
            categories.clear()
            amounts.add("10.0")
            categories.add("sample")
            configChartView(amounts, categories, choose)
            editor.clear()
        }
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
    fun configChartView(amounts:ArrayList<String>, categories:ArrayList<String>, type:String){
        val pie : Pie = AnyChart.pie()
        val dataPieChart: MutableList<DataEntry> = mutableListOf()
        dataPieChart.clear()
        for (index in amounts.indices){
            dataPieChart.add(ValueDataEntry(categories.elementAt(index),amounts.elementAt(index).toDouble()))
        }
        pie.data(dataPieChart)
        pie.title("Income per ${type} Overview")
        chart.setChart(pie)
    }
    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }
}