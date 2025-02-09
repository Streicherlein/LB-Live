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
    private lateinit var fabAdd: FloatingActionButton
    private val items = MutableList(32) { "" }  // Alle Felder starten leer
    private var isSliderMode = false  // Gibt an, ob der Slider-Modus aktiv ist


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        fabAdd = view.findViewById(R.id.fab_add)

        // Warten, bis das Layout fertig ist
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Berechnung der nutzbaren Bildschirmhöhe
                val screenWidth = requireContext().resources.displayMetrics.widthPixels
                val windowHeight = view.height // Höhe des Fragments ohne Statusleiste & ActionBar

                val columns = 4
                val rows = 8
                val itemWidth = screenWidth / columns
                val itemHeight = windowHeight / rows

                recyclerView.layoutManager = GridLayoutManager(requireContext(), columns)
                adapter = GridAdapter(items, itemWidth, itemHeight, isSliderMode)
                recyclerView.adapter = adapter

                // Plus-Button: Aktiviert das nächste Feld
                fabAdd.setOnClickListener {
                    showSelectionDialog()
                }

                val itemTouchHelper = ItemTouchHelper(ItemMoveCallback(adapter))
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }
        })
    }

    // Dialog zur Auswahl zwischen Mute und Slider
    private fun showSelectionDialog() {
        val options = arrayOf("Mute", "Slider")
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Wähle eine Option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> addMuteField()  // "Mute" gewählt
                    1 -> addSliderField()  // "Slider" gewählt
                }
            }
        builder.create().show()
    }

    // Mute-Feld hinzufügen
    private fun addMuteField() {
        val nextEmptyIndex = items.indexOfFirst { it.isEmpty() }
        if (nextEmptyIndex != -1) {
            items[nextEmptyIndex] = "Mute"
            adapter.notifyItemChanged(nextEmptyIndex)
        }
    }
    // Slider-Feld hinzufügen
    private fun addSliderField() {
        val nextEmptyIndex = items.indexOfFirst { it.isEmpty() }
        if (nextEmptyIndex != -1) {
            items[nextEmptyIndex] = "Slider"
            adapter.notifyItemChanged(nextEmptyIndex)
        }
    }
}




















