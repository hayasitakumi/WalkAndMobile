package com.matuilab.walkandmobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat


class CameraFragment : Fragment(), CameraBridgeViewBase.CvCameraViewListener2 {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    var mCameraView: CameraBridgeViewBase? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCameraView = view.findViewById(R.id.camera_cameraview) as CameraBridgeViewBase?
        mCameraView!!.setCvCameraViewListener(this)
    }


    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
        return inputFrame!!.rgba()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCameraView = null
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {
    }


    private class OpenCVLoaderCallback(context: Context?, private val mCameraView: CameraBridgeViewBase) : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> mCameraView.enableView()
                else -> super.onManagerConnected(status)
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, activity,
//                OpenCVLoaderCallback(activity, mCameraView!!))

        if (!OpenCVLoader.initDebug()) {
            //Log.d("onResume", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, activity, OpenCVLoaderCallback(activity, mCameraView!!))
        } else {
            //Log.d("onResume", "OpenCV library found inside package. Using it!");
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
            OpenCVLoaderCallback(activity, mCameraView!!).onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onPause() {
        super.onPause()
        mCameraView!!.disableView()
    }

    //ここでnative-libの外身を定義する
    private external fun recog(imageAddr: Long, sample: IntArray?)
}