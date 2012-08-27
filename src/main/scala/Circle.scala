import javax.media.opengl._
import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util._
import java.nio.{IntBuffer,FloatBuffer}

class Circle( val gl:GL2ES2, val radius:Float, val sides:Integer ) {

  val numberOfPoints = sides+2

  val pointBuffer = {
    val vertices = new Array[Float]((numberOfPoints)*3)
    vertices(0) = 0
    vertices(1) = 0
    vertices(2) = 0
    for (i <- 0 to sides) {
      val angle = 2 * math.Pi * i.toFloat / sides.toFloat
      val (x,y) = (radius * Math.cos(angle), radius * Math.sin(angle))
      vertices(i*3) = x.toFloat
      vertices(i*3+1) = y.toFloat
      vertices(i*3+2) = 0
    }
    new EasyArrayBuffer(vertices, gl)
  }

  val colorBuffer = {
    val colors = new Array[Float](numberOfPoints * 3) // x,y,z r,g,b so same number of floats
    for (i <- 0 until numberOfPoints) {
      val percent = i.toFloat / (numberOfPoints).toFloat
      colors(3*i) = percent
      colors(3*i+1) = 1.0f - percent
      colors(3*i+2) = math.abs(1.0f - 2.0f*percent)
    }
    new EasyArrayBuffer(colors, gl)
  }

  def draw:Unit = {
    pointBuffer.bind
    gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0)

    // bind the colors
    colorBuffer.bind
    gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0)
    gl.glDrawArrays( GL.GL_TRIANGLE_FAN, 0, numberOfPoints )
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)

  }

  def dispose:Unit = {
    pointBuffer.dispose
    colorBuffer.dispose
  }
}
