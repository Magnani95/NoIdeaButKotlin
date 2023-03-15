package com.example.noideabutkotlin.mandatory

import android.util.Log
import com.example.noideabutkotlin.Ship
import com.example.noideabutkotlin.hundredToDouble
import com.example.noideabutkotlin.mapToHundred
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.system.exitProcess

// ---  CLASSES
class Energy ( var battery:Battery, var solarPanels: SolarPanels ){

	// - METHODS

	fun tick(ship: Ship){
		Log.d("Magnani", "tick: Energy begin")
		val g = tickGeneration(ship)
		Log.d("Magnani", "tick: pre consumption")
		val c = tickConsumption(ship)
		if (! this.battery.tick((g-c).toLong())) {
			Log.d("Magnani", "tick: Zero energy stored")
		}
		Log.d("Magnani", "tick: energy finished")

	}

	private fun tickGeneration(ship: Ship) : ULong{
		Log.d("Magnani", "tickGeneration ${ship.status}")
		return this.solarPanels.tick(ship.status.starsEnergyExposition)
	}

	private fun tickConsumption(ship: Ship): ULong{
		return 0u
	}
}

class Battery (var maxCapacity : ULong, var load : ULong, var maxErogation: ULong){
	//var load : ULong = Random.nextULong()%(maxCapacity/2u) + (maxCapacity/2u)

	var operative : Boolean = true
	var health : UByte= 100u
		set(v) = when(v){
			in 1u..100u -> field = v
			0.toUByte() -> { field = v; this.operative = false }
			else        -> {
				Log.d("MAGNANI", "Energy-health-setter: invalid v"); exitProcess(-1)
			}
		}
	var heat : UByte = 0u
		set(v) = if (v > 100u){
			field = 100u
		}else{
			field = v
		}
	// - METHODS

	fun tick(energyDiff:Long) : Boolean {
		val erogation : Boolean = (energyDiff < 0)
		val e :ULong= (energyDiff.absoluteValue).toULong()
		Log.d("MAGNANI", "value of e is ${e} - ${erogation}")
		print(e)
		// REQUIRE power
		if (erogation){
			if (e > this.load){
				this.load = 0u

			}else{
				this.load -= e
			}
		// SAVE power
		}else{
			if (e + this.load > this.maxCapacity){
				this.load = this.maxCapacity
			}else{
				this.load += e
			}
			if (Random.nextUInt()%100u < 10u){
				this.heat++
			}
		}
		 // TODO spostare tutti in setter e getter
		if ( e > this.maxErogation){
			this.heat++
		}
		 if ( this.heat > 70u){
			 if (Random.nextUInt()%100u < this.heat){
				 this.health--
				 if(this.health == 0u.toUByte()){
					 this.operative = false
				 }
			 }
		 }
		 return this.operative
	}
}

// TODO add health fun to modify generation based on panels status
class SolarPanels(var maxGeneration : ULong, var efficiency : UByte){

	init {
		assert(efficiency <=100u, { "SolarPanels Efficiency should be in 0u..100u" })
		}

	fun tick(exposition : UInt): ULong {
		return ( exposition.toDouble()* hundredToDouble(efficiency)%maxGeneration.toDouble()).toULong()
	}
}
