package com.example.noideabutkotlin.activities

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.noideabutkotlin.ContentShip
import com.example.noideabutkotlin.R
import com.example.noideabutkotlin.Ship
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.*

//  FUNCTIONs---------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
fun accessDB(auth: FirebaseUser?, ship: Ship) {
	if (auth == null) return
	val database = FirebaseDatabase.getInstance()
	val ref = database.reference
	val listener = DbListener()
	var hash = HashMap<String, Ship>()
	hash["ship"] = ship
	ref.child("ship").updateChildren(hash as Map<String, Any>)
		.addOnCompleteListener(listener)

}
//  --- CLASSes-------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------
class FirebaseActivity : AppCompatActivity() {

	val auth : FirebaseAuth = FirebaseAuth.getInstance()
	val listener = FirebaseListener(this)
	lateinit var ship : Ship
	lateinit var px : String
	lateinit var py : String
	lateinit var sx : String
	lateinit var sy : String
	lateinit var b : Bundle

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.firebase_login_activity)
		ship = intent.getParcelableExtra<Ship>("ship")!!
		px = intent.getStringExtra("px")!!
		py = intent.getStringExtra("py")!!
		sx = intent.getStringExtra("sx")!!
		sy = intent.getStringExtra("sy")!!
		Log.d("COORD", "onCreate: [${sx}][${sy} ${px} - ${py}]")

		var e = findViewById<EditText>(R.id.email)
		var p = findViewById<EditText>(R.id.password)
		var r = findViewById<TextView>(R.id.result)

		if (savedInstanceState != null) {
			b = savedInstanceState
		}else{
			b = Bundle()
		}

		if (b.containsKey("user")){
			e.setText(b.getString("user"))
		}
		if(b.containsKey("password")){
			p.setText(b.getString("password"))
		}
		if (b.containsKey("result")){
			r.text = b.getString("result")
		}

	}
	override fun onResume() {
		super.onResume()
		var login = findViewById<Button>(R.id.login)
		login.setOnClickListener(listener)

		var register = findViewById<Button>(R.id.create)
		register.setOnClickListener(listener)

		var save = findViewById<Button>(R.id.saveButton)
		var load = findViewById<Button>(R.id.loadButton)
		save.setOnClickListener(listener)
		load.setOnClickListener(listener)

		listener.populate()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)

		var e = findViewById<EditText>(R.id.email)
		var p = findViewById<EditText>(R.id.password)
		var r = findViewById<TextView>(R.id.result)
		outState.putString("user", e.text.toString())
		outState.putString("password", p.text.toString())
		outState.putString("result", r.text.toString())
	}

	override fun finish() {
		var i = Intent()
		setResult(RESULT_OK,i)
		i.putExtra("px", px)
		i.putExtra("py", py)
		i.putExtra("sx", sx)
		i.putExtra("sy", sy)
		super.finish()
	}
}

class FirebaseListener(var activity:FirebaseActivity) : View.OnClickListener{

	var createListener = CreateListener(activity)
	var loginListener = LoginListener(activity)

	fun populate(){
		Log.d("MAGNANI", "populate: insert of user jojo")
		var v = ContentValues()
		v.put(ContentShip.USER, "jojo")
		v.put(ContentShip.POSITIONX, "42")
		v.put(ContentShip.POSITIONY, "42")
		v.put(ContentShip.SECTORX, "100")
		v.put(ContentShip.SECTORY, "101")

		activity.contentResolver.insert(ContentShip.CONTENT_URI,v)
	}

