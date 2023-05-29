package com.example.noideabutkotlin.activities

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
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
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.firebase_login_activity)
		ship = intent.getParcelableExtra<Ship>("ship")!!
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

	}

	fun contentProvider(user : String){
		var resolver : ContentResolver = this.contentResolver
		var client = resolver.acquireContentProviderClient(ContentShip.CONTENT_URI)
		var contentShip = client?.localContentProvider;

		val projection = arrayOf<String>(ContentShip.USER, ContentShip.POSITIONX, ContentShip.POSITIONY)
		val selectionClause: String = ContentShip.USER.toString() + "= ?"
		val s = Array<String>(1) { user }

		var c : Cursor? = this.contentResolver.query(ContentShip.CONTENT_URI, projection, selectionClause,s, null )
		if (c!=null){
			if(c.count >0){
				while(c.moveToNext()){
					var index = c.getColumnIndex(ContentShip.POSITIONX)
					val x = c.getInt(index)

					index = c.getColumnIndex(ContentShip.POSITIONY)
					val y = c.getInt(index)

					index = c.getColumnIndex(ContentShip.SECTORX)
					val sx = c.getInt(index)

					index = c.getColumnIndex(ContentShip.SECTORY)
					val sy = c.getInt(index)
					Log.d("MAGNANI","read [$sx][$sy] $x - $y")
				}
			}else{
				Log.d("TAG", "insieme vuoto")
			}
		}else{
			Log.d("TAG", "Errore nel db o nella query")
		}

	}
}

class FirebaseListener(var activity:FirebaseActivity) : View.OnClickListener{

	var createListener = CreateListener(activity)
	var loginListener = LoginListener(activity)


	override fun onClick(v: View?) {
		var e = activity.findViewById<EditText>(R.id.email)
		var p = activity.findViewById<EditText>(R.id.password)

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
			var p = activity.ship.position
			v.put(ContentShip.USER, e.toString())
			v.put(ContentShip.POSITIONX, p.coordinates['x'].toString())
			v.put(ContentShip.POSITIONY, p.coordinates['y'].toString())
			v.put(ContentShip.SECTORX, p.sector['x'].toString())
			v.put(ContentShip.SECTORY, p.sector['y'].toString())
			Log.d("MAGNANI0", "preinsert ")
			activity.contentResolver.insert(ContentShip.CONTENT_URI,v)
			Log.d("MAGNANI0", "Data saved on disk ")
		}else if(v.toString().contains("app:id/loadButton")){
			activity.contentProvider(e.toString())
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


