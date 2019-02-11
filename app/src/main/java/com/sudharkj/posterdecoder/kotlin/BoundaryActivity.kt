package com.sudharkj.posterdecoder.kotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.sudharkj.posterdecoder.kotlin.models.AsyncObject
import com.sudharkj.posterdecoder.kotlin.models.AsyncResponse
import com.sudharkj.posterdecoder.kotlin.utils.Helper
import com.sudharkj.posterdecoder.kotlin.utils.ImageAsyncTask
import com.sudharkj.posterdecoder.kotlin.views.CropView

import kotlinx.android.synthetic.main.activity_boundary.*
import kotlinx.android.synthetic.main.content_boundary.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.OpenCVLoader
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class BoundaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boundary)
        setSupportActionBar(toolbar)

        val photoUri: Uri = intent.extras.get(MediaStore.EXTRA_OUTPUT) as Uri
        boundary_image.setImageURI(photoUri)

        boundary_next.setOnClickListener { view ->
            ImageAsyncTask(CroppedImage(boundary_image), CroppedImageUri(this)).execute()
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private class CroppedImage(view: CropView) : AsyncObject<Uri?> {
        val view: CropView = view
        companion object {
            const val TAG: String = "CroppedImage"
        }

        fun getCroppedBitmap(): Bitmap {
            val bitmap = view.drawable.toBitmap()

            val bRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val cRatio = view.width.toFloat() / view.height.toFloat()
            val ratio =
                if (bRatio < cRatio) {
                    view.height.toFloat() / bitmap.height.toFloat()
                } else {
                    view.width.toFloat() / bitmap.width.toFloat()
                }
            val (iWidth, iHeight) =
                floor(bitmap.width * ratio).toInt() to floor(bitmap.height * ratio).toInt()
            val bitmapRect = Rect(
                ceil((view.width - iWidth) / 2f).toInt(),
                ceil((view.height - iHeight) / 2f).toInt(),
                floor((view.width + iWidth) / 2f).toInt(),
                floor((view.height + iHeight) / 2f).toInt()
            )
            val rect = view.getCropRect()
            val cropRect = Rect(
                max(bitmapRect.left, rect.left),
                max(bitmapRect.top, rect.top),
                min(bitmapRect.right, rect.right),
                min(bitmapRect.bottom, rect.bottom)
            )

            val bitmapCropRect = Rect(
                ceil((cropRect.left - bitmapRect.left) / ratio).toInt(),
                ceil((cropRect.top - bitmapRect.top) / ratio).toInt(),
                floor((cropRect.right - bitmapRect.left) / ratio).toInt(),
                floor((cropRect.bottom - bitmapRect.top) / ratio).toInt()
            )

            return Bitmap.createBitmap(bitmap, bitmapCropRect.left, bitmapCropRect.top,
                bitmapCropRect.right - bitmapCropRect.left, bitmapCropRect.bottom - bitmapCropRect.top)
        }

        fun saveBitmap(croppedBitmap: Bitmap): Uri? {
            try {
                val file = Helper().getFile(view.context, R.string.cropped_image_prefix)
                FileOutputStream(file).use { fileStream: FileOutputStream ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileStream)
                }
                Log.d(TAG, "Saved cropped bitmap to ${file.absolutePath}")
                return file.toUri()
            } catch (ex: IOException) {
                Log.e(TAG, view.context.getString(R.string.error_image_creation), ex)
            }
            return null
        }

        private val mLoaderCallback = object : BaseLoaderCallback(view.context) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    LoaderCallbackInterface.SUCCESS -> {
                        Log.i("OpenCV", "OpenCV loaded successfully")
                    }
                    else -> {
                        Log.i("OpenCV", "Loading OpenCV")
                        super.onManagerConnected(status)
                    }
                }
            }
        }

        override fun process(): Uri? {
            val croppedBitmap = getCroppedBitmap()
            Log.d(TAG, "Obtained cropped bitmap")
            if (!OpenCVLoader.initDebug()) {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, view.context, mLoaderCallback)
            } else {
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
            }
            val mat = Mat()
            Utils.bitmapToMat(croppedBitmap, mat)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.Canny(mat, mat, 50.toDouble(), 50.toDouble())
            var contours: List<MatOfPoint> = ArrayList()
            var hierarchy = Mat()
            Imgproc.findContours(mat.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
//            val contourSet = sortedSetOf(contours, object : Comparator<MatOfPoint> {
//                override fun compare(o1: MatOfPoint?, o2: MatOfPoint?): Int {
//                    val a1 = Imgproc.contourArea(o1)
//                    val a2 = Imgproc.contourArea(o2)
//                    return (a1 - a2).toInt()
//                }
//            })
            val newBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, newBitmap)
            return saveBitmap(newBitmap)
        }
    }

    private class CroppedImageUri(activity: Activity) : AsyncResponse {
        val activity: Activity = activity
        companion object {
            const val TAG: String = "CroppedImageUri"
        }

        override fun processFinish(uri: Uri?) {
            Log.d(TAG, "Processed image crop uri")
            uri?.let {
                Intent(activity, ImageActivity::class.java).also {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    activity.startActivity(it)
                }
            }
        }
    }
}
