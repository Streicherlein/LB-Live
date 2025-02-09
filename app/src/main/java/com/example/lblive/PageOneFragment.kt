package com.example.lblive

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageOneFragment : Fragment(R.layout.first_page) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GridAdapter
    private lateinit var fabAction: FloatingActionButton
    private val items = MutableList(32) { "" }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        fabAction = view.findViewById(R.id.fab_add)

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val screenWidth = requireContext().resources.displayMetrics.widthPixels
                val windowHeight = view.height

                val columns = 4
                val rows = 8
                val itemWidth = screenWidth / columns
                val itemHeight = windowHeight / rows

                recyclerView.layoutManager = GridLayoutManager(requireContext(), columns)
                adapter = GridAdapter(requireContext(), items, itemWidth, itemHeight, ::updateFabIcon)
                recyclerView.adapter = adapter

                fabAction.tag = "add"

                fabAction.setOnClickListener {
                    if (fabAction.tag == "add") {
                        showSelectionDialog()
                    }
                }

                val itemTouchHelper = ItemTouchHelper(ItemMoveCallback(adapter, ::onItemDroppedOnTrash))
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }
        })
    }

    private fun showSelectionDialog() {
        val options = arrayOf("Mute", "Slider")
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Wähle eine Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> addMuteField()
                    1 -> addSliderField()
                }
            }
        builder.create().show()
    }

    private fun addMuteField() {
        val nextEmptyIndex = items.indexOfFirst { it.isEmpty() }
        if (nextEmptyIndex != -1) {
            items[nextEmptyIndex] = "Mute"
            adapter.notifyItemChanged(nextEmptyIndex)
        }
    }

    private fun addSliderField() {
        val startIndex = items.indexOfFirst { it.isEmpty() }
        if (startIndex != -1 && startIndex % 4 == 0 && startIndex + 3 < items.size) {
            for (i in 0..3) {
                items[startIndex + i] = "Slider"
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateFabIcon(isDragging: Boolean) {
        if (isDragging) {
            fabAction.setImageResource(R.drawable.ic_trash)
            fabAction.tag = "trash"
        } else {
            fabAction.setImageResource(R.drawable.ic_plus)
            fabAction.tag = "add"
        }
    }

    private fun onItemDroppedOnTrash(position: Int) {
        if (items[position].isNotEmpty()) {
            items[position] = ""
            adapter.notifyItemChanged(position)
        }
    }
}






















