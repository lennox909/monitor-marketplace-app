package com.example.myfirstapp.userinterface

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R
import com.example.myfirstapp.model.Listing

class ListingAdapter(
    private var items: List<Listing>,
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<ListingAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvCondition: TextView = itemView.findViewById(R.id.tvCondition)
    }

    fun updateData(newItems: List<Listing>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_listing, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val listing = items[position]
        holder.tvTitle.text = listing.title
        holder.tvPrice.text = "$${"%.2f".format(listing.price)}"
        holder.tvCategory.text = listing.category
        holder.tvCondition.text = listing.condition

        holder.itemView.setOnClickListener { onClick(listing.id) }
    }

    override fun getItemCount(): Int = items.size
}