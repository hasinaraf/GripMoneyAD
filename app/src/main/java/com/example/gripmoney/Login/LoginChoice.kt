package com.example.gripmoney.Login

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.edit
import com.example.gripmoney.AccountGroup.AccDataClass
import com.example.gripmoney.Category.CateDataClass
import com.example.gripmoney.Category.CategoryFragment
import com.example.gripmoney.MainActivity
import com.example.gripmoney.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase

class LoginChoice : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    var db = FirebaseFirestore.getInstance()
    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_choice)
        //configure the Google SignIn
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()// we only need email from google account
            .build()
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val btnSignInEmail = findViewById<Button>(R.id.btnSignInEmail)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnSignInGoogle = findViewById<Button>(R.id.btnSignInGoogle)
        btnSignInGoogle.setOnClickListener{
            Log.d(TAG,"onCreate: begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
        btnSignInEmail.setOnClickListener{
            startActivity(Intent(this, LoginEmail::class.java))
            finish()
        }
        btnSignUp.setOnClickListener{
            startActivity(Intent(this, RegisterEmail::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode:Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RC_SIGN_IN) {
            Log.d(TAG, "OnActivityResult: Google Sign In intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            //google sign in success, now auth with firebase
            val account = accountTask.getResult(ApiException::class.java)
            val sharedPref = this.getSharedPreferences("UtmNews-data", Context.MODE_PRIVATE)
            sharedPref.edit{
                putString("GoogleIDToken", account!!.idToken.toString())
            }
            firebaseAuthWithGoogleAccount(account)
        }}

    override fun onStart() {
        super.onStart()
        val currUser: FirebaseUser? = auth.currentUser
        if (currUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?){
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(
            account!!.idToken,null
        )
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                    authResult ->
                Log.d(TAG ,"firebaseAuthWithGoogleAccount: LoggedIn")
                val firebaseUser = auth.currentUser
                val uid = firebaseUser!!.uid
                val email = firebaseUser.providerData?.get(1)?.email.toString()
                Log.d(TAG,"firebaseAuthWithGoogleAccount: Uid: $uid")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")
                //check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Account created... \n$email")
                    Toast.makeText(this,"Account created... \n$email", Toast.LENGTH_SHORT).show()
                    var auth = Firebase.auth
                    val user = auth.currentUser
                    val email = user!!.email
                    val index = email?.lastIndexOf('@')
                    val substring = email?.subSequence(0, index!!.toInt())
                    val uName = hashMapOf("name" to substring, "age" to "-", "gender" to "-")
                    var db = FirebaseFirestore.getInstance()
                    db.collection(uid).document("userProfile").set(uName)
                    val pin = hashMapOf("number" to "0")
                    db.collection(uid).document("pin").set(pin)
                    val sub = hashMapOf("premium" to "-")
                    db.collection(uid).document("subscribe").set(sub)
                    val userType = hashMapOf("type" to "google")
                    db.collection(uid).document("userType").set(userType)

                    createDefaultAccGroupCate(uid)
                }
                else{
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Existing user... \n$email")
                    Toast.makeText(this,"LoggedIn... \n$email", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(this,"signInWithGoogle:success",Toast.LENGTH_LONG).show()
                val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                //to get the user uid and pass it to main activity for linking with firebase firestore storage
                editor.putString("user",uid)
                editor.apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                    e -> Log.d(TAG,"firebaseAuthWithGoogleAccount: Log In Failed due to ${e.message}")
                Toast.makeText(this,"Log In Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createDefaultAccGroupCate(userID:String){
        for(acc in arrayOf("Accounts","Card","Cash")) {
            var count = 0
            db.collection(userID).document("account group").collection(acc)
                .get()
                .addOnSuccessListener { result ->

                    for (document in result) {
                        val myobject = document.toObject(AccDataClass::class.java)
                        count += 1
                    }
                    if (count == 0) {
                        val account = hashMapOf(
                            "AccountID" to "$acc.1",
                            "Amount" to "100",
                            "Name" to acc,
                            "Group" to acc,
                            "Note" to "This is default account"
                        )

                        db.collection(userID).document("account group").collection(acc).document("$acc.1")
                            .set(account)
                            .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                                //notifyUser("Your default account group is created")
                            }
                            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e)
                                //notifyUser("Your default account group is fail to create")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }

        val categoryName = arrayOf("Food","Gift","Transportation")
        var count = 0
        db.collection(userID).document("category").collection("category1")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    var myObject = document.toObject(CateDataClass::class.java)
                    count+=1
                }
                if (count == 0) {
                    for(num in 1..3){
                        val category = hashMapOf(
                            "CateID" to "category.$num",
                            "Name" to categoryName[num-1],
                            "Description" to "This is a default category",
                            "Icon" to CategoryFragment.iconList[num-1],
                            "Color" to CategoryFragment.colorList[num-1]
                        )
                        db.collection(userID).document("category").collection("category1").document("category.$num")
                            .set(category)
                            .addOnSuccessListener { documentReference ->
                                //     Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference}")
                                //notifyUser("Your default category is successful created")
                            }
                            .addOnFailureListener { e ->
                                //  Log.w(ContentValues.TAG, "Error adding document", e)
                                //notifyUser("Your default category is fail to create")
                            }
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    fun notifyUser(str:String){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show()
    }
}