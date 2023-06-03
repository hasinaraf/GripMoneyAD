package com.example.gripmoney.Category

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddCategory : AppCompatActivity() {
    lateinit var btnSaveCat : Button
    lateinit var user : String
    val db = Firebase.firestore
    var icon:String =""
    var color:String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        // calling the action bar
        var actionBar = supportActionBar
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val recyclerviewIcon = findViewById<RecyclerView>(R.id.iconRecyclerView)
        val recyclerviewColor = findViewById<RecyclerView>(R.id.colorRecyclerView)
        recyclerviewIcon.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        recyclerviewColor.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        val dataIcon = ArrayList<CateDataClass>()
        val dataColor = ArrayList<CateDataClass>()

        //list of colors for user to choose
        for(icon in CategoryFragment.iconList){
            dataIcon.add(CateDataClass(Icon = icon, isSelected = false))
        }

        //list of colors for user to choose
        for(colordata in CategoryFragment.colorList){
            dataColor.add(CateDataClass(Color = colordata, isSelected = false))
        }

        // Making icon adapter
        val adapterIcon = CateRecyclerAdapter(dataIcon)
        var prevIconPosition = -1
        // Setting the Adapter with the recyclerview
        recyclerviewIcon.adapter = adapterIcon
        adapterIcon.setonItemClickListener(object: CateRecyclerAdapter.onItemClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemClick(position: Int) {
                icon= dataIcon[position].Icon.toString()
                dataIcon[position].isSelected = true
                if (prevIconPosition != -1 && prevIconPosition != position) {
                    dataIcon[prevIconPosition].isSelected = false
                }
                prevIconPosition = position
                adapterIcon.notifyDataSetChanged()
            }
        })
        // Making color adapter

        val adapterColor = CateRecyclerAdapter(dataColor)
        var prevColorPosition = -1
        // Setting the Adapter with the recyclerview
        recyclerviewColor.adapter = adapterColor
        adapterColor.setonItemClickListener(object: CateRecyclerAdapter.onItemClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemClick(position: Int) {
                color= dataColor[position].Color.toString()
                dataColor[position].isSelected = true

                if(prevColorPosition != -1 && prevColorPosition != position){
                    dataColor[prevColorPosition].isSelected = false
                }

                prevColorPosition = position
                adapterColor.notifyDataSetChanged()
            }
        })

        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        user = sharedPreference.getString("user","")!!
        val etName = findViewById<EditText>(R.id.et_category_name)
        val etDescription: EditText = findViewById(R.id.et_description)
        btnSaveCat=  findViewById(R.id.btn_category_save)

        btnSaveCat.setOnClickListener{
            val docName = getNewDocName()
            val catName:String = etName.text.toString()
            val catDescription:String = etDescription.text.toString()
            if (catName.isEmpty() || catDescription.isEmpty() || color.isEmpty() || icon.isEmpty()){
                notifyUser("All the information must be filled and at least one icon and one color must be selected")
            }else{
                //count number of times adding to database
                addCategory(catName,catDescription,icon,color,user,docName)
            }
        }
    }
    private fun addCategory(catName:String, catDescription: String, catIcon:String, catColor:String,  user:String, docName: String){
        val category = hashMapOf(
            "CateID" to docName,
            "Name" to catName,
            "Description" to catDescription,
            "Icon" to catIcon,
            "Color" to catColor
        )
        db.collection(user).document("category").collection("category1").document(docName)
            .set(category)
            .addOnSuccessListener { documentReference ->
                //     Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference}")
                Toast.makeText(this, "Your data was successfully saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                //  Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(this, "Your data was failed to saved!", Toast.LENGTH_SHORT).show()
            }
    }

    fun getNewDocName(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val newName: String = prefs.getString(user + "categoryID", "category.1").toString()

        return newName
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish();
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun notifyUser(str:String){
        Toast.makeText(this,str, Toast.LENGTH_SHORT).show()
    }
}