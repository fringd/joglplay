import javax.media.opengl._
import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util._
import java.nio.{IntBuffer,FloatBuffer}


object MyScene extends GLEventListener {

  var x = 1.0f

  var indices = new Array[Int](1)

  def display(drawable:GLAutoDrawable) = {
    update()
    render(drawable)
  }

  def init(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL().getGL2ES2()
    gl.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)

    val triangle = Array( -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f,  1.0f, 0.0f)

    gl.glGenBuffers(1, indices, 0)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, indices(0))

    val buffer = FloatBuffer.wrap(triangle)
    buffer.rewind() // is this needed?

    gl.glBufferData(
      GL.GL_ARRAY_BUFFER,
      buffer.capacity() * 4, // 4 bytes per float?
      buffer,
      GL.GL_STATIC_DRAW
    )

    loadShaders(gl)
  }

  def dispose(drawable:GLAutoDrawable) = {
  }

  def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {
  }

  def update() = {
    x = x - 0.002.toFloat
    ()
  }

  def render(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL().getGL2ES2()
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)

    gl.glEnableVertexAttribArray(0)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, indices(0))
    gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0)

    gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3)
    gl.glDisableVertexAttribArray(0)

    drawable.swapBuffers()
  }

  def loadShaders(gl:GL2ES2) = {
    val v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER)
    val f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER)

    var source = scala.io.Source.fromFile("glsl/vertex.glsl")
    val vsrc = source.getLines.mkString("\n")
    source.close()

    gl.glShaderSource(v, 1, Array(vsrc), null)
    gl.glCompileShader(v)

    source = scala.io.Source.fromFile("glsl/fragment.glsl")
    val fsrc = source.getLines.mkString("\n")
    source.close()

    gl.glShaderSource(f, 1, Array(fsrc), null)
    gl.glCompileShader(f)

    val shaderprogram = gl.glCreateProgram()
    gl.glAttachShader(shaderprogram, v)
    gl.glAttachShader(shaderprogram, f)
    gl.glLinkProgram(shaderprogram)
    gl.glValidateProgram(shaderprogram)

    gl.glUseProgram(shaderprogram)
  }
}
