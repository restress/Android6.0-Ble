package com.restress.bluetoothblekotlin

import android.bluetooth.BluetoothGattCharacteristic
import android.content.IntentFilter


/**
 * Created by win10 on 2017/12/20.
 */
object ConstantDataContract{

    interface View : BaseMvpView{
        fun updateConnectionState(resourceId: Int)

        fun displayData(data: String?)

        fun clearUI()
    }

    interface Presenter : BaseMvpPresenter<View>{

        fun sendMsg(paramString: String,
                    mNotifyCharacteristic: BluetoothGattCharacteristic?,
                    mBlueService:BluetoothLeService?)

        fun makeGattUpdateIntentFilter(): IntentFilter
    }
}