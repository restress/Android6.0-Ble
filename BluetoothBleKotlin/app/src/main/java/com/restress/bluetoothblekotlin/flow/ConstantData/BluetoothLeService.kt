package com.restress.bluetoothblekotlin

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

/**
 * Created by win10 on 2017/12/27.
 */
class BluetoothLeService : Service {

    private var mBluetoothManager : BluetoothManager? = null
    private var mBluetoothAdapter :BluetoothAdapter? =null
    private var mBluetoothDeviceAddress :String? =null
    private var mBluetoothGatt :BluetoothGatt? =null
    private var mConnectionState : Int = STATE_DISCONNECTED
    private var number : Int = 0


    constructor() : super()

    companion object {
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2

        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"


        val SPECIFIC_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val SPECIFIC_CHARCTER_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }

    //初始化本地iBinder
    inner class LocalBinder : Binder() {
       fun getService() :BluetoothLeService = this@BluetoothLeService
    }

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }
    private val mBinder:IBinder = LocalBinder()

    //初始化本地蓝牙适配器，如果初始化成功，返回true
    //在activity中调用
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.getAdapter()
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    //连接远程GATTserver，如果初始化成功，返回true。回调触发函数BluetoothGattCallback#onConnectionStateChange
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")

            return when(mBluetoothGatt!!.connect()){
                true -> {
                    mConnectionState = STATE_CONNECTING
                    true
                }
                false -> false
            }
        }

        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING

        return true
    }


    // GATT返回值，例如连接状态和service的改变 etc
    private val mGattCallback = object : BluetoothGattCallback() {
        //连接状态改变
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices())

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                broadcastUpdate(intentAction)
            }
        }

        //发现服务
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
            }
        }

        //被读
        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                // number += 1;
            }
        }

        //特性改变
        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            //TODO
            /* if ( number == 19){
                number = 0;
            }else {*/
            number += 1
            //}
        }

        //特性书写
        override fun onCharacteristicWrite(gatt: BluetoothGatt,
                                           characteristic: BluetoothGattCharacteristic,
                                           status: Int) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            number = 0
        }
    }

    //广播连接状态的改变
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)

    }

    //广播的更新，包括数据的处理,读取heart的数据
    private fun broadcastUpdate(action: String,
                                characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        //这是处理和解析数据的方法
        when(characteristic.uuid){
            SPECIFIC_CHARCTER_UUID ->{
                //TODO 处理数据
                intent.putExtra(EXTRA_DATA, characteristic.value)
            }

            else -> {
                intent.putExtra(EXTRA_DATA, characteristic.value)
            }
        }

        sendBroadcast(intent)
    }


    //获取C2541的特性服务
    fun getBluetoothGattCharacteristic(): BluetoothGattCharacteristic {
        return mBluetoothGatt!!.getService(SPECIFIC_SERVICE_UUID).getCharacteristic(SPECIFIC_CHARCTER_UUID)
    }

    //开启或者关闭notification
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        //此处设置characteridtic的notification
       //onCharacteristicChanged()回调
        if (SPECIFIC_CHARCTER_UUID == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(
                    UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    //断开连接远程GATTserver，回调触发函数BluetoothGattCallback#onConnectionStateChange
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    //发送characteristic，回调触发函数BluetoothGattCallback#onCharacteristicWrite
    //添加write的函数
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
        mBluetoothGatt!!.writeCharacteristic(characteristic)
        return true
    }
}