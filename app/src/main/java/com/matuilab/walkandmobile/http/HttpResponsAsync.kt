package com.matuilab.walkandmobile.http

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Room
import com.matuilab.walkandmobile.R
import com.matuilab.walkandmobile.data.AppDatabase
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpResponsAsync(private val mActivity: Activity) : AsyncTask<String?, Void?, String?>() {
    override fun doInBackground(vararg params: String?): String? {
        // DBへ接続
        val db = Room.databaseBuilder(mActivity, AppDatabase::class.java, "tenji").build()
        // 案内文問合せ
        val message: Array<String?>?
        message = db.tenjiDao().getMessage(params[1]!!.toInt(), params[2]!!.toInt(), "normal"
        )
        // DBに案内文があるか確認
        if (message!!.isNotEmpty()) {
            if (message[0] != null) {
                // あれば終了
                Log.d("java_debug", """CODE : ${params[1]}	ANGLE : ${params[2]}${message[0]}""")
                return message[0]
            }
        }


        ////// サーバから案内文取得
        var connection: HttpURLConnection? = null
        val sb = StringBuilder()
        try {
            val url = URL(params[0])
            connection = url.openConnection() as HttpURLConnection
            val `is` = connection.inputStream
            val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var line: String? = ""
            while (reader.readLine().also { line = it } != null) sb.append(line)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection!!.disconnect()
        }
        return sb.toString()
    }

    override fun onPostExecute(result: String?) {
        // doInBackground後処理
        if(result!!.take(4) == "http"){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))

            startActivity( mActivity, intent, null)
        }
        mActivity.findViewById<TextView>(R.id.main_info).text = result
    }
}