package com.example.lblive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GridAdapter(
    private val items: MutableList<String>,
    private val itemWidth: Int,
    private val itemHeight: Int
) : RecyclerView.Adapter<GridAdapter.ViewHolder>(), ItemMoveCallback.ItemMoveListener {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
        val relativeLayout: RelativeLayout = view.findViewById(R.id.field)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item, parent, false)

        val layoutParams = view.layoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemHeight
        view.layoutParams = layoutParams

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = items[position]

        if (content.isEmpty()) {
            // Unsichtbares Feld
            holder.relativeLayout.visibility = View.GONE
        } else {
            // Aktiviertes Feld
            holder.relativeLayout.visibility = View.VISIBLE

            // Checkbox: Wechselt zwischen "Mute" und "Unmute"
            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    holder.textView.text = "mute"
                } else {
                    holder.textView.text = "unmute"
                }
            }
        }
    }

    override fun getItemCount() = items.size

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }
}


