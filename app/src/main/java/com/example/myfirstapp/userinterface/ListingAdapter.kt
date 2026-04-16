package com.example.myfirstapp.userinterface

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R
import com.example.myfirstapp.model.Listing
import java.io.File

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
        val tvStatus   : TextView  = itemView.findViewById(R.id.tvStatus)
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
        holder.tvBrand.text     = listing.brand
        holder.tvCondition.text = listing.condition

        if (!Session.isBuyMode) {
            holder.tvStatus.visibility = View.VISIBLE
            when (listing.status) {
                "ACTIVE"  -> {
                    holder.tvStatus.text = "● In Stock"
                    holder.tvStatus.setTextColor(Color.parseColor("#F97316"))
                }
                "REMOVED" -> {
                    holder.tvStatus.text = "● Sold Out"
                    holder.tvStatus.setTextColor(Color.parseColor("#6B7280"))
                }
                else -> {
                    holder.tvStatus.text = listing.status
                    holder.tvStatus.setTextColor(Color.parseColor("#6B7280"))
                }
            }
        } else {
            holder.tvStatus.visibility = View.GONE
        }

        if (!listing.photoUri.isNullOrEmpty()) {
            try {
                val file = File(listing.photoUri)
                if (file.exists()) {
                    holder.ivThumbnail.setImageURI(Uri.fromFile(file))
                } else {
                    holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } catch (e: Exception) {
                holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { onClick(listing.id) }
    }

    override fun getItemCount() = items.size
}