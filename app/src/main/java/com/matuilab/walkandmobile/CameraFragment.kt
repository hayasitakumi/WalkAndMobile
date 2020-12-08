package com.matuilab.walkandmobile

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
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

//CameraBridgeViewBase.CvCameraViewListener2
class CameraFragment : Fragment() {

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

    private var mOpenCvCameraView: CameraBridgeViewBase? = null

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
//        val cameraView: JavaCameraView = view.findViewById(R.id.camera_cameraview) as JavaCameraView
//        view.camera_cameraview.setCvCameraViewListener(this)

//        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_FULLSCREEN)

//        requireActivity().setContentView(R.layout.fragment_camera)

//        mOpenCvCameraView = requireActivity().findViewById<View>(R.id.camera_cameraview) as CameraBridgeViewBase
//        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE
//        mOpenCvCameraView!!.setCvCameraViewListener(this)
        return view
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (mOpenCvCameraView != null)
//            mOpenCvCameraView!!.disableView()
//
//    }

//    override fun onStart() {
//        super.onStart()
//        camera_cameraview.enableView()
//    }

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
//        mOpenCvCameraView = requireActivity().findViewById<View>(R.id.camera_cameraview) as CameraBridgeViewBase
//
//        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE
//        // プレビューを有効にする
//        mOpenCvCameraView.setCameraPermissionGranted()
//        camera_cameraview.setCvCameraViewListener(this)
//        camera_cameraview.enableView()
//
//        mOpenCvCameraView!!.setCvCameraViewListener(this)
//        Log.d("camerawidth", camera_cameraview.width.toString())

//        val cameraView = view.findViewById(R.id.camera_cameraview);
//        camera_cameraview.setCvCameraViewListener(this)
//        val observer: ViewTreeObserver = camera_cameraview.viewTreeObserver
//        observer.addOnGlobalLayoutListener {
//            Log.d("cameraView", "camera_view:width=${camera_cameraview.width}, height=${camera_cameraview.height}")
//        }

//        camera_cameraview.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
//            override fun onCameraViewStarted(width: Int, height: Int) { }
//
//            override fun onCameraViewStopped() { }
//
//            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
//                // このメソッド内で画像処理. 今回はポジネガ反転.
//                val mat = requireNotNull(inputFrame).rgba()
//                Core.bitwise_not(mat, mat)
//                return mat
//            }
//        })

        camera_cameraview.setMaxFrameSize(500, 500)
        camera_cameraview.enableView()

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
//    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
//        //10まで予約しているが全て使っているわけでない
//        Ret = IntArray(10)
//
////        Log.d("camerawidth", inputFrame.width().toString())
////        Log.d("camerawidth", inputFrame.height().toString())
//        //Cのポインタを入れる？
//        val addr = inputFrame!!.rgba().nativeObjAddr
//        Code = 0
//        Angle = -1
//        mean0 = 0
//        recog(addr, Ret)
//        Code = Ret[1]
//        Angle = Ret[2]
//        mean0 = Ret[3]
//
//        if ((Ret[0] >= 0) && (Code > 0) && (Angle >= 0)) {
//            if (Code != CodeSab || Angle != AngleSab) {
//                showCodeAndAngle(Code, Angle)
//                // 案内文取得
//                val url: String = serverConnection.getMessageUrl(Code, Angle, "normal", localLang)
//                val task = HttpResponsAsync(requireActivity())
//                task.execute(url, java.lang.String.valueOf(Code), java.lang.String.valueOf(Angle), languageProcessor.addressLanguage(localLang)) //引数追加
//
//                audioStop = true
//            }
//
//            if (!HttpGetAudio.mediaPlayer.isPlaying && audioStop) {
//                // URL作成
//                val audioFile = String.format("wm%05d_%d.mp3", Code, Angle)
//                val audioUrl: String = serverConnection.getVoiceUrl(languageProcessor.addressLanguage(localLang)) + "/" + audioFile
//                // 保存先パスの作成
//                val savePath = saveAppDir + "/message" + languageProcessor.addressLanguage(localLang) + "/" + audioFile // アプリ専用ディレクトリ/message_en/wm00129_3.mp3
//                // 音声取得再生タスクの実行
//                val audioTask = HttpGetAudio(requireActivity())
//                audioTask.execute(audioUrl, savePath) //引数は【音声ファイルのURL】と【音声ファイルの絶対パス】
//            }
//
//            // 取得コードをCodeSabに入れ、同じコードを取得し続けても通信をしないようにする
//            CodeSab = Code
//            AngleSab = Angle
//        }
//
////        mHandler!!.post(Runnable
////        //run()の中の処理はメインスレッドで動作されます。
////        {
////            if(Code != null){
////                camera_code.text = Code.toString()
////                camera_angle.text = Angle.toString()
////            }else{
////                camera_code.text = "0"
////                camera_angle.text = "-1"
////            }
//
////        })

//        Log.d("inputFrame", "inputFrame:width=${inputFrame!!.rgba().width()}, height=${inputFrame.rgba().width()}")
//        return inputFrame.rgba()
//    }


//    override fun onCameraViewStarted(width: Int, height: Int) {
//    }
//
//    override fun onCameraViewStopped() {
//    }

    override fun onResume() {
        super.onResume()

        CodeSab = 0
        AngleSab = -1

        // 非同期でライブラリの読み込み/初期化を行う
//        if (!OpenCVLoader.initDebug()) {
//            //Log.d("onResume", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, activity, mLoaderCallback)
//        } else {
//            //Log.d("onResume", "OpenCV library found inside package. Using it!")
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
//        }
    }

    override fun onPause() {
        super.onPause()
        if (camera_cameraview != null) camera_cameraview.disableView()
//        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onStop() {
        super.onStop()
        if (camera_cameraview != null) camera_cameraview.disableView()
        Code = 0
        AngleSab = -1
    }

    private fun showCodeAndAngle(code: Int, angle: Int) {
        mHandler!!.post {
            camera_code.text = code.toString()
            camera_angle.text = angle.toString()
        }
    }

    //ここでnative-libの外身を定義する
    private external fun recog(imageAddr: Long, sample: IntArray?)
}