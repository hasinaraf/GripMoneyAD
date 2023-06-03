package com.example.gripmoney.Category

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
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.MainActivity
import com.example.gripmoney.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.recyclerview.widget.GridLayoutManager

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CategoryFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var myFragment:View
    lateinit var addingButton : FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var listAdapter: CateCreatedRecyclerAdapter
    private lateinit var callbackActivity: Activity
    lateinit var dataArrayList: ArrayList<CateDataClass>
    lateinit var i: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        callbackActivity = (activity as MainActivity)
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_category, container, false)
        addingButton = myFragment.findViewById(R.id.fab)
        recyclerView= myFragment.findViewById(R.id.categoryRecyclerView)
        i = Intent(activity, ViewCategory::class.java)
        addingButton.setOnClickListener {
            val intent=Intent(activity, AddCategory::class.java)
            startActivity(intent)
        }

        return myFragment
    }

    fun getCategory(){

    }

    override fun onResume() {
        super.onResume()
        val db =  Firebase.firestore

        val prefs = PreferenceManager.getDefaultSharedPreferences(callbackActivity)
        val editor = prefs.edit()
        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        val layoutManager = GridLayoutManager(this.activity, 3)
        recyclerView.layoutManager =layoutManager
        recyclerView.setHasFixedSize(true)
        dataArrayList = arrayListOf()
        listAdapter = CateCreatedRecyclerAdapter(dataArrayList)
        recyclerView.adapter = listAdapter
        db.collection(user.toString()).document("category").collection("category1").
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
                    if(dc.type==DocumentChange.Type.ADDED){
                        dataArrayList.add(dc.document.toObject(CateDataClass::class.java))

                        val name = dc.document.id
                        //i.putExtra("docID",name)
                        val substring: String =
                            name.subSequence(name.indexOf(".")+1, name.length).toString()
                        val newIndex = substring.toInt() + 1
                        val newName = name.subSequence(0, name.indexOf(".")+1).toString() + newIndex

                        Log.d(ContentValues.TAG, "New category document name -> $newName")
                        editor.putString(user + "categoryID" ,newName)
                    } else if (dc.type==DocumentChange.Type.REMOVED){
                        Log.d(ContentValues.TAG, "Removed data on firestore: ${dc.document.data.getValue("CateID")}")
                        val element = dataArrayList.filter{s -> s.CateID == "${dc.document.data.getValue("CateID")}"}
                        Log.d(ContentValues.TAG, "Removed data on array list: $element")
                    }
                }
                editor.apply()

                listAdapter.notifyDataSetChanged()
                listAdapter.setonItemClickListener(object: CateCreatedRecyclerAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val id = dataArrayList[position].CateID.toString()
                        val name = dataArrayList[position].Name.toString()
                        val description = dataArrayList[position].Description.toString()
                        val icon = dataArrayList[position].Icon.toString()
                        val color = dataArrayList[position].Color.toString()

                        //make a file for categoryEdit!
                        i.putExtra("id",id)
                        i.putExtra("name",name)
                        i.putExtra("description",description)
                        i.putExtra("icon",icon)
                        i.putExtra("color",color)
                        startActivity(i)
                    }
                })
            }

        })
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        var iconList = arrayOf("ic_baseline_emoji_food_beverage_24",
            "ic_baseline_card_giftcard_24",
            "ic_baseline_emoji_transportation_24",
            "ic_baseline_library_books_24")

        var colorList = arrayOf(
            "#367588",
            "#88c459",
            "#faebd7",
            "#ff1493"
        )
    }
}