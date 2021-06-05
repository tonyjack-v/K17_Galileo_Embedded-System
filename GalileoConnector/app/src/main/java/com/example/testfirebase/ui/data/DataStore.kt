package com.example.testfirebase.ui.data

import android.util.Log
import com.example.testfirebase.ui.Device
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class DataStore private constructor(){
    private var macList = ArrayList<String>()
    private var cache : MutableMap<String,String> = mutableMapOf()
    private var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("CESLAB")
    companion object{
        var instance = DataStore()
    }
    fun getMacList(): ArrayList<String>{
        return macList
    }

    fun getCache(): MutableMap<String,String>{
        return cache
    }

    fun addCache(mac: String,ip:String){
       cache.put(mac,ip)
    }
    fun fetchDeviceList(fetchdeviceCallback: FetchDeviceCallback ){
        Log.d("TAG","go to fetch device list")
        ref.addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    var deviceList = ArrayList<Device>()
                    var keyList = ArrayList<String>()
                    macList.clear()
                    Log.d("TAG","goto snapshot: $snapshot")
                    snapshot.let {
                        for(h in snapshot.children){
                            h.key?.let { it1 -> keyList.add(it1)}
                            Log.d("TAG","goto key: ${h.key}")
                            Log.d("TAG","goto snapshot children: ${h.getValue()}")
//                            h.child("LED")
                            Log.d("TAG","LED: ${h.child("LED")}")
                            var device = h.child("SENSOR").getValue(Device::class.java)
                            if (device != null) {
                                device.LED = h.child("LED").child("LED").getValue() as Long
                                deviceList.add(device)
                                macList.add(device.MAC_Address)
                            }
                            Log.d("TAG","device: $deviceList")
                        }
                        fetchdeviceCallback.onFetch(deviceList,keyList)
                    }
                }
            }
        )
    }
    interface FetchDeviceCallback{
        fun onFetch(list: ArrayList<Device>, keylist: ArrayList<String>)
    }

}