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
		super.onCreate(savedInstanceState)
		setContentView(R.layout.game_activity)

		if (savedInstanceState != null) {
			b = savedInstanceState
		}else{
			b = Bundle()
		}
		if (b.containsKey("ship")){
			this.ship = b.getParcelable<Ship>("ship")!!
			this.ship.pause = false
		}
		if (b.containsKey("begin")){
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
		var share = findViewById<Button>(R.id.share)
		event.setOnClickListener(listener)
		share.setOnClickListener(listener)
	}

	override fun onStart() {
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
		super.onResume()
		this.ship.pause = false
	}

	override fun onPause() {
		super.onPause()
		this.ship.pause = true
		this.ship.running = false
		this.begin = true
	}
	override fun onStop() {
		super.onStop()
		this.ship.pause = true

	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		ship.pause = true
		ship.running = false
		outState.putParcelable("ship",ship)
		outState.putBoolean("begin", true)
	}
	override fun onDestroy() {
		super.onDestroy()

	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d("MAGNANI", "onActivityResult: INIZIO")
		if(requestCode==1 && resultCode == RESULT_OK){
			var c = data?.getIntExtra("choice", -1)
			Log.d("MAGNANI", "onActivityResult: ${c}")
			when (c) {
				0 -> { ship.engineModule.tank.refill(100000u)}
				1 -> {
					ship.engineModule.tank.refill(50000u)
				}
				else -> {
					Log.d("MAGNANI", "onActivityResult: Impossible bracket")
					exitProcess(1)
				}
			}
		} else if(requestCode==2 && resultCode == RESULT_OK){
			var s = data?.getParcelableExtra<Ship>("ship")
			/*Log.d("COORD", "onActivityResult: OLD [${ship.position.sector['x']}][${ship.position.sector['y']}] ${ship.position.coordinates['x']} - ${ship.position.coordinates['y']}")
			Log.d("COORD", "onActivityResult: NEW [${s!!.position.sector['x']!!}][${s.position.sector['y']!!}] ${s.position.coordinates['x']} - ${s.position.coordinates['y']}")
			ship.position.coordinates['x'] = s!!.position.coordinates['x']!!
			ship.position.coordinates['y'] = s.position.coordinates['y']!!
			ship.position.sector['x'] = s.position.sector['x']!!
			ship.position.sector['y'] = s.position.sector['y']!!
			 */
		}
	}
}

class GameListener(var activity: GameActivity) : View.OnClickListener, AppCompatActivity() {

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
			}else if (v.toString().contains("app:id/event")) {
				var intent = Intent(activity, EventActivity::class.java)
				val REQUEST_CODE = 0
				this.activity.ship.pause = true
				intent.putExtra("title", "Refill tank")
				intent.putExtra("description", R.string.eventResource)
				intent.putExtra("ship", activity.ship)
				//activity.startActivity(intent)
				activity.startActivityForResult(intent, 1)

			}else if (v.toString().contains("app:id/share")){

				var intent : Intent = Intent(activity, FirebaseActivity::class.java)
				intent.putExtra("ship", activity.ship)
				activity.startActivityForResult(intent, 2)

			}else{
				Log.d("MAG-value", "premuto altro " )
			}
		}
		activity.ship.updateGraphic(activity)



	}
}