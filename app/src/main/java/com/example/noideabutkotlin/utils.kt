package com.example.noideabutkotlin

import android.util.Log

const val TAG = "MAGNANI"


inline fun mapToHundred (x : UByte, max : UByte) : UByte = (x.toULong()*100u /max).toUByte()
inline fun mapToHundred (x : UInt, max : UInt)   : UByte  = (x.toULong()*100u /max).toUByte()
inline fun mapToHundred (x : ULong, max : ULong) : UByte = (x.toDouble()/max.toDouble()*100.0).toUInt().toUByte()

inline fun hundredToDouble(x : UByte) : Double = x.toDouble()/100
inline fun hundredToDouble(x : UInt) : Double = x.toDouble()/100
inline fun hundredToDouble(x : ULong) : Double = x.toDouble()/100

fun generateFunction(knownPoints:Array<FunctionMappingData<Int>>, rangeData:FunctionRange, modifiers: ModifiersData): Array<FunctionMappingData<Int>> {
    
    val range : Int = rangeData.maxX - rangeData.minX
    val function = Array<FunctionMappingData<Int>>(range+1) { FunctionMappingData<Int>(it + rangeData.minX,0) }
    var mean : Int = 0
    // ->   populating efficiency with known points
    //Log.d(TAG, "generateFunction: points number ${knownPoints.size}")
    knownPoints.sort()
    if (modifiers.meanStrength != 0){
        for (i in 0..knownPoints.size){
            mean += knownPoints[i].y
        }
        mean /= knownPoints.size
        for (i in 0.. knownPoints.size){
            knownPoints[i].y += (mean-knownPoints[i].y)*modifiers.meanStrength/100
        }

    }
    val l = MutableList<FunctionMappingData<Int>>(knownPoints.size) {
        FunctionMappingData<Int>(knownPoints[it].x, knownPoints[it].y)
    }
    //Log.d(TAG, "generateFunction: points number ${l.size}")


    // Interpolation on other points
    // ->   Interpolation loop

    for (i in 0..range ){
        if (l.size != 0 && i + rangeData.minX == l[0].x){
            val e =  l.removeFirst()
            assert(e.x == function[i].x) {"generateFunction: inconsistency in points data"}
            function[i].y = e.y
        }
        val yd : Double = interpolate(knownPoints, i+rangeData.minX)
        //Log.d(TAG, "generateFunction: interpolation $i \t$yd")
        var y : Int = yd.toInt()
        if ( modifiers.translation > 0 && y+modifiers.translation > rangeData.maxY ){
            y = rangeData.maxY
        }else if (modifiers.translation < 0 && y+modifiers.translation < rangeData.minY  ){
            y = rangeData.minY
        }
        function[i].x = i
        function[i].y = y
    }

    // SMOOTH
    // TODO sistemare smoothstreng perchè sia percentuale
    if (modifiers.smoothStrength != 0){
        val max : Array<Int>
        val min : Array<Int>
        if (modifiers.smoothType == SmoothType.BOTH || modifiers.smoothType == SmoothType.HIGH_VALUES ){
            max = functionMaximum(function)
            for (i in max.indices){
                for ( j in (-modifiers.smoothWidth)..(modifiers.smoothWidth) ){
                    if (function.isNotEmpty() && i+j >0 && max[i]+j < function.size){
                        function[max[i]+j].y -= modifiers.smoothStrength
                    }
                }
            }
        }
        if (modifiers.smoothType == SmoothType.BOTH || modifiers.smoothType == SmoothType.LOW_VALUES ){
            min = functionMinimum(function)
            for (i in min.indices){
                for ( j in (-modifiers.smoothWidth)..(modifiers.smoothWidth-1) ){
                    if (function.isNotEmpty() && i+j >0 && min[i]+j < function.size) {
                        function[min[i]+j].y += modifiers.smoothStrength
                    }
                }
            }
        }

    }
    return function
}
// https://www.geeksforgeeks.org/lagranges-interpolation/

// function to interpolate the given
// data points using Lagrange's formula
// xi corresponds to the new data point
// whose value is to be obtained
fun  interpolate(funData: Array<FunctionMappingData<Int>>, xi: Int): Double {

    var result: Double = 0.0 // Initialize result

    // Compute individual terms of above formula
    for(i in funData.indices) {
        // keep support variable for cast because error otherwise \_o_/ (1h spent on this)
        val x: Double = funData[i].x.toDouble()
        var term: Double = funData[i].y.toDouble()
        for(j in funData.indices) {
            if (j != i) {
                val xj: Double = funData[j].x.toDouble()
                term *= (xi.toDouble() - xj) / (x - xj)
            }
        }
        // Add current term to result
        result += term
    }
    return result
}

fun functionMinimum (function: Array<FunctionMappingData<Int>>) :  Array<Int> {
    var minimum : Array<Int> = Array(0) {0}
    for (i in 1 until function.size-1){
        if (function[i].y <= function[i-1].y && function[i].y <= function[i+1].y){
            minimum += function[i].x
            //Log.d(TAG, "functionMinimum: ${i}")
        }
    }
    return minimum
}
fun functionMaximum (function: Array<FunctionMappingData<Int>>) :  Array<Int> {
    var maximum : Array<Int> = Array(0) {0}
    for (i in 1 until function.size-1){
        if (function[i].y >= function[i-1].y && function[i].y >= function[i+1].y){
            maximum += function[i].x
            //Log.d(TAG, "functionMaximum: ${i}")
        }
    }
    return maximum
}

// ---  STRUCT
data class FunctionMappingData<T :Comparable<T> >( var x: T, var y: T ): Comparable<FunctionMappingData<T>>{
    override fun compareTo(other: FunctionMappingData<T>) = compareValuesBy(this, other, {it.x}, {it.y})
/*        when{
            x <  other.x ->         return -1
            x == other.x -> when{
                y < other.y ->      return -1
                y == y ->           return 0
                y > other.y ->      return -1
            }
            x > other.x ->          return 1
        }
    }
*/
}

data class FunctionRange( var minX:Int, var maxX:Int, var minY:Int, var maxY:Int )

data class ModifiersData(
        val translation:Int = 0, val jump:UInt=0u, val meanStrength:Int=0,
        val smoothType: SmoothType = SmoothType.BOTH, val smoothStrength:Int = 5, val smoothWidth:Int=3
)
// ---  ENUM
enum class SmoothType{
    LOW_VALUES, HIGH_VALUES, BOTH
}