package com.example.myfirstapp.userinterface

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R
import com.example.myfirstapp.model.Listing

class ListingAdapter(private var listings: List<Listing>) :
    RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val course: TextView = view.findViewById(R.id.tvCourse)
        val specs: TextView = view.findViewById(R.id.tvSpecs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listings[position]

        holder.title.text = item.title
        holder.price.text = "$${item.price}"
        holder.course.text = "Course: ${item.courseTag}"
        holder.specs.text = "${item.size} | ${item.resolution} | ${item.refreshRate}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ListingDetailActivity::class.java)
            intent.putExtra("LISTING_ID", item.id)
            context.startActivity(intent)
        }
    }

    fun updateData(newList: List<Listing>) {
        listings = newList
        notifyDataSetChanged()
    }
}