package com.example.lblive

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.lblive.com.example.lblive.DeviceListAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import android.widget.Button

class PageTwoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceListAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private val devices = mutableListOf<Device>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.second_page, container, false)

        recyclerView = view.findViewById(R.id.device_list_item)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DeviceListAdapter(devices) { device ->
            devices.remove(device)
            adapter.notifyDataSetChanged()
            saveDevices()
        }
        recyclerView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener { checkDeviceStatus() }

        val scanButton: Button = view.findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            performNetworkScan()
        }

        loadDevices() // Geräte beim Start laden
        checkDeviceStatus()

        return view
    }


    private fun performNetworkScan() {
        swipeRefreshLayout.isRefreshing = true

        thread {
            val client = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.MILLISECONDS)
                .readTimeout(100, TimeUnit.MILLISECONDS)
                .writeTimeout(100, TimeUnit.MILLISECONDS)
                .build()

            val subnet = getSubnet()
            for (i in 1..256) {
                val ip = "$subnet.$i"
                val url = "http://$ip/info"

                try {
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.string()?.let {
                            val json = JSONObject(it)
                            val name = json.optString("Name")
                            if (name.isNotEmpty()) {
                                val device = Device(name, ip, reachable = true)

                                synchronized(devices) {
                                    if (!devices.any { it.ip == ip }) {
                                        devices.add(device)
                                    }
                                }

                                checkDeviceStatus()
                                saveDevices()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("NetworkScan", "Failed for $ip: ${e.message}")
                }
            }

            requireActivity().runOnUiThread {
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun checkDeviceStatus() {
        thread {
            val client = OkHttpClient()

            devices.forEach { device ->
                val url = "http://${device.ip}/status"
                val url2 = "http://${device.ip}/info"

                try {
                    // Erste Anfrage: Gerätestatus überprüfen
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.string()?.let {
                            val json = JSONObject(it)
                            val power = json.optString("power")
                            device.reachable = true
                            device.status = when (power) {
                                "on" -> "green"
                                "sleep" -> "yellow"
                                else -> "red"
                            }
                        }
                    } else {
                        device.reachable = false
                        device.status = "gray"
                    }
                } catch (e: Exception) {
                    device.reachable = false
                    device.status = "gray"
                }

                // Zweite Anfrage: Gerätename überprüfen
                try {
                    val request2 = Request.Builder().url(url2).build()
                    val response2 = client.newCall(request2).execute()
                    if (response2.isSuccessful) {
                        response2.body?.string()?.let {
                            val json = JSONObject(it)
                            val newName = json.optString("name")
                            println(json)

                            // Überprüfen, ob der Name sich geändert hat
                            if (newName.isNotEmpty() && newName != device.name) {
                                device.name = newName // Name aktualisieren
                            }
                        }
                    } else {
                        device.reachable = false
                        device.status = "gray"
                    }
                } catch (e: Exception) {
                    device.reachable = false
                    device.status = "gray"
                }
            }

            // UI-Update im Hauptthread
            requireActivity().runOnUiThread {
                adapter.notifyDataSetChanged() // Geräte-Listeneinträge aktualisieren
                swipeRefreshLayout.isRefreshing = false // Swipe-Animation beenden
            }
        }
    }



    private fun saveDevices() {
        val sharedPrefs = requireContext().getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val deviceJsonArray = devices.map { JSONObject().apply {
            put("name", it.name)
            put("ip", it.ip)
            put("reachable", it.reachable)
        } }.toString()
        editor.putString("devices", deviceJsonArray)
        editor.apply()
    }

    private fun loadDevices() {
        val sharedPrefs = requireContext().getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        val deviceJsonArray = sharedPrefs.getString("devices", null)
        deviceJsonArray?.let {
            val jsonArray = org.json.JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                devices.add(Device(
                    name = json.getString("name"),
                    ip = json.getString("ip"),
                    reachable = json.getBoolean("reachable")
                ))
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun checkDeviceReachability() {
        thread {
            val client = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.MILLISECONDS)
                .build()

            devices.forEach { device ->
                try {
                    val request = Request.Builder().url("http://${device.ip}/info").build()
                    val response = client.newCall(request).execute()
                    device.reachable = response.isSuccessful
                } catch (e: Exception) {
                    device.reachable = false
                }
            }

            requireActivity().runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun getSubnet(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                if (!networkInterface.isLoopback && networkInterface.isUp) {
                    val inetAddresses = networkInterface.inetAddresses
                    while (inetAddresses.hasMoreElements()) {
                        val inetAddress = inetAddresses.nextElement()
                        if (inetAddress is InetAddress && inetAddress.hostAddress.contains(":").not()) {
                            return inetAddress.hostAddress.substringBeforeLast(".")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "192.168.178" // Fallback
    }
}






