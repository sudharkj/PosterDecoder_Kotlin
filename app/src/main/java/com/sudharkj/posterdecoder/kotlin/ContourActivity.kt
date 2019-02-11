package com.sudharkj.posterdecoder.kotlin

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_contour.*
import kotlinx.android.synthetic.main.content_contour.*

class ContourActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contour)
        setSupportActionBar(toolbar)

        val photoUri: Uri = intent.extras.get(MediaStore.EXTRA_OUTPUT) as Uri
        contour_image.setImageURI(photoUri)

        view_contour.setOnClickListener { view ->
            contour_image.showContour()
        }
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}
