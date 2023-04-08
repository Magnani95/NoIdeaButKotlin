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
	var sector :      MutableMap<Char, ULong> = mutableMapOf('x' to 0u, 'y' to 0u)
	var velocity :    MutableMap<Char, ULong> = mutableMapOf('x' to 0u, 'y' to 0u)
	var  forwardX :Boolean = true
	var  forwardY :Boolean = true
	var directionAngle : UInt = 0u
		set(value){
		field = value % 360u
	}
	var shipAngle : UInt = 0u
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
		if (this.directionAngle > 0u && this.directionAngle <90u) {
			if(ULong.MAX_VALUE - this.coordinates.getValue('x') <= this.velocity.getValue('x')) {
				this.sector['x'] = this.sector.getValue('x') + 1u
			}
			if(ULong.MAX_VALUE - this.coordinates.getValue('y') <= this.velocity.getValue('y')) {
				this.sector['y'] = this.sector.getValue('y') + 1u
			}
			this.coordinates['x'] = this.coordinates.getValue('x') + this.velocity.getValue('x')
			this.coordinates['y'] = this.coordinates.getValue('y') + this.velocity.getValue('y')

		}else if(this.directionAngle >=90u && this.directionAngle <180u){
			if(this.coordinates.getValue('x') <= this.velocity.getValue('x')) {
				this.sector['x'] = this.sector.getValue('x') - 1u
			}
			if(ULong.MAX_VALUE - this.coordinates.getValue('y') <= this.velocity.getValue('y')) {
				this.sector['y'] = this.sector.getValue('y') + 1u
			}
			this.coordinates['x'] = this.coordinates.getValue('x') - this.velocity.getValue('x')
			this.coordinates['y'] = this.coordinates.getValue('y') + this.velocity.getValue('y')

		}else if(this.directionAngle >= 180u && this.directionAngle < 270u){
			if(this.coordinates.getValue('x') <= this.velocity.getValue('x')) {
				this.sector['x'] = this.sector.getValue('x') - 1u
			}
			if(this.coordinates.getValue('y') <= this.velocity.getValue('y')) {
				this.sector['y'] = this.sector.getValue('y') - 1u
			}
			this.coordinates['x'] = this.coordinates.getValue('x') - this.velocity.getValue('x')
			this.coordinates['y'] = this.coordinates.getValue('y') - this.velocity.getValue('y')

		}else { // 4th quadrant
			if(ULong.MAX_VALUE - this.coordinates.getValue('x') <= this.velocity.getValue('x')) {
				this.sector['x'] = this.sector.getValue('x') + 1u
			}
			if(this.coordinates.getValue('y') <= this.velocity.getValue('y')) {
				this.sector['y'] = this.sector.getValue('y') - 1u
			}
			this.coordinates['x'] = this.coordinates.getValue('x') + this.velocity.getValue('x')
			this.coordinates['y'] = this.coordinates.getValue('y') - this.velocity.getValue('y')
		}

	}

	private fun updateVelocity(ship:Ship){
		val radians : Double = directionAngle.toDouble() * Math.PI/180.0

		this.velocity['y'] = this.velocity['y']!!
		var trustY : ULong = (sin(radians) * ship.engineModule.thrust.toDouble()).toULong()
		var trustX : ULong = (cos(radians) * ship.engineModule.thrust.toDouble()).toULong()

		if (ship.engineModule.direction == EngineDirection.FORWARD) {
			if(forwardX){
				this.velocity['x'] = this.velocity['x']!! + trustX
			}else{
				if (this.velocity['x']!! <= trustX){
					forwardX = true
					this.velocity['x'] = 0u
				}else {
					this.velocity['x'] = this.velocity['x']!! - trustX
				}
			}
			if(forwardY){
				this.velocity['y'] = this.velocity['y']!! + trustY
			}else{
				if (this.velocity['y']!! <= trustY){
					forwardY = true
					this.velocity['y'] = 0u
				}else {
					this.velocity['y'] = this.velocity['y']!! - trustY
				}
			}
		}else { // engine BACKWARD
			if(forwardX){
				if (this.velocity['x']!! <= trustX) {
					forwardX = false
					this.velocity['x'] = 0u
				}else {
					this.velocity['x'] = this.velocity['x']!! - trustX
				}
			}else{
				this.velocity['x'] = this.velocity['x']!! + trustX
			}
			if(forwardY){
				if (this.velocity['y']!! <= trustY) {
					forwardY = true
					this.velocity['y'] = 0u
				}else {
					this.velocity['y'] = this.velocity['y']!!- trustY
				}
			}else{
				this.velocity['y'] = this.velocity['y']!! + trustY
			}
		}
	}
	private fun changeDirectionX(): UInt{
		var d :UInt = this.directionAngle

		if ( d in 0u..90u){
			d = 180u - this.directionAngle
		}else if (d in 91u..180u){
			d = 0u + (180u-directionAngle)
		}else if (d in 181u..270u){
			d = 360u - (this.directionAngle-180u)
		}else{ // 4th quadrant
		    d = 180u+ (360u-this.directionAngle)
		}
		return d
	}
	private fun changeDirectionY(): UInt{
		var d :UInt = this.directionAngle

		if ( d in 0u..90u){
			d = 360u - this.directionAngle
		}else if (d in 91u..180u){
			d = 180u + (180u-directionAngle)
		}else if (d in 181u..270u){
			d = 180u - (this.directionAngle-180u)
		}else{ // 4th quadrant
			d = 0u+ (360u-this.directionAngle)
		}
		return d
	}
}



class Sensors(var lowRange: UInt, var midRange: UInt, var longRange: UInt){
}