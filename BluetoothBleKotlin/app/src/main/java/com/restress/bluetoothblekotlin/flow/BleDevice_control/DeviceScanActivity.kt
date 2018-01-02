package com.restress.bluetoothblekotlin

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast

import kotlinx.android.synthetic.main.device_scan.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult


/**
 * Created by win10 on 2017/12/18.
 */
class DeviceScanActivity : BaseMvpActivity<DeviceScanContract.View,
        DeviceScanContract.Presenter>(),
        DeviceScanContract.View{

    private var  mAdapter : DeviceAdapter? = null
    private lateinit var  mBluetoothAdapter :BluetoothAdapter
    private var mbluetoothLeScanner :BluetoothLeScanner? = null
    private var bleDevices: MutableList<BluetoothDevice>? = mutableListOf()
    override var mPresenter: DeviceScanContract.Presenter = DeviceScanPresenter()

    companion object {
        private const val REQUEST_CODE_LOCATION_SETTINGS = 2
        private const val REQUEST_CODE_ACCESS_COARSE_LOCATION = 1
        private const val REQUEST_ENABLE_BT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_scan)

        initView()

        initPermission()
    }

    private fun initView(){

        device_recycler.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false)

        mAdapter = DeviceAdapter(bleDevices,{
            startActivity(ConstantDataActivity.newIntent(this,it.name,it.address))
            mbluetoothLeScanner!!.stopScan(mLeScanCallback)
        })
        device_recycler.adapter = mAdapter

    }

    private fun initPermission(){
        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= 23){//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_SHORT).show()
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_CODE_ACCESS_COARSE_LOCATION)
            }
        }

        val locationPermission = mPresenter.isLocationEnable(this)
        //TODO 不知道这样可不可以
        if (!locationPermission) setLocationService()
    }

    private fun setLocationService() {
         val locationIntent  = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS)
    }

    private val mLeScanCallback = object : ScanCallback() {
         override fun onScanResult(callbackType: Int, result: ScanResult) {
             super.onScanResult(callbackType, result)

             if ((bleDevices==null && result.device.name != null) ||
                     bleDevices!=null && !bleDevices!!.contains(result.device) && result.device.name != null) {
                 bleDevices?.add(result.device)
                 updateRecyclerView(mAdapter)
             }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)

            for (item in results){
                if ((bleDevices==null && item.device.name != null) ||
                        bleDevices!=null && !bleDevices!!.contains(item.device) && item.device.name != null) {
                    bleDevices?.add(item.device)
                    updateRecyclerView(mAdapter)
                }

            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(this@DeviceScanActivity,"搜索失败",Toast.LENGTH_LONG)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }else{
            mbluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
        }

        initView()

        mPresenter.scanLeDevice(true,mbluetoothLeScanner,mLeScanCallback)
    }

    override fun onPause() {
        super.onPause()
        mPresenter.scanLeDevice(false,mbluetoothLeScanner,mLeScanCallback)
        mAdapter?.clear()
    }

    //执行完上面的请求权限后，系统会弹出提示框让用户选择是否允许改权限。选择的结果可以在回到接口中得知：
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    //跳转到devicecontrol进行连接的返回值
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 用户没有开启蓝牙
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            if (mPresenter.isLocationEnable(this)) {
                //定位已打开的处理
                Toast.makeText(this, "定位已经打开", Toast.LENGTH_SHORT).show()

            } else {
                //定位依然没有打开的处理
                Toast.makeText(this, "定位没有打开", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_ENABLE_BT ) {
            mbluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
            return
        }else if(resultCode == Activity.RESULT_CANCELED){
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun updateRecyclerView(mAdapter: DeviceAdapter?) {
        mAdapter!!.notifyDataSetChanged()
    }
}