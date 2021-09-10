package com.viatom.littlePu.thermo


import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.vaca.aojthermo.MainApplication
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

import java.lang.Thread.sleep

class ThermoBleDataWorker {

    private val connectChannel = Channel<String>(Channel.CONFLATED)
    var myBleDataManager: ThermoBleDataManager? = null



    data class FileProgress(
        var name: String = "",
        var progress: Int = 0,
        var success: Boolean = false
    )

    private val comeData = object : ThermoBleDataManager.OnNotifyListener {
        override fun onNotify(device: BluetoothDevice?, data: Data?) {
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



                            }
                        }
                    }
                }
            }
        }
    }




    private val connectState = object : ConnectionObserver {
        override fun onDeviceConnecting(device: BluetoothDevice) {

        }

        override fun onDeviceConnected(device: BluetoothDevice) {


        }

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {

        }

        override fun onDeviceReady(device: BluetoothDevice) {

        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {

        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
            myBleDataManager?.disconnect()?.enqueue()
        }

    }


    fun initWorker(context: Context, bluetoothDevice: BluetoothDevice?) {
        try {
            myBleDataManager?.disconnect()?.enqueue()
            sleep(200)
        } catch (ep: Exception) {

        }

        bluetoothDevice?.let {
            myBleDataManager?.connect(it)
                ?.useAutoConnect(false)
                ?.timeout(10000)
                ?.retry(500, 20)
                ?.done {

                    Log.i("BLE", "连接成功了.>>.....>>>>")


                }?.fail(object : FailCallback {
                    override fun onRequestFailed(device: BluetoothDevice, status: Int) {

                    }

                })
                ?.enqueue()
        }
    }

    suspend fun waitConnect() {
        connectChannel.receive()
    }

    init {
        myBleDataManager = ThermoBleDataManager(MainApplication.application)
        myBleDataManager?.setNotifyListener(comeData)
        myBleDataManager?.setConnectionObserver(connectState)
    }

}