package com.example.noideabutkotlin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.noideabutkotlin.R
import com.example.noideabutkotlin.Ship

class EventActivity : AppCompatActivity() {

	val listener : EventListener = EventListener(this)
	var choice : Int = 0
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.event_activity)

		var title = findViewById<TextView>(R.id.Title)
		var desc = findViewById<TextView>(R.id.Description)
		title.text = intent.getStringExtra("title")
		desc.text = intent.getStringExtra("description")

		val yesButton = findViewById<Button>(R.id.yesButton)
		val stillYesButton = findViewById<Button>(R.id.stillYesButton)

		yesButton.setOnClickListener(listener)
		stillYesButton.setOnClickListener(listener)

		var ship: Ship = intent.getParcelableExtra<Ship>("ship")!!


	}

	override fun finish() {
		var i = Intent()
		i.putExtra("choice", this.choice)
		setResult(RESULT_OK, i)
		super.finish()
	}
}

class EventListener(var activity: EventActivity) : View.OnClickListener, AppCompatActivity(){

	override fun onClick(v: View?) {
		if (v.toString().contains("app:id/yesButton")){
			activity.choice = 0
		} else if (v.toString().contains("app:id/stillYesButton")){
			activity.choice = 1
		}else{
			Log.d("MAGNANI", "onClick: impossible bracket")
		}
		activity.finish()
	}

}