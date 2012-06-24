import sbt._
import java.io.File

class SunshineProject(info:ProjectInfo) extends DefaultProject(info)
{
  // tells sbt which class to run
  override def mainClass:Option[String] = Some("com.googlecode.sunshine.Sunshine")

  def nativeJOGL:String = {
    var os = System.getProperty("os.name").toLowerCase
    var arch = System.getProperty("os.arch").toLowerCase

    // this is to match the JOGL builds
    if (arch.matches("i386")) arch = "i586"

    if (os.contains("windows")) {
      os = "windows"
      arch = "i586"
    }
    println("OS: %s".format(os))
    println("JOGL Path: %s".format("./lib/jogl-2.0-%s-%s".format(os, arch)))

    "./lib/jogl-2.0-%s-%s".format(os, arch)
  }

  override def fork = forkRun("-Djava.library.path=%s".format(nativeJOGL) :: Nil)
}
