package com.example.lblive

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class WidgetSelectionDialog : DialogFragment() {
    private var listener: ((WidgetType) -> Unit)? = null

    enum class WidgetType { ONE_BY_FOUR, ONE_BY_ONE, TWO_BY_ONE }

    fun setOnWidgetSelectedListener(listener: (WidgetType) -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Wähle ein Widget")
        builder.setItems(arrayOf("1x4", "1x1", "2x1")) { _, which ->
            val widgetType = when (which) {
                0 -> WidgetType.ONE_BY_FOUR
                1 -> WidgetType.ONE_BY_ONE
                2 -> WidgetType.TWO_BY_ONE
                else -> null
            }
            widgetType?.let { listener?.invoke(it) }
        }
        return builder.create()
    }
}
