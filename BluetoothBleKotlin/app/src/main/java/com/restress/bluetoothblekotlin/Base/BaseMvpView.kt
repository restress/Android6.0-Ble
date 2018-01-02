package com.restress.bluetoothblekotlin

import android.content.Context
import android.support.annotation.StringRes

/**
 * Created by win10 on 2017/12/17.
 */
interface BaseMvpView {
    fun getContext():Context

    fun showError(eror: String?)

    fun showError(@StringRes stringResId: Int)

    fun showMessage(@StringRes strResId: Int)

    fun showMessage(message: String)
}