package com.example.noideabutkotlin.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.noideabutkotlin.R
import com.example.noideabutkotlin.Ship
import com.example.noideabutkotlin.mandatory.EngineDirection
import com.google.android.material.chip.Chip
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GameActivity() : AppCompatActivity() {

	var listener : GameListener = GameListener(this)
	var ship : Ship = Ship()
	var begin : Boolean = true


	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d("MAG_Entry", "GameCreate - OnCreate")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.game_activity)
		var s: String? = intent.getStringExtra("code")

		var switch = findViewById<TextView>(R.id.engineDirection)
		var enginePlus = findViewById<Button>(R.id.enginePlus)
		var engineMinus = findViewById<Button>(R.id.engineMinus)

		switch.setOnClickListener(listener)
		enginePlus.setOnClickListener(listener)
		engineMinus.setOnClickListener(listener)


	}

	override fun onStart() {
		Log.d("MAGNANI", "onStart: inizio funzione")
		super.onStart()

		val workerPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
		if (begin) {
			workerPool.submit {
				ship.gameCycle(this,workerPool)
			}
		}
		begin = false
	}

}

class GameListener(var activity: GameActivity) : View.OnClickListener {

	override fun onClick(v: View?) {

		Log.d("MAG-Entry", "onClick: ")
		Log.d("MAG-value", "value: ${v?.id}")
		Log.d("MAG-value", "value: ${v?.toString()}")

		v?.id

		if (v != null) {
			if (v.toString().contains("app:id/button}")) {
				Log.d("MAG-value", "bottone")

			} else if (v.toString().contains("app:id/engineDirection")) {
				Log.d("MAG-value", "engineDirection")
				if (activity.ship.engineModule.direction == EngineDirection.BACKWARD) {
					activity.ship.engineModule.direction = EngineDirection.FORWARD
				} else {
					activity.ship.engineModule.direction = EngineDirection.BACKWARD
				}
				Log.d("MAGNANI", "onClick: ${activity.ship.engineModule.direction}")
			} else if (v.toString().contains("app:id/enginePlus")){
				if(activity.ship.engineModule.getThrustPercentage() <100u)
					activity.ship.engineModule.setThrustPercentage((activity.ship.engineModule.getThrustPercentage()+1u).toUByte())
				Log.d("MAGNANI", "EnginePlus to ${activity.ship.engineModule.getThrustPercentage()}")
			} else if (v.toString().contains("app:id/engineMinus")){
				if(activity.ship.engineModule.getThrustPercentage() > 0u)
					activity.ship.engineModule.setThrustPercentage((activity.ship.engineModule.getThrustPercentage()-1u).toUByte())
					Log.d("MAGNANI", "EngineMinus to ${activity.ship.engineModule.getThrustPercentage()}")
			}else{
				Log.d("MAG-value", "premuto altro " )
			}
		}
		activity.ship.updateGraphic(activity)



	}
}