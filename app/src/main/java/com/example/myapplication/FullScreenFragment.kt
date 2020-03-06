package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.layout_full_screen_dialog.*

class FullScreenFragment : DialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.layout_full_screen_dialog, container, false)
    }
//    override fun getTheme(): Int {
//        return R.style.DialogTheme
//    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            //send back data to PARENT fragment using callback
//            callbackListener.onDataReceived(editText.text.toString())
            // Now dismiss the fragment
            dismiss()
        }

    }
}