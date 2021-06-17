package io.mju.habux.ui.system

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.MainViewModel
import io.mju.habux.R
import io.mju.habux.databinding.FragmentSystemBinding

class SystemFragment : Fragment() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var galleryViewModel: SystemViewModel
    private lateinit var ui: FragmentSystemBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mainViewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        galleryViewModel =
                ViewModelProvider(this).get(SystemViewModel::class.java)

        ui = FragmentSystemBinding.inflate(inflater)

        mainViewModel.underCounterTemp.observe(viewLifecycleOwner, Observer { imu_temp ->
            ui.textImuTemperature.text = "%.1f°C".format(imu_temp)
        })

        return ui.root
    }
}