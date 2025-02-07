package com.example.lblive

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class PageOneFragment : Fragment(R.layout.first_page) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GridAdapter
    private val items = MutableList(32) { "Feld ${it + 1}" }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)

        // Warten, bis das Layout fertig ist
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Berechnung der nutzbaren Bildschirmhöhe
                val screenWidth = requireContext().resources.displayMetrics.widthPixels
                val screenHeight = requireContext().resources.displayMetrics.heightPixels

                val windowHeight = view.height // Höhe des Fragments ohne Statusleiste & ActionBar

                val columns = 4
                val rows = 8
                val itemWidth = screenWidth / columns
                val itemHeight = windowHeight / rows

                recyclerView.layoutManager = GridLayoutManager(requireContext(), columns)
                adapter = GridAdapter(items, itemWidth, itemHeight)
                recyclerView.adapter = adapter

                val itemTouchHelper = ItemTouchHelper(ItemMoveCallback(adapter))
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }
        })
    }
}




















