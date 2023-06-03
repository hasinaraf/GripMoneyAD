package com.example.gripmoney.Calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.R

//RecyclerView.Adapter
class DataListAdapter(private val dataArrayList: ArrayList<DataDaily>) : RecyclerView.Adapter<DataListAdapter.MyViewHolder>() {
    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position:Int)
    }
    fun setonItemClickListener(listener: onItemClickListener){
        mListener =listener
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataListAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_tile,parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: DataListAdapter.MyViewHolder, position: Int) {
        val dataDaily = dataArrayList[position]
        holder.tvAmount.text = "RM "+dataDaily.Amount
        if(dataDaily.type.toString() != "transfer"){
        holder.tvAccount.text = dataDaily.Account
        if(dataDaily.Note.toString()=="")
        {holder.tvCategory.text = dataDaily.Category}
        else{ holder.tvCategory.text = dataDaily.Category + "[${dataDaily.Note}]"}}
        else{
            holder.tvAccount.text = dataDaily.From
            holder.tvAccount.setTextColor(Color.BLACK)
            if(dataDaily.Note.toString()=="")
            {holder.tvCategory.text = "-->   "+ dataDaily.To}
            else{ holder.tvCategory.text = "-->   "+dataDaily.To + "[${dataDaily.Note}]"}
        }
        if(dataDaily.type.toString() == "expenses"){
            //salmon color
            holder.card.setCardBackgroundColor(Color.rgb(250,128,114))
        }else if(dataDaily.type.toString() == "income"){
            //light blue color
            holder.card.setCardBackgroundColor(Color.rgb(153,204,255))
        }else{
            //light green
            holder.card.setCardBackgroundColor(Color.rgb(102,255,102))
        }
    }

    override fun getItemCount(): Int {
        return dataArrayList.size
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        val card: CardView = itemView.findViewById(R.id.dataCard)
        val tvAccount: TextView = itemView.findViewById(R.id.tvAccount)
        val tvCategory:TextView = itemView.findViewById(R.id.tvCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}