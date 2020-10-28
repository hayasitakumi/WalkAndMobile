package com.matuilab.walkandmobile

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.matuilab.walkandmobile.http.HttpGetAudio
import com.matuilab.walkandmobile.http.HttpGetAudio.Companion.mediaPlayer
import com.matuilab.walkandmobile.http.HttpGetJson
import com.matuilab.walkandmobile.http.HttpResponsAsync
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.*


@Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
class MainActivity : AppCompatActivity(), CvCameraViewListener {
    companion object {
        lateinit var Ret: IntArray
        var Code = 0
        var Angle = 0
        var mean0 = 0

        var CodeSab = 0
        var AngleSab = 5

        var audioTask: HttpGetAudio? = null

        init {
            System.loadLibrary("native-lib")
        }
    }

    var mHandler: Handler? = null
    private var saveAppDir: String? = null

    private var lang: String? = null //言語設定（ja , en） --- 2020/03/06
    private lateinit var code_lang: Array<String> //対応している言語コードの配列(onCreate()にてvalues/strings.xmlから読み込み) --- 2020/03/06

    var serverConnection: ServerConnection = ServerConnection() // サーバ接続用（ここでは主にURL取得に使用）

    // 言語コードからcode_langの添え字を返す、端末の言語コードが対応しているか調べることも可能 --- 2020/03/06
    private fun indexOfLanguage(l: String?): Int {
        // 言語コードの配列にて検索
        return listOf(code_lang).indexOf(l)
    }

    // URLとDB用にアンダーバーを付けて返す --- 2020/03/06
    private fun addressLanguage(l: String): String? {
        // DBの仕様上、日本語を基準とし、それ以外は名称の後ろに付ける
        // 日本語用 : テーブルblockdata    英語用 : テーブルblockdata_en
        return if (l == "ja") {
            "" //日本語にはアンダーバーなし
        } else {
            "_$l"
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> main_cameraview!!.enableView()
                else -> super.onManagerConnected(status)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mHandler = Handler()

        ////////// 変数の定義 --- 2020/03/06
        code_lang = resources.getStringArray(R.array.code_language)

        ////////// 言語設定取得（未設定の場合は端末の設定値を使用） --- 2020/03/06
        // 環境設定 : https://developer.android.com/training/data-storage/shared-preferences?hl=ja
        // 端末言語 :  https://qiita.com/BlackCat/items/c223bf48c2dbfada0d42
        lang = Locale.getDefault().language   //端末の設定言語を取得
        if (indexOfLanguage(lang) <= 0) {
            // 対応リストに無ければ英語を使用（日本語、英語でもなければ英語を設定）
            lang = "en"
        }
        Log.d("java_debug", "Use Language : $lang")


        saveAppDir = filesDir.absolutePath
        Log.d("java_debug", "Use Save Dir : $saveAppDir")

        //事前ダウンロード
        val getJson = HttpGetJson(this)

        AlertDialog.Builder(this)
                .setTitle("Download in Advance")
                .setMessage(getString(R.string.download_in_advance_message))
                .setPositiveButton(getString(R.string.download_in_advance_pbutton)) { _, _ -> getJson.execute(saveAppDir, addressLanguage(lang!!)) }
                .setNeutralButton(getString(R.string.download_in_advance_nebutton)) { _, _ -> //第3引数は空欄以外なんでも良い、何か入っていれば音声は取得しない
                    getJson.execute(saveAppDir, addressLanguage(lang!!), "FALSE")
                }
                .setNegativeButton(getString(R.string.download_in_advance_nbutton), null)
                .show()

        main_cameraview.setCvCameraViewListener(this)
        audioTask = HttpGetAudio()
    }

    //ここでインプットを行列に変換している、returnしたものが表示される
    override fun onCameraFrame(inputFrame: Mat): Mat {

        //10まで予約しているが全て使っているわけでない
        Ret = IntArray(10)

        //Cのポインタを入れる？
        val addr = inputFrame.nativeObjAddr
        Code = 0
        Angle = -1
        mean0 = 0
        recog(addr, Ret)
        Code = Ret[1]
        Angle = Ret[2]
        mean0 = Ret[3]

        if ((Ret[0] >= 1) && (Code > 0) && (Angle >= 0)) {
            if (Code != CodeSab || Angle != AngleSab) {
                // 案内文取得
                val url: String = serverConnection.getMessageUrl(Code, Angle, "normal", lang)
                val task = HttpResponsAsync(this)
                task.execute(url, java.lang.String.valueOf(Code), java.lang.String.valueOf(Angle), addressLanguage(lang!!)) //引数追加 ------ 2020/02/11
            }

            if (!mediaPlayer.isPlaying) {
                // URL作成
                val audioFile = String.format("wm%05d_%d.mp3", Code, Angle)
                val audioUrl: String = serverConnection.getVoiceUrl(addressLanguage(lang!!)).toString() + "/" + audioFile
                // 保存先パスの作成
                val savePath = saveAppDir + "/message" + addressLanguage(lang!!) + "/" + audioFile // アプリ専用ディレクトリ/message_en/wm00129_3.mp3
                // 音声取得再生タスクの実行
                audioTask = HttpGetAudio()
                audioTask!!.execute(audioUrl, savePath) //引数は【音声ファイルのURL】と【音声ファイルの絶対パス】
            }

            // 取得コードをCodeSabに入れ、同じコードを取得し続けても通信をしないようにする  ------ 2020/02/01
            CodeSab = Code
            AngleSab = Angle
        }

        mHandler!!.post(Runnable
        //run()の中の処理はメインスレッドで動作されます。
        {
            if (Code == 0) {
                main_code.text = "0"
                main_angle.text = "-1"
            } else {
                main_code.text = Code.toString()
                main_angle.text = Angle.toString()
            }
        })
        return inputFrame
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}
    override fun onResume() {
        super.onResume()
        // 非同期でライブラリの読み込み/初期化を行う
        if (!OpenCVLoader.initDebug()) {
            //Log.d("onResume", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            //Log.d("onResume", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (main_cameraview != null) main_cameraview!!.disableView()
    }

    public override fun onRestart() {
        super.onRestart()
    }

    override fun onStop() {
        super.onStop()
        Code = 0
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //ここでnative-libの外身を定義する
    external fun recog(imageAddr: Long, sample: IntArray?)
}