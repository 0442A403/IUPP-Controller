package com.iupp.iuppcontroller

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class WifiSocket(private val host: String,
                 private val port: Int,
                 private val callback: SocketCallback,
                 private val checkConnection: Boolean = false): AsyncTask<Void, SocketCode, Void>() {
    private val timeout = 2000
    private val socket: Socket = Socket()
    private var inStream: BufferedReader? = null
    private var outStream: OutputStream? = null
    private var actualTask: SocketCode? = null
    override fun doInBackground(vararg p0: Void?): Void? {
        try {
            Log.i("IUPPSocket", "Creating socket with params: host - |$host|, port - $port")
            socket.connect(InetSocketAddress(host, port), timeout)
            inStream = BufferedReader(InputStreamReader(socket.getInputStream()))
            outStream = socket.getOutputStream()
        } catch (e: Exception) {
            e.printStackTrace()
            callback.callback(SocketCode.ConnectionError)
        }
        callback.callback(SocketCode.ConnectionCompleted)
        if (checkConnection) {
            disconnect()
            return null
        }
        try {
            while (socket.isConnected) {
                val data =
                        if (inStream!!.ready())
                            inStream!!.readLine()
                        else
                            null
                if (data != null) {
                    Log.i("IUPPSocket", "Message: $data")
                    onProgressUpdate(SocketCode.valueOf(data))
                }
                if (actualTask != null) {
                    outStream!!.write(actualTask!!.name.toByteArray())
                    Log.i("IUPPSocket", "Command: ${actualTask!!.name}")
                    actualTask = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.callback(SocketCode.RuntimeConnectionError)
        }
        return null
    }

    override fun onProgressUpdate(vararg values: SocketCode?) {
        super.onProgressUpdate(*values)
        if (values.isEmpty())
            return
        val msg = values[0]!!
        callback.callback(msg)
    }

    fun send(socketCode: SocketCode) {
        actualTask = socketCode
    }

    private fun close() {
        socket.close()
        inStream?.close()
        outStream?.close()
    }

    fun disconnect() {
        try {
            outStream!!.write(SocketCode.Disconnection.name.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        close()
    }
}