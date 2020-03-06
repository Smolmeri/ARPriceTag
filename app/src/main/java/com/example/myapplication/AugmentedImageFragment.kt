package com.example.myapplication


import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class AugmentedImageFragment: ArFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): android.view.View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        return view
    }



    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        setupAugmentedImageDatabase(config, session)
        config.setFocusMode(Config.FocusMode.AUTO)
        return config
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session?) {
        val augmentedImageDb = AugmentedImageDatabase(session)
        val assetManager = context?.assets

        val inputStream1 = assetManager?.open("karhuSneaker.jpg")
        val augmentedImageBitmap1 = BitmapFactory.decodeStream(inputStream1)

        val inputStream2 = assetManager?.open("paperboy.jpg")
        val augmentedImageBitmap2 = BitmapFactory.decodeStream(inputStream2)

        val inputStream3 = assetManager?.open("hardhat.jpg")
        val augmentedImageBitmap3 = BitmapFactory.decodeStream(inputStream3)

        val inputStream4 = assetManager?.open("skiboot.png")
        val augmentedImageBitmap4 = BitmapFactory.decodeStream(inputStream4)


        augmentedImageDb.addImage("karhuSneaker", augmentedImageBitmap1)
        augmentedImageDb.addImage("paperboy", augmentedImageBitmap2)
        augmentedImageDb.addImage("hardhat", augmentedImageBitmap3)
        augmentedImageDb.addImage("skiboot", augmentedImageBitmap4)

        config.augmentedImageDatabase = augmentedImageDb
    }
}