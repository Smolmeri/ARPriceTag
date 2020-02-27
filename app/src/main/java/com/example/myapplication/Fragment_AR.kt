package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.ar_fragment.*


/**
arFragment = sceneform_fragment as ArFragment
arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
return@setOnTapArPlaneListener
}
val anchor = hitResult.createAnchor()
placeObject(arFragment, anchor)
}
 */

class Fragment_AR : Fragment() {

    var arFragment: ArFragment? = null

    val TAG = "FragmentAR"
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        Thread {
            Log.d(TAG, "thread started")
            arFragment = sceneform_fragment as ArFragment
            arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
                if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                    return@setOnTapArPlaneListener
                }
                val anchor = hitResult.createAnchor()
                placeObject(arFragment!!, anchor)
            }
        }


        super.onCreate(savedInstanceState)


        Log.d(TAG, "onCreate")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        return inflater!!.inflate(R.layout.ar_fragment,container,false)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
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
                val builder = AlertDialog.Builder(context!!)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }
    private fun addControlsToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
    }


}