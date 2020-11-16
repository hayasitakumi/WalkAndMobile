package com.matuilab.walkandmobile.http

import android.media.MediaPlayer
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/* 音声案内を取得するためのクラス
   execute()の引数は、execute( URL全体 , ファイル名含むパス , 言語 )
*/
class HttpGetAudio : AsyncTask<String?, Void?, String?>() {
    override fun doInBackground(vararg params: String?): String? {
        /** 案内音声を取得する
         * もし取得済みなら中断してonPostExecute()で音声再生
         */
        // データ取得の参考 : https://java.keicode.com/lang/http-download-and-save.php

        // returnで使う（保存成功、ファイルありなら該当ファイル名を、失敗ならnullを返す）
        var returnFile: String? = null

        // ファイルがあるか確認、あれば終了
        val audioFile = File(params[1])
        if (audioFile.exists()) {
            Log.i("java_info", "Found Audio.")
            return params[1]
        }
        Log.i("java_info", "Start Get Audio.")

        // 各種接続用変数（後のfinallyで閉じるため）
        var connection: HttpURLConnection? = null
        try {
            // 音声ファイルのURL受け取り
            val audioUrl = URL(params[0])

            // URLから接続作成（おそらくこの時点でアクセスされてる）
            /* URL : https://docs.oracle.com/javase/jp/8/docs/api/java/net/URL.html
               URLConnection : https://docs.oracle.com/javase/jp/8/docs/api/java/net/URLConnection.html */
            connection = audioUrl.openConnection() as HttpURLConnection

            // 接続結果（レスポンスステータスコード）を確認
            val statusCode = connection.responseCode
            if (statusCode != HttpURLConnection.HTTP_OK) {
                // エラー表示用に、ステータスコードで例外メッセージを設定
                throw Exception("Response Status Code : $statusCode")
            }

            // データ読取り用のストリームを取得（データ形式を意識せずにパッと受け取ってローカルに垂れ流す方針）
            /* DataInputStream : https://developer.android.com/reference/java/io/DataInputStream?hl=ja */
            val inputStream = DataInputStream(connection.inputStream)

            // ディレクトリの確認
            //ファイル名を取得（splitで/の位置で切り分け）
            val dirs: List<String> = params[1]!!.split("/")
            //ファイル名を含まないパスで切り出し（パスparams[1]の頭から、ファイル名の長さを除いた部分）
            val dir = params[1]!!.substring(0, params[1]!!.length - dirs[dirs.size - 1].length)
            val fdir = File(dir)
            if (!fdir.exists()) {
                fdir.mkdir()
                Log.d("java_debug", "Make Directory : " + fdir.absolutePath)
            }

            // データの出力用のストリームを作成（データをストレージに輸送するための管）
            /* DataOutputStream : https://developer.android.com/reference/java/io/DataOutputStream?hl=ja
               ファイル出力を効率化するためにBufferedOutputStreamを挟む
               https://blog.kengo-toda.jp/entry/20100613/1276433003
               内部ストレージを使用する方法
               https://developer.android.com/training/data-storage/files/internal?hl=ja */
            val outputStream = DataOutputStream(
                    BufferedOutputStream(FileOutputStream(params[1]))
            )

            // 読取りデータサイズ
            val buff = ByteArray(1024) //とりあえず1KBずつでデータを扱う
            var readSize = 0

            // 読み取るデータが無くなるまで、読み続ける（buffにデータを入れて、入れたサイズをreadSizeに入れて、それが-1でなければ続行）
            while (-1 != inputStream.read(buff).also { readSize = it }) {
                outputStream.write(buff, 0, readSize)
            }

            // 後始末
            inputStream.close()
            outputStream.close()

            // ここまで来れたら取得成功
            // 第3引数に何か含まれていたら再生しない、空欄だったら音声再生
            returnFile = if (3 <= params.size) {
                null
            } else {
                params[1]
            }
        } catch (e: MalformedURLException) {
            Log.e("java_error", "Malformed URL.")
            e.printStackTrace()
        } catch (e: IOException) {
            Log.e("java_error", "Failed open connection.")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("java_error", e.message)
            e.printStackTrace()
        } finally {
            // 後始末
            Log.d("java_debug", "End of getting Audio.")
            connection!!.disconnect()
        }
        return returnFile
    }

    override fun onPostExecute(result: String?) {
        // doInBackground後処理
        if (result == null) {
            return
        }

        /*
        音声再生
        https://akira-watson.com/android/audio-player.html
        https://developer.android.com/reference/android/media/MediaPlayer.html?hl=ja
        ファイル名の0埋めについて：https://qiita.com/kikkutonton/items/400dcfc343864028800e
         */

        // 音声再生中なら動作しない
        if (mediaPlayer.isPlaying) {
            return
        }
        try {
            // URLからロード
            mediaPlayer.setDataSource(result)
            // 音声データのロード（プリペアとは？、要調査）
            mediaPlayer.prepare()
            // 停止後のリスナ定義（これも丸写し、要調査）
            mediaPlayer.setOnCompletionListener { mediaPlayer ->
                Log.d("java_debug", "end of audio")
                // 以下、止めて、シークリセット、解放、変数初期化
                mediaPlayer.stop()
                mediaPlayer.reset()
                //mediaPlayer.release();    //release()までやってしまうと、次にsetDataSource()ができなくなる（公式ドキュメントMediaPlayerを参照）
            }
            // 再生!!
            mediaPlayer.start()
        } catch (e: IOException) {
            // 以下、setDataSource()使用時に必要な例外処理
            e.printStackTrace()
            Log.e("java_error", "not Found.($result)")
        }
    }

    companion object {
        // メンバ変数に格上げ（再生・停止を制御するために複数宣言を防止）
        val mediaPlayer = MediaPlayer()
    }

}