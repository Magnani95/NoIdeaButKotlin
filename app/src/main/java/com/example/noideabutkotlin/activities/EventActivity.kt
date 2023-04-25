package com.example.noideabutkotlin.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.noideabutkotlin.R
import com.example.noideabutkotlin.Ship

class EventActivity : AppCompatActivity() {

	val listener : EventListener = EventListener(this)

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

		val yes = findViewById<Button>(R.id.yesButton)
		val stillYes = findViewById<Button>(R.id.stillYesButton)
		yes.setOnClickListener(listener)
		stillYes.setOnClickListener(listener)
	}

	override fun onStart() {
		super.onStart()


	}
}

class EventListener(activity: EventActivity)  : View.OnClickListener{
	override fun onClick(v: View?) {


	}

}