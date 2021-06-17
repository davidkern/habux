package io.mju.habux.ui.orientation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.R
import io.mju.habux.databinding.FragmentOrientationBinding

class OrientationFragment : Fragment() {

    private lateinit var model: FragmentOrientationBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        model = FragmentOrientationBinding.inflate(inflater)

        return model.root
    }
}