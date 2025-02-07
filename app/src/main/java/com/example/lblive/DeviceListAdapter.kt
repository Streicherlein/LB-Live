package com.example.lblive.com.example.lblive

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lblive.R
import com.example.lblive.Device
import android.widget.Button

class DeviceListAdapter(
    private val devices: MutableList<Device>,
    private val onDelete: (Device) -> Unit
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_list_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name
        holder.deviceIp.text = device.ip
        holder.statusLed.setBackgroundResource(
            when (device.status) {
                "green" -> R.drawable.led_green
                "yellow" -> R.drawable.led_yellow
                "red" -> R.drawable.led_red
                else -> R.drawable.led_gray
            }
        )
        holder.deleteButton.setOnClickListener { onDelete(device) }
    }

    override fun getItemCount(): Int = devices.size

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        val deviceIp: TextView = itemView.findViewById(R.id.deviceIp)
        val statusLed: View = itemView.findViewById(R.id.statusLed)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

}


