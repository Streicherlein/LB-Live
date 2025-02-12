package com.example.lblive

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import android.view.DragEvent
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast


class PageOneFragment : Fragment(R.layout.first_page) {

    private lateinit var gridLayout: GridLayout
    private var widgetCounter = 0  // Unique ID for widgets
    private val grid = Array(8) { IntArray(4) }  // Represents the 8x4 grid, 0 means free, 1 means occupied
    private val widgetViews = mutableListOf<View>() // Keeps track of added widget views
    private val widgetPositions = mutableMapOf<View, Pair<Int, Int>>() // Mapping widget views to their grid positions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate the gridLayout and FAB from the layout
        gridLayout = view.findViewById(R.id.gridLayout)
        val fabAdd: FloatingActionButton = view.findViewById(R.id.fab_add)

        // Set up the FAB click listener to show size options
        fabAdd.setOnClickListener {
            showWidgetSizeMenu(it)
        }

        // Set up drag and drop functionality
        setupDragAndDrop(view)
    }

    private fun showWidgetSizeMenu(view: View) {
        val menu = PopupMenu(requireContext(), view)
        menu.menu.add("1x1")
        menu.menu.add("1x4")
        menu.menu.add("2x2")
        menu.menu.add("2x1")

        menu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "1x1" -> addWidget(Widget(widgetCounter++, 1, 1))
                "1x4" -> addWidget(Widget(widgetCounter++, 1, 4))
                "2x2" -> addWidget(Widget(widgetCounter++, 2, 2))
                "2x1" -> addWidget(Widget(widgetCounter++, 2, 1))
            }
            true
        }
        menu.show()
    }

    private fun addWidget(widget: Widget) {
        val firstFreePosition = findFirstFreeSpace(widget.width, widget.height)
        if (firstFreePosition != null) {
            placeWidgetAt(firstFreePosition.first, firstFreePosition.second, widget)
        }
    }

    private fun findFirstFreeSpace(width: Int, height: Int): Pair<Int, Int>? {
        // Search the grid for the first available spot based on the widget size
        for (row in 0 until 8 - height + 1) { // Ensure it fits vertically
            for (col in 0 until 4 - width + 1) { // Ensure it fits horizontally
                if (isSpaceAvailable(row, col, width, height)) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    private fun isSpaceAvailable(row: Int, col: Int, width: Int, height: Int): Boolean {
        // Make sure that the widget will fit in the grid and does not overlap with other widgets
        if (row + height > 8 || col + width > 4) {
            Log.d("SpaceCheck", "Not enough space in the grid: Row: $row, Col: $col, Widget Width: $width, Widget Height: $height")
            return false
        }

        // Check for space in the grid for the widget's entire area
        for (r in row until row + height) {
            for (c in col until col + width) {
                if (grid[r][c] != 0) { // If there's already a widget in the grid cell
                    Log.d("SpaceCheck", "Space is occupied at Row: $r, Col: $c")
                    return false
                }
            }
        }

        Log.d("SpaceCheck", "Space available at Row: $row, Col: $col")
        return true
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun placeWidgetAt(row: Int, col: Int, widget: Widget) {
        // First check if the position is valid (within bounds and available)
        if (isValidPosition(row, col, widget)) {
            // Mark the cells as occupied
            for (r in row until row + widget.height) {
                for (c in col until col + widget.width) {
                    grid[r][c] = 1 // Occupy this cell
                }
            }

            // Create the widget view and position it
            val cellWidth = gridLayout.width / 4
            val cellHeight = gridLayout.height / 8

            val widgetView = TextView(context).apply {
                text = "Widget ${widget.id}"
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                gravity = Gravity.CENTER
                textSize = 16f

                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellWidth * widget.width
                    height = cellHeight * widget.height
                    columnSpec = GridLayout.spec(col, widget.width)
                    rowSpec = GridLayout.spec(row, widget.height)
                    setMargins(4, 4, 4, 4)
                }

                // Store the widget in the view's tag
                setTag(widget)

                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val shadow = View.DragShadowBuilder(v)
                        v.startDragAndDrop(null, shadow, v, 0)
                        v.visibility = View.INVISIBLE  // Hide widget during drag
                        true
                    } else {
                        false
                    }
                }
            }

            // Add widget to views list and map
            widgetViews.add(widgetView)
            widgetPositions[widgetView] = Pair(row, col) // Store the widget's position
            gridLayout.addView(widgetView)
        } else {
            // If no space, find the closest valid position instead of just defaulting
            val closestValidPosition = findClosestValidPosition(row, col, widget)
            if (closestValidPosition != null) {
                placeWidgetAt(closestValidPosition.first, closestValidPosition.second, widget)
            } else {
                // If no valid position found, reset to (0, 0) or similar starting position
                placeWidgetAt(0, 0, widget)
            }
        }
    }

    // This function will check if a space is valid and available for placing the widget
    private fun isValidPosition(row: Int, col: Int, widget: Widget): Boolean {
        if (row < 0 || col < 0 || row + widget.height > grid.size || col + widget.width > grid[0].size) {
            return false  // Out of bounds
        }

        // Check if the space is free (i.e., not occupied)
        for (r in row until row + widget.height) {
            for (c in col until col + widget.width) {
                if (grid[r][c] == 1) {  // Cell occupied
                    return false
                }
            }
        }
        return true
    }

    // This function searches for the closest available position in the grid
    private fun findClosestValidPosition(row: Int, col: Int, widget: Widget): Pair<Int, Int>? {
        for (r in 0 until grid.size) {
            for (c in 0 until grid[0].size) {
                if (isValidPosition(r, c, widget)) {
                    return Pair(r, c)  // Return the closest valid position
                }
            }
        }
        return null  // No valid position found
    }

    // When restoring the widget (after invalid drop), ensure the view is fully visible
    private fun restoreWidgetPosition(widgetView: View) {
        widgetView.visibility = View.VISIBLE
        // Additional logic if you need to remove from previous position can be added here.
    }

    private fun setupDragAndDrop(view: View) {
        gridLayout.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // No need to do anything here
                    true
                }

                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as View
                    val widget = getWidgetByView(draggedView) // Get the widget by its view

                    // Calculate drop position and widget size
                    val dropX = event.x
                    val dropY = event.y
                    val cellWidth = gridLayout.width / 4
                    val cellHeight = gridLayout.height / 8

                    // Log drop coordinates
                    Log.d("DragDrop", "Dropped at X: $dropX, Y: $dropY")

                    // Calculate the grid cell where the widget should be dropped
                    val dropRow = (dropY / cellHeight).toInt()
                    val dropCol = (dropX / cellWidth).toInt()

                    // Log the calculated grid cell
                    Log.d("DragDrop", "Calculated grid position: Row: $dropRow, Col: $dropCol")

                    // Ensure the widget only drops inside the grid
                    if (dropRow in 0 until 8 && dropCol in 0 until 4) {
                        // Ensure the widget fits in the new grid cell
                        if (isSpaceAvailable(dropRow, dropCol, widget.width, widget.height)) {
                            Log.d("DragDrop", "Space available, moving widget.")
                            // Move the widget to the new free position
                            moveWidgetToPosition(draggedView, dropRow, dropCol, widget)
                        } else {
                            Log.d("DragDrop", "No space available, restoring widget.")
                            // If no space is available, restore the widget to its original position
                            val originalPosition = widgetPositions[draggedView]
                            if (originalPosition != null) {
                                moveWidgetToPosition(
                                    draggedView,
                                    originalPosition.first,
                                    originalPosition.second,
                                    widget
                                )
                            }
                        }
                    } else {
                        Log.d("DragDrop", "Drop outside grid, restoring widget.")
                        // If outside the grid, restore the widget to its original position
                        val originalPosition = widgetPositions[draggedView]
                        if (originalPosition != null) {
                            moveWidgetToPosition(
                                draggedView,
                                originalPosition.first,
                                originalPosition.second,
                                widget
                            )
                        }
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    val draggedView = event.localState as View
                    draggedView.visibility = View.VISIBLE
                    true
                }

                else -> false
            }
        }
    }



    private fun getWidgetByView(view: View): Widget {
        // Retrieve the Widget object associated with the view using its tag
        return view.getTag() as? Widget
            ?: throw IllegalStateException("No widget associated with this view") // Better error handling
    }

    private fun moveWidgetToPosition(view: View, row: Int, col: Int, widget: Widget) {
        val currentPosition = widgetPositions[view]

        // Clear the old position
        currentPosition?.let { (oldRow, oldCol) ->
            for (r in oldRow until oldRow + widget.height) {
                for (c in oldCol until oldCol + widget.width) {
                    grid[r][c] = 0 // Free the space
                }
            }
        }

        // Ensure the new space is valid
        if (!isSpaceAvailable(row, col, widget.width, widget.height)) {
            Log.d("DragDrop", "New space occupied, trying to push other widgets")
            pushWidgetsIfNeeded(row, col, widget.width, widget.height)
        }

        // Update the widget's position in the UI
        val cellWidth = gridLayout.width / 4
        val cellHeight = gridLayout.height / 8
        view.x = col * cellWidth.toFloat()
        view.y = row * cellHeight.toFloat()

        // Update grid occupancy
        for (r in row until row + widget.height) {
            for (c in col until col + widget.width) {
                grid[r][c] = 1 // Mark as occupied
            }
        }

        // Store new position
        widgetPositions[view] = Pair(row, col)
    }


    private fun pushWidgetsIfNeeded(row: Int, col: Int, width: Int, height: Int) {
        for (view in widgetViews) {
            val (widgetRow, widgetCol) = widgetPositions[view] ?: continue
            val widget = getWidgetByView(view)

            if (widgetRow < row + height && widgetRow + widget.height > row &&
                widgetCol < col + width && widgetCol + widget.width > col) {

                Log.d("DragDrop", "Pushing widget ${widget.id} away from ($widgetRow, $widgetCol)")

                val newPosition = findClosestValidPosition(widgetRow, widgetCol, widget)
                if (newPosition != null) {
                    moveWidgetToPosition(view, newPosition.first, newPosition.second, widget)
                } else {
                    Log.d("DragDrop", "No space to push widget ${widget.id}, leaving in place")
                }
            }
        }
    }

    private fun getClosestValidPosition(row: Int, col: Int, width: Int, height: Int): Pair<Int, Int>? {
        // Check if the calculated position is out of bounds or invalid
        if (row >= grid.size || col >= grid[0].size || row + height > grid.size || col + width > grid[0].size) {
            // Try finding the closest valid position within bounds
            for (r in 0 until grid.size) {
                for (c in 0 until grid[0].size) {
                    // Check if this spot is available
                    if (isSpaceAvailable(r, c, width, height)) {
                        return Pair(r, c)
                    }
                }
            }
        }
        return null
    }



    private fun removeWidgetFromGrid(view: View) {
        // Retrieve the widget's position from the map
        val position = widgetPositions[view]
        if (position != null) {
            val (row, col) = position

            // Clear the grid cells occupied by this widget
            for (r in row until row + getWidgetByView(view).height) {
                for (c in col until col + getWidgetByView(view).width) {
                    grid[r][c] = 0 // Mark the cells as empty
                }
            }

            // Remove the widget from the position map
            widgetPositions.remove(view)

            // Remove the view itself from the layout
            (view.parent as? ViewGroup)?.removeView(view)
        }
    }

    // Helper class to represent grid cells
    data class GridCell(val row: Int, val col: Int)

    // Example Widget class with width and height
    data class Widget(val id: Int, val width: Int, val height: Int)
}
































