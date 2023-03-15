package com.example.noideabutkotlin.mandatory

import android.util.Log
import com.example.noideabutkotlin.Ship
import com.example.noideabutkotlin.generator.sensorGenerator
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.nextULong


class Position() {
	var coordinates : MutableMap<Char, ULong> = mutableMapOf('x' to Random.nextULong(), 'y' to Random.nextULong())
	var sector :      MutableMap<Char, ULong> = mutableMapOf('x' to Random.nextULong(), 'y' to Random.nextULong())
	var velocity :    MutableMap<Char, ULong> = mutableMapOf('x' to 0u.toULong(), 'y' to 0u.toULong())
	var directionAngle : UInt = 0u
	set(value){
		field = value % 360u
	}
	var sensors : Sensors = sensorGenerator()

	init {
		sensorGenerator()
	}

	fun tick(ship: Ship){
		Log.d("Magnani", "pre update position")
		updatePosition()
		Log.d("Magnani", "pre update velocity")
		updateVelocity(ship)
	}

	private fun updatePosition(){

		// Update X component
		if (ULong.MAX_VALUE - this.coordinates.getValue('x') < this.velocity.getValue('x')){
			this.sector['x'] = this.sector.getValue('x') +1u
		}else if (this.coordinates.getValue('x') < this.velocity.getValue('x') ){
			this.sector['x'] = this.sector.getValue('x') -1u
		}
		this.coordinates['x'] = this.coordinates.getValue('x') + this.velocity.getValue('x')
		// Update Y component
		if (ULong.MAX_VALUE - this.coordinates.getValue('y') < this.velocity.getValue('y') ){
			this.sector['y'] = this.sector.getValue('y') +1u
		}else if (this.coordinates.getValue('y') < this.velocity.getValue('y') ){
			this.sector['y'] = this.sector.getValue('y') -1u
		}
		this.coordinates['y'] = this.coordinates.getValue('y') + this.velocity.getValue('y')
	}

	private fun updateVelocity(ship:Ship){
		val radians : Double = directionAngle.toDouble() * Math.PI/180.0
		if (ship.engineModule.direction == EngineDirection.FORWARD) {
			this.velocity['y'] = this.velocity['y']!! + (sin(radians) * ship.status.thrust.toDouble()).toULong()
			this.velocity['x'] = this.velocity['x']!! + (cos(radians) * ship.status.thrust.toDouble()).toULong()
		}else{
			this.velocity['y'] = this.velocity['y']!! - (sin(radians) * ship.status.thrust.toDouble()).toULong()
			this.velocity['x'] = this.velocity['x']!! - (cos(radians) * ship.status.thrust.toDouble()).toULong()
		}
	}

}

class Sensors(var lowRange: UInt, var midRange: UInt, var longRange: UInt){
}