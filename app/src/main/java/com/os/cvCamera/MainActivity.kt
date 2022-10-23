package com.os.cvCamera

import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import com.os.cvCamera.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK
import org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    val TAG: String = javaClass.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var mRGBA: Mat
    private lateinit var mRGBAT: Mat
    private var mCameraId: Int = CAMERA_ID_BACK

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    binding.CvCamera.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadOpenCVconfigs()

        binding.cvCameraChangeFab.setOnClickListener {
            cameraSwitch()
        }
    }

    private fun cameraSwitch() {
        mCameraId = if (mCameraId == CAMERA_ID_BACK) {
            CAMERA_ID_FRONT
        } else {
            CAMERA_ID_BACK
        }

        binding.CvCamera.disableView()
        binding.CvCamera.setCameraIndex(mCameraId)
        binding.CvCamera.enableView()

    }

    private fun loadOpenCVconfigs() {
        //OpenCV Camera
        Log.d(TAG, "CvCameraLoaded")
        binding.CvCamera.visibility = SurfaceView.VISIBLE
        binding.CvCamera.setCameraIndex(CAMERA_ID_BACK)
        binding.CvCamera.setCvCameraViewListener(this)
        binding.CvCamera.setCameraPermissionGranted()
    }

    //external fun stringFromJNI(): String

    companion object {
        // Used to load the 'cvcamera' library on application startup.
        init {
            System.loadLibrary("cvcamera")
            System.loadLibrary("opencv_java4")
        }
    }


    override fun onCameraViewStarted(width: Int, height: Int) {
        mRGBA = Mat(height, width, CvType.CV_8UC4)
        mRGBAT = Mat()

    }

    override fun onCameraViewStopped() {
        mRGBA.release()
        mRGBAT.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

        if (inputFrame != null) {
            mRGBA = inputFrame.rgba()
        }
        Core.flip(mRGBA, mRGBAT, -1)
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size())
        return mRGBA
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.CvCamera.disableView()
    }

    override fun onPause() {
        super.onPause()
        binding.CvCamera.disableView()
    }

    override fun onResume() {
        super.onResume()

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded")
            mLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
        } else {
            Log.d(TAG, "OpenCV didn't load")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        }
        //mOpenCvCameraView.enableView()
    }
}