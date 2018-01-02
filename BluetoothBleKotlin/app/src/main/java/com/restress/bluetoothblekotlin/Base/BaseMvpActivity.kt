package com.restress.bluetoothblekotlin

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

/**
 * Created by win10 on 2017/12/17.
 */
abstract class BaseMvpActivity <in V: BaseMvpView,T:BaseMvpPresenter<V>>
    :AppCompatActivity(),BaseMvpView{

    protected abstract var mPresenter: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this as V)
    }

    override fun getContext(): Context = this

    override fun showError(error: String?) {
        Toast.makeText(this, error,Snackbar.LENGTH_LONG).show()
    }

    override fun showError(stringResId: Int) {
        Toast.makeText(this,stringResId,Snackbar.LENGTH_LONG).show()
    }

    override fun showMessage(strResId: Int) {
        Toast.makeText(this,strResId,Snackbar.LENGTH_LONG).show()
    }

    override fun showMessage(message: String) {
        Toast.makeText(this,message,Snackbar.LENGTH_LONG).show()
    }
}