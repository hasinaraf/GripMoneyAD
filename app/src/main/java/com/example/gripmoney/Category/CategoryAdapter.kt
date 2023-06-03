package com.example.gripmoney.Category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gripmoney.Category.DataCategory
import android.content.Context
import android.graphics.Color
import androidx.cardview.widget.CardView
import androidx.test.core.app.ApplicationProvider

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.example.gripmoney.R


class CategoryAdapter(private val dataList: ArrayList<DataCategory>) :RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
    private lateinit var mListener : onItemClickListener
    private lateinit var c: Context
    interface onItemClickListener{
        fun onItemClick(position:Int)
    }

    fun setonItemClickListener(listener: onItemClickListener){
        mListener =listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.MyViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_category,parent,false)
        c= parent.context
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.MyViewHolder, position: Int) {
       val category : DataCategory = dataList[position]
        if(category.Icon!=null){
        val id: Int =
            c.resources.getIdentifier("@drawable/" + category.Icon, null, c.packageName)
        holder.icon.setImageResource(id)}
        if(category.Color!=null)
        holder.card.setCardBackgroundColor(Color.parseColor(category.Color.toString()))

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    public class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val icon: ImageView =itemView.findViewById(R.id.icon)
        val card: CardView = itemView.findViewById(R.id.categoryCard)
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}