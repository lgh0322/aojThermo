package com.vaca.aojthermo.ble

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vaca.aojthermo.MainApplication
import com.viatom.littlePu.thermo.ThermoBleDataWorker

object BleServer {


    val bluetoothManager =
        MainApplication.application.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
    val mBluetoothAdapter = bluetoothManager.adapter
    val scanner = mBluetoothAdapter.bluetoothLeScanner

    var connectFlag=false

    val settings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()


    val builder = ScanFilter.Builder()
    val filter = builder.build()

    var bleData=ThermoBleDataWorker()

    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if(result!=null){
                val device=result.device
                val rssi=result.rssi
                if(rssi<-70){
                    return
                }
                val name=result.device?.name
                if(name!=null){
                    Log.e("Ble Scan",name)
                    if(name.contains("AOJ")){
                        Log.e("Ble Scan22",name)
                        if(!connectFlag){
                            connectFlag=true
                            bleData.initWorker(MainApplication.application,device)
                        }
                    }
                }
            }

        }
    }

    fun startScan() {
        scanner.startScan(listOf(filter), settings, scanCallback)
    }

}