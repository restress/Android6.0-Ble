package com.restress.bluetoothblekotlin


import android.bluetooth.BluetoothDevice
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.device_item.*


/**
 * Created by win10 on 2017/12/20.
 */
class DeviceAdapter(private var devices: MutableList<BluetoothDevice>?,
                    private val onClick: (BluetoothDevice) -> Unit)
    :RecyclerView.Adapter<DeviceAdapter.ViewHolder>(){
    private lateinit var deviceName : String

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
       return LayoutInflater.from(parent?.context)
               .inflate(R.layout.device_item,parent,false).let {
           ViewHolder(it,onClick)
       }
    }

    override fun getItemCount():Int{

        return when(devices){
            null -> 0
            else ->  devices!!.size
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (devices!=null){
            holder?.bindData(devices!!.get(position))
        }

    }

    class ViewHolder(override val containerView : View, val onClick: (BluetoothDevice) -> Unit)
        :RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindData(device: BluetoothDevice){
            with(device){
                name_tv.text= name
                address_tv.text = address
                containerView.setOnClickListener { onClick(this) }
            }
        }
    }

    fun clear(){ devices?.clear()}



}