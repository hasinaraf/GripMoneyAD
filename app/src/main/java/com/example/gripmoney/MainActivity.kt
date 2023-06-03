package com.example.gripmoney

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.gripmoney.AccountGroup.AccDataClass
import com.example.gripmoney.AccountGroup.AccGroupFragment
import com.example.gripmoney.AccountGroup.AccRecyclerAdapter
import com.example.gripmoney.Calendar.CalendarFragment
import com.example.gripmoney.Category.CateCreatedRecyclerAdapter
import com.example.gripmoney.Category.CateDataClass
import com.example.gripmoney.Category.CategoryFragment
import com.example.gripmoney.Debt.DebtManageFragment
import com.example.gripmoney.Help.HelpFragment
import com.example.gripmoney.Home.HomeFragment
import com.example.gripmoney.Login.LoginChoice
import com.example.gripmoney.Premium.PremiumFragment
import com.example.gripmoney.Settings.SettingChangePass
import com.example.gripmoney.Settings.SettingFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.internal.bind.util.ISO8601Utils.format
import java.lang.String.format
import java.text.MessageFormat.format
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth;
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    var db = FirebaseFirestore.getInstance()
    lateinit var expiredDate: String
    var status = "Not subscribed"

    override fun onResume() {
        super.onResume()

        val navigationView : NavigationView = findViewById(R.id.nav_view)
        var headerView: View = navigationView.getHeaderView(0)
        var username = headerView.findViewById<TextView>(R.id.user_name)
        var email = headerView.findViewById<TextView>(R.id.tvDashEmail)
        var subscribe = headerView.findViewById<TextView>(R.id.tvDashPremium)
        val firebaseAuth = FirebaseAuth.getInstance().currentUser
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val user = sharedPreference?.getString("user","")
        var currentDate = getCurrentDateTime()

        val ref = db.collection(user.toString()).document("userProfile")
        ref.get()
            .addOnSuccessListener { document ->
                if (document.data?.getValue("name").toString() != "-") {
                    username.text = document.data?.getValue("name").toString()
                    email.text = firebaseAuth?.email.toString()
                    Log.d(ContentValues.TAG, "Document Username data [1]: ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

        val doc = db.collection(user.toString()).document("subscribe")
        doc.addSnapshotListener(object: EventListener<DocumentSnapshot>{
            override fun onEvent(
                value: DocumentSnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore error", error.message.toString())
                    return
                }
                if (value!!.exists()){
                    if (value.data?.getValue("premium").toString() == "Premium") {
                        val docRef = db.collection(user.toString()).document("Premium").collection("Payment").document("Subscription")
                        docRef.get()
                            .addOnSuccessListener { document ->
                                expiredDate = document.data?.getValue("EndDate").toString()
                                if(currentDate!! <= expiredDate){
                                    subscribe.text = "Premium member"
                                    status = "Premium member"
                                    Log.d(ContentValues.TAG, "Document subscribe premium data: ${value.data}")
                                }else{
                                    subscribe.text = "Not subscribed"
                                    status = "Not subscribed"
                                    val sub = hashMapOf("premium" to "-")
                                    db.collection(user.toString()).document("subscribe").set(sub)
                                    Log.d(ContentValues.TAG, "Document subscribe premium data : ${value.data}")
                                }
                                Log.d(ContentValues.TAG, "Current Date : ${currentDate}")
                                Log.d(ContentValues.TAG, "Expired Date : ${expiredDate}")
                                Log.d(ContentValues.TAG, "Document premium end date data: ${document.data}")
                            }
                            .addOnFailureListener { exception ->
                                Log.d(ContentValues.TAG, "get failed with Document premium end date data", exception)
                            }
                    }else if(value.data?.getValue("premium").toString() != "Premium"){
                        subscribe.text = "Not subscribed"
                        status = "Not subscribed"
                        Log.d(ContentValues.TAG, "Document subscribe premium data: ${value.data}")
                    }
                }
            }
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        replaceFragment(HomeFragment(), "Home")

        navView.setNavigationItemSelectedListener {

            it.isChecked = true
            when(it.itemId){

                R.id.nav_home -> replaceFragment(HomeFragment(), it.title.toString())
                R.id.nav_acc_group -> replaceFragment(AccGroupFragment(), it.title.toString())
                R.id.nav_category -> replaceFragment(CategoryFragment(), it.title.toString())
                R.id.nav_calendar -> replaceFragment(CalendarFragment(), it.title.toString())
                R.id.nav_setting -> replaceFragment(SettingFragment(), it.title.toString())
                R.id.nav_premium -> replaceFragment(PremiumFragment(), it.title.toString())
                R.id.nav_debt -> {
                    if(status == "Not subscribed"){
                        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
                        builder.setTitle("Not subscribed")
                        builder.setMessage("This function is only provided to premium members. Upgrade to premium member before using this features")
                        builder.setPositiveButton("ok", DialogInterface.OnClickListener(){ dialog, which->
                            it.isChecked = false
                            replaceFragment(HomeFragment(), "Home")
                        })
                        builder.show()
                    }else{
                        replaceFragment(DebtManageFragment(), it.title.toString())
                    }
                }
                R.id.nav_help-> replaceFragment(HelpFragment(), it.title.toString())
                R.id.nav_sign_out -> logout()
            }
            true
        }
    }

    private fun logout(){
        auth.signOut()
        startActivity(Intent(this, LoginChoice::class.java))
        finish()
    }

    private fun replaceFragment(fragment: Fragment, title: String){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser == null){
            startActivity(Intent(this, LoginChoice::class.java))
            finish()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDateTime(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(ContentValues.TAG, "getCurrentDateTime: greater than O")
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } else {
            Log.d(ContentValues.TAG, "getCurrentDateTime: less than O")
            val SDFormat = SimpleDateFormat("yyyy-MM-dd")
            SDFormat.format(Date())
        }
    }

    private fun checkStatus(title:String){
        if(status == "Not subscribed"){
            var builder : AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Not subscribed")
            builder.setMessage("This function is only provided to premium members. Upgrade to premium member before using this features")
            builder.setPositiveButton("ok", DialogInterface.OnClickListener(){ dialog, which->
                dialog.dismiss()
            })
            builder.show()
        }else{
            replaceFragment(DebtManageFragment(), title)
        }
    }

}