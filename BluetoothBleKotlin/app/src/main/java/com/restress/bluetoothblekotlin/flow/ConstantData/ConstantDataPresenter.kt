package com.restress.bluetoothblekotlin

import android.bluetooth.BluetoothGattCharacteristic
import android.content.IntentFilter

/**
 * Created by win10 on 2017/12/20.
 */
class ConstantDataPresenter : BaseMvpPresenterImpl<ConstantDataContract.View>(),
        ConstantDataContract.Presenter{


    override fun sendMsg(paramString: String,mNotifyCharacteristic: BluetoothGattCharacteristic?,mBlueService:BluetoothLeService?) {
        var arrayOfByte1 = ByteArray(20)
        val arrayOfByte2 = ByteArray(20)
        arrayOfByte2[0] = 0
        if (paramString.isNotEmpty()) {
            arrayOfByte1 = paramString.toByteArray()
        }

        mNotifyCharacteristic!!.setValue(arrayOfByte2[0].toInt(), 17, 0)
        mNotifyCharacteristic.value = arrayOfByte1
        mBlueService?.writeCharacteristic(mNotifyCharacteristic!!)
    }

    //注册广播时定义intent的各种属性
    override fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        return intentFilter
    }
}