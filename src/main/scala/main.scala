import javax.media.opengl._
import com.jogamp._

object Main {
  def main(args:Array[String]) = {
    try {
      val caps = new GLCapabilities()
      puts("jogl works")
    } catch (exception e) {
      print(e)
    }
  }
}
