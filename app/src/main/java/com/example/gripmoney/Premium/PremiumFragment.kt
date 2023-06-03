package com.example.gripmoney.Premium

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gripmoney.MainActivity
import com.example.gripmoney.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PremiumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PremiumFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val db = FirebaseFirestore.getInstance()
    var ExpiryDate: String? = null
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = sdf.format(Date())
    val todayDate: Date = sdf.parse(currentDate.toString()) as Date
    var secondDate: Date = sdf.parse(currentDate.toString()) as Date

    private lateinit var callbackActivity: Activity

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
        val myFragment = inflater.inflate(R.layout.fragment_premium, container, false)

        val btnPremiumUpgrade = myFragment.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPremiumUpgrade)

        btnPremiumUpgrade.setOnClickListener{
            if(ExpiryDate!=null)
            {
                if (todayDate.before(secondDate)) {
                    Toast.makeText(activity, "Your premium subscription will expired on $ExpiryDate", Toast.LENGTH_LONG).show()
                    Toast.makeText(activity, "Please wait until then to upgrade again", Toast.LENGTH_SHORT).show()
                }
                else if (todayDate.after(secondDate)) {
                    startActivity(PremiumPay.newIntent(callbackActivity))
                }
                else if (todayDate == secondDate) {
                    startActivity(PremiumPay.newIntent(callbackActivity))
                }
            }
            else {
                startActivity(PremiumPay.newIntent(callbackActivity))
            }
        }

        return myFragment

    }

    override fun onResume() {
        super.onResume()

        val sharedPreference = this.activity?.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val userID:String = sharedPreference?.getString("user","")!!

        db.collection(userID).document("Premium").collection("Payment")
            .get()
            .addOnSuccessListener {
                if (!it.documents.isEmpty()){
                    ExpiryDate= it.documents.get(0).data?.get("EndDate").toString()
                    secondDate= sdf.parse(ExpiryDate.toString()) as Date
                }
            }
            .addOnFailureListener{
                it.printStackTrace()
                Toast.makeText(activity, "Failed to retrieve data from database", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PremiumFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PremiumFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}