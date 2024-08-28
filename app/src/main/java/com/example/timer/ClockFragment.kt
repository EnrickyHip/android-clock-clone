package com.example.timer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.time.LocalDateTime

class ClockFragment : Fragment() {
    private lateinit var clockTextView: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clock, container, false)
        clockTextView = view.findViewById(R.id.clockTextView)
        return view
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun updateTime() {
        val now = LocalDateTime.now()
        val hour = now.hour
        val minutes = now.minute
        val seconds = now.second

        clockTextView.text = getString(R.string.time_format, hour, minutes, seconds)
    }
}
