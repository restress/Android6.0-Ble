package com.restress.bluetoothblekotlin


import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.data_content.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by win10 on 2017/12/20.
 */
class ConstantDataActivity : BaseMvpActivity<ConstantDataContract.View,
        ConstantDataContract.Presenter>(),
        ConstantDataContract.View{

    override var mPresenter: ConstantDataContract.Presenter = ConstantDataPresenter()

    private var mBluetoothLeService:BluetoothLeService? = null
    private var mDeviceAddress:String? = null
    private var mConnected : Boolean = false
    private var mNotifyCharacteristic :BluetoothGattCharacteristic ? = null

    companion object {
        private val TAG = ConstantDataActivity::class.java.simpleName

        private const val NAME = "name"
        private const val ADDRESS = "address"

        //用于传递点击的蓝牙item到前台去
        fun newIntent(context: Context, name: String, address: String): Intent =
                Intent(context, ConstantDataActivity::class.java).apply {
                    putExtra(NAME, name)
                    putExtra(ADDRESS,address)
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_constant)



        cd_ble_name_tv.text = intent.getStringExtra(NAME)
        cd_ble_connect_tv.text = intent.getStringExtra(ADDRESS)
        mDeviceAddress = intent.getStringExtra(ADDRESS)

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }



    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).getService()
            if (!mBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }

            // Automatically connects to the device upon successful start-up initialization.
            try {
                mBluetoothLeService!!.connect(mDeviceAddress)
            } catch (e: Exception) {
                println(e.message)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    //重启时、开始时注册广播
    override fun onResume() {
        super.onResume()
        //注册广播
        registerReceiver(mGattUpdateReceiver, mPresenter.makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d(TAG, "Connect request result=" + result)
        }
    }

    //停止时，注销广播
    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)
    }

    //关闭activity
    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    //BroadcastReceiver：从service中传回来的参数，从BluetoothLeService中Broadcast中传回来的参数
    //若广播是service状态的改变（连接，未连接，发现，获得data）
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            when(action){
                BluetoothLeService.ACTION_GATT_CONNECTED ->{
                    mConnected = true
                    updateConnectionState(R.string.connected)
                    invalidateOptionsMenu()
                    //TODO 刚加上
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                }

                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    mConnected = false
                    updateConnectionState(R.string.disconnected)
                    invalidateOptionsMenu()
                    clearUI()
                }

                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED ->{

                    //在此处修改了，使得发现服务后直接开启获得数据
                    mNotifyCharacteristic = mBluetoothLeService!!.getBluetoothGattCharacteristic()
                    if (mNotifyCharacteristic == null || 0x10 or mNotifyCharacteristic!!.properties <= 0) {
                        return
                    }
                    mBluetoothLeService!!.setCharacteristicNotification(mNotifyCharacteristic!!, true)
                    //TODO 刚加上
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                }

                BluetoothLeService.ACTION_DATA_AVAILABLE -> {
                    //TODO在此处修改了，使得发现服务后直接开启获得数据
                    mBluetoothLeService!!.setCharacteristicNotification(mNotifyCharacteristic!!, true)
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                }

            }
        }
    }

    //显示当前连接状态
    override fun updateConnectionState(resourceId: Int) {
        runOnUiThread { connect_state_tv.setText(resourceId) }
    }

    //显示传来的数据
    override fun displayData(data: String?) {
        if (data != null) {
            /* mDataField.append(data);*/
            cd_ble_flow_tv.text = data
        }
    }

    //设备未连接时清除界面内容
    override fun clearUI() {
        cd_ble_flow_tv.setText(R.string.no_data)
    }

    //开灯按键响应函数
    fun activateBt(v: View) {
        if (mConnected == false) {
            Toast.makeText(this, "请先连接设备", Toast.LENGTH_SHORT).show()
        } else {
            mPresenter.sendMsg("BEGIN",mNotifyCharacteristic,this.mBluetoothLeService)
        }
    }

}