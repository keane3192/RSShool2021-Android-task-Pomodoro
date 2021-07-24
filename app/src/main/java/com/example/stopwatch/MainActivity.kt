package com.example.stopwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stopwatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopwatchListener , LifecycleObserver {
    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    var timer = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState != null){
            nextId = savedInstanceState.getInt(NEXTID)
            val inst = savedInstanceState.getInt(INSTANCE)
            for (i in 0 until inst){
                val id = savedInstanceState.getInt("$ID$i")
                val startMs = savedInstanceState.getLong("$STARTMS$i")
                val currentMs = savedInstanceState.getLong("$MS$i")
                val started = savedInstanceState.getBoolean("$START$i")
                stopwatches.add(Stopwatch(id,  currentMs, started, startMs,true))
            }
            stopwatchAdapter.submitList(stopwatches.toList())
        }
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }
        binding.addNewStopwatchButton.setOnClickListener {
            val startTimer = binding.minutes.text.toString()
            if (startTimer.isNotBlank() && startTimer.toLong() < 1441) {
                timer = startTimer.toLong() * 60000L
                stopwatches.add(Stopwatch(nextId++,  timer, false, timer))
                stopwatchAdapter.submitList(stopwatches.toList())
            } else Toast.makeText(this, "Invalid data", Toast.LENGTH_LONG).show()
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id,  currentMs ?: it.currentMs,  isStarted, it.startMs))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(NEXTID, nextId)
        outState.putInt(INSTANCE, stopwatches.size)
        for (i in stopwatches.indices){
            outState.putInt("$ID$i", stopwatches[i].id)
            outState.putLong("$STARTMS$i", stopwatches[i].startMs)
            outState.putLong("$MS$i", stopwatches[i].currentMs)
            outState.putBoolean("$START$i", stopwatches[i].isStarted)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val time = stopwatches.find { it.isStarted }
        if (time != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, count)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private companion object {
        const val NEXTID = "nextid"
        const val ID = "id"
        const val MS = "ms"
        const val STARTMS = "startms"
        const val START = "start"
        const val INSTANCE = "instance"
    }
}
