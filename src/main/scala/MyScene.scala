import javax.media.opengl._
import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util._


object MyScene extends GLEventListener {

  var x = 1.0.toFloat

  def display(drawable:GLAutoDrawable) = {
    var interval = drawable.getAnimator().getTotalFPSDuration()
    update(interval)
    render(drawable)
  }

  def init(drawable:GLAutoDrawable) = {
  }

  def dispose(drawable:GLAutoDrawable) = {
  }

  def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {
  }

  def update(interval:Long) = {
    x = x - 0.002.toFloat
    ()
  }

  def render(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL().getGL2()
    gl.glClear(GL.GL_COLOR_BUFFER_BIT)
    gl.glBegin(GL.GL_TRIANGLES)
    gl.glColor3f(1, 0, 0)
    gl.glVertex2f(-1, -1)
    gl.glColor3f(0, 1, 0)
    gl.glVertex2f(0, x)
    gl.glColor3f(0, 0, 1)
    gl.glVertex2f(1, -1)
    gl.glEnd
  }

}
