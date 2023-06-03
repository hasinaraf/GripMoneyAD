package com.example.gripmoney.Category

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ViewCategory : AppCompatActivity() {
    lateinit var btnUpdateCat : Button
    lateinit var btnDeleteCat : Button
    lateinit var docName : String
    val db = Firebase.firestore
    var icon:String =""
    var color:String =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_category)

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

        var count = 0
        var iconOriPos =0
        //list of colors for user to choose
        for(iconTemp in CategoryFragment.iconList){
            if(iconTemp == intent.getStringExtra("icon")){
                icon = intent.getStringExtra("icon")!!
                dataIcon.add(CateDataClass(Icon = iconTemp, isSelected = true))
                iconOriPos = count
            }
            else
                dataIcon.add(CateDataClass(Icon = iconTemp, isSelected = false))
            count +=1
        }
        count = 0
        var colorOriPos = 0
        //list of colors for user to choose
        for(colorTemp in CategoryFragment.colorList){
            if(colorTemp == intent.getStringExtra("color")){
                color = intent.getStringExtra("color")!!
                dataColor.add(CateDataClass(Color = colorTemp, isSelected = true))
                colorOriPos = count
            }
            else
                dataColor.add(CateDataClass(Color = colorTemp, isSelected = false))
            count+=1
        }
        count = 0

        // Making icon adapter
        val adapterIcon = CateRecyclerAdapter(dataIcon)
        var prevIconPosition = iconOriPos
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
        var prevColorPosition = colorOriPos
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
        val user = sharedPreference?.getString("user","")
        val etName = findViewById<EditText>(R.id.et_category_name)
        val etDescription: EditText = findViewById(R.id.et_description)
        btnUpdateCat=  findViewById(R.id.btn_category_update)
        btnDeleteCat=  findViewById(R.id.btn_category_delete)

        etName.setText(intent.getStringExtra("name"), TextView.BufferType.EDITABLE)
        etDescription.setText(intent.getStringExtra("description"), TextView.BufferType.EDITABLE)
        docName = intent.getStringExtra("id")!!

        btnUpdateCat.setOnClickListener{
            val catName:String = etName.text.toString()
            val catDescription:String = etDescription.text.toString()
            if (catName.isEmpty() || catDescription.isEmpty() || color.isEmpty() || icon.isEmpty()){
                notifyUser("All the information must be filled and at least one icon and one color must be selected")
            }else{
                //count number of times adding to database
                UpdateCategory(catName,catDescription,icon,color,user.toString(),docName)
            }
        }

        btnDeleteCat.setOnClickListener{
            val catName:String = etName.text.toString()
            //count number of times adding to database
            deleteConfirmation(catName, user.toString())
        }
    }

    private fun UpdateCategory(catName:String, catDescription: String, catIcon:String, catColor:String,  user:String, docName:String){
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

    private fun deleteConfirmation(catName:String, user:String) {
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Category")
        builder.setMessage("Are you sure want to delete this category? the deleted category detail cannot be retrieve back")
        builder.setPositiveButton("Proceed", DialogInterface.OnClickListener(){ dialog, which->
            DeleteCategory(user)
            dialog.dismiss()
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener(){ dialog, which->
            dialog.dismiss()
        })
        builder.show()
    }

    private fun DeleteCategory(user:String){
        if(docName == "category.1" || docName == "category.2" || docName == "category.3"){
            notifyUser("This is default category you cannot delete this.")
        }else{
            db.collection(user).document("category").collection("category1").document(docName)
                .delete()
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
                    notifyUser("The category has been deleted")
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error deleting document", e)
                    notifyUser("!! Fail to delete the category !!")
                }
        }
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