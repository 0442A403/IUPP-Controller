package com.iupp.iuppcontroller

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_connection.*

class ConnectionActivity : AppCompatActivity(), SocketCallback {
    private var host: String? = null
    private var port: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        connect_button.setOnClickListener {
            Log.i("GKGB", "Trying to connect")
            connect(host_editText.text.toString(),
                    port_editText.text.toString())
        }
    }

    override fun callback(code: SocketCode) {
        Log.i("IUPPCallback", "callback ${code.name}")
        connect_button.isClickable = true
        if (code == SocketCode.ConnectionCompleted) {
            runOnUiThread {
                startActivity(
                        Intent(this, ControllerActivity::class.java)
                                .putExtra("host", host!!)
                                .putExtra("port", port!!)
                )
            }
        }
        else if (code == SocketCode.ConnectionError
                || code == SocketCode.TimeoutError
                || code == SocketCode.RuntimeConnectionError) {
            Log.i("IUPPError", code.name)
            runOnUiThread {
                Snackbar.make(connectionLayout,
                        "Не удалось подключиться", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun connect(host: String, port: String) {
        if (host.isEmpty() || port.isEmpty()) {
            Snackbar.make(connectionLayout, "Не удалось подключиться", Snackbar.LENGTH_SHORT).show()
            return
        }
        connect_button.isClickable = false
        this.host = host
        this.port = port.toInt()
        Log.d("GKGB", "12321321321")
        WifiSocket(host, port.toInt(), this, true).execute()
    }
}
