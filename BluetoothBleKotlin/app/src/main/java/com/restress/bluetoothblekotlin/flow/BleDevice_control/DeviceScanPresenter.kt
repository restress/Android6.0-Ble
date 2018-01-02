package com.restress.bluetoothblekotlin


import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.location.LocationManager
import android.os.Handler

/**
 * Created by win10 on 2017/12/20.
 */
class DeviceScanPresenter : BaseMvpPresenterImpl<DeviceScanContract.View>(),
        DeviceScanContract.Presenter{

    companion object {
        private const val SCAN_PERIOD :Long = 10000
    }

     override fun isLocationEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return  (networkProvider || gpsProvider)
    }

    //搜索函数，反馈是mLeScanCallback
    override fun scanLeDevice(enable: Boolean,mbluetoothLeScanner: BluetoothLeScanner?,
                             mLeScanCallback: ScanCallback) {

        if (mbluetoothLeScanner != null){
            when(enable){
                true -> {
                    val mHandler :Handler? = Handler()
                    mHandler?.postDelayed( {
                        mbluetoothLeScanner!!.stopScan(mLeScanCallback)},SCAN_PERIOD)

                    mbluetoothLeScanner!!.startScan(mLeScanCallback)
                }
                false ->{
                    mbluetoothLeScanner!!.stopScan(mLeScanCallback)
                }
            }
        }

    }



}