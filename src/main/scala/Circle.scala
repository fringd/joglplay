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

  val uvBuffer = {
    val uvCoords = new Array[Float](numberOfPoints * 2) // u,v for each point
    for (i <- 0 until numberOfPoints) {
      val percent = i.toFloat / (numberOfPoints).toFloat
      /* just grab the x and y coordinates */
      val (x,y) = (pointBuffer.get(i*3), pointBuffer.get(i*3+1))

      /* map [-radius,radius] to [0,1] */
      uvCoords(2*i)   = (x/radius + 1.0f) / 2.0f
      uvCoords(2*i+1) = (y/radius + 1.0f) / 2.0f
    }
    new EasyArrayBuffer(uvCoords, gl)
  }

  def draw:Unit = {

    pointBuffer.bind
    gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0)

    // bind the uv coords
    uvBuffer.bind
    gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 0, 0)


    gl.glDrawArrays( GL.GL_TRIANGLE_FAN, 0, numberOfPoints )
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)

  }

  def dispose:Unit = {
    pointBuffer.dispose
    uvBuffer.dispose
  }
}
