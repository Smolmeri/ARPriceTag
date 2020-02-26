package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.GsonBuilder
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.*
import okio.IOException
import java.net.URL


@JsonClass(generateAdapter = true)
    data class Products(val data: List<Product>)
@JsonClass(generateAdapter = true)
    data class Product(
        val id: Int,
        val name: String,
        val item: String,
        val description: String,
        val inventory: Int,
        val url: String,
        val tags: List<String>
    )


class MainActivity : AppCompatActivity() {



    private lateinit var svBarcode: SurfaceView
    private lateinit var tvBarcode: TextView

    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    private var globalResult: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchJson()

        svBarcode = findViewById(R.id.svBarcode)
        tvBarcode = findViewById(R.id.tvBarcode)

        detector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build()
        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<Barcode>?){
                val barcodes = detections?.detectedItems
                if (barcodes!!.size() > 0) {
                    tvBarcode.post {
                        tvBarcode.text = barcodes.valueAt(0).displayValue

                    }
                }
            }
        })

        cameraSource = CameraSource.Builder(this, detector).setRequestedPreviewSize(1024, 768)
            .setRequestedFps(25f).setAutoFocusEnabled(true).build()

        svBarcode.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraSource.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                cameraSource.start(holder)
                else ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA), 123)
            }
        })
    }

//    private fun fetchJson() {
////        val url = "http://users.metropolia.fi/~tuomamp/arData.json"
////        val request = Request.Builder().url(url).build()
////
////        val client = OkHttpClient()
////        client.newCall(request).enqueue(object: Callback {
////
////            override fun onResponse(call: Call, response: Response) {
////                var body = response.body?.string()
////
////                val moshi: Moshi = Moshi.Builder().build()
////                val adapter: JsonAdapter<Products> = moshi.adapter(Products::class.java)
////                val products = adapter.fromJson(body)
////
////                Log.d("dbg", " hees ${products}")
////
////            }
////
////            override fun onFailure(call: Call, e: IOException) {
////                Log.d("dbg", "Failed to execute request")
////            }
////
////
////        })
////
////
////    }

    private fun fetchJson() {
        val url = "http://users.metropolia.fi/~tuomamp/arData.json"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                val gson = GsonBuilder().create()
                val products = gson.fromJson(body, Products::class.java)

                val data = products.data

                for (product in data) {
                    Log.d("dbg", "${product.name}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed")
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                cameraSource.start(svBarcode.holder)
            else Toast.makeText(this, "Scanner won't work without permission", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        cameraSource.stop()
        cameraSource.release()
    }



}


