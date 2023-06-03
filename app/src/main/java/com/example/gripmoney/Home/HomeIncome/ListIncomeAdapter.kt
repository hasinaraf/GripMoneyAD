package com.example.gripmoney.Home.HomeIncome

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R

class ListIncomeAdapter(private val dataList: ArrayList<DataIncome>) :RecyclerView.Adapter<ListIncomeAdapter.MyViewHolder>() {
    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setonItemClickListener(listener: onItemClickListener){
        mListener =listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListIncomeAdapter.MyViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_tile,parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: ListIncomeAdapter.MyViewHolder, position: Int) {
       val inData : DataIncome = dataList[position]
        if(inData.Note.toString()=="")
        {holder.inCategory.text = inData.Category}
        else{
            holder.inCategory.text = inData.Category + "[${inData.Note}]"}
        holder.inAccount.text = inData.Account
        holder.inAmount.text= "RM "+inData.Amount
        holder.inAmount.setTextColor(Color.parseColor("#0000ff"))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    public class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val inAccount:TextView = itemView.findViewById(R.id.tvAccount)
        val inCategory:TextView=itemView.findViewById(R.id.tvCategory)
        val inAmount:TextView=itemView.findViewById(R.id.tvAmount)
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}