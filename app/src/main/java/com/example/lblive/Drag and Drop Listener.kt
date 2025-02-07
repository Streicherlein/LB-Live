package com.example.lblive

import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import androidx.gridlayout.widget.GridLayout


class WidgetDragTouchListener : View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val dragShadowBuilder = View.DragShadowBuilder(v)
            v?.startDragAndDrop(null, dragShadowBuilder, v, 0)
            return true
        }
        return false
    }
}

class WidgetDropListener : View.OnDragListener {
    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        when (event?.action) {
            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as View
                val targetView = v as GridLayout

                // Update the widget's position in the grid
                val params = GridLayout.LayoutParams()
                params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1)
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1)
                draggedView.layoutParams = params

                // Add the dragged view to the new position
                targetView.removeView(draggedView)
                targetView.addView(draggedView)
            }
        }
        return true
    }
}
