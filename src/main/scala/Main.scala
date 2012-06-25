import javax.media.opengl._
import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util._


object Main {

  def main(args:Array[String]) = {
    val glp = GLProfile.get(GLProfile.GL2)
    val caps = new GLCapabilities(glp)

    val window = GLWindow.create(caps);
    window.setSize(300, 300);
    window.setVisible(true);
    window.setTitle("NEWT Window Test");

    window.addGLEventListener(MyScene)
    
    var animator:FPSAnimator = null

    object MyWindowAdapter extends WindowAdapter {
      override def windowDestroyNotify(arg0:WindowEvent) {
        if ((arg0.getEventType & WindowEvent.EVENT_WINDOW_DESTROYED) > 0) {
          animator.stop()
          System.exit(0)
          ()
        }
      }
    }
    window.addWindowListener(MyWindowAdapter)


    print("jogl works\n")
    animator = new FPSAnimator(window, 30)
    animator.add(window)
    animator.start()
    ()
  }
}
