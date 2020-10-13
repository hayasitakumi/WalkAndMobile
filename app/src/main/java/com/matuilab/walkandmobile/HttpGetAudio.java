package com.matuilab.walkandmobile;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpGetAudio extends AsyncTask<String, Void, String> {
    /** 音声案内を取得するためのクラス
     * execute()の引数は、execute(URL全体 , ファイル名含むパス);
     * 保存用のファイル名を作るのが面倒なので引数で渡してください。
     * */

    private Activity mActivity;

    // メンバ変数に格上げ（再生・停止を制御するために複数宣言を防止）
    static final MediaPlayer mediaPlayer = new MediaPlayer();

    public HttpGetAudio(Activity activity) {
        mActivity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        /** 案内音声を取得する
         *  もし取得済みなら中断してonPostExecute()で音声再生
         *  */
        // データ取得の参考 : https://java.keicode.com/lang/http-download-and-save.php

        // returnで使う（保存成功、ファイルありなら該当ファイル名を、失敗ならnullを返す）
        String return_file = null;

        // ファイルがあるか確認、あれば終了
        File wavfile = new File(params[1]);
        if(wavfile.exists()){
            Log.i("java_info", "Found Audio.");
            return params[1];
        }
        Log.i("java_info", "Start Get Audio.");

        // 各種接続用変数（後のfinallyで閉じるため）
        HttpURLConnection connection = null;

        try {
            // 音声ファイルのURL受け取り
            URL wavurl = new URL(params[0]);

            // URLから接続作成（おそらくこの時点でアクセスされてる）
            /* URL : https://docs.oracle.com/javase/jp/8/docs/api/java/net/URL.html
               URLConnection : https://docs.oracle.com/javase/jp/8/docs/api/java/net/URLConnection.html */
            connection = (HttpURLConnection) wavurl.openConnection();

            // 接続結果（レスポンスステータスコード）を確認
            int statusCode = connection.getResponseCode();
            if(statusCode != HttpURLConnection.HTTP_OK) {
                // エラー表示用に、ステータスコードで例外メッセージを設定
                throw new Exception("Response Status Code : " + statusCode);
            }

            // データ読取り用のストリームを取得（データ形式を意識せずにパッと受け取ってローカルに垂れ流す方針）
            /* DataInputStream : https://developer.android.com/reference/java/io/DataInputStream?hl=ja */
            DataInputStream inputStream = new DataInputStream(connection.getInputStream());

            // データの出力用のストリームを作成（データをストレージに輸送するための管）
            /* DataOutputStream : https://developer.android.com/reference/java/io/DataOutputStream?hl=ja
               ファイル出力を効率化するためにBufferedOutputStreamを挟む
               https://blog.kengo-toda.jp/entry/20100613/1276433003
               内部ストレージを使用する方法
               https://developer.android.com/training/data-storage/files/internal?hl=ja */
            DataOutputStream outputStream = new DataOutputStream(
                    new BufferedOutputStream( new FileOutputStream( params[1] ) )
            );

            // 読取りデータサイズ
            byte[] buff = new byte[1024];   //とりあえず1KBずつでデータを扱う
            int readSize = 0;

            // 読み取るデータが無くなるまで、読み続ける（buffにデータを入れて、入れたサイズをreadSizeに入れて、それが-1でなければ続行）
            while(-1 != (readSize = inputStream.read(buff))) {
                outputStream.write(buff, 0, readSize);
            }

            // 後始末
            inputStream.close();
            outputStream.close();

            // ここまで来れたら取得成功
            // 第3引数に何か含まれていたら再生しない、空欄だったら音声再生
            if(3 <= params.length) {
                return_file = null;
            }else{
                return_file = params[1];
            }

        }catch (java.net.MalformedURLException e) {
            Log.e("java_error","Malformed URL.");
            e.printStackTrace();
        }catch (IOException e) {
            Log.e("java_error","Failed open connection.");
            e.printStackTrace();
        }catch (Exception e) {
            Log.e("java_error", e.getMessage());
            e.printStackTrace();
        }finally {
            // 後始末
            Log.d("java_debug", "End of getting Audio.");
            connection.disconnect();
        }

        return return_file;

    }

    @Override
    protected void onPostExecute(String result) {
        // doInBackground後処理

        if(result==null){
            return;
        }

        /*
        音声再生
        https://akira-watson.com/android/audio-player.html
        https://developer.android.com/reference/android/media/MediaPlayer.html?hl=ja
        ファイル名の0埋めについて：https://qiita.com/kikkutonton/items/400dcfc343864028800e
         */

        // mediaPlayerをメンバ変数化、複数宣言をできないようにして、再生中なら停止させる
        //MediaPlayer mediaPlayer = new MediaPlayer();

        // 音声再生中なら動作しない  ------2020/02/02
        if(mediaPlayer.isPlaying()){
            return;
        }

        try{
            // URLからロード
            mediaPlayer.setDataSource(result);
            // 音声データのロード（プリペアとは？、要調査）
            mediaPlayer.prepare();
            // 停止後のリスナ定義（これも丸写し、要調査）
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d("java_debug", "end of audio");
                    // 以下、止めて、シークリセット、解放、変数初期化
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    //mediaPlayer.release();    //release()までやってしまうと、次にsetDataSource()ができなくなる（公式ドキュメントMediaPlayerを参照）
                }
            });
            // 再生!!
            mediaPlayer.start();

        }catch (IOException e){
            // 以下、setDataSource()使用時に必要な例外処理
            e.printStackTrace();
            Log.e("java_error","not Found.("+result+")");
        }

    }


}
