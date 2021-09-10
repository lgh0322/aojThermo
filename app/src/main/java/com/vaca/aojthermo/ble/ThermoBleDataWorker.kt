package com.viatom.littlePu.thermo


import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.vaca.aojthermo.MainActivity
import com.vaca.aojthermo.MainApplication
import com.vaca.aojthermo.Temperature
import com.vaca.aojthermo.ble.BleServer
import com.vaca.aojthermo.utils.CRCUtils
import com.viatom.checkme.ble.manager.ThermoBleDataManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import org.greenrobot.eventbus.EventBus

import java.lang.Thread.sleep

class ThermoBleDataWorker {


    val  myBleDataManager = ThermoBleDataManager(MainApplication.application)



    private val comeData = object : ThermoBleDataManager.OnNotifyListener {
        override fun onNotify(device: BluetoothDevice?, data: Data?) {
            Log.e("yes","ye222s")
            data?.value?.apply {
                val size = this.size
                if (size == 8) {
                    if (this[0] == 0xaa.toByte()) {
                        if (this[1] == 0x01.toByte()) {
                            if (CRCUtils.calXOR(this) == this[7]) {
                                val a1 = this[4].toUByte().toInt()
                                val a2 = this[5].toUByte().toInt()
                                val a3 = (a1 * 256 + a2) / 100f
                                val a4 = Math.round(a3 * 10) / 10f;
                                EventBus.getDefault().post(Temperature(a4))
                                Log.e("yes","yes")

                            }
                        }
                    }
                }
            }
        }
    }




    private val connectState = object : ConnectionObserver {
        override fun onDeviceConnecting(device: BluetoothDevice) {
            MainActivity.stateString.postValue("蓝牙断开中")
        }

        override fun onDeviceConnected(device: BluetoothDevice) {
            MainActivity.stateString.postValue("蓝牙已连接")

        }

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
            MainActivity.stateString.postValue("蓝牙连接失败")
        }

        override fun onDeviceReady(device: BluetoothDevice) {
            MainActivity.stateString.postValue("蓝牙已就绪")
        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {
            MainActivity.stateString.postValue("蓝牙断开中")
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
            BleServer.connectFlag=false
            MainActivity.stateString.postValue("蓝牙已断开")
        }

    }


    fun initWorker(context: Context, bluetoothDevice: BluetoothDevice?) {
        bluetoothDevice?.let {
            myBleDataManager.connect(it)
                ?.useAutoConnect(false)
                ?.timeout(10000)
                ?.retry(500, 20)
                ?.done {
                    Log.i("BLE", "连接成功了.>>.....>>>>")
                }?.fail(object : FailCallback {
                    override fun onRequestFailed(device: BluetoothDevice, status: Int) {
                        BleServer.connectFlag=false
                    }

                })
                ?.enqueue()
        }
    }


    init {

        myBleDataManager.setNotifyListener(comeData)
        myBleDataManager.setConnectionObserver(connectState)
    }

}