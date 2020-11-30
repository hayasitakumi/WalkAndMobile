/**サーバから案内情報blockmessageのJSONを取得して、DBに追加する処理
 */
package com.matuilab.walkandmobile.http
// DB移行に関して
import android.app.Activity
import android.os.AsyncTask
import android.telecom.Call
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.matuilab.walkandmobile.R
import com.matuilab.walkandmobile.data.AppDatabase
import com.matuilab.walkandmobile.data.AppDatabase.Companion.MIGRATION_1_2
import com.matuilab.walkandmobile.data.model.Blockmessage
import com.matuilab.walkandmobile.util.ServerConnection
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.security.auth.callback.Callback


/*
* Roomについての解説 : https://tech.recruit-mp.co.jp/mobile/post-12311/
* */
class HttpGetJson(private val mActivity: Activity) : AsyncTask<String?, String?, String?>() {
    // サーバ接続用クラス
    private var serverConnection: ServerConnection = ServerConnection()

    // サーバURLの取得（URLの変更はServerConnection.javaにて行います）
    private val server_url: String = serverConnection.serverUrl

    // プログレスダイアログ(本当はプログレスバーが良い）
    private var dialog: AlertDialog? = null

    private var callbacktask: CallBackTask? = null
    override fun onPreExecute() {
        // 進捗表示の為にダイアログを用意
        dialog = MaterialAlertDialogBuilder(mActivity)
                .setTitle(mActivity.getString(R.string.download_in_advance_downloading_title)) //ダイアログのタイトル表示
                .setMessage("...") //ダイアログの本文
                .setCancelable(false) //勝手に閉じさせないようにする
                .setNegativeButton(mActivity.getString(R.string.download_in_advance_nbutton)) { _, _ ->
                    // キャンセルボタン押したとき
                    cancel(true)
                }
                .show() //表示実行
    }

