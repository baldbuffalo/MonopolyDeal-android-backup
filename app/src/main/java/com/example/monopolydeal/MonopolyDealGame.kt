package com.example.monopolydeal

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

data class Card(val type: String, val value: Int)

class MonopolyDealGame {
    private val playerHands = mutableListOf<List<Card>>()

    init {
        // Sample initialization with two players
        playerHands.add(listOf(Card("Property", 100), Card("Action", 50)))
        playerHands.add(listOf(Card("Money", 200), Card("Property", 150)))
    }

    fun getPlayerHands(): List<List<Card>> {
        // Return the player hands
        return playerHands.toList()
    }

    fun drawCard() {
        // Implement drawCard logic
    }

    fun playCard(cardToPlay: List<Card>?): Boolean {
        // Implement playCard logic
        return false
    }

    // Add more game logic as needed
}

class OpenGLCardRenderer(private val context: Context, monopolyDealGame: MonopolyDealGame) :
    GLSurfaceView.Renderer {

    private var cards: List<Card> = monopolyDealGame.getPlayerHands().flatten()

    private val mvpMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var program: Int = 0

    private var positionHandle: Int = 0
    private var mvpMatrixHandle: Int = 0

    private val vertexBuffer: FloatBuffer

    private val vertexShaderCode =
        "attribute vec4 vPosition;" +
                "uniform mat4 uMVPMatrix;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    init {
        // Assuming a simple layout for the cards
        val cardWidth = 0.1f
        val cardHeight = 0.15f

        val cardVertices = mutableListOf<Float>()

        cards.forEachIndexed { index, _ ->
            val xOffset = index * (cardWidth + 0.02f) // Adjust the spacing between cards
            cardVertices.addAll(
                listOf(
                    -cardWidth / 2 + xOffset, cardHeight / 2, 0.0f,
                    -cardWidth / 2 + xOffset, -cardHeight / 2, 0.0f,
                    cardWidth / 2 + xOffset, -cardHeight / 2, 0.0f,
                    cardWidth / 2 + xOffset, cardHeight / 2, 0.0f
                )
            )
        }

        val bb = ByteBuffer.allocateDirect(cardVertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(cardVertices.toFloatArray())
        vertexBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

        Matrix.multiplyMM(mvpMatrix, 0, rotationMatrix, 0, mvpMatrix, 0)

        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(
            positionHandle, 3,
            GLES20.GL_FLOAT, false,
            12, vertexBuffer
        )

        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, cards.size * 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        val projectionMatrix = FloatArray(16)

        Matrix.frustumM(
            projectionMatrix, 0,
            -ratio, ratio, -1f, 1f, 3f, 7f
        )

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}