	override fun onClick(v: View?) {
		var e = activity.findViewById<EditText>(R.id.email)
		var p = activity.findViewById<EditText>(R.id.password)
		var r = activity.findViewById<TextView>(R.id.result)
		if (v.toString().contains("app:id/create")) {
			if (e.text.toString().isEmpty() || p.text.toString().isEmpty()){
				Log.d("MAGNANI", "a field is empty")
				return
			}
			var email = e.text.toString()
			var password = p.text.toString()

			activity.auth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(activity, createListener)
		}else if (v.toString().contains("app:id/login")){
			if (e.text.toString().isEmpty() || p.text.toString().isEmpty()){
				Log.d("MAGNANI", "a field is empty")
				return
			}
			activity.auth.signInWithEmailAndPassword(e.text.toString(), p.text.toString())
				.addOnCompleteListener(loginListener)
		}else if(v.toString().contains("app:id/saveButton")){
			if (e.text.toString().isEmpty()) {
				Log.d("MAGNANI", "USER field is empty")
				return
			}
			Log.d("MAGNANI0", "savings... ")
			var v = ContentValues()

			v.put(ContentShip.USER, e.toString())
			v.put(ContentShip.POSITIONX, activity.px)
			v.put(ContentShip.POSITIONY, activity.py)
			v.put(ContentShip.SECTORX, activity.sx)
			v.put(ContentShip.SECTORY, activity.sy)

			activity.contentResolver.insert(ContentShip.CONTENT_URI,v)
			r.text = "Data saved on disk [${activity.sx}][${activity.sy}] ${activity.px} - ${activity.py}"
		}else if(v.toString().contains("app:id/loadButton")){
			var projection : Array<String> = arrayOf(ContentShip.POSITIONX, ContentShip.POSITIONY, ContentShip.SECTORX, ContentShip.SECTORY)
			var selectionClause = ContentShip.USER + "=?"
			var user : Array<String> = arrayOf(e.toString())

			var c = activity.contentResolver.query(ContentShip.CONTENT_URI,projection, selectionClause, user, null )

			if (c != null){
				if (c.count >0){
					while (c.moveToNext()){
						var index : Int = c.getColumnIndex(ContentShip.POSITIONX)
						var px = c.getString(index)
						index = c.getColumnIndex(ContentShip.POSITIONY)
						var py = c.getString(index)
						index = c.getColumnIndex(ContentShip.SECTORX)
						var sx = c.getString(index)
						index = c.getColumnIndex(ContentShip.SECTORY)
						var sy = c.getString(index)
						Log.d("MAGNANI", "load of ${e.text}: [$sx][$sy] $px - $py ")

						val p = activity.ship.position
						activity.px = px
						activity.py= py
						activity.sx = sx
						activity.sy = sx
						r.text = "${e.text} visited: [$sx][$sy] $px - $py "
						//activity.finish()
					}
				}else Log.d("MAGNANI", "insieme vuoto")
			}else Log.d("MAGNANI", "Errore nel db o nella query")
		}else{
			Log.d("MAGNANI", "onClick: Impossible branch")
		}
	}
}



class LoginListener (var activity:FirebaseActivity) :OnCompleteListener<AuthResult>{


	override fun onComplete(task: Task<AuthResult>) {
		var status = activity.findViewById<TextView>(R.id.result)
		if (task.isSuccessful) {
			// Sign in success, update UI with the signed-in user's information
			Log.d("FBAPP", "Autenticazione a buon fine!")
			status.text = "Authentication successful !"
			Toast.makeText(activity, "Benvenuto " + activity.auth.getCurrentUser()!!.getEmail(), Toast.LENGTH_LONG)
				.show()
			activity.intent.getParcelableExtra<Ship>("ship")
				?.let { accessDB(activity.auth.getCurrentUser(), it) }
		} else {
			// If sign in fails, display a message to the user.
			Log.d("FBAPP", "Id o PW errati " + task.exception!!.message)
			status.text = "Authentication failed"
			activity.intent.getParcelableExtra<Ship>("ship")?.let { accessDB(null, it) }
		}
	}
}

class CreateListener(var activity:FirebaseActivity) :OnCompleteListener<AuthResult>{

	override fun onComplete(task: Task<AuthResult>) {
		var status = activity.findViewById<TextView>(R.id.result)
		if (task.isSuccessful) {
			Log.d("FBAPP", "Utente registrato")
			var user = activity.auth.getCurrentUser()
			status.text = "Utente registrato: " + user!!.getEmail()
		} else {
			Log.d("FBAPP", "Utente NON registrato")
			status.text = ("Errore nella registrazione. ")
		}
	}
}

class DbListener () : OnCompleteListener<Void>, AppCompatActivity(){
	override fun onComplete(task: Task<Void>) {
		var result = findViewById<TextView>(R.id.result)
		if (task.isSuccessful) {
			result.text = "Creazione/scrittura DB riuscita"
			//leggiDB(ref)
		} else {
			task.getException()?.message?.let { Log.e("FBAPP", it) }
			result.text="Creazione/Scrittura DB fallita..."
		}
	}

}


