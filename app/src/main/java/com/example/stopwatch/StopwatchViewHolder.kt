package com.example.stopwatch

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView

import com.example.stopwatch.databinding.StopwatchItemBinding

var count: Long = 0

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.customView.setPeriod(stopwatch.startMs)
        when {
            stopwatch.currentMs == stopwatch.startMs -> {
                binding.item.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))


                binding.customView.setCurrent(stopwatch.currentMs)
            }
        }
        when {
            stopwatch.isStarted -> startTimer(stopwatch)
            else -> stopTimer(stopwatch)
        }
        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.currentMs > 0L){
                when {
                    stopwatch.isStarted -> listener.stop(stopwatch.id, stopwatch.currentMs)
                    else -> {
                        StopwatchAdapter.Start.start = stopwatch.id
                        listener.start(stopwatch.id)
                    }
                }
            }
        }
        binding.deleteButton.setOnClickListener {
            binding.customView.setCurrent(0)
            binding.item.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))
            stopTimer(stopwatch)
            listener.delete(stopwatch.id)
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = "stop"
        this.timer?.cancel()
        this.timer = getCountDownTimer(stopwatch)
        this.timer?.start()
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = "start"
        timer?.cancel()
        stopwatch.isStarted = false
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                count = stopwatch.currentMs
                when (StopwatchAdapter.Start.start) {
                    stopwatch.id ->   {
                        stopwatch.currentMs -= interval
                        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                        binding.customView.setCurrent(stopwatch.startMs - stopwatch.currentMs)}
                    else -> listener.stop(stopwatch.id, stopwatch.currentMs)
                }
                if (stopwatch.currentMs <= 0L) {
                    onFinish()
                    return
                }
            }

            override fun onFinish() {
                timer?.cancel()
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                stopTimer(stopwatch)
                binding.item.setBackgroundColor(resources.getColor(R.color.teal_700))

            }
        }
    }

    private companion object {
        private const val UNIT_TEN_MS = 100L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}