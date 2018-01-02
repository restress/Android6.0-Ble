package com.restress.bluetoothblekotlin

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context


/**
 * Created by win10 on 2017/12/20.
 */
object DeviceScanContract{

    interface View : BaseMvpView{

        fun updateRecyclerView(mAdapter:DeviceAdapter?)
    }

    interface Presenter : BaseMvpPresenter<View>{

        fun isLocationEnable(context: Context):Boolean

        fun scanLeDevice(enable: Boolean, mbluetoothLeScanner: BluetoothLeScanner?,
                         mLeScanCallback: ScanCallback)


    }
}