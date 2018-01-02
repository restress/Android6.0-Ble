package com.restress.bluetoothblekotlin

/**
 * Created by win10 on 2017/12/17.
 */
interface BaseMvpPresenter<in V :BaseMvpView>{
    fun attachView(view : V)

    fun detachView()
}