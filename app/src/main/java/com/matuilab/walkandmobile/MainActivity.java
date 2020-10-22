///////////////2019.11.21///////////////////
//String型のsampleを設定
//onCameraFrame内でsampleの中を設定、エラー
//TextViewにfinal型ｗ付けてもならない
//jniから渡された変数を表示する方法を調査する必要あり

//////////////////2019.11.26///////////////////////
//まだできない
//リストビューを使って表示できないか試す


package com.matuilab.walkandmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


import static com.matuilab.walkandmobile.HttpGetAudio.mediaPlayer;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {
    static {
        System.loadLibrary("native-lib");
    }

    //public  MediaPlayer mediaPlayer;//*******2020-1-31**************

    private CameraBridgeViewBase m_cameraView;
    static int[] Ret;
    static int Code;
    static int Angle;
    static int mean0 = 0;
    //static int mean1 = 0;
    static int CodeSab = 0;
    static int AngleSab = 5;
    //static int count = 50;
    //int m = 0;
    static String annai;
    TextView textCode;
    TextView textAnnai;
    Handler mHandler;
    private String saveAppDir;              //保存先パス（外部ストレージ優先でアプリ固有のディレクトリ）

    static HttpGetAudio audioTask;      // ------ 2020/02/02

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // 読み込みが成功したらカメラプレビューを開始
                case LoaderCallbackInterface.SUCCESS:
                    m_cameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //mediaPlayer = new MediaPlayer();//**************2020-1-31*************

        setContentView(R.layout.activity_main);

        getPermissionCamera(this);
        //getPermission(this);    // 権限取得(両用) ------ 2020/02/12

        mHandler = new Handler();

        //バグがあるため画面を横向きに固定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 保存先のパスを取得 ------ 2020/02/12
        // SDカードを使用する手立て無し
        /*
        // 外部ストレージの状態を確認
        if( (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) &&
            (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
            // 外部ストレージ使用可能（【外部ストレージが存在している】 AND 【権限がある】）
            saveAppDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            Log.d("java_debug", "Nice boat.");
        }else{
            // 外部ストレージが存在しない
            saveAppDir = getFilesDir().getAbsolutePath();
        }*/
        saveAppDir = getFilesDir().getAbsolutePath();
        Log.d("java_debug", "Use Save Dir : "+saveAppDir);


        // 事前ダウンロード ------ 2020/02/11
        final HttpGetJson getJson = new HttpGetJson(this);
        new AlertDialog.Builder(this)
                .setTitle("Download in Advance")
                .setMessage("案内情報と案内音声をダウンロードしますか？\nオフライン時も使用できるようになります。\n（案内音声は容量が大きいためWi-Fi環境でのダウンロードが推奨されます。）")
                .setPositiveButton("両方取得", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        getJson.execute(saveAppDir);
                    }
                })
                .setNeutralButton("案内情報のみ取得", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //第2引数は空欄以外なんでも良い、何か入っていれば音声は取得しない
                        getJson.execute(saveAppDir, "FALSE");
                    }
                })
                .setNegativeButton("Cancel",null)
                .show();

        m_cameraView = findViewById(R.id.camera_view);
        m_cameraView.setCvCameraViewListener(this);
        m_cameraView.setMaxFrameSize(1280,720);
        //     m_cameraView.enableView();

        textCode = findViewById(R.id.text_view_Code);
        textAnnai = findViewById(R.id.text_view_Annai);

        audioTask = new HttpGetAudio(this);

    } //onCreate



    public static boolean getPermissionCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(
                activity, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    0);
            return false;

        } else {
            return true;
        }
    }

    public static void getPermission(Activity activity){
        // カメラと外部ストレージの権限取得
        if( (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
                ||
            (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        ) {
            // 権限が欲しい、requestPermissions()
            String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    0);
        }
    }

    /*  SDカードを使用する手立てがないため未使用
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        ////// requestPermissions()実行後に呼び出される関数 //////
        switch (requestCode){
            case 0:
                // requestPermissions() に許可したい権限として {カメラ、外部ストレージ} で渡してるので grantResults[] は 1 を参照したい（0はカメラ？）
                if (grantResults.length > 0
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    ////// 外部ストレージ権限許可 //////
                    // 念のため外部ストレージを使えるか確認
                    File exdf = getExternalFilesDir(null);  // 外部ストレージのFile型を取得（端末の機能的に使えないとnullが返る）
                    if( (exdf!=null) ||
                            (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                        // 外部ストレージ使用可能（外部ディレクトリがnullじゃない AND 権限がある）
                        saveAppDir = exdf.getAbsolutePath();
                    }else{
                        // 外部ストレージが存在しない
                        saveAppDir = getFilesDir().getAbsolutePath();
                    }

                } else {
                    ////// 外部ストレージ権限拒否 //////
                    // 内部ストレージのパスを使用
                    saveAppDir = getFilesDir().getAbsolutePath();
                }

                Log.d("java_debug", "(onRequestPermissionResult)Use Save Dir : "+saveAppDir);
                return;
        }
    }
    */

    //ここでインプットを行列に変換している、returnしたものが表示される
    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        Mat imageMat = inputFrame;

        Ret = new int[10];
        long addr = imageMat.getNativeObjAddr();

        Code = 0;
        Angle = -1;
        mean0 = 0;

        recog(addr,Ret);

        Code = Ret[1];
        //  Angle = Ret[2];
        Angle = Ret[2];
        mean0 = Ret[3];
        //mean1 = Ret[4];

//        if(Ret[0] >= 0) {///////////////////////イベント用動画再生////////////////
//            //鼓門
//            if (Code == 896) {
//                String videoId = "KmpLfGiAVfI";
//
//                try {
//                    Log.d("MainLog", "try");
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
//                    startActivity(intent);
//                } catch (ActivityNotFoundException ex) {
//                    Log.d("MainLog", "catch");
//                    Intent intent = new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("https://www.youtube.com/watch?v=" + videoId));
//                    startActivity(intent);
//                }
//            }
//
//            //ひがし茶屋街
//            if (Code == 784) {
//                String videoId = "xU-6auotI0w";
//
//                try {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
//                    startActivity(intent);
//                } catch (ActivityNotFoundException ex) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("https://www.youtube.com/watch?v=" + videoId));
//                    startActivity(intent);
//                }
//            }
//
//            //武家屋敷
//            if (Code == 800) {
//                String videoId = "T-_ScSVt7j8";
//
//                try {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
//                    startActivity(intent);
//                } catch (ActivityNotFoundException ex) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("https://www.youtube.com/watch?v=" + videoId));
//                    startActivity(intent);
//                }
//            }
//
//            //石川門
//            if (Code == 832) {
//                String videoId = "a7RsZ09vN0A";
//
//                try {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
//                    startActivity(intent);
//                } catch (ActivityNotFoundException ex) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW,
//                            Uri.parse("https://www.youtube.com/watch?v=" + videoId));
//                    startActivity(intent);
//                }
//            }
//        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //if (Ret[0] >= 0 &&  Code > 0 && Angle >= 0 && (Code != CodeSab || Angle != AngleSab)) {//  2個以上のコード取得はRet[0]>=1 に!!!!!!!!! 0は1個でも動作
        if (Ret[0] >= 0 &&  Code > 0 && Angle >= 0 ) {

            if(mediaPlayer.isPlaying() != true) {
                // URL作成
                String wavfile = String.format("wm%05d_%d.mp3", Code, Angle);
                String wavurl = "http://ec2-3-136-168-45.us-east-2.compute.amazonaws.com/tenji/message/" + wavfile;
                // 音声取得再生タスクの実行 ------ 2020/02/02
                audioTask = new HttpGetAudio(this);
                audioTask.execute(wavurl, saveAppDir + "/" + wavfile);    //引数は【音声ファイルのURL】と【音声ファイルの絶対パス】
                // getFilesDir()について : https://developer.android.com/training/data-storage/files/internal?hl=ja
            }

            if(Code != CodeSab || Angle != AngleSab) {
                // 案内文取得
                Log.d("MainLog", "task start");
                String urlSt = "http://ec2-3-136-168-45.us-east-2.compute.amazonaws.com/tenji/get_message.py?";
                String url = urlSt + "code=" + Code + "&angle=" + Angle;
                HttpResponsAsync task = new HttpResponsAsync(this);
                task.execute(url, String.valueOf(Code), String.valueOf(Angle));      //引数追加 ------ 2020/02/11
            }
            // 取得コードをCodeSabに入れ、同じコードを取得し続けても通信をしないようにする  ------ 2020/02/01
            CodeSab = Code;
            AngleSab = Angle;


        }//コード正常取得判断  if(Ret[0] >= 0 && Code > 0 && Angle >= 0 )

                /*
                音声再生
                https://akira-watson.com/android/audio-player.html
                https://developer.android.com/reference/android/media/MediaPlayer.html?hl=ja
                 */
        ////////////////////////////////////////////////////////////////////////////
        ///////////////////////////// 内部onnsei file 再生 main/assets
        //////   String str = String.format("wm%05d_%d.wav", Code,Angle);// file name
        //////
        //////  if(mediaPlayer.isPlaying() != true){
        //////    try {
        //////        //AssetFileDescriptor afd = getAssets().openFd("wm32832_1.wav");
        //////        AssetFileDescriptor afd = getAssets().openFd(str);
        //////               // file open error の処理必要　file がない場合
        //////        mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
        //////        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //////        afd.close();
        //////        mediaPlayer.prepare();
        //////    } catch (IOException e) {
        //////        e.printStackTrace();
        //////    }
        //////    mediaPlayer.start();
        //////    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        //////        @Override
        //////        public void onCompletion(MediaPlayer mp) {
        //////            mediaPlayer.stop();
        //////            mediaPlayer.reset();
        //////        }
        //////    });
        ////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////


        ////// HttpGetAudioにより以下未使用 ------ 2020/02/01

        // インスタンス化
        //final MediaPlayer mediaPlayer = new MediaPlayer();//*************2020-1-31
                /*
                if(mediaPlayer.isPlaying() != true) {
                    try {
                        // URLからロード
                        mediaPlayer.setDataSource(wavurl);
                        // 音量は端末の設定に依存させる設定（参考から丸写し、要調査）
                        setVolumeControlStream(AudioManager.STREAM_MUSIC);
                        //Log.d("debug", mediaPlayer.getTrackInfo().toString());
                        // 音声データのロード（プリペアとは？、要調査）
                        mediaPlayer.prepare();
                        // 停止後のリスナ定義（これも丸写し、要調査）
                        /* mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                Log.d("debug", "end of audio");
                                // 以下、止めて、シークリセット、解放、変数初期化
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                        });
                        *
                        // 再生!!
                        //mediaPlayer.start();

                    } catch (IOException e) {
                        // 以下、setDataSource()使用時に必要な例外処理
                        e.printStackTrace();
                        //Log.d("error", "not Found.(" + wavurl + ")");
                    }
                    mediaPlayer.start();

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }
                    });
                }*/
        //-------------comment out 2020/02/01

        mHandler.post(new Runnable() {
            //run()の中の処理はメインスレッドで動作されます。
            public void run() {

                if(Code == 0) {
                    textCode.setText("Code :" + 0 + "\nMeanGC= " + mean0 + "\nAngle  :  " + -1);

                }else{
                    textCode.setText("Code :" + Code +"\nMeanAGC=" + mean0 + "\nAngle  :  " + Angle);
                }
            }

        });
        return imageMat;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    protected void onResume() {
        super.onResume();

        CodeSab = 0;
        AngleSab = -1;
        // 非同期でライブラリの読み込み/初期化を行う
        if (!OpenCVLoader.initDebug()) {
            //        Log.d("onResume", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            //       Log.d("onResume", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (m_cameraView != null)
            m_cameraView.disableView();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Code=0;
        //mediaPlayer.release();// リソースの解放
        //mediaPlayer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //ここでnative-libの外身を定義する
    public native void recog(long imageAddr, int[] sample);
}

/////////////https://dev.classmethod.jp/etc/22853/
/////////////http://www.mitchy-world.jp/java/jni/jni05.htm
/////////////https://teratail.com/questions/157584