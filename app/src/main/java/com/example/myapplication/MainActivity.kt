package com.example.myapplication

import android.content.Context
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.vr.VrListenerService
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_full_screen_dialog.*
import kotlinx.android.synthetic.main.price_tag.*
import kotlinx.android.synthetic.main.price_tag.view.*
import okhttp3.*
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


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

//@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {


    var arrayList_details: ArrayList<Model> = ArrayList()
    private lateinit var fragment: ArFragment

    private var testeri = "moi"
    private var fitToScanImageView: ImageView? = null
    private var sneakerRenderable: ModelRenderable? = null
    private var hardHatRenderable: ModelRenderable? = null
    private var skiBootRenderable: ModelRenderable? = null
    private lateinit var productNameRenderable: ViewRenderable
    private lateinit var productNameRenderableSkiboot: ViewRenderable
    private val url = "http://users.metropolia.fi/~tuomamp/arData.json"
    private var modelIndex = 4
    lateinit var view: View

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        showDialog()
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.arimage_fragment) as ArFragment
        fitToScanImageView = findViewById(R.id.fit_to_scan_img)






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


        //inflate()
        val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
        view = inflater.inflate(R.layout.price_tag, fragment_holder, false)

        //toimii
        //view.basicInfoCard.text = "moroo"


        val textView: TextView = view?.findViewById(R.id.basicInfoCard) as TextView
        //textView.text = "arrayList_details[0].name"

        //Log.d("dbg", "oncreate ${arrayList_details[0].name}")


        modelIndex = 0
        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> productNameRenderable = renderable }

        modelIndex = 2
        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> productNameRenderableSkiboot = renderable }


        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            frameUpdate()
        }
        //test network
        if (networkOk()) {
            doAsync {
                run(url)
                uiThread {
                    //textView2.text = "moi"
                    Log.d("dbg", "asyncUIThread modelindex $modelIndex")
                    textView123.text = arrayList_details[0].name
                    basicInfoText(modelIndex)
                }

            }
        }

    }

    private fun basicInfoText (id: Int) {
        view.basicInfoCard.text = arrayList_details[id].name
    }

    private fun inflate(i: Int) {
        if(i==0) {
            val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
            view = inflater.inflate(R.layout.price_tag, fragment_holder, false)
            Log.d("dbg", "inflate modelIndex $modelIndex")
            Log.d("dbg", "view!! ${view.id}")
            view.basicInfoCard.text = arrayList_details[i].name

            //testing textview in the corner
            textView123.text = arrayList_details[modelIndex].name
        } else if (i==1) {
            val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
            view = inflater.inflate(R.layout.price_tag_skiboot, fragment_holder, false)
            Log.d("dbg", "inflate modelIndex $modelIndex")
            Log.d("dbg", "view!! ${view.id}")
            view.basicInfoCard.text = arrayList_details[i].name

            //testing textview in the corner
            textView123.text = arrayList_details[modelIndex].name
        }
    }



    private fun frameUpdate() {
        val arFrame = fragment.arSceneView.arFrame
        if (arFrame == null || arFrame.camera.trackingState != TrackingState.TRACKING) {
            return
        }


        val updatedAugmentedImages = arFrame.getUpdatedTrackables(AugmentedImage::class.java)
        /*if (updatedAugmentedImages.size > 1){
            for (i in updatedAugmentedImages){
                updatedAugmentedImages.remove(i)
            }
        }*/

        Log.d("updatedimage", "$updatedAugmentedImages")
        updatedAugmentedImages.forEach {
            when (it.trackingState) {

                TrackingState.PAUSED -> {
                    val text = "Detected Image: " + it.name + " - need more info"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                    textView123.text = text


                }
                TrackingState.TRACKING -> {


                    Log.d("tracking", "hees")
                    var anchors = it.anchors
                    Log.d("anchors", "ANCHORS: ${it.anchors}")



                    if (anchors.isEmpty()) {
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
                        textNode.setLocalRotation(Quaternion.axisAngle(Vector3(1f, 0f, 0f), -180f))
                        if (it.name == "skiboot") {
                            basicInfoText(1)
                            textNode.renderable = productNameRenderable
                            Log.d("dbg", "SKIBOOTSKIBOOT productNameRenderableSkiboot")
                        } else if (it.name == "karhuSneaker") {
                            basicInfoText(0)
                            textNode.renderable = productNameRenderable
                        } else if (it.name == "hardhat") {
                            basicInfoText(2)
                            textNode.renderable = productNameRenderable
                        }



                        when {
                            it.name == "karhuSneaker" -> imgNode.renderable = sneakerRenderable
                            it.name == "hardhat" -> imgNode.renderable = hardHatRenderable
                            it.name == "skiboot" -> imgNode.renderable = skiBootRenderable
                        }
                        if (it.name == "karhuSneaker") {
                            Log.d("dbg", "olen karhu")
                            inflate(1)
                            Log.d("dbg", "modelIndex $modelIndex")
                        }
                        if (it.name == "skiboot") {
                            Log.d("dbg", "olen mono")
                            inflate(3)
                            Log.d("dbg", "modelIndex $modelIndex")
                        }

                    }
                }
                TrackingState.STOPPED -> {
                    val text = "Tracking stopped: " + it.name
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                    Log.d("dbg","TRACKING STOPPED ${it.name}")
                }
            }
        }
    }

    private fun showDialog() {
        val dialogFragment = FullScreenFragment()
        dialogFragment.show(supportFragmentManager, "signature")
        //AlertDialog.Builder(this, R.style.DialogTheme).show()
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
                val json_contact: JSONObject = JSONObject(str_response)
                Log.d("dbg", "json_contact $json_contact")
                //creating json array
                Log.d("dbg", "creating json array")
                val jsonarrayInfo: JSONArray = json_contact.getJSONArray("data")
                Log.d("dbg", "json array ok")
                var i: Int = 0
                val size: Int = jsonarrayInfo.length()
                Log.d("dbg", "creating arraylist")
                arrayList_details = ArrayList()
                Log.d("dbg", "created arraylist")

                for (i in 0..size - 1) {
                    Log.d("dbg", "for loop $i")
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: Model = Model();
                    model.id = jsonObjectdetail.getString("id")
                    model.name = jsonObjectdetail.getString("name")
                    model.item = jsonObjectdetail.getString("item")
                    model.item = jsonObjectdetail.getString("description")
                    model.inventory = jsonObjectdetail.getString("inventory")
                    model.url = jsonObjectdetail.getString("url")
                    model.tags = jsonObjectdetail.getString("tags")

                    arrayList_details.add(model)
                    Log.d("model", "$model")

                }

                testeri = arrayList_details[0].name
                runOnUiThread {

                    //stuff that updates ui
                    val obj_adapter: CustomAdapter
                    obj_adapter = CustomAdapter(applicationContext, arrayList_details)
                    //listView_details.adapter=obj_adapter

                    Log.d("json", " in uiThread $arrayList_details")

                    Log.d("model", arrayList_details[0].name)


                    //val myTextView2 = findViewById<TextView>(R.id.textView2)

                    //val textView: TextView = findViewById<TextView>(R.id.basicInfoCard)
                    //textView.text = "arrayList_details[0].name"


                    Log.d("json", "UIThread finished")


                }

            }
        })
    }

    private fun networkOk(): Boolean {
        val connService = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connService.activeNetworkInfo?.isConnected ?: false
    }


}



