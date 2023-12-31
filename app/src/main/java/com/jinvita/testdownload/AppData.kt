package com.jinvita.testdownload

import android.content.Context
import android.util.Log
import android.widget.Toast

object AppData {
    var isHttpLog = true
    private var isDebug = true
    fun debug(tag: String, msg: String) {
        if (isDebug) Log.d(tag, msg)
    }

    fun error(tag: String, msg: String) {
        if (isDebug) Log.e(tag, msg)
    }

    fun error(tag: String, msg: String, ex: Exception) {
        if (isDebug) Log.e(tag, msg, ex)
    }

    private lateinit var toast: Toast
    fun showToast(context: Context, msg: String) {
        if (::toast.isInitialized) toast.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast.show()
    }
}