@file:OptIn(ExperimentalUnsignedTypes::class)

package com.example.noideabutkotlin.mandatory

import android.util.Log
import com.example.noideabutkotlin.*

import kotlin.random.Random
import kotlin.random.nextUBytes
import kotlin.random.nextUInt
import kotlin.system.exitProcess


const val TAG = "MAGNANI"
// ---  FUNCTION


// ---  CLASSES
class Engine (var fuelType : FuelType, var tank : Tank, var efficiencyManager: LoadManager, engineLevel : UByte = 0u) {
	// components

	//characteristics
	var fuelConsumptionRate : UInt = Random.nextUInt() % (100u*(engineLevel+1u))

	var maxForwardThrust : UInt = Random.nextUInt() % (UInt.MAX_VALUE/(10u-engineLevel))
	var maxBackwardThrust : UInt = Random.nextUInt() % (UInt.MAX_VALUE/(20u-engineLevel))

	// health
	var health : UByte = 100u
		set(v) = when(v){
			in 1u..100u -> field = v
			0.toUByte() -> { field = v; this.operative = false }
			else        -> {Log.d("MAGNANI", "Engine-health-setter: invalid v"); exitProcess(-1)
			}
		}

	var operative: Boolean = true
		set(v) = if (!v){
				this.thrust = 0u
				field = v
			}else{
				field = v
		}

	var direction: EngineDirection = EngineDirection.FORWARD
		set(v) = if (v != field){
				this.thrust = 0u
				field = v
			}else{}

	private var overHeat: UByte = 0u
		set(v) = if (v > 100u){
				field = 100u
			}else{
				field = v
		}

	var thrust : UInt = 0u
		set(v) = when (this.direction){
			EngineDirection.FORWARD ->
				if (v <= this.maxForwardThrust){
					field = v
				}else{
					Log.d("MAGNANI", "thrust-set(): [FORWARD] value is over max ${v} / ${this.maxForwardThrust}")
					System.exit(-1)
				}
			EngineDirection.BACKWARD ->
				if (v <= this.maxBackwardThrust){
					field = v
				}else{
					Log.d("MAGNANI", "thrust-set(): [BACKWARD] value is over max")
					System.exit(-1)
				}
		}

	// - METHODS

	fun getThrustPercentage() : UByte{
		return when (this.direction){
			EngineDirection.FORWARD -> mapToHundred(this.thrust, this.maxForwardThrust)
			EngineDirection.BACKWARD -> mapToHundred(this.thrust, this.maxBackwardThrust)
		}
	}

	fun getEfficiency(i:UByte) : UByte = this.efficiencyManager.efficiency[i.toInt()]

	fun getFuelPercentage():ULong{
		return this.tank.percentage()
	}

	fun tick(ship:Ship) {
		Log.d("MAGNANI", "tick: here 0 ")
		if (this.operative){
			Log.d("MAGNANI", "tick: here 1a ")
			ship.status.thrust = this.thrust
			Log.d("MAGNANI", "tick:Overheating")
			tickOverheating()
			Log.d("MAGNANI", "tick:Fuel consumption")
			tickFuelConsumption()

			//tickHealth()
			//canKeepGoing()        //maybe, just to be sure everything is ok
			//tickEvents()
		}else{
			Log.d("MAGNANI", "tick: here 1b ")
		}

	}

	private fun tickOverheating(){
		val p : UByte = getThrustPercentage()
		val r = (Random.nextUInt()%2u).toUByte()

		when(this.fuelType){
			FuelType.STEAM -> if (p > 95u) { this.overHeat++ } else {this.overHeat = (this.overHeat-r).toUByte()}
			FuelType.CHEMICAL -> if (p > 90u) { this.overHeat++ } else {this.overHeat = (this.overHeat-r).toUByte()}
			FuelType.ELECTRIC -> if (p > 90u) { this.overHeat++ } else {this.overHeat = (this.overHeat-r).toUByte()}
			FuelType.FUSION -> if (p > 50u) { this.overHeat++ } else {this.overHeat = (this.overHeat-r).toUByte()}
			FuelType.FISSION -> if (p > 99u) { this.overHeat++ } else {this.overHeat = (this.overHeat-r).toUByte()}
		}
	}

