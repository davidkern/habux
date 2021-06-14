package io.mju.habux

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class Sys constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: Sys? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Sys(context).also {
                    INSTANCE = it
                }
            }
    }

    val networkQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
}