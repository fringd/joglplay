import javax.media.opengl._
import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util._
import java.nio.{IntBuffer,FloatBuffer}


object MyScene extends GLEventListener {


  var (shaderProgram, vertShader fragShader) = (0,0,0)

  var ModelViewProjectionMatrix_location = 0

  def display(drawable:GLAutoDrawable) = {
    update()
    render(drawable)
  }

  def init(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL().getGL2ES2()
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities())
    System.err.println("INIT GL IS: " + gl.getClass().getName())
    System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR))
    System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER))
    System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION))

    vertShader = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
    fragShader = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);

    //Compile the vertexShader String into a program.
    val vlines = Array( vertexShader )
    val vlengths = Array( vlines(0).length() )
    gl.glShaderSource(vertShader, vlines.length, vlines, vlengths, 0)
    gl.glCompileShader(vertShader)

    //Check compile status.
    val compiled = Array( 0 )
    gl.glGetShaderiv(vertShader, GL2ES2.GL_COMPILE_STATUS, compiled,0)
    if (compiled[0]!=0) {
      println("Horray! vertex shader compiled")
    } else {
      val logLength = Array(0)
      gl.glGetShaderiv(vertShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0)

      val log = new Array[Byte](logLength(0))
      gl.glGetShaderInfoLog(vertShader, logLength(0), null, 0, log, 0)

      println("Error compiling the vertex shader: " + new String(log))
      System.exit(1)
    }

    //Compile the fragmentShader String into a program.
    val flines = Array( fragmentShader )
    val flengths = Array( flines[0].length() )
    gl.glShaderSource(fragShader, flines.length, flines, flengths, 0)
    gl.glCompileShader(fragShader)

    //Check compile status.
    gl.glGetShaderiv(fragShader, GL2ES2.GL_COMPILE_STATUS, compiled,0)
    if(compiled(0)!=0){
      println("Horray! fragment shader compiled")
    }
    else {
      val logLength = new Array[Int](1)
      gl.glGetShaderiv(fragShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0)

      val log = new Array[Byte](logLength(0))
      gl.glGetShaderInfoLog(fragShader, logLength(0), null, 0, log, 0)

      println("Error compiling the fragment shader: " + new String(log))
      System.exit(1)
    }


    //Each shaderProgram must have
    //one vertex shader and one fragment shader.
    shaderProgram = gl.glCreateProgram()
    gl.glAttachShader(shaderProgram, vertShader)
    gl.glAttachShader(shaderProgram, fragShader)

    //Associate attribute ids with the attribute names inside
    //the vertex shader.
    gl.glBindAttribLocation(shaderProgram, 0, "attribute_Position")
    gl.glBindAttribLocation(shaderProgram, 1, "attribute_Color")

    gl.glLinkProgram(shaderProgram)

    //Get a id number to the uniform_Projection matrix
    //so that we can update it.
    ModelViewProjectionMatrix_location = gl.glGetUniformLocation(shaderProgram, "uniform_Projection")
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
