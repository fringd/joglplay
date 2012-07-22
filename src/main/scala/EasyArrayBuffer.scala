import javax.media.opengl._
import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util._
import java.nio.{IntBuffer,FloatBuffer}

class EasyArrayBuffer(val points:Array[Float], val gl:GL2ES2) {
  private val vboIndex = {
    val vboTmp = new Array[Int](1)
    gl.glGenBuffers(1, vboTmp, 0)
    vboTmp(0)
  }
  gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vboIndex )
  gl.glBufferData( GL.GL_ARRAY_BUFFER, 4*points.length, FloatBuffer.wrap(points), GL.GL_STATIC_DRAW )

  def bind = {
    gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vboIndex )
  }

  def dispose = {
    gl.glDeleteBuffers(1, Array(vboIndex), 0)
  }
}