	private fun tickFuelConsumption(){
		if (this.operative){
			val p : UByte = getThrustPercentage()
			val e = getEfficiency(p)
			// TODO [PERFORMANCE][GOOD2CACHE] consider to fit consumption into a static array
			val consumption : UInt = when{
				 e != 0u.toUByte()  -> p*( this.fuelConsumptionRate + (fuelConsumptionRate/e) )
				else                -> p*( this.fuelConsumptionRate + (fuelConsumptionRate/1u) )
			}

			this.tank.tick(consumption)
			if (this.tank.isEmpty()) {
				this.operative = false
			}
		}else{
			this.tank.tick()
		}

	}

	fun setThrustPercentage(p : UByte) {
		if (this.tank.isEmpty()){
			this.thrust = 0u
		}else{
			if (p < 100u) {
				Log.d("MAGNANI", "setThrust(percent) ${p}")
				when (this.direction) {
					EngineDirection.FORWARD -> this.thrust =
						(maxForwardThrust.toDouble() / 100.0 * p.toDouble() + 1).toUInt()
					EngineDirection.BACKWARD -> this.thrust =
						(maxBackwardThrust.toDouble() / 100.0 * p.toDouble() + 1).toUInt()
				}
				Log.d("MAGNANI", "setThrustPercentage: ${this.thrust} - ${getThrustPercentage()}")
			}else if (p == 100.toUByte()){
				when (this.direction) {
					EngineDirection.FORWARD -> this.thrust = maxForwardThrust
					EngineDirection.BACKWARD -> this.thrust = maxBackwardThrust
				}
			}else{
				Log.d("MAGNANI", "setThrust(percent): value is over 100%")
				System.exit(-1)
			}
		}
	}
}
class LoadManager{

	var efficiency = UByteArray(101)
	val rangeInfo: FunctionRange = FunctionRange(0, 100, 0, 100)


	// ---  CONSTRUCTOR
	// From points number; put them at const distance, y-value generated
	constructor(pointsNumber: UByte =4u, efficiencyData: ModifiersData = ModifiersData()){
		val range : Int = ( this.rangeInfo.maxX / (pointsNumber.toInt()-1) )
		var efficiencyPoints : UByteArray = Random.nextUBytes(pointsNumber.toInt())

		val knownPoints : Array<FunctionMappingData<UByte>> = Array(efficiencyPoints.size) {
			FunctionMappingData<UByte>(x= (range*it).toUByte(), y= (mapToHundred(efficiencyPoints[it], UByte.MAX_VALUE) +1u).toUByte())
		}
		generateEfficiency(knownPoints, efficiencyData)
	}
	// From points (x,y)
	constructor(knownPoints: Array<FunctionMappingData<UByte>>, modifiers: ModifiersData){
		generateEfficiency(knownPoints, modifiers)
	}

	private fun generateEfficiency(knownPointsUB: Array<FunctionMappingData<UByte>>, modifiers: ModifiersData){
		// Mapping x and y data point into proper data class; casting to Int
		val knownPoints : Array<FunctionMappingData<Int>> = Array(knownPointsUB.size) {
			FunctionMappingData(x=knownPointsUB[it].x.toInt(), y=knownPointsUB[it].y.toInt())
		}
		// check consistency
		for( (i,p) in knownPointsUB.withIndex() ){
			val r : Int = p.x.toInt() - this.rangeInfo.minX
			assert(p.x.toInt() == knownPointsUB[i].x.toInt()) { "Inconsistency between X:${p.x} and index r:${knownPointsUB[i].x}" }
		}
		/*for (p in knownPoints){
			Log.d("MAGNANI", "${p.x} ; ${p.y} ")
		}*/

		val f = generateFunction(knownPoints, this.rangeInfo, modifiers)
		for ( i in 0..100){
			//Log.d(TAG, "generateEfficiency:${i} - ${f[i]}")
			this.efficiency[i] = f[i].y.toUByte()
		}

	}

	// ---  METHODS
}

class Tank (var max_capacity:ULong, var load: ULong){
	var leakage: Boolean = false

	fun refill(qnt:ULong = ULong.MAX_VALUE){
		load += qnt
		if ( load > max_capacity){
			load = max_capacity
		}
	}

	fun isEmpty() : Boolean = this.load == 0uL

	fun percentage() : ULong = this.load*100u/this.max_capacity


	fun tick(consumption: UInt = 0u) {
		var c = consumption
		if (this.leakage){
			c +=(consumption*100u/10u)+1u
		}
		 if (c.toULong() >= this.load){
			this.load = 0u
		}else{
			this.load -= c
		}
	}

}

// ---  ENUM
enum class FuelType {
	STEAM, CHEMICAL, ELECTRIC, FISSION, FUSION
}

enum class EngineDirection{
	FORWARD, BACKWARD
}

