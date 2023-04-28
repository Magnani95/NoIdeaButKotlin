package com.example.noideabutkotlin.activities

import android.content.Intent
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
import java.lang.System.exit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class GameActivity() : AppCompatActivity() {

	var listener : GameListener = GameListener(this)
	var ship : Ship = Ship()
	var begin : Boolean = true
	lateinit var b : Bundle
	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d("MAGNANI", "GameCreate - OnCreate")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.game_activity)
		Log.d("MAGNANI", "onCreate: bundle ${savedInstanceState}")

		if (savedInstanceState != null) {
			b = savedInstanceState
		}else{
			b = Bundle()
		}
		if (b.containsKey("ship")){
			Log.d("MAGNANI", "onCreate: ship parcelize")
			this.ship = b.getParcelable<Ship>("ship")!!
			this.ship.pause = false
		}
		if (b.containsKey("begin")){
			Log.d("MAGNANI", "onCreate: begin")
			this.begin = b.getBoolean("begin")
		}



		var switch = findViewById<TextView>(R.id.engineDirection)
		var enginePlus = findViewById<Button>(R.id.enginePlus)
		var engineMinus = findViewById<Button>(R.id.engineMinus)

		switch.setOnClickListener(listener)
		enginePlus.setOnClickListener(listener)
		engineMinus.setOnClickListener(listener)


		var turnClockwise = findViewById<Button>(R.id.angleUp)
		var turnCounterclockwise = findViewById<Button>(R.id.angleDown)

		turnClockwise.setOnClickListener(listener)
		turnCounterclockwise.setOnClickListener(listener)

		var event = findViewById<Button>(R.id.event)

		event.setOnClickListener(listener)
	}

	override fun onStart() {
		Log.d("MAGNANI", "onStart:")
		super.onStart()
		this.ship.running = true
		val workerPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
		if (begin) {
			workerPool.submit {
				ship.gameCycle(this,workerPool)
			}
		}
		begin = false
		this.ship.pause= false
		ship.updateGraphic(this)
	}

	override fun onResume() {
		Log.d("Magnani", "onResume: ")
		super.onResume()
		this.ship.pause = false
	}

	override fun onPause() {
		super.onPause()
		Log.d("Magnani", "onPause: ")
		this.ship.pause = true
		this.ship.running = false
		this.begin = true
	}
	override fun onStop() {
		super.onStop()
		Log.d("Magnani", "onStop: ")
		this.ship.pause = true

	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Log.d("MAGNANI", "onSave..: inserting value")
		ship.pause = true
		ship.running = false
		outState.putParcelable("ship",ship)
		outState.putBoolean("begin", true)
		outState.putInt("conta", 5)
	}
	override fun onDestroy() {
		super.onDestroy()

	}
}

class GameListener(var activity: GameActivity) : View.OnClickListener, AppCompatActivity() {

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d("MAGNANI", "onActivityResult: INIZIO")
		if(requestCode==1 && resultCode == RESULT_OK){
			var c = data?.getIntExtra("choice", -1)
			Log.d("MAGNANI", "onActivityResult: ${c}")
			when (c) {
				0 -> { activity.ship.engineModule.tank.refill(100000u)}
				1 -> {
					activity.ship.engineModule.tank.refill(50000u)
				}
				else -> {
					Log.d("MAGNANI", "onActivityResult: Impossible bracket")
					exitProcess(1)
				}
			}
		}
	}

	override fun onClick(v: View?) {

		Log.d("MAG-Entry", "onClick: ")
		Log.d("MAG-value", "value: ${v?.id}")
		Log.d("MAG-value", "value: ${v?.toString()}")

		v?.id

		if (v != null) {
			if (v.toString().contains("app:id/button}")) {

			} else if (v.toString().contains("app:id/engineDirection")) {
				if (activity.ship.engineModule.direction == EngineDirection.BACKWARD) {
					activity.ship.engineModule.direction = EngineDirection.FORWARD
				} else {
					activity.ship.engineModule.direction = EngineDirection.BACKWARD
				}
			} else if (v.toString().contains("app:id/enginePlus")){
				if(activity.ship.engineModule.getThrustPercentage() <100u)
					activity.ship.engineModule.setThrustPercentage((activity.ship.engineModule.getThrustPercentage()+1u).toUByte())
			} else if (v.toString().contains("app:id/engineMinus")){
				if(activity.ship.engineModule.getThrustPercentage() > 0u)
					activity.ship.engineModule.setThrustPercentage((activity.ship.engineModule.getThrustPercentage()-1u).toUByte())

			} else if (v.toString().contains("app:id/angleUp")) {
				activity.ship.position.directionAngle = (activity.ship.position.directionAngle +1u) % 360u

			} else if (v.toString().contains("app:id/angleDown")) {
				if (activity.ship.position.directionAngle == 0u) {
					activity.ship.position.directionAngle = 359u
				} else {
					activity.ship.position.directionAngle--
				}
			}else if (v.toString().contains("app:id/event")){
				var intent = Intent(activity, EventActivity::class.java)
				val REQUEST_CODE = 0
				this.activity.ship.pause = true
				intent.putExtra("title", "Refill tank")
				intent.putExtra( "description", "found some rocks")
				intent.putExtra("ship", activity.ship)
				//activity.startActivity(intent)
				activity.startActivityForResult(intent, 1)
			}else{
				Log.d("MAG-value", "premuto altro " )
			}
		}
		activity.ship.updateGraphic(activity)



	}
}