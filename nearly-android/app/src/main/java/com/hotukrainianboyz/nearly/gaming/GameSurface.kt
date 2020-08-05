package com.hotukrainianboyz.nearly.gaming

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// constructor called from xml
class GameSurface(context: Context, attrs: AttributeSet): GLSurfaceView(context, attrs) {

    private val mEngine = MicroEngine()
    private val mRenderer = Renderer(mEngine)
    private lateinit var mGame: IGame
    private var isGameRunning = false

    init{
        setEGLConfigChooser(8, 8, 8, 0, 16, 0)
        setEGLContextClientVersion(3)
        setRenderer(mRenderer)
    }

    fun attachGame(g: IGame){
        mGame = g
        mGame.attachUiUpdateFunction { setMatrix(it) }
    }

    // marking as suspend to make sure it runs asynchronously
    suspend fun launchGame(){
        if(!this::mGame.isInitialized){
            Log.d(TAG, "Game was not attached while trying to run it")
            return
        }
        // separate boolean for later options to stop the game
        isGameRunning = true
        while(isGameRunning){
            mGame.step()
        }
    }

    private fun setMatrix(matrix: Array<ByteArray>) {
        mRenderer.matrix = matrix
    }

    private class Renderer(private val engine: MicroEngine): GLSurfaceView.Renderer{

        var matrix = arrayOf(byteArrayOf(0,1,1))

        override fun onDrawFrame(gl10: GL10) {
            engine.render2DMatrix(matrix)
        }

        override fun onSurfaceChanged(gl10: GL10, w: Int, h: Int) {
            engine.resize(w,h)
        }

        override fun onSurfaceCreated(gl10: GL10, p1: EGLConfig) {
            engine.init()
        }
    }

    companion object{
        private const val TAG = "NativeGameSurface"
    }
}