package io.mju.habux.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import io.mju.habux.habctl_url
import io.mju.habux.Sys

fun request_telemetry(context: Context) =
    Sys.getInstance(context).networkQueue.add(JsonObjectRequest(
        Request.Method.GET,
        habctl_url + "/telemetry",
        null,
        { response ->
            // TODO: Handle json response
        },
        { error ->
            // TODO: Handle error
        }
    ))
