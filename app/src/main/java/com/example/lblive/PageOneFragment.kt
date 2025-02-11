package com.example.lblive

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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
                (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object :
                    SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (items[position] == "Slider") {
                            return (4)
                        }
                        else {
                            return 1
                        }
                    }
                }
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
        val nextEmptyIndex = items.indexOfFirst { it.isEmpty() }
        if (nextEmptyIndex != -1) {
            items[nextEmptyIndex] = "Slider"
            adapter.notifyItemChanged(nextEmptyIndex)
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

    private fun updateFabSize(isOverTrash: Boolean) {
        val scale = if (isOverTrash) 1.5f else 1.0f // Größer, wenn Item darüber ist
        fabAction.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
    }


    private fun onItemDroppedOnTrash(position: Int, view: View) {
        Log.d("PageOneFragment", "$position")
        if (position == 31) {
            Log.d("PageOneFragment", "Item dropped on trash")
            items[position] = "" // Item wirklich löschen
            adapter.notifyItemRemoved(position)
            adapter.saveLayout()
        }
    }
}






















