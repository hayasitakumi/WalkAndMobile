package com.matuilab.walkandmobile.http

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.room.Room
import com.matuilab.walkandmobile.R
import com.matuilab.walkandmobile.data.AppDatabase
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpResponsAsync(private val mActivity: Activity) : AsyncTask<String?, Void?, String?>() {
    protected override fun doInBackground(vararg params: String?): String? {

        ////// ローカルDBへの登録状況を確認 ------ 2020/02/11
        // DBへ接続
        val db = Room.databaseBuilder(mActivity, AppDatabase::class.java, "tenji").build()
        // 案内文問合せ
        val message: Array<String?>?
        message = db.tenjiDao().getMessage(params[1]!!.toInt(), params[2]!!.toInt(), "normal"
        )
        // DBに案内文があるか確認
        if (0 < message!!.size) {    //クラッシュ防止
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
        (mActivity.findViewById<View>(R.id.main_info) as TextView).text = result
        // doInBackground後処理
    }
}