package com.example.noideabutkotlin

import android.os.Parcelable
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.example.noideabutkotlin.activities.GameActivity
import com.example.noideabutkotlin.generator.energyGenerator
import com.example.noideabutkotlin.generator.engineGenerator
import com.example.noideabutkotlin.generator.positionGenerator
import com.example.noideabutkotlin.mandatory.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock


class Ship() : Parcelable{
	var engineModule: Engine = engineGenerator()
	var energyModule : Energy = energyGenerator()
	var position : Position = positionGenerator()
	var status : Status = Status()
	var tickDone = ReentrantLock()
	var running : Boolean = true
	var pause : Boolean = false

	constructor(parcel: android.os.Parcel) : this() {
		running = parcel.readByte() != 0.toByte()
		pause = parcel.readByte() != 0.toByte()
	}

	fun tick( workerPool: ExecutorService){

		val l = ReentrantLock()
		val s = Semaphore(3)
		workerPool.submit{
			s.acquire()
			l.lock()
			this.engineModule.tick(this)
			l.unlock()
			s.release()
		}
		workerPool.submit{
			s.acquire()
			this.energyModule.tick(this)
			s.release()
		}
		workerPool.submit{
			s.acquire()
			l.lock()
			this.position.tick(this)
			l.unlock()
			s.release()
		}
		//workerPool.shutdown()
		//workerPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
		while (s.availablePermits() != 3){
			Thread.sleep(10)
		}

	}

	fun gameCycle(activity: GameActivity, workerPool: ExecutorService){
		while (running){
			Log.d("MAGNANI", "running")
			if (!pause){
				while (!pause){
					Log.d("MAGNANI", "tick")
					this.tick(workerPool)
					updateGraphic(activity)
					Thread.sleep(1000)
				}
			}else{
				Log.d("MAGNANI", "tock")
				Thread.sleep(1000)
			}
		}
	}
	fun updateGraphic(activity: GameActivity){
		val enginePercentage = engineModule.getThrustPercentage()
		val engineBar = activity.findViewById<ProgressBar>(R.id.engineEfficiency)
		val engineText = activity.findViewById<TextView>(R.id.enginePercentage)
		val fuelProgress = activity.findViewById<ProgressBar>(R.id.fuelProgress)
		val fuelText = activity.findViewById<TextView>(R.id.fuelPercentage)


		engineText.text = "${enginePercentage.toString()} %"
		engineBar.progress = this.engineModule.getEfficiency(enginePercentage).toInt()
		Log.d("MAGNANI", "updateGraphic: FUEL PERCENTAGE ${engineModule.getFuelPercentage()}")
		fuelText.text = "${this.engineModule.getFuelPercentage().toString()} %"
		fuelProgress.progress = this.engineModule.getFuelPercentage().toInt()

		val sectorX = activity.findViewById<TextView>(R.id.SectorX)
		val sectorY = activity.findViewById<TextView>(R.id.SectorY)
		val coordX  = activity.findViewById<TextView>(R.id.CoordinatesX)
		val coordY = activity.findViewById<TextView>(R.id.coordinatesY)

		sectorY.text = "Y: ${this.position.sector.get('y')}"
		sectorX.text = "X: ${this.position.sector.get('x')}"
		coordY.text = "Y: ${this.position.coordinates.get('y')}"
		coordX.text = "X: ${this.position.coordinates.get('x')}"

		val velocityY = activity.findViewById<TextView>(R.id.velocityY)
		val velocityX = activity.findViewById<TextView>(R.id.velocityX)

		velocityY.text = "Y:${ this.position.velocity.get('y').toString() }"
		velocityX.text = "X:${this.position.velocity.get('x').toString()}"

		val directionAngle = activity.findViewById<TextView>(R.id.directionAngle)
		directionAngle.text = "${this.position.directionAngle.toString()}°"

		val revX = activity.findViewById<TextView>(R.id.reverseX)
		val revY = activity.findViewById<TextView>(R.id.reverseY)
        /*
        Log.d("MAGNANI", "updateGraphic: here")
		if(this.position.forwardX) {
			revX.text = "Forward"
		} else {
			revX.text = "Backward"
		}
		Log.d("MAGNANI", "updateGraphic: here2")
		if(this.position.forwardY) {
			revY.text = "Forward"
		} else {
			revY.text = "Backward"
		}
		Log.d("MAGNANI", "updateGraphics: end")
		*/
	}

	override fun describeContents(): Int {
		TODO("Not yet implemented")
	}

	override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
		parcel.writeByte(if (running) 1 else 0)
		parcel.writeByte(if (pause) 1 else 0)
	}

	companion object CREATOR : Parcelable.Creator<Ship> {
		override fun createFromParcel(parcel: android.os.Parcel): Ship {
			return Ship(parcel)
		}

		override fun newArray(size: Int): Array<Ship?> {
			return arrayOfNulls(size)
		}
	}
}
