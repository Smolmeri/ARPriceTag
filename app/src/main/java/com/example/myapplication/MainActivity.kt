package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.price_tag.*
import okhttp3.*
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject


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


    var arrayList_details:ArrayList<Model> = ArrayList()
    private lateinit var fragment: ArFragment

    private var fitToScanImageView: ImageView? = null
    private var sneakerRenderable: ModelRenderable? = null
    private var hardHatRenderable: ModelRenderable? = null
    private var skiBootRenderable: ModelRenderable? = null
    private lateinit var productNameRenderable: ViewRenderable
    private val url = "http://users.metropolia.fi/~tuomamp/arData.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.arimage_fragment) as ArFragment
        fitToScanImageView = findViewById(R.id.fit_to_scan_img)
        showDialog()

        run(url)




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

        val inflater:LayoutInflater = LayoutInflater.from(applicationContext)
        val view = inflater.inflate(R.layout.price_tag, fragment_holder, false)

        //val textView: TextView = view?.findViewById(R.id.basicInfoCard) as TextView
        //textView.text = "arrayList_details[0].name"

        //Log.d("dbg", "oncreate ${arrayList_details[0].name}")




        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> productNameRenderable = renderable }


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
                            val textNode = TransformableNode(fragment.transformationSystem)

                            imgNode.setParent(anchorNode)
                            imgNode.setLocalRotation(
                                    Quaternion.axisAngle(Vector3(1f, 0f, 0f), -180f)
                            )
                            textNode.setParent(imgNode)
                            textNode.setLocalRotation( Quaternion.axisAngle(Vector3(1f, 0f, 0f), -180f) )
                            textNode.renderable = productNameRenderable

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



    fun run(url: String) {
        Log.d("dbg", "fetch started")
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("dbg", "$e")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("dbg", "onResponse")
                val str_response = response.body!!.string()
                //creating json object
                val json_contact:JSONObject = JSONObject(str_response)
                Log.d("dbg", "json_contact $json_contact")
                //creating json array
                Log.d("dbg", "creating json array")
                val jsonarrayInfo: JSONArray = json_contact.getJSONArray("data")
                Log.d("dbg", "json array ok")
                var i:Int = 0
                val size:Int = jsonarrayInfo.length()
                Log.d("dbg", "creating arraylist")
                arrayList_details= ArrayList()
                Log.d("dbg", "created arraylist")

                for (i in 0.. size-1) {
                    Log.d("dbg", "for loop $i")
                    val jsonObjectdetail:JSONObject=jsonarrayInfo.getJSONObject(i)
                    val model:Model= Model();
                    model.id=jsonObjectdetail.getString("id")
                    model.name=jsonObjectdetail.getString("name")
                    model.item=jsonObjectdetail.getString("item")
                    model.item=jsonObjectdetail.getString("description")
                    model.inventory=jsonObjectdetail.getString("inventory")
                    model.url=jsonObjectdetail.getString("url")
                    model.tags=jsonObjectdetail.getString("tags")

                    arrayList_details.add(model)
                    Log.d("model", "$model")
                }

                runOnUiThread {
                    //stuff that updates ui
                    //val obj_adapter : CustomAdapter
                    //obj_adapter = CustomAdapter(applicationContext,arrayList_details)
                    //listView_details.adapter=obj_adapter
                    Log.d("json", "$arrayList_details")

                    Log.d("model", arrayList_details[0].name)

                }

            }
        })
    }







}


