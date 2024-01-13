package com.example.monopolydeal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

class CardRenderer(private val context: Context, private val useOpenGL: Boolean = false) :
    GLSurfaceView.Renderer {

    // Load the card texture
    private var cardBitmap: Bitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.illinois_avenue_red
    )

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer
    private val mvpMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    // OpenGL variables
    private var program: Int = 0
    private var positionHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var textureHandle: Int = 0
    private var textureCoordinateHandle: Int = 0

    private var card: Card? = null

    fun setCardTexture(bitmap: Bitmap) {
        cardBitmap = bitmap
    }

    fun setCard(card: Card) {
        this.card = card
    }

    private fun initializeOpenGL() {
        val cardWidth = 0.1f
        val cardHeight = 0.15f

        val cardVertices = mutableListOf<Float>()
        val textureCoordinates = mutableListOf<Float>()

        // Assuming a simple layout for the cards
        card?.let {
            val xOffset = 0.0f
            cardVertices.addAll(
                listOf(
                    -cardWidth / 2 + xOffset, cardHeight / 2, 0.0f,
                    -cardWidth / 2 + xOffset, -cardHeight / 2, 0.0f,
                    cardWidth / 2 + xOffset, -cardHeight / 2, 0.0f,
                    cardWidth / 2 + xOffset, cardHeight / 2, 0.0f
                )
            )

            textureCoordinates.addAll(
                listOf(
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f
                )
            )
        }

        val bb = ByteBuffer.allocateDirect(cardVertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(cardVertices.toFloatArray())
        vertexBuffer.position(0)

        val textureBufferByte = ByteBuffer.allocateDirect(textureCoordinates.size * 4)
        textureBufferByte.order(ByteOrder.nativeOrder())
        textureBuffer = textureBufferByte.asFloatBuffer()
        textureBuffer.put(textureCoordinates.toFloatArray())
        textureBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 aTextureCoord;" +
                    "uniform mat4 uMVPMatrix;" +
                    "varying vec2 vTextureCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vTextureCoord = aTextureCoord;" +
                    "}"

        val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 vTextureCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, vTextureCoord);" +
                    "}"

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        textureCoordinateHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")

        initializeOpenGL()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        card?.let {
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

            GLES20.glVertexAttribPointer(
                textureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false,
                0, textureBuffer
            )
            GLES20.glEnableVertexAttribArray(textureCoordinateHandle)

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

            // Bind the texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            GLES20.glUniform1i(textureHandle, 0)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(textureCoordinateHandle)
        }
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
