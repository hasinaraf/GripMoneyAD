package com.example.gripmoney.AccountGroup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class AccRecyclerAdapter(private val accountList: ArrayList<AccDataClass>) : RecyclerView.Adapter<AccRecyclerAdapter.ViewHolder>(){
    private var db = Firebase.firestore
    lateinit var mContext: Context
    var accountCategory = arrayOf("Accounts","Card","Cash","Debit Card")

    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setonItemClickListener(listener: onItemClickListener){
        mListener =listener
    }

    private var images = intArrayOf(R.drawable.ic_baseline_android_24,R.drawable.ic_baseline_android_24,R.drawable.ic_baseline_android_24,
        R.drawable.ic_baseline_android_24,R.drawable.ic_baseline_android_24,R.drawable.ic_baseline_android_24,R.drawable.ic_baseline_android_24,
        R.drawable.ic_baseline_android_24,)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.account_card_layout,parent,false)
        mContext = parent.context
        return ViewHolder(v,mListener)
    }


    override fun getItemCount(): Int {
        return accountList.size
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.accountName.text = accountList[position].Name
        val pref = mContext.getSharedPreferences("HIDE_STATE", Context.MODE_PRIVATE)
        val visibility = pref?.getBoolean("visibility",false)

        if(visibility == true){
            holder.accountAmount.text = "****.**"
        }else{
            val roundOff = String.format("%.2f", accountList[position].Amount.toDouble())
            holder.accountAmount.text = roundOff
        }
        //holder.itemImage.setImageResource(images[position])
    }

    class ViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
//        var itemImage: ImageView = itemView.findViewById(R.id.item_image)
        var accountName: TextView = itemView.findViewById(R.id.accountName)
        var accountAmount: TextView = itemView.findViewById(R.id.accountAmount)

        init{
            //itemImage

            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }

        }
    }
}