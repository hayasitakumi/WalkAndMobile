///////////////2019.11.21///////////////////
//String型のsampleを設定
//onCameraFrame内でsampleの中を設定、エラー
//TextViewにfinal型ｗ付けてもならない
//jniから渡された変数を表示する方法を調査する必要あり
//////////////////2019.11.26///////////////////////
//まだできない
//リストビューを使って表示できないか試す
package com.matuilab.walkandmobile

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.matuilab.walkandmobile.http.HttpGetAudio
import com.matuilab.walkandmobile.http.HttpGetJson
import com.matuilab.walkandmobile.http.HttpResponsAsync
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat

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
    private var saveAppDir: String? = null //保存先パス（外部ストレージ優先でアプリ固有のディレクトリ）
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

        saveAppDir = filesDir.absolutePath
        Log.d("java_debug", "Use Save Dir : $saveAppDir")

        //事前ダウンロード
        val getJson = HttpGetJson(this)
        AlertDialog.Builder(this)
                .setTitle("Download in Advance")
                .setMessage("案内情報と案内音声をダウンロードしますか？\nオフライン時も使用できるようになります。\n（案内音声は容量が大きいためWi-Fi環境でのダウンロードが推奨されます。）")
                .setPositiveButton("両方取得") { _, _ -> getJson.execute(saveAppDir) }
                .setNeutralButton("案内情報のみ取得") { _, _ -> //第2引数は空欄以外なんでも良い、何か入っていれば音声は取得しない
                    getJson.execute(saveAppDir, "FALSE")
                }
                .setNegativeButton("Cancel", null)
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
            if (!HttpGetAudio.mediaPlayer.isPlaying) {
                // URL作成
                val wavfile = String.format("wm%05d_%d.mp3", Code, Angle)
                val wavurl = "http://ec2-3-136-168-45.us-east-2.compute.amazonaws.com/tenji/message/$wavfile"
                // 音声取得再生タスクの実行 ------ 2020/02/02
                audioTask = HttpGetAudio()
                audioTask!!.execute(wavurl, "$saveAppDir/$wavfile") //引数は【音声ファイルのURL】と【音声ファイルの絶対パス】
            }
            if (Code != CodeSab || Angle != AngleSab) {
                // 案内文取得
                val urlSt = "http://ec2-3-136-168-45.us-east-2.compute.amazonaws.com/tenji/get_message.py?"
                val url = urlSt + "code=" + Code + "&angle=" + Angle
                val task = HttpResponsAsync(this)
                task.execute(url, Code.toString(), Angle.toString()) //引数追加 ------ 2020/02/11
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