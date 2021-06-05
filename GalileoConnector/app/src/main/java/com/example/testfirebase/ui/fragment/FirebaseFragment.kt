package com.example.testfirebase.ui.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testfirebase.R
import com.example.testfirebase.ui.Device
import com.example.testfirebase.ui.ListDeviceActivity
import com.example.testfirebase.ui.data.DataStore
import com.example.testfirebase.ui.utils.ConnectionType
import com.example.testfirebase.ui.utils.NetworkMonitorUtil
import com.google.firebase.database.FirebaseDatabase

class FirebaseFragment : Fragment() {
    lateinit var recyclerview : RecyclerView
    lateinit var nowifi: RelativeLayout
    lateinit var linearlayoutmanager: LinearLayoutManager

    private lateinit var adapter : DeviceAdapter
    var dataStore = DataStore.instance
    private var listDevice = ArrayList<Device>()
    private var listKey = ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_firebase,container,false)
        var activity = activity as Context
        nowifi = view.findViewById(R.id.ln_no_wifi)
        recyclerview = view.findViewById(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(activity)
        adapter = DeviceAdapter(activity,listDevice,listKey)
        recyclerview.adapter = adapter
        checkwifi()
        return view
    }

    override fun onResume() {
        super.onResume()
        dataStore.fetchDeviceList(object : DataStore.FetchDeviceCallback{
            override fun onFetch(list: ArrayList<Device>,keylist: ArrayList<String>) {
                listDevice = list
                listKey = keylist
                adapter.updateData(list,listKey)
            }
        })
    }
    fun checkwifi(){
        var network = NetworkMonitorUtil(requireContext())
        network.register()
        network.result = { isAvailable, type ->
            activity?.runOnUiThread() {
                when (isAvailable) {
                    true -> {
                        when (type) {
                            ConnectionType.Wifi -> {
                                recyclerview.visibility = View.VISIBLE
                                nowifi.visibility = View.GONE
                            }
                            ConnectionType.Cellular -> {
                                recyclerview.visibility = View.VISIBLE
                                nowifi.visibility = View.GONE
                            }
                            else -> { }
                        }
                    }
                    false -> {
                        Log.i("NETWORK_MONITOR_STATUS", "No Connection")
                        recyclerview.visibility = View.GONE
                        nowifi.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    class DeviceAdapter(private var context: Context, private val listDevice : ArrayList<Device>, private val listKey: ArrayList<String>): RecyclerView.Adapter<DeviceAdapter.ViewHolder>(){
        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val humid: TextView
            var mac : TextView
            var led: TextView
            var temp : TextView
            var rain: TextView
            var layout: LinearLayout
            init{
                humid = itemView.findViewById(R.id.tv_humid)
                mac = itemView.findViewById(R.id.tv_mac)
                led = itemView.findViewById(R.id.tv_led)
                temp = itemView.findViewById(R.id.tv_temp)
                rain = itemView.findViewById(R.id.tv_rain)
                layout = itemView.findViewById(R.id.layout_item)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_device,parent,false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var device = listDevice.get(position)
            holder.humid.text = device.Humidity.toString()
            if(device.LED.toString().equals("1")){
                holder.led.text = "ON"
            }else{
                holder.led.text = "OFF"
            }
            holder.temp.text = device.Temperature.toString()
            holder.mac.text = device.MAC_Address
            holder.rain.text = device.Rain.toString()
            holder.layout.setOnClickListener {
                var dialogLayout = LayoutInflater.from(context).inflate(R.layout.layout_edit_text,null)
                var sw = dialogLayout.findViewById<Switch>(R.id.sw) as Switch
                sw.isChecked = device.LED.toString().equals("1")
                sw.setOnCheckedChangeListener { _, ischecked ->
                    if (ischecked) {
                        pushData(1, position)
                    } else {
                        pushData(0, position)
                    }
                }
                AlertDialog.Builder(context).apply {
                    setPositiveButton("OK"){dialog, which ->
                        dialog.dismiss()
                    }
                    setView(dialogLayout)
                    show()
                }
            }
        }
        fun pushData(led: Int,position: Int){
            Log.d("TAG","go to push: $led , $position, ${listKey[position]}")
            var reference = FirebaseDatabase.getInstance().getReference("CESLAB")

            Handler().postDelayed({
                reference.child(listKey[position]).child("LED").child("LED").setValue(led)
            },500)
        }
        override fun getItemCount(): Int {
            return listDevice.size
        }
        fun updateData(data: List<Device>, key: List<String>) {
            listDevice.clear()
            listDevice.addAll(data)
            listKey.clear()
            listKey.addAll(key)
            notifyDataSetChanged()
        }
    }
}