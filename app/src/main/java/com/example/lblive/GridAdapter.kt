package com.example.lblive

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.content.ClipData
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Collections
import com.google.android.flexbox.FlexboxLayout


class FlexboxWidgetAdapter(
    private val context: Context,
    private val items: MutableList<String>,
    private val columns: Int,
    private val rows: Int,
    private val dragStateListener: (Boolean) -> Unit
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("grid_layout", Context.MODE_PRIVATE)

    init {
        loadLayout()
    }

    fun createView(item: String, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.flexbox_item, parent, false)
        val textView: TextView = view.findViewById(R.id.textView)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
        val slider: SeekBar = view.findViewById(R.id.seekBar)
        val gridField: RelativeLayout = view.findViewById(R.id.mute_field)

        val screenWidth = context.resources.displayMetrics.widthPixels
        val itemWidth = screenWidth / columns
        val itemHeight = screenWidth / rows

        val layoutParams = FlexboxLayout.LayoutParams(itemWidth, itemHeight)
        layoutParams.flexGrow = 1f

        when (item) {
            "" -> {
                textView.visibility = View.GONE
                checkbox.visibility = View.GONE
                slider.visibility = View.GONE
                gridField.setBackgroundResource(R.drawable.background_empty)
            }
            "Mute" -> {
                gridField.setBackgroundResource(R.drawable.rounded_background)
                textView.text = item
                textView.visibility = View.VISIBLE
                checkbox.visibility = View.VISIBLE
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    textView.text = if (isChecked) "mute" else "unmute"
                }
            }
            "Slider" -> {
                gridField.setBackgroundResource(R.drawable.rounded_background)
                textView.text = item
                textView.visibility = View.VISIBLE
                slider.visibility = View.VISIBLE
                layoutParams.width = itemWidth * 4 // Slider nimmt 4 Spalten ein
            }
        }

        view.layoutParams = layoutParams

        view.setOnLongClickListener {
            val clipData = ClipData.newPlainText("", item)
            val shadow = View.DragShadowBuilder(view)
            view.startDragAndDrop(clipData, shadow, view, 0)
            dragStateListener(true)
            true
        }

        view.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as View
                    val oldIndex = parent.indexOfChild(draggedView)
                    val newIndex = parent.indexOfChild(v)

                    Collections.swap(items, oldIndex, newIndex)
                    saveLayout()
                    dragStateListener(false)
                    parent.removeView(draggedView)
                    parent.addView(draggedView, newIndex)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    dragStateListener(false)
                    true
                }
                else -> false
            }
        }
        return view
    }

    fun saveLayout() {
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




