package com.example.lblive

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class Pageadapt(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2 // Anzahl der Seiten

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> com.example.lblive.PageOneFragment() // Slider
            1 -> com.example.lblive.PageTwoFragment() // Zweite Seite
            else -> throw IllegalStateException("Unerwartete Seitenposition: $position")
        }
    }
}
