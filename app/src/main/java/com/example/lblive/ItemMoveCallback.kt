package com.example.lblive

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallback(
    private val listener: ItemMoveListener,
    private val onItemDroppedOnTrash: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN or
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
) {

    private var isDragging = false

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPos = viewHolder.adapterPosition
        val toPos = target.adapterPosition

        if (listener.isMovable(fromPos, toPos)) {
            listener.onItemMove(fromPos, toPos)
            return true
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            isDragging = true
            listener.onDragStateChanged(true)
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            isDragging = false
            listener.onDragStateChanged(false)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (isDragging) {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemDroppedOnTrash(position)
            }
        }
    }

    interface ItemMoveListener {
        fun onItemMove(fromPosition: Int, toPosition: Int)
        fun isMovable(fromPosition: Int, toPosition: Int): Boolean
        fun onDragStateChanged(isDragging: Boolean)
    }
}


