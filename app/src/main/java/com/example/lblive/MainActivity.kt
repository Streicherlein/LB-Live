package com.example.lblive

import android.os.Bundle
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Hier den Fehler korrigieren

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = Pageadapt(this)

    }


}
