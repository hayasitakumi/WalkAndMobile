package com.matuilab.walkandmobile

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.matuilab.walkandmobile.http.HttpGetAudio
import com.matuilab.walkandmobile.http.HttpResponsAsync
import com.matuilab.walkandmobile.util.LanguageProcessor
import com.matuilab.walkandmobile.util.ServerConnection
import kotlinx.android.synthetic.main.fragment_camera.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.*


class CameraFragment : Fragment(), CameraBridgeViewBase.CvCameraViewListener {

    companion object {
        lateinit var Ret: IntArray
        var Code = 0
        var Angle = 0
        var mean0 = 0

        var CodeSab = 0
        var AngleSab = 5


        init {
            System.loadLibrary("native-lib")
        }
    }

    var mHandler: Handler? = null
    private var saveAppDir: String? = null

    private var localLang: String? = null //言語設定（ja , en）
    lateinit var languageProcessor: LanguageProcessor

    private var audioStop: Boolean = true

    var serverConnection: ServerConnection = ServerConnection() // サーバ接続用（ここでは主にURL取得に使用）

    var mCameraView: CameraBridgeViewBase? = null

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(activity) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> camera_cameraview!!.enableView()
                else -> super.onManagerConnected(status)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** 変数の定義 */
        languageProcessor = LanguageProcessor(resources.getStringArray(R.array.code_language))
        mHandler = Handler()

        /** 言語設定取得（未設定の場合は端末の設定値を使用）*/
        localLang = Locale.getDefault().language   //端末の設定言語を取得
        if (languageProcessor.indexOfLanguage(localLang) <= 0) {
            // 対応リストに無ければ英語を使用（日本語、英語でもなければ英語を設定）
            localLang = "en"
        }

        saveAppDir = requireActivity().filesDir.absolutePath

        /** カメラビュー */
        camera_cameraview.setCvCameraViewListener(this)

        /**音声停止ボタン*/
        camera_stopaudiobutton.setOnClickListener {
            audioStop = false
            //再生終了
            HttpGetAudio.mediaPlayer.stop()
            // リセット
            HttpGetAudio.mediaPlayer.reset()
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
                showCodeAndAngle(Code, Angle)
                // 案内文取得
                val url: String = serverConnection.getMessageUrl(Code, Angle, "normal", localLang)
                val task = HttpResponsAsync(requireActivity())
                task.execute(url, java.lang.String.valueOf(Code), java.lang.String.valueOf(Angle), languageProcessor.addressLanguage(localLang)) //引数追加

                audioStop = true
            }

            if (!HttpGetAudio.mediaPlayer.isPlaying && audioStop) {
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

//        mHandler!!.post(Runnable
//        //run()の中の処理はメインスレッドで動作されます。
//        {
//            if(Code != null){
//                camera_code.text = Code.toString()
//                camera_angle.text = Angle.toString()
//            }else{
//                camera_code.text = "0"
//                camera_angle.text = "-1"
//            }

//        })
        return inputFrame
    }


    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {
    }

    override fun onResume() {
        super.onResume()

        CodeSab = 0
        AngleSab = -1
        // 非同期でライブラリの読み込み/初期化を行う
        if (!OpenCVLoader.initDebug()) {
            //Log.d("onResume", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, activity, mLoaderCallback)
        } else {
            //Log.d("onResume", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onPause() {
        super.onPause()
        if (camera_cameraview != null) camera_cameraview!!.disableView()
    }

    override fun onStop() {
        super.onStop()
        Code = 0
        AngleSab = -1
    }

    private fun showCodeAndAngle(code:Int, angle:Int){
        mHandler!!.post {
            camera_code.text = code.toString()
            camera_angle.text = angle.toString()
        }
    }

    //ここでnative-libの外身を定義する
    private external fun recog(imageAddr: Long, sample: IntArray?)
}