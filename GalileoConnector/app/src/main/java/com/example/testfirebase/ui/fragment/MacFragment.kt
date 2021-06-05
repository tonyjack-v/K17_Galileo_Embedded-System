package com.example.testfirebase.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.testfirebase.R
import com.example.testfirebase.ui.data.DataStore
import dmax.dialog.SpotsDialog
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.net.InetAddress

class MacFragment: Fragment() {
    lateinit var veri1: EditText
    lateinit var veri2: EditText
    lateinit var veri3: EditText
    lateinit var veri4: EditText
    lateinit var veri5: EditText
    lateinit var veri6: EditText
    lateinit var getweb: Button
    lateinit var webview: WebView
    lateinit var spinner_method: Spinner
    lateinit var ln_spinner_device: LinearLayout
    lateinit var spinner_device: Spinner
    lateinit var ln_mac_edit: LinearLayout
    private  var method: Int = 0
    lateinit var spinner_picker : String
    private var TIMEOUT: Int =100
    lateinit var dialog: AlertDialog
    var dataStore = DataStore.instance
    private var selected_method: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_mac, container, false)
        veri1 = view.findViewById(R.id.veri1)
        veri2 = view.findViewById(R.id.veri2)
        veri3 = view.findViewById(R.id.veri3)
        veri4 = view.findViewById(R.id.veri4)
        veri5 = view.findViewById(R.id.veri5)
        veri6 = view.findViewById(R.id.veri6)
        getweb = view.findViewById(R.id.getweb)
        webview = view.findViewById(R.id.webview)
        spinner_device = view.findViewById(R.id.spinner_device)
        spinner_method = view.findViewById(R.id.spinner_method)
        ln_spinner_device = view.findViewById(R.id.ln_spinner_device)
        ln_mac_edit = view.findViewById(R.id.ln_mac_edit)

        dialog = SpotsDialog.Builder()
            .setContext(requireContext())
            .setMessage("Loading...")
            .setCancelable(false).build()
        return view
    }

    override fun onStart() {
        super.onStart()

    }

    fun setUpSpinner(){
        var method_list = resources.getStringArray(R.array.mode)
        var listDevice: List<String> = DataStore.instance.getMacList()
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,method_list)
        val adapter_device = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,listDevice)
        spinner_method.adapter = adapter
        spinner_method.setSelection(selected_method)
        spinner_method.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                Log.d("TAG","pos: $position")
                selected_method = position
                if(position == 0){
                    method = 0
                    ln_mac_edit.visibility = View.VISIBLE
                    ln_spinner_device.visibility = View.GONE
                }else{
                    method = 1
                    ln_mac_edit.visibility = View.GONE
                    ln_spinner_device.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
        spinner_device.adapter = adapter_device
        spinner_device.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinner_picker = listDevice[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG","RESUME")
        setUpSpinner()

        getweb.setOnClickListener{
            dialog.show()
            if(method == 0){
                if(veri1.text.isEmpty() || veri2.text.isEmpty() ||veri3.text.isEmpty() ||veri4.text.isEmpty() ||
                    veri5.text.isEmpty()){
                    Toast.makeText(requireContext(), "Empty field!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }else{
                    var mac = veri1.text.toString()+":"+veri2.text.toString()+":"+veri3.text.toString()+":"+veri4.text.toString()+":"+veri5.text.toString()+":"+veri6.text.toString()
                    Log.d("TAG", "mac: $mac")
                    Thread{
                        startPingServer(requireContext(),mac)
                    }.start()
                }
            }else if(method == 1){
                Thread{
                    startPingServer(requireContext(),spinner_picker)
                }.start()
            }
        }
    }

    fun startPingServer(context: Context,mac: String){
        var wifimanager : WifiManager =context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiInfo = wifimanager.connectionInfo
        var subnet: String = getSubnetAddress(wifimanager.dhcpInfo.gateway)
        //check cache
        var cache = dataStore.getCache()
        if(!cache.isEmpty()){
            var ip = cache[mac]
            if(InetAddress.getByName(ip).isReachable(TIMEOUT)){
                if (ip != null) {
                    loadWeb(ip)
                }
            }else{
                for (i in 1..255){
                    var host = "$subnet.$i"
                    Log.d("TAG1","Ping host: $host")
                    if(InetAddress.getByName(host).isReachable(TIMEOUT)){
                        var stfMacAddress:String = getMacAddressFromIP(host)
                        Log.d("TAG1","reachable: $stfMacAddress")
                        if(mac.equals(stfMacAddress, ignoreCase = true)){
                            Log.d("TAG1","ping success")
                            dataStore.addCache(mac,host)
                            loadWeb(host)
                            break
                        }
                    }
                    if(i == 255){
                        activity?.runOnUiThread{
                            dialog.dismiss()
                            Toast.makeText(requireContext(),"No device are available!",Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }else{
            for (i in 1..255){
                var host = "$subnet.$i"
                Log.d("TAG1","Ping host: $host")
                if(InetAddress.getByName(host).isReachable(TIMEOUT)){
                    var stfMacAddress:String = getMacAddressFromIP(host)
                    Log.d("TAG1","reachable: $stfMacAddress")
                    if(mac.equals(stfMacAddress, ignoreCase = true)){
                        Log.d("TAG1","ping success")
                        dataStore.addCache(mac,host)
                        loadWeb(host)
                        break
                    }
                }
                if(i == 255){
                    activity?.runOnUiThread{
                        dialog.dismiss()
                        Toast.makeText(requireContext(),"No device are available!",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }

    fun loadWeb(host: String){
        activity?.runOnUiThread {
            webview.clearCache(true)
            var ip = "$host:9000"
            webview.settings.javaScriptEnabled = true
            webview.loadUrl(ip)
            dialog.dismiss()
            Toast.makeText(requireContext(),"Connect successful!",Toast.LENGTH_LONG).show()
        }
    }

    fun getSubnetAddress(address: Int): String{
        return String.format(
            "%d.%d.%d", address and 0xff,
            address shr 8 and 0xff,
            address shr 16 and 0xff
        )
    }

    fun getMacAddressFromIP(host: String): String{
        var bufferReader: BufferedReader? = null
        try{
            bufferReader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String
            var iterator = bufferReader.lineSequence().iterator()
            while (iterator.hasNext()){
                val line = iterator.next()
                val splitted = line.split(" +".toRegex()).toTypedArray()
                if (splitted != null && splitted.size >= 4) {
                    val ip:String = splitted[0]
                    val mac:String = splitted[3]
                    if (mac.matches("..:..:..:..:..:..".toRegex())) {
                        if (ip.equals(host, ignoreCase = true)) {
                            return mac
                        }
                    }
                }
            }

        }catch (e: FileNotFoundException) {
            e.printStackTrace();
        } catch (e: IOException) {
            e.printStackTrace();
        } finally{
            try {
                if (bufferReader != null) {
                    bufferReader.close()
                };
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        return "00:00:00:00"
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TAG","DESTROY")
    }

    override fun onPause() {
        super.onPause()
        Log.d("TAG1","PAUSE")
    }
}