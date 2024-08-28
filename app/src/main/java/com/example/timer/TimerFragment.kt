package com.example.timer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import java.util.concurrent.TimeUnit

class TimerFragment : Fragment() {
    private var running: Boolean = false
    private var editing: Boolean = true

    private lateinit var playButton: ImageButton
    private lateinit var resetButton: ImageButton
    private lateinit var backspaceButton: ImageButton

    private lateinit var hoursTextView: TextView
    private lateinit var minutesTextView: TextView
    private lateinit var secondsTextView: TextView

    private lateinit var timerTitleView: TextView

    private lateinit var secondsLetterTextView: TextView
    private lateinit var minutesLetterTextView: TextView
    private lateinit var hoursLetterTextView: TextView

    private lateinit var timerTextView: TextView

    private lateinit var numericKeyboardGridLayout: GridLayout

    private var mediaPlayer: MediaPlayer? = null

    private var hours: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0

    private var elapsedTime: Int = 0
    private var timeRemaining: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (running) {
                timeRemaining = getTotalTimeInMillis() - (System.currentTimeMillis().toInt() - elapsedTime)
                if (timeRemaining <= 0) {
                    timeRemaining = 0
                    handler.removeCallbacks(this)
                    playAlarm()
                } else {
                    updateTimerText()
                    handler.postDelayed(this, 10)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        this.hoursTextView = view.findViewById(R.id.hoursTextView)
        this.minutesTextView = view.findViewById(R.id.minutesTextView)
        this.secondsTextView = view.findViewById(R.id.secondsTextView)

        this.secondsLetterTextView = view.findViewById(R.id.secondsLetter)
        this.minutesLetterTextView = view.findViewById(R.id.minutesLetter)
        this.hoursLetterTextView = view.findViewById(R.id.hoursLetter)

        this.timerTextView = view.findViewById(R.id.timerTextView)
        this.timerTitleView = view.findViewById(R.id.timerTitle)

        this.playButton = view.findViewById(R.id.timerPlayButton)
        this.resetButton = view.findViewById(R.id.resetTimerButton)
        this.backspaceButton = view.findViewById(R.id.backspaceButton)

        this.numericKeyboardGridLayout = view.findViewById(R.id.numericKeyboardGridLayout)

        val buttons = listOf(
            R.id.button0 to 0,
            R.id.button1 to 1,
            R.id.button2 to 2,
            R.id.button3 to 3,
            R.id.button4 to 4,
            R.id.button5 to 5,
            R.id.button6 to 6,
            R.id.button7 to 7,
            R.id.button8 to 8,
            R.id.button9 to 9
        )

        buttons.forEach { (id, number) ->
            view.findViewById<Button>(id).setOnClickListener {
                typeNumber(number)
            }
        }

        this.updateInputTimerText()

        this.resetButton.visibility = View.GONE
        this.timerTextView.visibility = View.GONE
        this.timerTitleView.visibility = View.GONE

        this.playButton.setOnClickListener {
            if (this.seconds == 0 && this.minutes == 0 && this.hours == 0) {
                return@setOnClickListener
            }

            this.running = !this.running
            if (this.running) {
                this.play()
            } else {
                this.pause()
            }
        }

        this.resetButton.setOnClickListener {
            this.reset()
        }

        this.backspaceButton.setOnClickListener {
            this.handleBackspace()
        }

        return view
    }

    private fun reset() {
        this.editing = true
        this.running = false
        this.numericKeyboardGridLayout.visibility = View.VISIBLE
        this.handleVisibility()
        this.handler.removeCallbacks(this.updateTimerRunnable)
        this.playButton.setImageResource(R.drawable.baseline_play_circle_24)
    }

    private fun play() {
        if (this.editing) {
            this.elapsedTime = System.currentTimeMillis().toInt()
            this.numericKeyboardGridLayout.visibility = View.GONE
            this.handleVisibility()
        } else {
            this.elapsedTime = System.currentTimeMillis().toInt() - this.elapsedTime
        }

        this.editing = false
        this.handler.post(this.updateTimerRunnable)
        this.playButton.setImageResource(R.drawable.baseline_pause_circle_24)
    }

    private fun handleBackspace() {
        when {
            this.hours > 0 -> {
                this.seconds = ((this.minutes % 10) * 10) + (this.seconds / 10)
                this.minutes = (this.hours % 10) * 10 + (this.minutes / 10)
                this.hours /= 10
            }
            this.minutes > 0 -> {
                this.seconds = ((this.minutes % 10) * 10) + (this.seconds / 10)
                this.minutes /= 10
            }
            this.seconds > 0 -> {
                this.seconds /= 10
            }
        }

        this.updateInputTimerText()
    }

    private fun handleVisibility() {
        val runVisibility = if (this.running) View.VISIBLE else View.GONE
        val editVisibility = if (this.running) View.GONE else View.VISIBLE

        this.resetButton.visibility = runVisibility
        this.timerTextView.visibility = runVisibility
        this.timerTitleView.visibility = runVisibility

        this.secondsTextView.visibility = editVisibility
        this.secondsLetterTextView.visibility = editVisibility
        this.minutesTextView.visibility = editVisibility
        this.minutesLetterTextView.visibility = editVisibility
        this.hoursTextView.visibility = editVisibility
        this.hoursLetterTextView.visibility = editVisibility
    }

    private fun pause() {
        this.mediaPlayer?.stop()
        this.elapsedTime = System.currentTimeMillis().toInt() - this.elapsedTime
        this.handler.removeCallbacks(this.updateTimerRunnable)
        this.playButton.setImageResource(R.drawable.baseline_play_circle_24)
    }

    private fun typeNumber(number: Int) {
        if (this.seconds == 0 && this.minutes == 0 && this.hours == 0) {
            this.seconds = number
            return this.updateInputTimerText()
        }

        if (this.seconds < 10 && this.minutes == 0 && this.hours == 0) {
            this.seconds = (this.seconds * 10) + number
            return this.updateInputTimerText()
        }

        if (this.minutes == 0 && this.hours == 0) {
            this.minutes = this.seconds / 10
            this.seconds = ((this.seconds % 10) * 10) + number
            return this.updateInputTimerText()
        }

        if (this.minutes < 10 && this.hours == 0) {
            this.minutes = (this.minutes * 10) + (this.seconds / 10)
            this.seconds = ((this.seconds % 10) * 10) + number
            return this.updateInputTimerText()
        }

        if (this.hours == 0) {
            this.hours = this.minutes / 10
            this.minutes = ((this.minutes % 10) * 10) + (this.seconds / 10)
            this.seconds = ((this.seconds % 10) * 10) + number
            return this.updateInputTimerText()
        }

        if (this.hours < 10) {
            this.hours = (this.hours * 10) + (this.minutes / 10)
            this.minutes = ((this.minutes % 10) * 10) + (this.seconds / 10)
            this.seconds = ((this.seconds % 10) * 10) + number
            return this.updateInputTimerText()
        }
    }

    private fun updateInputTimerText() {
        this.hoursTextView.text = getString(R.string.digits, this.hours)
        this.minutesTextView.text = getString(R.string.digits, this.minutes)
        this.secondsTextView.text = getString(R.string.digits, this.seconds)
    }

    private fun updateTimerText() {
        val hours = TimeUnit.MILLISECONDS.toHours(this.timeRemaining.toLong()).toInt()
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this.timeRemaining.toLong()).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this.timeRemaining.toLong()).toInt() % 60

        val ninetyNineHours = 99 * 60 * 60 * 1000
        val format = if (getTotalTimeInMillis().toLong() > ninetyNineHours) {
            R.string.timer_format
        } else {
            R.string.time_format
        }

        val formattedTime = getString(format, hours, minutes, seconds)
        this.timerTextView.text = formattedTime
    }

    private fun getTotalTimeInMillis(): Int {
        val secondsInMillis = this.seconds * 1000
        val minutesInMillis = this.minutes * 60 * 1000
        val hoursInMillis = this.hours * 60 * 60 * 1000

        return secondsInMillis + minutesInMillis + hoursInMillis
    }

    private fun playAlarm() {
        this.mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
        this.mediaPlayer?.isLooping = true
        this.mediaPlayer?.start()
    }
}