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
import android.view.LayoutInflater
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

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add)


        // Set up the FAB click listener to show size options
        fabAdd.setOnClickListener {
            showWidgetSizeMenu(it)
        }

        // Set up drag and drop functionality
        setupDragAndDrop(view)
    }

    private fun createWidgetView(widget: Widget): View {
        val widgetLayoutRes = when (widget.width to widget.height) {
            1 to 1 -> R.layout.mute_item
            1 to 4 -> R.layout.slider_item
            2 to 2 -> R.layout.equalizer_item
            2 to 1 -> R.layout.preset_item
            else -> throw IllegalArgumentException("Unknown widget size")
        }

        // Inflate the layout based on widget size
        val widgetView = LayoutInflater.from(requireContext()).inflate(widgetLayoutRes, null)

        // Optionally set widget-specific content, for example:
        // widgetView.findViewById<TextView>(R.id.widget_title).text = "Widget ${widget.id}"

        return widgetView
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

            // Create the widget view from its layout resource
            val widgetView = createWidgetView(widget)

            // Set the size and position of the widget
            val cellWidth = gridLayout.width / 4
            val cellHeight = gridLayout.height / 8

            widgetView.layoutParams = GridLayout.LayoutParams().apply {
                width = cellWidth * widget.width
                height = cellHeight * widget.height
                columnSpec = GridLayout.spec(col, widget.width)
                rowSpec = GridLayout.spec(row, widget.height)
                setMargins(0, 0, 0, 0)
            }

            // Store the widget in the view's tag for later reference
            widgetView.setTag(widget)

            widgetView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val shadow = View.DragShadowBuilder(v)
                    v.startDragAndDrop(null, shadow, v, 0)
                    v.visibility = View.INVISIBLE  // Hide widget during drag
                    true
                } else {
                    false
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
        for (r in grid.indices) {
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
                    view.findViewById<FloatingActionButton>(R.id.fab_add).visibility = View.GONE
                    view.findViewById<FloatingActionButton>(R.id.fab_delete).visibility =
                        View.VISIBLE
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
                    var dropRow = (dropY / cellHeight).toInt()
                    var dropCol = (dropX / cellWidth).toInt()

                    // Log the calculated grid cell
                    Log.d("DragDrop", "Calculated grid position: Row: $dropRow, Col: $dropCol")


                    val removeRow = dropRow
                    val removeCol = dropCol

                        // Step 1: Adjust for out-of-bounds placement
                        val adjustedPosition = adjustForBounds(dropRow, dropCol, widget)
                        dropRow = adjustedPosition.first
                        dropCol = adjustedPosition.second

                        // Step 2: If space is occupied, move other widgets
                        if (!isSpaceAvailable(dropRow, dropCol, widget.width, widget.height)) {
                            Log.d("DragDrop", "No space available, finding alternative position.")

                            // Try moving existing widgets
                            moveWidgetsAway(dropRow, dropCol, widget.width, widget.height)

                            // Check again if space is available after moving widgets
                            if (!isSpaceAvailable(dropRow, dropCol, widget.width, widget.height)) {
                                Log.d(
                                    "DragDrop",
                                    "Still no space available, restoring to original position."
                                )
                                val originalPosition = widgetPositions[draggedView]
                                if (originalPosition != null) {
                                    dropRow = originalPosition.first
                                    dropCol = originalPosition.second
                                }
                            }
                        }

                        // Step 3: Move the widget to the determined position
                        Log.d("DragDrop", "Final drop position: Row: $dropRow, Col: $dropCol")
                        moveWidgetToPosition(draggedView, dropRow, dropCol, widget)
                        removeWidgetFromGrid(draggedView, removeRow, removeCol, dropRow, dropCol)
                        true

                }


                DragEvent.ACTION_DRAG_ENDED -> {
                    val draggedView = event.localState as View
                    draggedView.visibility = View.VISIBLE
                    view.findViewById<FloatingActionButton>(R.id.fab_add).visibility =
                        View.VISIBLE
                    view.findViewById<FloatingActionButton>(R.id.fab_delete).visibility =
                        View.GONE
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

        // Clear old position
        currentPosition?.let { (oldRow, oldCol) ->
            for (r in oldRow until oldRow + widget.height) {
                for (c in oldCol until oldCol + widget.width) {
                    grid[r][c] = 0 // Free the space
                }
            }
        }

        // Check if out of bounds and adjust
        val adjustedPosition = adjustForBounds(row, col, widget)
        var (newRow, newCol) = adjustedPosition

        // Check if the new space is occupied and move widgets away
        if (!isSpaceAvailable(newRow, newCol, widget.width, widget.height)) {
            pushWidgetsIfNeeded(newRow, newCol, widget.width, widget.height)
        }

        // Now place the widget at its final position
        val cellWidth = gridLayout.width / 4
        val cellHeight = gridLayout.height / 8
        view.x = newCol * cellWidth.toFloat()
        view.y = newRow * cellHeight.toFloat()

        // Update grid occupancy
        for (r in newRow until newRow + widget.height) {
            for (c in newCol until newCol + widget.width) {
                grid[r][c] = 1 // Mark as occupied
            }
        }

        // Store new position
        widgetPositions[view] = Pair(newRow, newCol)
    }

    private fun adjustForBounds(row: Int, col: Int, widget: Widget): Pair<Int, Int> {
        var newRow = row
        var newCol = col
        Log.d("DragDrop", "Adjusting for bounds: Row: $row, Col: $col")
        // Ensure the widget doesn't exceed the grid height
        if (newRow + widget.height > 8) {
            newRow = 8 - widget.height // Move up until it fits
            Log.d("DragDrop", "New Row after adjustment: $newRow")
        }

        // Ensure the widget doesn't exceed the grid width
        if (newCol + widget.width > 4) {
            newCol = 4 - widget.width // Move left if needed
            Log.d("DragDrop", "New Col after adjustment: $newCol")
        }

        return Pair(newRow, newCol)
    }

    private fun moveWidgetsAway(row: Int, col: Int, width: Int, height: Int) {
        val widgetsToMove = mutableListOf<View>()

        // Find widgets in the target space
        for (view in widgetViews) {
            val position = widgetPositions[view]
            if (position != null) {
                val (widgetRow, widgetCol) = position
                val widget = getWidgetByView(view)

                // Check if this widget is in the way
                if (widgetRow < row + height && widgetRow + widget.height > row &&
                    widgetCol < col + width && widgetCol + widget.width > col) {
                    widgetsToMove.add(view)
                }
            }
        }

        // Move each widget to a nearby free space
        for (view in widgetsToMove) {
            val widget = getWidgetByView(view)
            val newPosition = findFirstFreeSpace(widget.width, widget.height)
            if (newPosition != null) {
                moveWidgetToPosition(view, newPosition.first, newPosition.second, widget)
            }
        }
    }





    private fun pushWidgetsIfNeeded(row: Int, col: Int, width: Int, height: Int) {
        val widgetsToMove = mutableListOf<View>()

        // Identify widgets that need to be moved
        for (view in widgetViews) {
            val (widgetRow, widgetCol) = widgetPositions[view] ?: continue
            val widget = getWidgetByView(view)

            if (widgetRow < row + height && widgetRow + widget.height > row &&
                widgetCol < col + width && widgetCol + widget.width > col) {

                widgetsToMove.add(view)
            }
        }

        // Move each affected widget to the nearest free space
        for (view in widgetsToMove) {
            val widget = getWidgetByView(view)
            val newPosition = findClosestValidPosition(row, col, widget)

            if (newPosition != null) {
                moveWidgetToPosition(view, newPosition.first, newPosition.second, widget)
            } else {
                Log.d("DragDrop", "No space found for widget ${widget.id}, leaving it in place")
            }
        }
    }





    private fun removeWidgetFromGrid(view: View, deleteRow: Int, deleteCol: Int, dropRow: Int, dropCol: Int) {
        // Retrieve the widget's position from the map
        val position = widgetPositions[view]
        Log.d("WidgetRemoval", "Checking to remove widget at position: $position")

        // Make sure the position is not null and it's within valid grid bounds
        if (position != null) {
            // Check if the widget is dropped in the bottom-right field (7, 3)
            if (deleteRow == 7 && deleteCol == 3) {
                // Remove the widget from the grid
                for (r in dropRow until dropRow + getWidgetByView(view).height) {
                    for (c in dropCol until dropCol + getWidgetByView(view).width) {
                        grid[r][c] = 0 // Mark the cells as empty
                    }
                }

                // Remove the widget from the position map
                widgetPositions.remove(view)

                // Remove the view itself from the layout
                (view.parent as? ViewGroup)?.removeView(view)

                // Optionally log the removal or show a message
                Log.d("WidgetRemoval", "Widget removed at position ($dropRow, $dropCol)")

            } else {
                // If the drop position isn't the bottom-right field, no need to remove
                Log.d("WidgetRemoval", "Widget drop position ($dropRow, $dropCol) is not the bottom-right field.")

            }
        }

    }

    // Helper class to represent grid cells
    data class GridCell(val row: Int, val col: Int)

    // Example Widget class with width and height
    data class Widget(val id: Int, val width: Int, val height: Int)
}
































