package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.DpToMetersViewSizer
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.price_tag.view.*
import okhttp3.*
import okio.IOException
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject


//@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    var arrayList_details: ArrayList<Model> = ArrayList()
    private lateinit var fragment: ArFragment
    private lateinit var nColor: List<Float>
    private var testeri = "moi"
    private val renderScale = 1000
    private var fitToScanImageView: ImageView? = null
    private var sneakerRenderable: ModelRenderable? = null
    private var hardHatRenderable: ModelRenderable? = null
    private var skiBootRenderable: ModelRenderable? = null
    private lateinit var productNameRenderable: ViewRenderable
    private lateinit var productNameRenderableSkiboot: ViewRenderable
    private lateinit var sneakerInfoRenderable: ViewRenderable
    private lateinit var skibootInfoRenderable: ViewRenderable
    private lateinit var hardhatInfoRenderable: ViewRenderable

    private val url = "http://users.metropolia.fi/~tuomamp/arData.json"
    var a = 0
    var b = 0
    var c = 0
    lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        showDialog()
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.arimage_fragment) as ArFragment
        fitToScanImageView = this.findViewById(R.id.fit_to_scan_img)

        showDialog()

        /** Create Models **/

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


        /** Inflate View layout **/

        val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
        view = inflater.inflate(R.layout.price_tag, fragment_holder, false)

        /** Create TextView Renderables **/

        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> productNameRenderable = renderable }

        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> sneakerInfoRenderable = renderable }

        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> skibootInfoRenderable = renderable }


        ViewRenderable.builder()
            .setView(this, view)
            .build()
            .thenAccept { renderable -> hardhatInfoRenderable = renderable }

        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            frameUpdate()
        }

        /** Test if network is ok **/

        if (networkOk()) {
            doAsync {
                run(url)
                uiThread {

                }
            }
        } else {Toast.makeText(this, "Connect to the internet before continuing", Toast.LENGTH_LONG )}

            }



     /** Helper function to remove current nodes from scene **/

    private fun setInvisible(node1:TransformableNode) {
        if (node1.isEnabled) {
            Log.d("aa", "first node changed")
            node1.isEnabled = false
        }
    }

    /** Loop through color options and change renderables color atribute using rgb values from list **/

    private fun changeColor(node: TransformableNode){
        var colorList = mutableListOf<Float>(0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 255.0f, 255.0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f, 255.0f, 255.0f, 255.0f)

        if (node != null) {
            node.renderable?.material?.setFloat3("baseColorTint", colorList[a], colorList[b], colorList[c])
        }

        /** Create counter to change color each time user clicks on button **/
        if (a < colorList.size) {
            try {
                Log.d("check", "went in to try")
                a += 1
                b += 1
                c += 1
            } catch (e: IOException)
            {
                Log.d("check", "went in to catch")
               Toast.makeText(this, "No more colors", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("check", "went in to else")
            Toast.makeText(this, "No more colors", Toast.LENGTH_LONG).show()
        }

        Log.d("numb", "${a}, ${b}, ${c}")

    }

    /** Begin tracking of images, if tracking state is Tracking then check images with AugmentedImages database for mach **/

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
//                        val imgNode = TransformableNode(fragment.transformationSystem)
                        var skibootNode = TransformableNode(fragment.transformationSystem)
                        var hardHatNode = TransformableNode(fragment.transformationSystem)
                        var sneakerNode = TransformableNode(fragment.transformationSystem)
                        var textNode = TransformableNode(fragment.transformationSystem)
                        var sneakerInfoNode = TransformableNode(fragment.transformationSystem)
                        var skibootInfoNode = TransformableNode(fragment.transformationSystem)
                        var hardhatInfoNode = TransformableNode(fragment.transformationSystem)

                        /** Delete button **/
                        button2.setOnClickListener {
                            Log.d("aa", "button Clicked")
                            setInvisible(sneakerNode)
//                            removeRenderable(anchorNode)
                        }

                        /** Change color button **/
                        button3.setOnClickListener {
                            Log.d("aa", "color button clicked")
                            changeColor(sneakerNode)
                        }

                        /** Checks recognised image with database and implements correct model,
                         * textview and textiview data from JSON. Also adjusts components rotation **/

                        if (it.name == "karhuSneaker") {

                            sneakerNode.setParent(anchorNode)
                            sneakerInfoNode.setParent(sneakerNode)

                            Log.d("aa", "1st")
                            sneakerNode.setLocalRotation(Quaternion.axisAngle(Vector3(1f, 0f, 0f), -180f))
                            sneakerInfoNode.setLocalRotation(Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f))

                            sneakerNode.renderable = sneakerRenderable
                            sneakerInfoNode.renderable = sneakerInfoRenderable
                            Log.d("aa", "1st rendered")
                            sneakerInfoRenderable.sizer = DpToMetersViewSizer(renderScale)

                            view.basicInfoCard.text = arrayList_details[0].name
                            view.description.text = arrayList_details[0].desc
                            view.url.text = arrayList_details[0].url

                        }
                        if (it.name == "skiboot") {
                            sneakerNode.setParent(anchorNode)
                            sneakerInfoNode.setParent(sneakerNode)

                            Log.d("aa", "2nd")
                            sneakerNode.setLocalRotation(Quaternion.axisAngle(Vector3(1f, 0f, 0f), -180f))
                            sneakerInfoNode.setLocalRotation(Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f))

                            sneakerNode.renderable = skiBootRenderable
                            sneakerInfoNode.renderable = skibootInfoRenderable
                            sneakerInfoRenderable.sizer = DpToMetersViewSizer(renderScale)
                            Log.d("aa", "2nd rendered")

                            view.basicInfoCard.text = arrayList_details[1].name
                            view.description.text = arrayList_details[1].desc
                            view.url.text = arrayList_details[1].url

                        }

                        if (it.name == "hardhat") {

                            sneakerNode.setParent(anchorNode)
                            sneakerInfoNode.setParent(sneakerNode)
                            Log.d("aa", "3rd")

                            sneakerNode.setLocalRotation(Quaternion.axisAngle(Vector3(1f, 0f, 0f), -180f))
                            sneakerInfoNode.setLocalRotation(Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f))

                            sneakerNode.renderable = hardHatRenderable
                            sneakerInfoNode.renderable = hardhatInfoRenderable
                            sneakerInfoRenderable.sizer = DpToMetersViewSizer(renderScale)
                            Log.d("aa", "3rd rendered")

                            view.basicInfoCard.text = arrayList_details[2].name
                            view.description.text = arrayList_details[2].desc
                            view.url.text = arrayList_details[2].url
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

    /** Creates fullscreen fragment for user info **/

    private fun showDialog() {
        val dialogFragment = FullScreenFragment()
        dialogFragment.show(supportFragmentManager, "signature")
        //AlertDialog.Builder(this, R.style.DialogTheme).show()
    }


    /** Fetches data from database and parses JSON into strings for futher use **/

    private fun run(url: String) {
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
                    model.desc = jsonObjectdetail.getString("description")
                    model.inventory = jsonObjectdetail.getString("inventory")
                    model.url = jsonObjectdetail.getString("url")
                    model.tags = jsonObjectdetail.getString("tags")

                    arrayList_details.add(model)
                    Log.d("model", "$model")

                }
                testeri = arrayList_details[0].name

            }
        })
    }

    /** Network check **/

    private fun networkOk(): Boolean {
        val connService = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connService.activeNetworkInfo?.isConnected ?: false
    }

}
