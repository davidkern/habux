package io.mju.habux.ui.orientation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.MISSING_DATA
import io.mju.habux.MainViewModel
import io.mju.habux.R
import io.mju.habux.databinding.FragmentOrientationBinding
import android.opengl.Matrix;

class OrientationFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var ui: FragmentOrientationBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        ui = FragmentOrientationBinding.inflate(layoutInflater)

        mainViewModel.accelerometer.observe(viewLifecycleOwner, Observer { accel ->
            if (accel != null) {
                // TODO: calibration matrix
                val square_mag_accel = accel[0]*accel[0] + accel[1]*accel[1] + accel[2]*accel[2]
                val mag_accel = Math.sqrt(square_mag_accel)

                // bail if magnitude doesn't make sense (should be close to 1)
                if ((mag_accel) > 0.75) {
                    val gx = accel[0] / mag_accel
                    val gy = accel[1] / mag_accel
                    val gz = accel[2] / mag_accel

                    val degrees = 180.0 / Math.PI

                    val phi = Math.atan(gy / gz) * degrees
                    val theta = (-gx / Math.sqrt(gy*gy + gz*gz)) * degrees

                    ui.textPitch.text = "%.2f°".format(phi)
                    ui.textRoll.text = "%.2f°".format(theta)
                } else {
                    // something is wrong
                    ui.textRoll.text = MISSING_DATA
                    ui.textPitch.text = MISSING_DATA
                }
            } else {
                ui.textRoll.text = MISSING_DATA
            }

        })

        return ui.root
    }
}