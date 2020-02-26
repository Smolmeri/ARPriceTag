package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.ar_fragment.*

class MainActivity : AppCompatActivity() {

    var arFragment: ArFragment? = null
    var isFragmentLoaded = false
    val fragManager = supportFragmentManager

    private lateinit var svBarcode: SurfaceView
    private lateinit var tvBarcode: TextView

    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        qrScanner()


        /** MARK: AR-creation
        arFragment = sceneform_fragment as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            placeObject(arFragment, anchor)
        }*/
    }

    fun qrScanner() {
        svBarcode = findViewById(R.id.svBarcode)
        tvBarcode = findViewById(R.id.tvBarcode)

        detector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build()
        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<Barcode>?){
                val barcodes = detections?.detectedItems
                if (barcodes!!.size() > 0) {
                    tvBarcode.post {
                        val text = barcodes.valueAt(0).displayValue
                        tvBarcode.text = text
                        // put scanned text to toast
                        Log.d("dbg", text)
                        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT)
                            .show()
                        if (!isFragmentLoaded) {
                            Log.d("dbg", "showfragment")
                            showFragment()

                        }



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

    private fun placeObject(fragment: ArFragment, anchor: Anchor) {
        ViewRenderable.builder()
            .setView(fragment.context, R.layout.price_tag)
            .build()
            .thenAccept {
                it.isShadowCaster = false
                it.isShadowReceiver = false
                addControlsToScene(fragment, anchor, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }


    fun trackPanes() {
        Log.d("dbg", "trackpanes()")
            Log.d("dbg", "in thread")
        Thread {
                arFragment = sceneform_fragment as? ArFragment
                Log.d("dbg", "arfrag")
                arFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
                    if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                        Log.d("dbg", "horizontal")
                        return@setOnTapArPlaneListener
                    }
                    Log.d("dbg", "tapped")
                    val anchor = hitResult.createAnchor()
                    Log.d("dbg", "anchor created")
                    placeObject(arFragment!!, anchor)
                    Log.d("dbg", "object set")
                }
                }.start()
    }
    fun showFragment() {
        Log.d("dbg", "showfragment() started")
        val transaction = fragManager.beginTransaction()
        Log.d("dbg", "begintransaction() ok")
        val fragment = Fragment_AR()

        transaction.replace(R.id.fragment_holder,fragment)
        Log.d("dbg", "replace ok")
        transaction.addToBackStack(null)
        Log.d("dbg", "before commit")
        transaction.commit()
        Log.d("dbg", "commit ok")
        isFragmentLoaded = true
        trackPanes()

    }

    private fun addControlsToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
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
