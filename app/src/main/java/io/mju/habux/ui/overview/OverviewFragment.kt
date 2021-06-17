package io.mju.habux.ui.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.MainViewModel
import io.mju.habux.databinding.FragmentOverviewBinding


class OverviewFragment : Fragment() {

    private lateinit var overviewViewModel: OverviewViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var ui: FragmentOverviewBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mainViewModel =
                ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        overviewViewModel =
                ViewModelProvider(this).get(OverviewViewModel::class.java)

        ui = FragmentOverviewBinding.inflate(layoutInflater)

        mainViewModel.batteryVoltage.observe(viewLifecycleOwner, Observer { battery_voltage ->
            ui.textBatteryVoltage.text = "%.2f V".format(battery_voltage)
        })

        mainViewModel.bigPower.observe(viewLifecycleOwner, Observer { big_power ->
            ui.textBigPower.text = "%d W".format(big_power)
        })

        mainViewModel.lilPower.observe(viewLifecycleOwner, Observer { lil_power ->
            ui.textLilPower.text = "%d W".format(lil_power)
        })

        mainViewModel.solarPower.observe(viewLifecycleOwner, Observer { solar_power ->
            ui.textSolarPower.text = "%d W".format(solar_power)
        })

        return ui.root
    }
}