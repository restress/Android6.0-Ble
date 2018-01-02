package com.restress.bluetoothblekotlin

/**
 * Created by win10 on 2017/12/17.
 */
open class BaseMvpPresenterImpl<V: BaseMvpView> :BaseMvpPresenter<V>{

    protected var mView: V? = null

    override fun attachView(view: V) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}