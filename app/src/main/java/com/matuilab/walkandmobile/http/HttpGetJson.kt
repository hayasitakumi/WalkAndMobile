/**サーバから案内情報blockmessageのJSONを取得して、DBに追加する処理
 */
package com.matuilab.walkandmobile.http

import android.app.Activity
import android.app.AlertDialog
import android.os.AsyncTask
import android.util.Log
import androidx.room.Room
import com.matuilab.walkandmobile.data.AppDatabase
import com.matuilab.walkandmobile.data.model.Blockmessage
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

//todo 毎回ダイアログが出るようになっている
//todo サーバ側のデータが更新されていなければダイアログを出さないようにする(バージョン管理)
//todo ダウンロードした場合全て入れ替えになるので毎回全件ダウンロードするようになっている
/*
* Roomについての解説 : https://tech.recruit-mp.co.jp/mobile/post-12311/
* */
class HttpGetJson(private val mActivity: Activity) : AsyncTask<String?, String?, Void?>() {

    // 本当はURLクラスを返却するモジュールを作るのが望ましい（サーバのアドレス変更に対応しやすい）
    private val server_url = "http://ec2-3-136-168-45.us-east-2.compute.amazonaws.com/tenji/"

    // プログレスダイアログ(本当はプログレスバーが良い）
    private var dialog: AlertDialog? = null
    override fun onPreExecute() {
        // 進捗表示の為にダイアログを用意
        dialog = AlertDialog.Builder(mActivity)
                .setTitle("ダウンロード中") //ダイアログのタイトル表示
                .setMessage("...") //ダイアログの本文
                .setCancelable(false) //勝手に閉じさせないようにする
                .setNegativeButton("Cancel") { dialogInterface, i ->
                    // キャンセルボタン押したとき
                    cancel(true)
                }
                .show() //表示実行
    }
    protected override fun doInBackground(vararg params: String?): Void? {
        /** 音声ファイルの保存を行いたいので、引数は getFilesDir().getAbsolutePath() をください
         * 引数0 : 音声ファイル保存先パス
         * 引数1 : 音声ファイル取得無効化（何かしら入力されていたらDB同期のみ、音声ファイルは取得しない）
         * メソッド内の構成
         * ◆データベース更新
         * 　・サーバ接続
         * 　・DB操作
         * ◆音声ファイル取得
         * 　・音声取得
         */
        var connection: HttpURLConnection? = null
        val sb = StringBuilder()
        val blockmessage: Array<Blockmessage?> //音声取得でも使うので大域宣言
        val blockmessage2: Array<Array<String?>> //上の変数に入れようとすると例外発動、仕方ないのでこれ

        /////////////////////////
        //// データベース更新 ////
        /////////////////////////
        try {
            ////////// サーバ接続
            publishProgress("データベース取得...")

            // URLを指定
            val url = URL(server_url + "get_db2json.py?data=blockmessage")
            // 指定URLに接続
            connection = url.openConnection() as HttpURLConnection

            // 接続結果（レスポンスステータスコード）を確認
            val statusCode = connection.responseCode
            if (statusCode != HttpURLConnection.HTTP_OK) {
                // エラー表示用に、ステータスコードで例外メッセージを設定
                throw Exception("Response Status Code : $statusCode")
            }

            // データ入力の作成（HttpResponsAsyncから移植）
            val inputStream = connection!!.inputStream
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
            blockmessage = arrayOfNulls(jsonArray.length())
            blockmessage2 = Array<Array<String?>>(jsonArray.length()) { arrayOfNulls<String>(7) } //代替案

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


            ////////// DB操作
            publishProgress("データベース同期...")

            // DB接続
            // Contextについて : https://qiita.com/roba4coding/items/0585b8240873ec5e9c20
            val db = Room.databaseBuilder(mActivity, AppDatabase::class.java, "tenji").build()
            // レコード全削除
            db.tenjiDao().deleteAllBlockmessage()
            // レコード追加
            //db.daoTenji().insertBlockmessage(blockmessage);
            for (i in blockmessage2.indices) {    //代替案
                db.tenjiDao().insertBlockmessageB(blockmessage2[i][0]!!.toInt(), blockmessage2[i][1]!!.toInt(), blockmessage2[i][2]!!.toInt(),
                        blockmessage2[i][3],
                        blockmessage2[i][4],
                        blockmessage2[i][5],
                        blockmessage2[i][6]
                )
                publishProgress("データベース同期...  " + (i + 1) + "件")
            } //INSERTの代替案
        } catch (e: MalformedURLException) {
            Log.e("java_error", "Get JSON from DB - Malformed URL.")
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            Log.e("java_error", "Get JSON from DB - Failed open connection.")
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            Log.e("java_error", "Get JSON from DB - " + e.message)
            e.printStackTrace()
            return null
        } finally {
            // 後始末
            Log.d("java_debug", "End of getting JSON.")
            connection!!.disconnect()
        }


        // 第2引数に何か入力されていたらここで終了
        if (2 <= params.size) {
            return null
        }


        /////////////////////////
        //// 音声ファイル取得 ////
        /////////////////////////
        publishProgress("案内音声取得...")
        try {
            ////////// 音声取得
            // 入出力準備
            var url: URL
            var inputStream: DataInputStream
            var outputStream: DataOutputStream

            // 音声ファイル連続取得
            var cnt = 0 //プログレス表示に使いたいだけ
            for (i in blockmessage.indices) {
                // サーバに接続
                Log.d("java_debug", "Get Audio - Start Connection :\t" + blockmessage2[i][6])
                //url = new URL(server_url + blockmessage[i].wav);
                url = URL(server_url + blockmessage2[i][6]) //代替案
                connection = url.openConnection() as HttpURLConnection

                // サーバ上の存在を確認
                if (blockmessage2[i][6] === "null") {
                    //DB上に音声ファイルの登録がなかった
                    Log.d("java_debug", "Skip Get Audio.\t" + blockmessage2[i][1] + " - " + blockmessage2[i][2])
                    continue
                }
                if (connection!!.responseCode != HttpURLConnection.HTTP_OK) {
                    //サーバ上に音声ファイルが見つからなかった場合
                    Log.e("java_error", "Audio Get Error : " + connection.responseCode + "\t" + blockmessage2[i][6])
                    continue
                    //throw new Exception("Response Status Code : " + connection.getResponseCode());
                }
                Log.d("java_debug", "Get Audio - Connected :\t" + blockmessage2[i][6])


                // 保存用にファイル名を切り出し
                //String filename = blockmessage[i].wav.substring(7);  //「message/wm00000_0.wav」がサーバで登録されているため、/の位置から切り出し
                val filename = blockmessage2[i][6]!!.substring(7) //代替案

                // 入出力準備
                inputStream = DataInputStream(connection.inputStream)
                outputStream = DataOutputStream(
                        BufferedOutputStream(FileOutputStream(params[0] + filename))
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
                publishProgress("案内音声取得...  " + ++cnt + "件")
                Log.d("java_debug", "Get Audio - Complete --------------------------\t$cnt")
            } //for - 音声ファイル連続取得
        } catch (e: MalformedURLException) {
            Log.e("java_error", "Get Audio from DB - Malformed URL.")
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            Log.e("java_error", "Get Audio from DB - Failed open connection.")
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            Log.e("java_error", "Get Audio from DB - " + e.message)
            e.printStackTrace()
            return null
        } finally {
            // 後始末
            Log.d("java_debug", "End of getting Audio.")
            connection.disconnect()
        }
        return null
    }

    protected override fun onProgressUpdate(vararg params: String?) {
        dialog!!.setMessage(params[0])
        dialog!!.show()
    }

    override fun onPostExecute(tmp: Void?) {
        dialog!!.dismiss()
    }

    override fun onCancelled() {
        Log.e("java_error", "Http Get Json is Cancelled.")
        dialog!!.dismiss()
    }

//    override fun doInBackground(vararg p0: String?): Void? {
//        TODO("Not yet implemented")
//    }

}