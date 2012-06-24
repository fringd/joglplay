import sbt_
import java.io.File
import net.java.games.jogl._

object Main {
  def main(args:Array[String]) = {
    try {
      System.loadLibrary("jogl")
      print("worked!")
      val caps = new GLCapabilities()
      puts("jogl works")
    } catch (exception e) {
      print(e)
    }
  }
}
