package com.example.noideabutkotlin.generator

import com.example.noideabutkotlin.mandatory.Engine
import com.example.noideabutkotlin.mandatory.FuelType
import com.example.noideabutkotlin.mandatory.LoadManager
import com.example.noideabutkotlin.mandatory.Tank
import kotlin.random.Random
import kotlin.random.nextULong

fun engineGenerator(updateLevel: UByte = 0u) : Engine{
	val engine : Engine = Engine(fuelTypeGenerator(), tankGenerator(), loadManagerGenerator(), updateLevel )
	return engine
}

inline fun fuelTypeGenerator() : FuelType{
	return FuelType.values().random()
}

fun tankGenerator() : Tank {
	val maxCapacity:ULong = Random.nextULong()
	val load : ULong = (Random.nextULong()%50u) + maxCapacity/2u
	return Tank(maxCapacity, load)
}

fun loadManagerGenerator( numberOfPoints:UByte=4u) : LoadManager{
	return LoadManager(numberOfPoints)
}