    override fun doInBackground(vararg params: String?): String? {
        /** 音声ファイルの保存を行いたいので、引数は getFilesDir().getAbsolutePath() をください
         * 引数0 : アプリ専用のディレクトリgetFilesDir().getAbsolutePath()（音声ファイル保存先パス）
         * 引数1 : アンダーバー付き言語コード
         * 引数2 : 音声ファイル取得無効化（何かしら入力されていたらDB同期のみ、音声ファイルは取得しない）
         * メソッド内の構成
         * ◆データベース更新
         * 　・サーバ接続
         * 　・DB操作
         * ◆音声ファイル取得
         * 　・音声取得
         */
        val result: String? = null
        val _lang = params[1] //アンダーバー付き言語コード
        var connection: HttpURLConnection? = null
        val sb = StringBuilder()
//        val blockmessage: Array<Blockmessage?> //音声取得でも使うので大域宣言
        val blockmessage2: Array<Array<String?>> //DBのデータ格納用（上の変数に入れようとすると例外発動、仕方ないのでこれ）

        /////////////////////////
        //// データベース更新 ////
        /////////////////////////
        try {
            ////////// サーバ接続
            publishProgress(mActivity.getString(R.string.download_in_advance_getDB))

            // データ取得サブシステムへ接続 - 2020/03/06(追加)
            connection = serverConnection.db2json_lang("blockmessage", _lang!!)

            // 接続結果（レスポンスステータスコード）を確認
            val statusCode = connection.responseCode
            if (statusCode != HttpURLConnection.HTTP_OK) {
                // エラー表示用に、ステータスコードで例外メッセージを設定
                Log.e("java_error", "Get JSON from DB - " + connection.url)
                throw Exception("Response Status Code : $statusCode")
            }

            // データ入力の作成（HttpResponsAsyncから移植）
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            var line: String? = ""
            // 文字列として取り出し
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            inputStream.close()

            // JSONを解読（JSONArrayに文字列丸投げ）
            val jsonArray = JSONArray(sb.toString())
            var jsonObject: JSONObject

            // DB更新用にblockmessageの型を量産、後に当てはめていく
//            blockmessage = arrayOfNulls<Blockmessage>(jsonArray.length())
            blockmessage2 = Array(jsonArray.length()) { arrayOfNulls(7) } //代替案

            // 配列を一つずつ取り出し（DB登録前の下準備）
            for (i in 0 until jsonArray.length()) {
                // 添え字iの要素を取り出し
                jsonObject = jsonArray.getJSONObject(i)
                // DB用blockmessageに格納
                if (jsonObject.has("id")) {
                    //代替案  - 配列の 0～6 を id～wav に割り当て
                    blockmessage2[i][0] = jsonObject.getString("id")
                    blockmessage2[i][1] = jsonObject.getString("code")
                    blockmessage2[i][2] = jsonObject.getString("angle")
                    blockmessage2[i][3] = jsonObject.getString("messagecategory")
                    blockmessage2[i][4] = jsonObject.getString("message")
                    blockmessage2[i][5] = jsonObject.getString("reading")
                    blockmessage2[i][6] = jsonObject.getString("wav")
                    /* 本当はこの書き方をしたいけど、怒られる
                       java.lang.NullPointerException: Attempt to write to field 'int com.example.application401.entity_blockmessage.id' on a null object reference
                       Roomにおける正しいINSERTのやり方とは
                    blockmessage[i].id = jsonObject.getInt("id");
                    blockmessage[i].code = jsonObject.getInt("code");
                    blockmessage[i].angle = jsonObject.getInt("angle");
                    blockmessage[i].messagecategory = jsonObject.getString("messagecategory");
                    blockmessage[i].message = jsonObject.getString("id");
                    blockmessage[i].reading = jsonObject.getString("reading");
                    blockmessage[i].wav = jsonObject.getString("wav");
                    throw new Exception("END");
                    */
                }
            }


            ////////// DB操作 - 2020/03/06(変更)
            publishProgress(mActivity.getString(R.string.download_in_advance_syncDB))

            // DB接続
            // Contextについて : https://qiita.com/roba4coding/items/0585b8240873ec5e9c20
            val db: AppDatabase = Room.databaseBuilder<AppDatabase>(mActivity, AppDatabase::class.java, "tenji")
                    .addMigrations(MIGRATION_1_2)
                    .build()

            // 使用言語によって分岐（使う登録するテーブルが異なる為）
            when (_lang) {
                "" -> {
                    ////////// 日本語
                    // レコード全削除
                    db.daoTenji().deleteAllBlockmessage()
                    /* 【エラーにより使用不可】動的呼び出し（リフレクション）による実行、各メソッド名の後ろにアンダーバー付き言語コードを付けているため
                    Method method = db.daoTenji().getClass().getMethod("deleteAllBlockmessage"+_lang, String.class);    //動的呼び出しの準備
                    method.invoke(db, new String("%"));
                     */

                    // レコード追加
                    //db.daoTenji().insertBlockmessage(blockmessage);
                    for (i in blockmessage2.indices) {    //代替案
                        db.daoTenji().insertBlockmessageB(blockmessage2[i][0]!!.toInt(), blockmessage2[i][1]!!.toInt(), blockmessage2[i][2]!!.toInt(),
                                blockmessage2[i][3],
                                blockmessage2[i][4],
                                blockmessage2[i][5],
                                blockmessage2[i][6]
                        )
                        /* 【エラーにより使用不可】動的呼び出し（リフレクション）による実行、各メソッド名の後ろにアンダーバー付き言語コードを付けているため
                    method = db.daoTenji().getClass().getMethod( "insertBlockmessageB", Integer.class, Integer.class, Integer.class, String.class, String.class, String.class, String.class );    //動的呼び出しの準備
                    method.invoke(db,
                            Integer.parseInt(blockmessage2[i][0]),
                            Integer.parseInt(blockmessage2[i][1]),
                            Integer.parseInt(blockmessage2[i][2]),
                            blockmessage2[i][3],
                            blockmessage2[i][4],
                            blockmessage2[i][5],
                            blockmessage2[i][6]);
                    */
                        // 進捗状況表示
                        publishProgress(mActivity.getString(R.string.download_in_advance_syncDB) + (i + 1) + mActivity.getString(R.string.download_in_advance_unit))
                    } //for - INSERTの代替案
                }
                "_en" -> {
                    ////////// 英語
                    // レコード全削除
                    db.daoTenji().deleteAllBlockmessage_en()
                    // レコード追加
                    for (i in blockmessage2.indices) {    //代替案
                        db.daoTenji().insertBlockmessageB_en(blockmessage2[i][0]!!.toInt(), blockmessage2[i][1]!!.toInt(), blockmessage2[i][2]!!.toInt(),
                                blockmessage2[i][3],
                                blockmessage2[i][4],
                                blockmessage2[i][5],
                                blockmessage2[i][6]
                        )
                        // 進捗状況表示
                        publishProgress(mActivity.getString(R.string.download_in_advance_syncDB) + (i + 1) + mActivity.getString(R.string.download_in_advance_unit))
                    } //for - INSERTの代替案
                }
                else -> {
                    // 未知の言語
                    throw Exception("Selected unknown language code : $_lang")
                }
            }
        } catch (e: MalformedURLException) {
            Log.e("java_error", "Get JSON from DB - Malformed URL.")
            e.printStackTrace()
            return result
        } catch (e: IOException) {
            Log.e("java_error", "Get JSON from DB - Failed open connection.")
            e.printStackTrace()
            return result
        } catch (e: NoSuchMethodException) {
            Log.e("java_error", "Get JSON from DB　- NoSuchMethodException : notFound DB query method.(deleteAllBlockmessage$_lang)or(insertBlockmessageB$_lang)")
            e.printStackTrace()
            return result
        } catch (e: IllegalAccessException) {
            Log.e("java_error", "Get JSON from DB　- IllegalAccessException : Failed use DB query method.(deleteAllBlockmessage$_lang)or(insertBlockmessageB$_lang)")
            e.printStackTrace()
            return result
        } catch (e: InvocationTargetException) {
            Log.e("java_error", "Get JSON from DB　- InvocationTargetException : Don't use DB query method.(deleteAllBlockmessage$_lang)or(insertBlockmessageB$_lang)")
            e.printStackTrace()
            return result
        } catch (e: Exception) {
            Log.e("java_error", "Get JSON from DB - " + e.message)
            e.printStackTrace()
            return result
        } finally {
            // 後始末
            Log.d("java_debug", "End of getting JSON.")
            connection!!.disconnect()
        }


        //// 音声取得の有無
        // 第3引数に何か入力されていたらここで終了
        if (3 <= params.size) {
            return result
        }


        /////////////////////////
        //// 音声ファイル取得 ////
        /////////////////////////
        publishProgress(mActivity.getString(R.string.download_in_advance_getVoices))
        try {
            ////////// 音声取得
            // 入出力準備
            var url: URL
            var inputStream: DataInputStream
            var outputStream: DataOutputStream


            // 音声ファイル連続取得
            var cnt = 0 //プログレス表示に使いたいだけ
            for (i in blockmessage2.indices) {

                // サーバ上の登録（存在）を確認 --- 2020/03/22(変更)
                if (blockmessage2[i][6] == null || blockmessage2[i][6] == "" || blockmessage2[i][6] == "null") {
                    //DB上に音声ファイルの登録がなかった
                    Log.d("java_debug", "Skip Get Audio.\t" + blockmessage2[i][1] + " - " + blockmessage2[i][2])
                    continue
                }

                // サーバに接続
                Log.d("java_debug", "Get Audio - Start Connection :\t" + blockmessage2[i][6])
                //url = new URL(server_url + blockmessage[i].wav);  //スマートなやり方
                connection = serverConnection.serverConnection(blockmessage2[i][6]!!)
                /* ServerConnectionクラス登場以前の接続方法
                url = new URL(server_url + blockmessage2[i][6]);    //代替案
                connection = (HttpURLConnection) url.openConnection();
                 */if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    //サーバ上に音声ファイルが見つからなかった場合
                    Log.e("java_error", "Audio Get Error : " + connection.responseCode + "\t" + blockmessage2[i][6])
                    continue
                }
                Log.d("java_debug", "Get Audio - Connected :\t" + blockmessage2[i][6])


                // 保存用にファイル名を切り出し(message/wm00129_3.mp3) - 2020/03/06(変更)
                //String filename = blockmessage[i].wav;  //テーブルの定義を使ったスマートに見えるやり方（上手く使えない）
                val filename = blockmessage2[i][6] //代替案

                // ディレクトリの確認 --- 2020/03/06 - 2020/03/13(変更)
                //ファイル名を取得（splitで/の位置で切り分け）
                val dirs = filename!!.split("/".toRegex()).toTypedArray()
                //ファイル名を含まないパス(アプリのディレクトリ/message)
                val dir = params[0] + "/" + dirs[0]
                val fdir = File(dir)
                if (!fdir.exists()) {
                    fdir.mkdir()
                    Log.d("java_debug", "Make Directory : " + fdir.absolutePath)
                }

                // 入出力準備 --- 2020/03/13(変更)
                inputStream = DataInputStream(connection.inputStream)
                outputStream = DataOutputStream(
                        BufferedOutputStream(FileOutputStream(params[0] + "/" + blockmessage2[i][6]))
                )


                // データ読取り
                Log.d("java_debug", "Get Audio - Saving... :\t" + blockmessage2[i][6])
                val buff = ByteArray(1024) //とりあえず1KBずつでデータを扱う
                var readSize = 0
                // 読み取るデータが無くなるまで、読み続ける（buffにデータを入れて、入れたサイズをreadSizeに入れて、それが-1でなければ続行）
                // ここが通信速度に左右されて遅くなる原因かもしれない
                while (-1 != inputStream.read(buff).also { readSize = it }) {
                    outputStream.write(buff, 0, readSize)
                }
                Log.d("java_debug", "Get Audio - Saved :\t$filename")

                // 後始末
                inputStream.close()
                outputStream.close()
                publishProgress(mActivity.getString(R.string.download_in_advance_getVoices) + ++cnt + mActivity.getString(R.string.download_in_advance_unit))
                Log.d("java_debug", "Get Audio - Complete --------------------------\t$cnt")
            } //for - 音声ファイル連続取得
        } catch (e: MalformedURLException) {
            Log.e("java_error", "Get Audio from DB - Malformed URL.")
            e.printStackTrace()
            return result
        } catch (e: IOException) {
            Log.e("java_error", "Get Audio from DB - Failed open connection.(IOException)")
            e.printStackTrace()
            return result
        } catch (e: Exception) {
            Log.e("java_error", "Get Audio from DB - " + e.message)
            e.printStackTrace()
            return result
        } finally {
            // 後始末
            Log.d("java_debug", "End of getting Audio.")
            connection.disconnect()
        }
        return result
    }

    override fun onProgressUpdate(vararg params: String?) {
        dialog!!.setMessage(params[0])
        dialog!!.show()
    }

    override fun onPostExecute(result: String?) {
        dialog!!.dismiss()
        callbacktask?.let {
            it.CallBack(result)
        }
    }

    override fun onCancelled() {
        Log.e("java_error", "Http Get Json is Cancelled.")
        dialog!!.dismiss()
    }

    fun setOnCallBack(_cbj: CallBackTask) {
        callbacktask = _cbj
    }

    open class CallBackTask {
        open fun CallBack(result: String?) {

        }
    }
}