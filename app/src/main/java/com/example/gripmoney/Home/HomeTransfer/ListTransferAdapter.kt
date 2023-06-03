package com.example.gripmoney.Home.HomeTransfer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R

class ListTransferAdapter(private val dataList: ArrayList<DataTransfer>) :RecyclerView.Adapter<ListTransferAdapter.MyViewHolder>() {
    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setonItemClickListener(listener: onItemClickListener){
        mListener =listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListTransferAdapter.MyViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transfer_card,parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: ListTransferAdapter.MyViewHolder, position: Int) {
       val trData : DataTransfer = dataList[position]
        if(trData.Note.toString()=="")
        {holder.trTo.text = trData.To}
        else{
            holder.trTo.text = trData.To + "[${trData.Note}]"}
        holder.trFrom.text = trData.From
        holder.trAmount.text= "RM "+trData.Amount
        holder.trAmount.setTextColor(Color.parseColor("#00ff00"))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    public class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val trFrom:TextView=itemView.findViewById(R.id.tvFrom)
        val trTo:TextView=itemView.findViewById(R.id.tvTo)
        val trAmount:TextView=itemView.findViewById(R.id.tvAmount)
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}