package com.example.myfirstapp.userinterface

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R

data class CartRow(
    val cartItemId: Long,
    val listingId: Long,
    val title: String,
    val price: Double,
    var qty: Int
)

class CartAdapter(
    private var items: MutableList<CartRow>,
    private val onQtyChanged: (CartRow, Int) -> Unit,
    private val onRemove: (CartRow) -> Unit
) : RecyclerView.Adapter<CartAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle  : TextView = v.findViewById(R.id.tvItemTitle)
        val tvPrice  : TextView = v.findViewById(R.id.tvItemPrice)
        val tvQty    : TextView = v.findViewById(R.id.tvQty)
        val btnMinus : Button   = v.findViewById(R.id.btnMinus)
        val btnPlus  : Button   = v.findViewById(R.id.btnPlus)
        val btnRemove: Button   = v.findViewById(R.id.btnRemove)
    }

    fun setData(newItems: List<CartRow>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val row = items[position]
        h.tvTitle.text = row.title
        h.tvPrice.text = "$${String.format("%.2f", row.price * row.qty)}"
        h.tvQty.text   = row.qty.toString()

        h.btnPlus.setOnClickListener {
            val pos = h.adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            items[pos].qty++
            h.tvQty.text   = items[pos].qty.toString()
            h.tvPrice.text = "$${String.format("%.2f", items[pos].price * items[pos].qty)}"
            onQtyChanged(items[pos], items[pos].qty)
        }

        h.btnMinus.setOnClickListener {
            val pos = h.adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            if (items[pos].qty <= 1) {
                onRemove(items[pos])
            } else {
                items[pos].qty--
                h.tvQty.text   = items[pos].qty.toString()
                h.tvPrice.text = "$${String.format("%.2f", items[pos].price * items[pos].qty)}"
                onQtyChanged(items[pos], items[pos].qty)
            }
        }

        h.btnRemove.setOnClickListener {
            val pos = h.adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            onRemove(items[pos])
        }
    }

    override fun getItemCount() = items.size

    fun getTotalPrice(): Double = items.sumOf { it.price * it.qty }
}