package com.matuilab.walkandmobile

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.matuilab.walkandmobile.PrivacyPolicyActivity
import com.matuilab.walkandmobile.http.HttpGetAudio
import com.matuilab.walkandmobile.http.HttpGetAudio.Companion.mediaPlayer
import com.matuilab.walkandmobile.http.HttpGetJson
import com.matuilab.walkandmobile.http.HttpResponsAsync
import com.matuilab.walkandmobile.util.LanguageProcessor
import com.matuilab.walkandmobile.util.ServerConnection
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

        var audioStop: Boolean = true

        init {
            System.loadLibrary("native-lib")
        }
    }

    var mHandler: Handler? = null
    private var saveAppDir: String? = null

    private var localLang: String? = null //言語設定（ja , en）
    lateinit var languageProcessor: LanguageProcessor

    var serverConnection: ServerConnection = ServerConnection() // サーバ接続用（ここでは主にURL取得に使用）

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

        ////////// 変数の定義
        languageProcessor = LanguageProcessor(resources.getStringArray(R.array.code_language))


        /** 言語設定取得（未設定の場合は端末の設定値を使用）*/
        // 環境設定 : https://developer.android.com/training/data-storage/shared-preferences?hl=ja
        // 端末言語 :  https://qiita.com/BlackCat/downloadButtons/c223bf48c2dbfada0d42
        localLang = Locale.getDefault().language   //端末の設定言語を取得
        if (languageProcessor.indexOfLanguage(localLang) <= 0) {
            // 対応リストに無ければ英語を使用（日本語、英語でもなければ英語を設定）
            localLang = "en"
        }

        saveAppDir = filesDir.absolutePath

        main_cameraview.setCvCameraViewListener(this)

        /**音声停止ボタン*/
        main_stopaudiobutton.setOnClickListener {
            audioStop = false
            //再生終了
            mediaPlayer.stop()
            // リセット
            mediaPlayer.reset()
        }

        /**Toolbarの設定*/
        setSupportActionBar(main_toolbar)
//        val actionBar: android.app.ActionBar? = actionBar
//        actionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDisplayShowHomeEnabled(true)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        } ?: IllegalAccessException("Toolbar cannot be null")

        /**NavigationDrawerの設定*/
        val toggle = ActionBarDrawerToggle(
                this, main_drawerlayout, main_toolbar,
                R.string.drawer_open,
                R.string.drawer_close)
        main_drawerlayout!!.addDrawerListener(toggle)
        toggle.syncState()
        main_navigationview.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.drawer_item_download_in_advance -> {
                    if (languageProcessor.indexOfLanguage(localLang) <= 0) {
                        // 対応リストに無ければ英語を使用（日本語、英語でもなければ英語を設定）
                        localLang = "en"
                    }

                    /**事前ダウンロードのダイアログ*/
                    val getJson = HttpGetJson(this)

                    val listDownloadButtons: MutableList<String> = mutableListOf(
                            getString(R.string.download_in_advance_pbutton),
                            getString(R.string.download_in_advance_nebutton),
                            getString(R.string.download_in_advance_nbutton))
//                    listDownloadButtons.add(getString(R.string.download_in_advance_pbutton))
//                    listDownloadButtons.add(getString(R.string.download_in_advance_nebutton))
//                    listDownloadButtons.add(getString(R.string.download_in_advance_nbutton))

                    val arrayAdapterButtons = ArrayAdapter(this,
                            R.layout.dialog_dia_row, R.id.dialog_dia_list_item, listDownloadButtons)

                    val content: View = layoutInflater.inflate(R.layout.dialog_download_in_advance, null)

                    //this is the ListView that lists your downloadButtons
                    val downloadButtons: ListView = content.findViewById(R.id.dialog_dia_list)
                    downloadButtons.adapter = arrayAdapterButtons


                    val builder = MaterialAlertDialogBuilder(this).setTitle(R.string.download_in_advance_title).setView(content)
                    val dialog = builder.create()

                    dialog.show()

                    downloadButtons.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        //when you need to act on itemClick
                        when (position) {
                            0 -> {
                                dialog.dismiss()
                                getJson.execute(saveAppDir, languageProcessor.addressLanguage(localLang))
                            }
                            1 -> {
                                dialog.dismiss()
                                getJson.execute(saveAppDir, languageProcessor.addressLanguage(localLang), "FALSE")
                            }
                            else -> {
                                dialog.cancel()
                            }
                        }
                    }
                }

                R.id.drawer_item_privacy_policy
                -> {
                    val intent = Intent(this, PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            main_drawerlayout.closeDrawer(GravityCompat.START)
            true
        }
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

        if ((Ret[0] >= 0) && (Code > 0) && (Angle >= 0)) {
            if (Code != CodeSab || Angle != AngleSab) {
                // 案内文取得
                val url: String = serverConnection.getMessageUrl(Code, Angle, "normal", localLang)
                val task = HttpResponsAsync(this)
                task.execute(url, java.lang.String.valueOf(Code), java.lang.String.valueOf(Angle), languageProcessor.addressLanguage(localLang)) //引数追加

                audioStop = true
            }

            if (!mediaPlayer.isPlaying && audioStop) {
                // URL作成
                val audioFile = String.format("wm%05d_%d.mp3", Code, Angle)
                val audioUrl: String = serverConnection.getVoiceUrl(languageProcessor.addressLanguage(localLang)) + "/" + audioFile
                // 保存先パスの作成
                val savePath = saveAppDir + "/message" + languageProcessor.addressLanguage(localLang) + "/" + audioFile // アプリ専用ディレクトリ/message_en/wm00129_3.mp3
                // 音声取得再生タスクの実行
                val audioTask = HttpGetAudio()
                audioTask.execute(audioUrl, savePath) //引数は【音声ファイルのURL】と【音声ファイルの絶対パス】
            }

            // 取得コードをCodeSabに入れ、同じコードを取得し続けても通信をしないようにする
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

        CodeSab = 0
        AngleSab = -1
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

    //ここでnative-libの外身を定義する
    private external fun recog(imageAddr: Long, sample: IntArray?)
}