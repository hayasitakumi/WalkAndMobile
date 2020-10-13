package com.matuilab.walkandmobile;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import androidx.room.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpResponsAsync extends AsyncTask<String, Void, String> {
    private Activity mActivity;

    public HttpResponsAsync(Activity activity) {
        mActivity = activity;
    }



    @Override
    protected String doInBackground(String... params) {

        ////// ローカルDBへの登録状況を確認 ------ 2020/02/11
        // DBへ接続
        db_tenji db = Room.databaseBuilder(mActivity,db_tenji.class,"tenji").build();
        // 案内文問合せ
        String[] message;
        message = db.daoTenji().getMessage(
                Integer.parseInt(params[1]), Integer.parseInt(params[2]), "normal"
        );
        // DBに案内文があるか確認
        if(0 < message.length) {    //クラッシュ防止
            if (message[0] != null) {
                // あれば終了
                Log.d("java_debug", "CODE : "+params[1] +"\tANGLE : "+params[2] +"\n"+message[0]);
                return message[0];
            }
        }


        ////// サーバから案内文取得
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null)
                sb.append(line);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            connection.disconnect();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String result) {

        ((TextView)mActivity.findViewById(R.id.text_view_Annai)).setText(result);
        // doInBackground後処理
    }

}
