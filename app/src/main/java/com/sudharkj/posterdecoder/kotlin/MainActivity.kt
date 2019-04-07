package com.sudharkj.posterdecoder.kotlin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.BaseAdapter
import android.widget.GridView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.sudharkj.posterdecoder.kotlin.models.AsyncObject
import com.sudharkj.posterdecoder.kotlin.models.AsyncResponse
import com.sudharkj.posterdecoder.kotlin.utils.Helper
import com.sudharkj.posterdecoder.kotlin.utils.ImageAsyncTask

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.image_entry.view.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab_gallery.setOnClickListener {
            dispatchLoadPictureIntent()
        }

        fab_camera.setOnClickListener {
            dispatchTakePictureIntent()
        }
        ImageAsyncTask(FetchImages(getExternalFilesDir(Environment.DIRECTORY_PICTURES)),
            LoadImages(application, scanned_images)).execute()
    }


    class FetchImages(private val dir: File?) : AsyncObject<List<Uri>> {
        override fun process(): List<Uri> {
            val files = dir!!.listFiles()
            val fileUris = ArrayList<Uri>()
            for (file in files) {
                fileUris.add(file.toUri())
            }
            return fileUris
        }
    }

    class LoadImages(private val context: Context, private val view: GridView) : AsyncResponse<List<Uri>> {
        override fun processFinish(fileUris: List<Uri>) {
            view.adapter = ScannedImageAdapter(context, fileUris)
        }
    }

    class ScannedImageAdapter(private val context: Context, private val fileNames: List<Uri?>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var imageView = inflater.inflate(R.layout.image_entry, null)
            imageView.scanned_image.setImageURI(fileNames[position])

            imageView.setOnClickListener {
                val intent = Intent(context, ImageActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileNames[position])
                context.startActivity(intent)
            }
            return imageView
        }

        override fun getItem(position: Int): Uri? {
            return fileNames[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return fileNames.size
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val TAG = "MainActivity" /* Logger tag. */
        const val REQUEST_IMAGE_LOAD = 1 /* Any integer != -1 is forces for valid request code check */
        const val REQUEST_IMAGE_CAPTURE = 2 /* Any integer != -1 is forces for valid request code check */
    }

    private var imageUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_IMAGE_LOAD || requestCode == REQUEST_IMAGE_CAPTURE) && resultCode == RESULT_OK) {
            data?.let {
                if (requestCode == REQUEST_IMAGE_LOAD) {
                    imageUri = data.data
                }
            }
            imageUri?.let {
                Intent(this, BoundaryActivity::class.java).also {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivity(it)
                }
            }
        }
    }

    private fun dispatchLoadPictureIntent() {
        Intent(Intent.ACTION_PICK).also { loadPictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            loadPictureIntent.type = "image/*"
            startActivityForResult(loadPictureIntent, REQUEST_IMAGE_LOAD)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File = try {
                    Helper().getFile(applicationContext, R.string.image_prefix).apply {
                        imageUri = toUri()
                        galleryAddPic()
                    }
                } catch (ex: IOException) {
                    // Error occurred while creating the File and return null
                    Log.e(TAG, getString(R.string.error_image_creation), ex)
                    null
                }!!
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        getString(R.string.base_package),
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = imageUri
            sendBroadcast(mediaScanIntent)
        }
    }
}
