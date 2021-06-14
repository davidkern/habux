package io.mju.habux.ui.climate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.R

class ClimateFragment : Fragment() {

    private lateinit var climateViewModel: ClimateViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        climateViewModel =
                ViewModelProvider(this).get(ClimateViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_climate, container, false)
        val textView: TextView = root.findViewById(R.id.text_climate)
        climateViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}