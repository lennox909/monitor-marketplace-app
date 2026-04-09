package com.example.myfirstapp.userinterface

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R
import com.example.myfirstapp.model.Listing

class ListingAdapter(
    private var items: List<Listing>,
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<ListingAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val tvTitle    : TextView  = itemView.findViewById(R.id.tvTitle)
        val tvPrice    : TextView  = itemView.findViewById(R.id.tvPrice)
        val tvBrand    : TextView  = itemView.findViewById(R.id.tvBrand)
        val tvCondition: TextView  = itemView.findViewById(R.id.tvCondition)
    }

    fun updateData(newItems: List<Listing>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val listing = items[position]

        holder.tvTitle.text     = listing.title
        holder.tvPrice.text     = "$${String.format("%.2f", listing.price)}"
        holder.tvBrand.text     = "${listing.brand} • ${listing.screenSize}"
        holder.tvCondition.text = listing.condition

        // Show thumbnail if photo exists
        if (!listing.photoUri.isNullOrEmpty()) {
            try {
                holder.ivThumbnail.setImageURI(Uri.parse(listing.photoUri))
            } catch (e: Exception) {
                holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Click on the root CardView
        holder.itemView.setOnClickListener { onClick(listing.id) }
    }

    override fun getItemCount() = items.size
}