package com.example.monopolydeal

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import androidx.recyclerview.widget.RecyclerView
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLCardRenderer(private val context: Context, recyclerView: RecyclerView) :
    GLSurfaceView.Renderer {

    private val cardAdapter: CardAdapter

    private val mvpMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var program: Int = 0

    private var positionHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var textureHandle: Int = 0
    private var textureCoordinateHandle: Int = 0

    private lateinit var textureBitmap: Bitmap
    private lateinit var textureBuffer: FloatBuffer

    private lateinit var vertexBuffer: FloatBuffer

    private val cards = mutableListOf<Card>()  // Placeholder for the missing cards variable

    private val vertexShaderCode =
        "attribute vec4 vPosition;" +
                "attribute vec2 aTextureCoord;" +
                "uniform mat4 uMVPMatrix;" +
                "varying vec2 vTextureCoord;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "  vTextureCoord = aTextureCoord;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vTextureCoord;" +
                "void main() {" +
                "  gl_FragColor = texture2D(uTexture, vTextureCoord);" +
                "}"

    init {
        cardAdapter = CardAdapter(context, cards)
        recyclerView.adapter = cardAdapter
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
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        textureCoordinateHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")

        // Initialize your vertex buffer, texture buffer, and other OpenGL-related setup here.
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

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, cards.size * 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle)
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
