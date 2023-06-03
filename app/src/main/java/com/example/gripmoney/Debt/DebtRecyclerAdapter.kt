package com.example.gripmoney.Debt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class DebtRecyclerAdapter(private val debtLendList: ArrayList<DebtManageDataClass>) : RecyclerView.Adapter<DebtRecyclerAdapter.ViewHolder>(){
    lateinit var mContext: Context

    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setonItemClickListener(listener: onItemClickListener){
        mListener =listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_debt,parent,false)
        mContext = parent.context
        return ViewHolder(v,mListener)
    }


    override fun getItemCount(): Int {
        return debtLendList.size
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.Name.text = debtLendList[position].Name
        val roundOff = String.format("%.2f", debtLendList[position].Amount.toDouble())
        holder.Amount.text = roundOff
    }

    class ViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        var Name: TextView = itemView.findViewById(R.id.dlName)
        var Amount: TextView = itemView.findViewById(R.id.Amount)

        init{
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }

        }
    }
}
