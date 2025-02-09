package com.example.lblive

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GridAdapter(
    private val context: Context,
    private val items: MutableList<String>,
    private val itemWidth: Int,
    private val itemHeight: Int,
    private val dragStateListener: (Boolean) -> Unit
) : RecyclerView.Adapter<GridAdapter.ViewHolder>(), ItemMoveCallback.ItemMoveListener {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("grid_layout", Context.MODE_PRIVATE)

    init {
        loadLayout()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
        val mute_field: RelativeLayout = view.findViewById(R.id.mute_field)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mute_item, parent, false)

        val layoutParams = view.layoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemHeight
        view.layoutParams = layoutParams

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = items[position]

        if (content.isEmpty()) {
            holder.mute_field.visibility = View.GONE
        } else if (content == "Mute") {
            holder.textView.text = content
            holder.mute_field.visibility = View.VISIBLE
            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                holder.textView.text = if (isChecked) "mute" else "unmute"
            }
        } else if (content == "Slider") {
            holder.textView.text = content
            holder.mute_field.visibility = View.VISIBLE
            }
    }

    override fun getItemCount() = items.size

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (items[fromPosition].isNotEmpty() && items[toPosition].isEmpty()) {
            items[toPosition] = items[fromPosition]
            items[fromPosition] = ""
        } else {
            val item = items.removeAt(fromPosition)
            items.add(toPosition, item)
        }
        notifyItemMoved(fromPosition, toPosition)
        saveLayout()
    }

    override fun isMovable(fromPosition: Int, toPosition: Int): Boolean {
        return items[fromPosition].isNotEmpty()
    }

    override fun onDragStateChanged(isDragging: Boolean) {
        dragStateListener(isDragging)
    }

    private fun saveLayout() {
        val json = Gson().toJson(items)
        sharedPreferences.edit().putString("grid_order", json).apply()
    }

    private fun loadLayout() {
        val json = sharedPreferences.getString("grid_order", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            val savedItems: MutableList<String> = Gson().fromJson(json, type)
            if (savedItems.size == items.size) {
                items.clear()
                items.addAll(savedItems)
            }
        }
    }
}




