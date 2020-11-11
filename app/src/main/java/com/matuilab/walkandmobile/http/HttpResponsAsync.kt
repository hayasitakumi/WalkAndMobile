package com.matuilab.walkandmobile.http
// DB移行に関して
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Room
import com.matuilab.walkandmobile.R
import com.matuilab.walkandmobile.data.AppDatabase
import com.matuilab.walkandmobile.data.AppDatabase.Companion.MIGRATION_1_2
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.InvocationTargetException
import java.net.HttpURLConnection
import java.net.URL

class HttpResponsAsync(private val mActivity: Activity) : AsyncTask<String?, Void?, String?>() {
    protected override fun doInBackground(vararg params: String?): String? {
        /* 案内文取得
         * 引数：execute( サーバURL , コード , アングル , アンダーバー付き言語コード )
         * */

        ////////////////////
        ////////// ローカルDBへの登録状況を確認 ------ 2020/02/11 - 2020/03/06(変更)
        // 言語コード取得
//        val _lang: String
//        _lang = if (4 <= params.size) ({
//            params[3]
//        })!!.toString() else {
//            ""
//        }

        val _lang: String = if (4 <= params.size) {
            params[3]!!
        } else {
            ""
        }

        // DBへ接続
        val db: AppDatabase = Room.databaseBuilder<AppDatabase>(mActivity, AppDatabase::class.java, "tenji")
                .addMigrations(MIGRATION_1_2)
                .build()

        // 案内文問合せ
        /* リフレクションを使用して動的にメソッドを呼び出ししようしたが断念（HttpGetJson内のdeleteAllblockmessage()引数無しにてエラー）
           シンプルな説明 : https://m-shige1979.hatenablog.com/entry/2017/02/08/080000
           もう少し詳しく : https://www.atmarkit.co.jp/ait/articles/0512/16/news110.html
           テーブル名やDB操作のメソッド名の末尾にアンダーバー付き言語コードを付けているため多言語対応しやすい
　 　          Method method = db.daoTenji().getClass().getMethod("getMessage" + _lang, Integer.class, Integer.class, String.class);
 　           message = (String[]) method.invoke(db, Integer.parseInt(params[1]), Integer.parseInt(params[2]), "normal");
        * */
        var message: Array<String?>? = null //DB問合せ結果
        try {
            // 使用言語によって分岐（使う登録するテーブルが異なる為）

            message = when (_lang) {
                "" -> {
                    ////////// 日本語
                    db.daoTenji().getMessage(Integer.parseInt(params[1]!!), Integer.parseInt(params[2]!!), "normal")
                }
                "_en" -> {
                    ////////// 英語
                    db.daoTenji().getMessage_en(Integer.parseInt(params[1]!!), Integer.parseInt(params[2]!!), "normal")
                }
                else -> {
                    // 未知の言語
                    throw Exception("Selected unknown language code : $_lang");
                }
            }


            // DBに案内文があるか確認
            if (message!!.isNotEmpty()) {    //クラッシュ防止
                Log.d("debug_message", "message is not null")
                return if (null != message[0]) {
                    Log.d("debug_message", "message is not null")
                    // あれば終了
                    Log.d("java_debug", """LocalDB 	CODE : ${params[1]}	ANGLE : ${params[2]}${message[0]}""")
                    message[0]
                } else {
                    Log.d("debug_message", "message is empty")
                    // ローカルDBに登録なしの場合
                    // 登録がない場合にテキスト表示を維持するには以下を使用する
                    null
                }
            }
        } catch (e: NoSuchMethodException) {
            Log.e("java_error", "NoSuchMethodException : notFound DB query method.(getMessage$_lang)")
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            Log.e("java_error", "IllegalAccessException : Failed use DB query method.(getMessage$_lang)")
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            Log.e("java_error", "InvocationTargetException : Don't use DB query method.(getMessage$_lang)")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("java_error", "Get Guidance Info - " + e.message)
            e.printStackTrace()
        }

        ////////////////////
        ////////// サーバから案内文取得
        var connection: HttpURLConnection? = null
        val sb = StringBuilder()
        try {

            val url = URL(params[0])
            Log.d("debug_message", "$url")
            connection = url.openConnection() as HttpURLConnection
            val `is` = connection.inputStream
            val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var line: String? = ""
            while (reader.readLine().also { line = it } != null) sb.append(line)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return sb.toString()
    }

    override fun onPostExecute(result: String?) {

        // doInBackground後処理
        if (result != null) {
            if (result.take(4) == "http") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
                startActivity(mActivity, intent, null)
            }
            // 表示内容が存在する場合（案内情報を取得できた、nullでない）
            (mActivity.findViewById<View>(R.id.main_info) as TextView).text = result
        }
    }
}