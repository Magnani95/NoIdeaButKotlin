package com.example.noideabutkotlin

import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.example.noideabutkotlin.activities.GameActivity
import com.example.noideabutkotlin.generator.energyGenerator
import com.example.noideabutkotlin.generator.engineGenerator
import com.example.noideabutkotlin.generator.positionGenerator
import com.example.noideabutkotlin.mandatory.*
import java.io.Serializable
import java.util.concurrent.ExecutorService
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class Ship{
	lateinit var engineModule: Engine
	lateinit var energyModule : Energy
	lateinit var position : Position
	var status : Status = Status()
	var tickDone = ReentrantLock()
	var running : Boolean = true
	var pause : Boolean = false
	init {

	}
	constructor(){
		Log.d("MAGNANI", "engine generation ")
		this.engineModule = engineGenerator()
		Log.d("MAGNANI", "energy generation ")
		this.energyModule = energyGenerator()
		Log.d("MAGNANI", "position generation ")
		this.position = positionGenerator()
	}
	constructor(engine: Engine){
		this.engineModule = engine
	}

	fun tick( workerPool: ExecutorService){
		/*
		Log.d("MAGNANI", "tick:prelock1")
		this.tickDone.lock()
		Log.d("MAGNANI", "tick:postlock1")
		val l = ReentrantLock()

		workerPool.submit{
			Log.d("MAGNANI", "tick:prelock2")
			//l.lock()
			Log.d("MAGNANI", "tick:postlock2")
			this.engineModule.tick(this)
			Log.d("MAGNANI", "tick:pre-unlock2")
			//l.unlock()
			Log.d("MAGNANI", "tick:post-unlock2")
		}
		workerPool.submit{
			this.energyModule.tick(this)
		}
		workerPool.submit{
			Log.d("MAGNANI", "tick:prelock3")
			//l.lock()
			Log.d("MAGNANI", "tick:postlock3")
			this.position.tick(this)
			Log.d("MAGNANI", "tick:pre-unlock3")
			//l.unlock()
			Log.d("MAGNANI", "tick:post-unlock2")
		}

		Log.d("MAGNANI", "tick:pre-unlock1")
		this.tickDone.unlock()
		Log.d("MAGNANI", "tick:post-unlock1")
		*/
		Log.d("MAGNANI", "tick:engine module")
		this.engineModule.tick(this)
		Log.d("MAGNANI", "tick:energy module")
		this.energyModule.tick(this)
		Log.d("MAGNANI", "tick:position module")
		this.position.tick(this)

	}

	fun gameCycle(activity: GameActivity, workerPool: ExecutorService){
		while (running){
			Log.d("MAGNANI", "running")
			if (!pause){
				while (!pause){
					Log.d("MAGNANI", "tick")
					this.tick(workerPool)
					Log.d("MAGNANI", "pre-lock")
					this.tickDone.lock()
					Log.d("MAGNANI", "post-lock ${activity}")
					updateGraphic(activity)
					this.tickDone.unlock()
					Thread.sleep(1000)
				}
			}else{
				Log.d("MAGNANI", "tock")
				Thread.sleep(1000)
			}
		}
	}
	fun updateGraphic(activity: GameActivity){
		var enginePercentage = engineModule.getThrustPercentage()
		var bar = activity.findViewById<ProgressBar>(R.id.engineEfficiency)
		var t = activity.findViewById<TextView>(R.id.enginePercentage)

		t.text = enginePercentage.toString()
		bar.progress = this.engineModule.getEfficiency(enginePercentage).toInt()

	}
}