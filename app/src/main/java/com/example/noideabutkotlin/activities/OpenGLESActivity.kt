package com.example.noideabutkotlin.activities

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

fun loadShader(type: Int, shaderCode: String): Int {

	return GLES20.glCreateShader(type).also { shader ->
		GLES20.glShaderSource(shader, shaderCode)
		GLES20.glCompileShader(shader)
	}
}

class OpenGLESActivity: AppCompatActivity(){

	lateinit var gLView : GLSurfaceView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		gLView= MyGLSurfaceView(this)
		setContentView(gLView)
	}
}

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

	private val renderer: MyGLRenderer

	init {
		setEGLContextClientVersion(2)
		renderer = MyGLRenderer()
		setRenderer(renderer)
	}
}

class MyGLRenderer : GLSurfaceView.Renderer {

	private lateinit var mTriangle: Triangle
	private lateinit var mSquare: Square

	override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
		mTriangle = Triangle()
		mSquare = Square()
	}

	override fun onDrawFrame(unused: GL10) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
		Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
		Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

		mTriangle.draw(vPMatrix)
		mTriangle.draw()
	}

	// vPMatrix is an abbreviation for "Model View Projection Matrix"
	private val vPMatrix = FloatArray(16)
	private val projectionMatrix = FloatArray(16)
	private val viewMatrix = FloatArray(16)

	override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
		GLES20.glViewport(0, 0, width, height)
		val ratio: Float = width.toFloat() / height.toFloat()
		Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 7f)
	}
}
// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
var triangleCoords = floatArrayOf(     // in counterclockwise order:
		0.0f, 0.622008459f, 0.0f,      // top
		-0.5f, -0.311004243f, 0.0f,    // bottom left
		0.5f, -0.311004243f, 0.0f      // bottom right
)

class Triangle {

	private var positionHandle: Int = 0
	private var mColorHandle: Int = 0
	val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

	private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
	private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
	private val vertexShaderCode =
		"uniform mat4 uMVPMatrix;" +
				"attribute vec4 vPosition;" +
				"void main() {" +
				"  gl_Position = uMVPMatrix * vPosition;" +
				"}"

	// Use to access and set the view transformation
	private var vPMatrixHandle: Int = 0

	private val fragmentShaderCode =
		"precision mediump float;" +
				"uniform vec4 vColor;" +
				"void main() {" +
				"  gl_FragColor = vColor;" +
				"}"

	private var mProgram: Int

	init {
		val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
		val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

		// create empty OpenGL ES Program
		mProgram = GLES20.glCreateProgram().also {
			GLES20.glAttachShader(it, vertexShader)
			GLES20.glAttachShader(it, fragmentShader)
			GLES20.glLinkProgram(it)
		}
	}

	fun draw() {
		GLES20.glUseProgram(mProgram)
		positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {

			GLES20.glEnableVertexAttribArray(it)
			GLES20.glVertexAttribPointer(
					it,
					COORDS_PER_VERTEX,
					GLES20.GL_FLOAT,
					false,
					vertexStride,
					vertexBuffer
			)
			mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
				GLES20.glUniform4fv(colorHandle, 1, color, 0)
			}
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
			GLES20.glDisableVertexAttribArray(it)
		}

	}

	fun draw(mvpMatrix: FloatArray) { // pass in the calculated transformation matrix
		vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
		GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
		GLES20.glDisableVertexAttribArray(positionHandle)
	}

	private var vertexBuffer: FloatBuffer =
		// (number of coordinate values * 4 bytes per float)
		ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
			// use the device hardware's native byte order
			order(ByteOrder.nativeOrder())

			// create a floating point buffer from the ByteBuffer
			asFloatBuffer().apply {
				// add the coordinates to the FloatBuffer
				put(triangleCoords)
				// set the buffer to read the first coordinate
				position(0)
			}
		}
}

// number of coordinates per vertex in this array
var squareCoords = floatArrayOf(
		-0.5f,  0.5f, 0.0f,      // top left
		-0.5f, -0.5f, 0.0f,      // bottom left
		0.5f, -0.5f, 0.0f,      // bottom right
		0.5f,  0.5f, 0.0f       // top right
)

class Square {

	private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

	// initialize vertex byte buffer for shape coordinates
	private val vertexBuffer: FloatBuffer =
		// (# of coordinate values * 4 bytes per float)
		ByteBuffer.allocateDirect(squareCoords.size * 4).run {
			order(ByteOrder.nativeOrder())
			asFloatBuffer().apply {
				put(squareCoords)
				position(0)
			}
		}

	// initialize byte buffer for the draw list
	private val drawListBuffer: ShortBuffer =
		// (# of coordinate values * 2 bytes per short)
		ByteBuffer.allocateDirect(drawOrder.size * 2).run {
			order(ByteOrder.nativeOrder())
			asShortBuffer().apply {
				put(drawOrder)
				position(0)
			}
		}
}