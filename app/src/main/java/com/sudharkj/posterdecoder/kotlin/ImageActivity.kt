package com.sudharkj.posterdecoder.kotlin

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.content_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        setSupportActionBar(toolbar)

        val photoUri: Uri = intent.extras.get(MediaStore.EXTRA_OUTPUT) as Uri
        scanned_image.setImageURI(photoUri)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}
