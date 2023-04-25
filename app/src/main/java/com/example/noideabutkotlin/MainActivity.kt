
package com.example.noideabutkotlin

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.example.noideabutkotlin.activities.GameActivity

const val RENDERING_EXTRA: UInt = 1000000u

// var generationDistance : ULong = (UInt.MAX_VALUE+ RENDERING_EXTRA).toULong()
var exitButtonCalls : UInt = 0u

class MainActivity : AppCompatActivity() {

    var bundle : Bundle? = null
    val listener : MainListener = MainListener(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MAG_Entry", "MainAct - OnCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

    override fun onStart() {
        super.onStart()
        Log.d("MAG_Entry", "MainAct: OnStart")


    }

    override fun onRestart() {
        Log.d("MAG_Entry", "MainAct: OnRestart")
        super.onRestart()
    }
    // -- LISTENER  CALLs
    fun exitButtonListener(v: View) = listener.exitButtonListener(v)
    fun startClick(v:View) = listener.startClick(v)
}

class MainListener(var activity: MainActivity) {

    var exitButtonCalls : UInt = 0u

    fun exitButtonListener(view: View) {
        if (exitButtonCalls <= 10u) {
            val s = Snackbar.make(view, R.string.exitButton, LENGTH_LONG)
            s.show()
            exitButtonCalls++
        } else {
            val s = Snackbar.make(view, R.string.exitButtonFinal, LENGTH_LONG)
            s.show()
        }
    }

    fun startClick(v:View){

        var intent : Intent = Intent(activity, GameActivity::class.java)
        val REQUEST_CODE : Int = 0
        intent.putExtra("code", "this is the message")
        //intent.putExtra("ship", activity.ship)
        //startActivityForResult(activity, intent, REQUEST_CODE, activity.bundle )
        activity.startActivity(intent)

    }

}