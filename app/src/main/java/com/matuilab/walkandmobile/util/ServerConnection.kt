package com.matuilab.walkandmobile.util

import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class ServerConnection {
    /**サーバURLのみを取得する
     */
    /* 以下の接続したい機能を選ぶだけで接続確立
    * */
    /** サーバURLをここで管理
     * 各種サーバURLや、
     * サーバ接続に使う HttpURLConnection を返す
     *
     * サーバURLの仕様変更の際は、
     * 本クラスの各種メソッド内のアドレスを更新してください。
     * （サーバドメインの変更 --> メンバ変数server_url）
     * （サブシステム(ページ)アドレスの変更 --> 該当メソッド内）
     *
     * --------------------
     * メソッド一覧
     * String getServerUrl()
     * String getMessageUrl(int code, int angle, String category, String lang)
     * String getVoiceUrl(String _lang)
     * HttpURLConnection getMessage(int code, int angle, String category, String lang)
     * HttpURLConnection db2json(String tableName)
     * HttpURLConnection db2json_lang(String tableName, String _lang)
     * HttpURLConnection serverConnection(String page)
     * HttpURLConnection openConnection(String url)
     *
     * --------------------
     * 使用方法
     * try{
     * // 接続確立【基本】
     * ServerConnection serverConnection = new ServerConnection();  //インスタンス化は処理負荷となるためここで生成
     * HttpURLConnection connection = serverConnection.getMessage(code,angle,"normal");
     * // レスポンスコード取得【HttpURLConnectionのメソッド】
     * int statusCode = connection.getResponseCode();
     * // InputStream取得【HttpURLConnectionのメソッド】
     * InputStream input = connection.getInputStream();
     * }catch(MalformedURLException e){
     * // URLに不備があったときの例外
     * }catch(IOException e){
     * // 接続できなかったときの例外
     * }finally{
     * // 後始末
     * connection.disconnect();
     * }
     */
    /////////////////////////////////////////////
    ////////// サーバURL変更の際はここを更新する
    //////////  末尾に"/スラッシュ"を付ける
    /////////////////////////////////////////////
//    val serverUrl = "http://ec2-3-136-168-45.us-east-2.compute.amazonaws.com/tenji/"
    val serverUrl = "http://18.224.144.136/tenji/"
    fun getMessageUrl(code: Int, angle: Int, category: String, lang: String?): String {
        /**案内情報取得(get_message)のURLのみを取得する
         * 引数は【言語コード】
         */
        return serverUrl + "get_message.py?code=" + code + "&angle=" + angle + "&messagecategory=" + category + "&language=" + lang
    }

    fun getVoiceUrl(_lang: String?): String {
        /**案内音声があるURLのみ取得する（ファイル名は含まない）
         * 引数は【アンダーバー付き言語コード】
         */
        return serverUrl + "message" + _lang + "/"
    }

    @Throws(MalformedURLException::class, IOException::class)
    fun getMessage(code: Int, angle: Int, category: String, lang: String): HttpURLConnection {
        /** 案内情報取得(get_message)へ接続する
         * 引数は【言語コード】
         */
        return URL(
                serverUrl + "get_message.py?code=" + code + "&angle=" + angle + "&messagecategory=" + category + "&language=" + lang)
                .openConnection() as HttpURLConnection
    }

    @Throws(MalformedURLException::class, IOException::class)
    fun db2json(tableName: String): HttpURLConnection {
        /** データ取得(db2json)へ接続する
         */
        return URL(serverUrl + "get_db2json.py?data=" + tableName)
                .openConnection() as HttpURLConnection
    }

    @Throws(MalformedURLException::class, IOException::class)
    fun db2json_lang(tableName: String, _lang: String): HttpURLConnection {
        /**データ取得(db2json)へ接続する
         * 多言語のblockmessage用に、テーブル名の末尾にアンダーバー付き言語コードを付与する
         * 引数は【アンダーバー付き言語コード】
         */
        return URL(serverUrl + "get_db2json.py?data=" + tableName + _lang)
                .openConnection() as HttpURLConnection
    }

    @Throws(MalformedURLException::class, IOException::class)
    fun serverConnection(page: String): HttpURLConnection {
        /**サーバの任意のページへ接続
         */
        return URL(serverUrl + page).openConnection() as HttpURLConnection
    }

    @Throws(MalformedURLException::class, IOException::class)
    fun openConnection(url: String?): HttpURLConnection {
        /**任意のURLへ接続
         */
        return URL(url).openConnection() as HttpURLConnection
    }
}