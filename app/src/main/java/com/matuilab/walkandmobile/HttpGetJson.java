/**サーバから案内情報blockmessageのJSONを取得して、DBに追加する処理
 * */
package com.matuilab.walkandmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Room;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
* Roomについての解説 : https://tech.recruit-mp.co.jp/mobile/post-12311/
* */

public class HttpGetJson extends AsyncTask<String, String , Void> {
    private Activity mActivity;

    // 本当はURLクラスを返却するモジュールを作るのが望ましい（サーバのアドレス変更に対応しやすい）
    private String server_url = "http://ec2-3-136-168-45.us-east-2.compute.amazonaws.com/tenji/";

    // プログレスダイアログ(本当はプログレスバーが良い）
    private AlertDialog dialog;


    public HttpGetJson(Activity activity) {
        mActivity = activity;
    }

    @Override
    protected void onPreExecute(){
        // 進捗表示の為にダイアログを用意
        dialog = new AlertDialog.Builder(mActivity)
                        .setTitle("ダウンロード中")  //ダイアログのタイトル表示
                        .setMessage("...")          //ダイアログの本文
                        .setCancelable(false)       //勝手に閉じさせないようにする
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            // キャンセルボタン押したとき
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                cancel(true);
                            }
                        })
                        .show();  //表示実行
    }

    @Override
    protected Void doInBackground(String... params) {
        /** 音声ファイルの保存を行いたいので、引数は getFilesDir().getAbsolutePath() をください
         * 引数0 : 音声ファイル保存先パス
         * 引数1 : 音声ファイル取得無効化（何かしら入力されていたらDB同期のみ、音声ファイルは取得しない）
         * メソッド内の構成
         * ◆データベース更新
         * 　・サーバ接続
         * 　・DB操作
         * ◆音声ファイル取得
         * 　・音声取得
         *  */

        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        entity_blockmessage[] blockmessage;     //音声取得でも使うので大域宣言
        String blockmessage2[][];               //上の変数に入れようとすると例外発動、仕方ないのでこれ

        /////////////////////////
        //// データベース更新 ////
        /////////////////////////
        try{
            ////////// サーバ接続
            publishProgress("データベース取得...");

            // URLを指定
            URL url = new URL(server_url+"get_db2json.py?data=blockmessage");
            // 指定URLに接続
            connection = (HttpURLConnection) url.openConnection();

            // 接続結果（レスポンスステータスコード）を確認
            int statusCode = connection.getResponseCode();
            if(statusCode != HttpURLConnection.HTTP_OK) {
                // エラー表示用に、ステータスコードで例外メッセージを設定
                throw new Exception("Response Status Code : " + statusCode);
            }

            // データ入力の作成（HttpResponsAsyncから移植）
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line="";
            // 文字列として取り出し
            while ( (line = reader.readLine()) != null ) {
                sb.append(line);
            }
            inputStream.close();

            // JSONを解読（JSONArrayに文字列丸投げ）
            JSONArray jsonArray = new JSONArray(sb.toString());
            JSONObject jsonObject;

            // DB更新用にblockmessageの型を量産、後に当てはめていく
            blockmessage = new entity_blockmessage[jsonArray.length()];
            blockmessage2 = new String[jsonArray.length()][7];    //代替案

            // 配列を一つずつ取り出し（DB登録前の下準備）
            for(int i=0; i<jsonArray.length(); i++){
                // 添え字iの要素を取り出し
                jsonObject = jsonArray.getJSONObject(i);
                // DB用blockmessageに格納
                if(jsonObject.has("id")) {
                    //代替案  - 配列の 0～6 を id～wav に割り当て
                    blockmessage2[i][0] = jsonObject.getString("id");
                    blockmessage2[i][1] = jsonObject.getString("code");
                    blockmessage2[i][2] = jsonObject.getString("angle");
                    blockmessage2[i][3] = jsonObject.getString("messagecategory");
                    blockmessage2[i][4] = jsonObject.getString("message");
                    blockmessage2[i][5] = jsonObject.getString("reading");
                    blockmessage2[i][6] = jsonObject.getString("wav");
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
            publishProgress("データベース同期...");

            // DB接続
            // Contextについて : https://qiita.com/roba4coding/items/0585b8240873ec5e9c20
            db_tenji db = Room.databaseBuilder(mActivity,db_tenji.class,"tenji").build();
            // レコード全削除
            db.daoTenji().deleteAllBlockmessage();
            // レコード追加
            //db.daoTenji().insertBlockmessage(blockmessage);
            for(int i=0; i<blockmessage2.length; i++){    //代替案
                db.daoTenji().insertBlockmessageB(
                        Integer.parseInt(blockmessage2[i][0]),
                        Integer.parseInt(blockmessage2[i][1]),
                        Integer.parseInt(blockmessage2[i][2]),
                        blockmessage2[i][3],
                        blockmessage2[i][4],
                        blockmessage2[i][5],
                        blockmessage2[i][6]
                );
                publishProgress("データベース同期...  " + (i+1) + "件");
            }//INSERTの代替案

        }catch (java.net.MalformedURLException e) {
            Log.e("java_error", "Get JSON from DB - Malformed URL.");
            e.printStackTrace();
            return null;
        }catch (IOException e) {
            Log.e("java_error", "Get JSON from DB - Failed open connection.");
            e.printStackTrace();
            return null;
        }catch (Exception e){
            Log.e("java_error", "Get JSON from DB - " + e.getMessage());
            e.printStackTrace();
            return null;
        }finally {
            // 後始末
            Log.d("java_debug", "End of getting JSON.");
            connection.disconnect();
        }



        // 第2引数に何か入力されていたらここで終了
        if(2 <= params.length){
            return null;
        }



        /////////////////////////
        //// 音声ファイル取得 ////
        /////////////////////////
        publishProgress("案内音声取得...");
        try{
            ////////// 音声取得
            // 入出力準備
            URL url;
            DataInputStream inputStream;
            DataOutputStream outputStream;

            // 音声ファイル連続取得
            int cnt=0;  //プログレス表示に使いたいだけ
            for(int i=0; i<blockmessage.length; i++){
                // サーバに接続
                Log.d("java_debug", "Get Audio - Start Connection :\t"+blockmessage2[i][6]);
                //url = new URL(server_url + blockmessage[i].wav);
                url = new URL(server_url + blockmessage2[i][6]);    //代替案
                connection = (HttpURLConnection) url.openConnection();

                // サーバ上の存在を確認
                if(blockmessage2[i][6] == "null") {
                    //DB上に音声ファイルの登録がなかった
                    Log.d("java_debug", "Skip Get Audio.\t" + blockmessage2[i][1]+" - "+blockmessage2[i][2]);
                    continue;
                }
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    //サーバ上に音声ファイルが見つからなかった場合
                    Log.e("java_error", "Audio Get Error : " + connection.getResponseCode() + "\t" + blockmessage2[i][6]);
                    continue;
                    //throw new Exception("Response Status Code : " + connection.getResponseCode());
                }
                Log.d("java_debug", "Get Audio - Connected :\t"+blockmessage2[i][6]);


                // 保存用にファイル名を切り出し
                //String filename = blockmessage[i].wav.substring(7);  //「message/wm00000_0.wav」がサーバで登録されているため、/の位置から切り出し
                String filename = blockmessage2[i][6].substring(7);    //代替案

                // 入出力準備
                inputStream = new DataInputStream(connection.getInputStream());
                outputStream = new DataOutputStream(
                        new BufferedOutputStream( new FileOutputStream(params[0]+filename) )
                );


                // データ読取り
                Log.d("java_debug", "Get Audio - Saving... :\t"+blockmessage2[i][6]);
                byte[] buff = new byte[1024];   //とりあえず1KBずつでデータを扱う
                int readSize = 0;
                // 読み取るデータが無くなるまで、読み続ける（buffにデータを入れて、入れたサイズをreadSizeに入れて、それが-1でなければ続行）
                // ここが通信速度に左右されて遅くなる原因かもしれない
                while(-1 != (readSize = inputStream.read(buff))) {
                    outputStream.write(buff, 0, readSize);
                }
                Log.d("java_debug", "Get Audio - Saved :\t"+filename);

                // 後始末
                inputStream.close();
                outputStream.close();
                publishProgress("案内音声取得...  " + (++cnt) + "件");
                Log.d("java_debug", "Get Audio - Complete --------------------------\t"+cnt);
            }  //for - 音声ファイル連続取得

        }catch (java.net.MalformedURLException e) {
            Log.e("java_error", "Get Audio from DB - Malformed URL.");
            e.printStackTrace();
            return null;
        }catch (IOException e) {
            Log.e("java_error", "Get Audio from DB - Failed open connection.");
            e.printStackTrace();
            return null;
        }catch (Exception e){
            Log.e("java_error", "Get Audio from DB - " + e.getMessage());
            e.printStackTrace();
            return null;
        }finally {
            // 後始末
            Log.d("java_debug", "End of getting Audio.");
            connection.disconnect();
        }


        return null;
    }

    @Override
    protected void onProgressUpdate(String... params){
        dialog.setMessage(params[0]);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Void tmp){
        dialog.dismiss();
    }

    @Override
    protected void onCancelled(){
        Log.e("java_error","Http Get Json is Cancelled.");
        dialog.dismiss();
    }

}