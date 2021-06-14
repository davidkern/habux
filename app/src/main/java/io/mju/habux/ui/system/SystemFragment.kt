package io.mju.habux.ui.system

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.R

class SystemFragment : Fragment() {

    private lateinit var galleryViewModel: SystemViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
                ViewModelProvider(this).get(SystemViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_system, container, false)
        val textView: TextView = root.findViewById(R.id.text_system)
        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}