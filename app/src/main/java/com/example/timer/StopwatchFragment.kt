package com.example.timer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import java.util.concurrent.TimeUnit

class StopwatchFragment : Fragment() {
    private var running: Boolean = false
    private var playButton: ImageButton? = null
    private var resetButton: ImageButton? = null
    private var stopWatchTextView: TextView? = null
    private var centiSecondsTextView: TextView? = null

    private var startTime: Long = 0
    private var currentTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (running) {
                currentTime = System.currentTimeMillis() - startTime
                updateTimerText()
                handler.postDelayed(this, 10)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stopwatch, container, false)
        this.playButton = view.findViewById(R.id.stopWatchPlayButton)
        this.stopWatchTextView = view.findViewById(R.id.stopWatchTextView)
        this.centiSecondsTextView = view.findViewById(R.id.centiSecondsTextView)
        this.resetButton = view.findViewById(R.id.resetStopwatchButton)

        updateTimerText()

        playButton?.setOnClickListener {
            this.running = !this.running
            if (this.running) {
                this.startTime = System.currentTimeMillis() - this.currentTime
                this.handler.post(this.updateTimerRunnable)
                this.playButton?.setImageResource(R.drawable.baseline_pause_circle_24)
            } else {
                this.handler.removeCallbacks(this.updateTimerRunnable)
                this.playButton?.setImageResource(R.drawable.baseline_play_circle_24)
            }
        }

        resetButton?.setOnClickListener {
            this.running = false
            this.startTime = 0
            this.currentTime = 0
            this.handler.removeCallbacks(this.updateTimerRunnable)
            this.updateTimerText()
            this.playButton?.setImageResource(R.drawable.baseline_play_circle_24)
        }

        return view
    }

    private fun updateTimerText() {
        val hours = TimeUnit.MILLISECONDS.toHours(this.currentTime).toInt()
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this.currentTime).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this.currentTime).toInt() % 60
        val centiSeconds = (this.currentTime % 1000) / 10

        val formattedTime = getString(R.string.time_format, hours, minutes, seconds)
        this.stopWatchTextView?.text = formattedTime
        this.centiSecondsTextView?.text = getString(R.string.digits, centiSeconds)
    }
}
