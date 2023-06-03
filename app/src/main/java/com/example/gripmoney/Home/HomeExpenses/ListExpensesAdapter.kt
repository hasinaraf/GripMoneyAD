package com.example.gripmoney.Home.HomeExpenses

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R

class ListExpensesAdapter(private val dataList: ArrayList<DataExpenses>) :RecyclerView.Adapter<ListExpensesAdapter.MyViewHolder>() {
    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setonItemClickListener(listener: onItemClickListener){
        mListener =listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListExpensesAdapter.MyViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_tile,parent,false)

        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: ListExpensesAdapter.MyViewHolder, position: Int) {
       val expData : DataExpenses = dataList[position]
        if(expData.Note.toString()=="")
        {holder.exCategory.text = expData.Category}
        else{
            holder.exCategory.text = expData.Category + "[${expData.Note}]"}
        holder.exAccount.text = expData.Account
        holder.exAmount.text= "RM "+expData.Amount
        holder.exAmount.setTextColor(Color.parseColor("#FF0000"))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val exAccount:TextView = itemView.findViewById(R.id.tvAccount)
        val exCategory:TextView=itemView.findViewById(R.id.tvCategory)
        val exAmount:TextView=itemView.findViewById(R.id.tvAmount)
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}