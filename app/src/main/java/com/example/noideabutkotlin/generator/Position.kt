package com.example.noideabutkotlin.generator

import com.example.noideabutkotlin.mandatory.Position
import com.example.noideabutkotlin.mandatory.Sensors
import kotlin.random.Random
import kotlin.random.nextUInt

fun positionGenerator(): Position {
	return Position()
}

fun sensorGenerator(updateLevel:UByte=0u): Sensors{
	return Sensors(Random.nextUInt() / 10000u,
			Random.nextUInt() / 1000u,
			Random.nextUInt() / 10u
			)
}