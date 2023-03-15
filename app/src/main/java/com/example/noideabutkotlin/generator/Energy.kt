package com.example.noideabutkotlin.generator

import android.util.Log
import com.example.noideabutkotlin.mandatory.Battery
import com.example.noideabutkotlin.mandatory.Energy
import com.example.noideabutkotlin.mandatory.SolarPanels

fun energyGenerator(updateLevel: UByte=0u) : Energy{
	Log.d("MAGNANI", "generate energy")
	return Energy(batteryGenerator(), solarPanelsGenerator() )

}

fun batteryGenerator (updateLevel: UByte=0u ) : Battery{
	return Battery(5000u, 4000u, 500u)
}

fun solarPanelsGenerator ( updateLevel: UByte=0u ) :  SolarPanels{
	return SolarPanels(1000u, 100u)
}
