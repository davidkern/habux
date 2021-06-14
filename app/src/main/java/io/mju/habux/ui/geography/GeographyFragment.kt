package io.mju.habux.ui.geography

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.R

class GeographyFragment : Fragment() {

    private lateinit var geographyViewModel: GeographyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        geographyViewModel =
            ViewModelProvider(this).get(GeographyViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_geography, container, false)
        val textView: TextView = root.findViewById(R.id.text_geography)
        geographyViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}