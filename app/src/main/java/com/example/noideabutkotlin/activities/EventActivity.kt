package com.example.noideabutkotlin.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.noideabutkotlin.R

class EventActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val title = findViewById<TextView>(R.id.Title)
		val desc = findViewById<TextView>(R.id.Description)
		title.text = "ciao"
		desc.text = "descrizione dell'evento"
	}
}