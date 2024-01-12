package com.example.monopolydeal

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.monopolydeal.com.example.monopolydeal.OpenGLCardRenderer

class MonopolyDealGameActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: OpenGLCardRenderer
    private lateinit var monopolyDealGame: MonopolyDealGame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.monopolydealgame)

        // Initialize Monopoly Deal game components
        monopolyDealGame = MonopolyDealGame()

        // Initialize OpenGL renderer and set it to GLSurfaceView
        glSurfaceView = findViewById(R.id.glSurfaceView)
        glRenderer = OpenGLCardRenderer(this, monopolyDealGame)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(glRenderer)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
}
