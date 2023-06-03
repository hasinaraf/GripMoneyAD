package com.example.gripmoney.Premium

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class PremiumPay : AppCompatActivity() {
    companion object{
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, PremiumPay::class.java)
            return intent
        }
    }

    var SECRET_KEY = "sk_test_51LdvUAHgGxF0kQyL78eeF4NqCFJtzxmTFmFQnAGIZR6TlPOUyABSr6RpvjVMuEwSo3zGmAeyjHVdc9kcNqWRwjoA00TUSj6s28"
    var PUBLISH_KEY = "pk_test_51LdvUAHgGxF0kQyLQJtx9QbWK11gwvzsX1XZU6subrqFMKXaNPF4rNR3Ct41t0VXRnTFf5JMZxhVmDQ0fm0XlUgn00wXB8YR1p"
    var paymentSheet: PaymentSheet? = null
    var paymentSheetM: PaymentSheet? = null
    var customerID: String? = null
    var EphemeralKey: String? = null
    var EphemeralKeyM: String? = null
    var ClientSecret: String? = null
    var ClientSecretM: String? = null
    lateinit var btnYear:Button
    lateinit var btnMonth:Button
    lateinit var user : String
    val db = Firebase.firestore
    protected var enabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_pay)
        // calling the action bar
        val actionBar = supportActionBar
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        Toast.makeText(this,"Loading payment details...", Toast.LENGTH_SHORT).show()
        screenDisable(true)

        btnYear = findViewById(R.id.button_Year)
        btnMonth = findViewById(R.id.button_Month)
        btnYear.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                PaymentFlow()
            }
        })
        btnMonth.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                PaymentFlowM()
            }
        })

        val sharedPreference = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        user = sharedPreference.getString("user","")!!

        PaymentConfiguration.init(this, PUBLISH_KEY)

        paymentSheet = PaymentSheet(this) { paymentSheetResult: PaymentSheetResult ->
            onPaymentResult(paymentSheetResult)
        }
        paymentSheetM = PaymentSheet(this) { paymentSheetResultM: PaymentSheetResult ->
            onPaymentResultM(paymentSheetResultM)
        }

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/customers",
            Response.Listener { response ->
                try {
                    val `object` = JSONObject(response)
                    customerID = `object`.getString("id")
                    getEphemeralKey(customerID)
                    getEphemeralKeyM(customerID)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = HashMap()
                header["Authorization"] = "Bearer $SECRET_KEY"
                return header
            }
        }
        val requestQueue = Volley.newRequestQueue(this@PremiumPay)
        requestQueue.add(stringRequest)
    }

    private fun onPaymentResultM(paymentSheetResultM: PaymentSheetResult) {
        if (paymentSheetResultM is PaymentSheetResult.Completed) {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val currentDate = sdf.format(Date())
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, 1)
            val dateAfter = sdf.format(cal.time)
            val premium = hashMapOf(
                "StartDate" to currentDate.toString(),
                "EndDate" to dateAfter.toString(),
                "Subscription" to "Month"
            )

            db.collection(user).document("Premium").collection("Payment").document("Subscription")
                .set(premium)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                    Toast.makeText(this, "Payment for One Month Subscription Succeed", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Your premium subscription will expired on $dateAfter", Toast.LENGTH_SHORT).show()
                    val sub = hashMapOf("premium" to "Premium")
                    db.collection(user).document("subscribe").set(sub)
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error writing document", e)
                    Toast.makeText(this, "Payment success but fail to record down into firestore", Toast.LENGTH_SHORT).show()
                }
            finish()
        }
    }

    private fun onPaymentResult(paymentSheetResult: PaymentSheetResult) {
        if (paymentSheetResult is PaymentSheetResult.Completed) {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val currentDate = sdf.format(Date())
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, 1)
            val dateAfter = sdf.format(cal.time)

            val premium = hashMapOf(
                "StartDate" to currentDate.toString(),
                "EndDate" to dateAfter.toString(),
                "Subscription" to "Year"
            )
            /*db.collection(user).document("Premium").collection("Payment")
                .add(premium)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                    Toast.makeText(this, "Your data was failed to saved!", Toast.LENGTH_SHORT).show()
                }*/
            db.collection(user).document("Premium").collection("Payment").document("Subscription")
                .set(premium)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference}")
                    Toast.makeText(this, "Payment for One Year Subscription Succeed", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Your premium subscription will expired on $dateAfter", Toast.LENGTH_SHORT).show()
                    val sub = hashMapOf("premium" to "Premium")
                    db.collection(user).document("subscribe").set(sub)
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                    Toast.makeText(this, "Payment success but fail to record down into firestore", Toast.LENGTH_SHORT).show()
                }
            finish()
        }
    }

    private fun getEphemeralKey(customerID: String?) {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/ephemeral_keys",
            Response.Listener { response ->
                try {
                    val `object` = JSONObject(response)
                    EphemeralKey = `object`.getString("id")
                    getClientSecret(customerID!!, EphemeralKey)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = HashMap()
                header["Authorization"] = "Bearer $SECRET_KEY"
                header["Stripe-Version"] = "2022-08-01"
                return header
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["customer"] = customerID!!
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(this@PremiumPay)
        requestQueue.add(stringRequest)
    }

    private fun getEphemeralKeyM(customerID: String?) {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/ephemeral_keys",
            Response.Listener { response ->
                try {
                    val `object` = JSONObject(response)
                    EphemeralKeyM = `object`.getString("id")
                    getClientSecretM(customerID!!, EphemeralKeyM)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = HashMap()
                header["Authorization"] = "Bearer $SECRET_KEY"
                header["Stripe-Version"] = "2022-08-01"
                return header
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["customer"] = customerID!!
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(this@PremiumPay)
        requestQueue.add(stringRequest)
    }

    private fun getClientSecret(customerID: String, EphemeralKey: String?) {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/payment_intents",
            Response.Listener { response ->
                try {
                    val `object` = JSONObject(response)
                    ClientSecret = `object`.getString("client_secret")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = HashMap()
                header["Authorization"] = "Bearer $SECRET_KEY"
                return header
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["customer"] = customerID
                params["amount"] = "1942"
                params["currency"] = "myr"
                params["automatic_payment_methods[enabled]"] = "true"
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(this@PremiumPay)
        requestQueue.add(stringRequest)
    }

    private fun getClientSecretM(customerID: String, EphemeralKeyM: String?) {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/payment_intents",
            Response.Listener { response ->
                try {
                    val `object` = JSONObject(response)
                    ClientSecretM = `object`.getString("client_secret")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val header: MutableMap<String, String> = HashMap()
                header["Authorization"] = "Bearer $SECRET_KEY"
                return header
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["customer"] = customerID
                params["amount"] = "249"
                params["currency"] = "myr"
                params["automatic_payment_methods[enabled]"] = "true"
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(this@PremiumPay)
        requestQueue.add(stringRequest)
        screenDisable(false)
        Toast.makeText(this,"Loading completed",Toast.LENGTH_SHORT).show()
    }

    private fun PaymentFlow() {
        paymentSheet!!.presentWithPaymentIntent(
            ClientSecret!!, PaymentSheet.Configuration(
                "GripMoney", PaymentSheet.CustomerConfiguration(
                    customerID.toString(),
                    EphemeralKey.toString()
                )
            )
        )
    }
    private fun PaymentFlowM() {
        paymentSheetM!!.presentWithPaymentIntent(
            ClientSecretM!!, PaymentSheet.Configuration(
                "GripMoney", PaymentSheet.CustomerConfiguration(
                    customerID.toString(),
                    EphemeralKeyM.toString()
                )
            )
        )
    }
    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish()
        return true
    }

    fun screenDisable(b: Boolean) {
        enabled = b
        var screenCover:LinearLayout = findViewById(R.id.cover)
        if(b == true){
            screenCover.visibility = View.VISIBLE
        }else{
            screenCover.visibility = View.GONE
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(enabled){
            return true
        } else{
            return super.dispatchTouchEvent(ev)
        }
    }
}