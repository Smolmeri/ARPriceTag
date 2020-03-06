package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.GsonBuilder
import okhttp3.*
import okio.IOException


data class Products(val data: List<Product>)
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

    private lateinit var fragment: ArFragment

    private var fitToScanImageView: ImageView? = null
    private var sneakerRenderable: ModelRenderable? = null
    private var hardHatRenderable: ModelRenderable? = null
    private var skiBootRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.arimage_fragment) as ArFragment
        fitToScanImageView = findViewById(R.id.fit_to_scan_img)
        showDialog()



        val sneaker = ModelRenderable.builder()
            .setSource(this, Uri.parse("10700_Sneaker_v201.sfb"))
            .build()
        sneaker.thenAccept { sneakerRenderable = it }

        val hardHat = ModelRenderable.builder()
                .setSource(this, Uri.parse("11687_hat_v1_L3.sfb"))
                .build()
        hardHat.thenAccept { hardHatRenderable = it }

        val skiBoot = ModelRenderable.builder()
                .setSource(this, Uri.parse("12308_boots_v2_l2.sfb"))
                .build()
        skiBoot.thenAccept { skiBootRenderable = it }



        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            frameUpdate()
        }
    }

        private fun frameUpdate() {
            val arFrame = fragment.arSceneView.arFrame
            if (arFrame == null || arFrame.camera.trackingState != TrackingState.TRACKING) {
                return
            }
            val updatedAugmentedImages = arFrame.getUpdatedTrackables(AugmentedImage::class.java)
            updatedAugmentedImages.forEach {
                Log.d("dbg", "${arFrame.camera.trackingState}")
                when(it.trackingState) {
                    TrackingState.PAUSED -> {
                        val text = "Detected Image: " + it.name + " - need more info"
                        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                    }
                    TrackingState.TRACKING -> {
                        Log.d("dbg", "hees")
                        var anchors = it.anchors
                        if( anchors.isEmpty() ) {
                            fitToScanImageView?.visibility = View.GONE
                            val pose = it.centerPose
                            val anchor = it.createAnchor(pose)
                            val anchorNode = AnchorNode(anchor)
                            anchorNode.setParent(fragment.arSceneView.scene)
                            val imgNode = TransformableNode(fragment.transformationSystem)
                            imgNode.setParent(anchorNode)
                            imgNode.setLocalRotation(
                                    Quaternion.axisAngle(Vector3(1f, 0f, 0f), -180f)
                            )
                            when {
                                it.name == "karhuSneaker" -> imgNode.renderable = sneakerRenderable
                                it.name == "hardhat" -> imgNode.renderable = hardHatRenderable
                                it.name == "skiboot" -> imgNode.renderable = skiBootRenderable
                            }
                        }
                    }
                        TrackingState.STOPPED -> {
                    val text = "Tracking stopped: " + it.name
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
                }
            }
        }
    private fun showDialog() {
        val dialogFragment = FullScreenFragment()
        dialogFragment.show(supportFragmentManager, "signature")
    }

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


}


