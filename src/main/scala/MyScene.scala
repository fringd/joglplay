import javax.media.opengl._
import com.jogamp.newt.event.WindowAdapter
import com.jogamp.newt.event.WindowEvent
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util._
import java.nio.{IntBuffer,FloatBuffer}


object MyScene extends GLEventListener {

/* Introducing the OpenGL ES 2 Vertex shader
 *
 * The main loop inside the vertex shader gets executed
 * one time for each vertex.
 *
 *      vertex -> *       uniform data -> mat4 projection = ( 1, 0, 0, 0,
 *      (0,1,0)  / \                                          0, 1, 0, 0,
 *              / . \  <- origo (0,0,0)                       0, 0, 1, 0,
 *             /     \                                        0, 0,-1, 1 );
 *  vertex -> *-------* <- vertex
 *  (-1,-1,0)             (1,-1,0) <- attribute data can be used
 *                        (0, 0,1)    for color, position, normals etc.
 *
 * The vertex shader recive input data in form of
 * "uniform" data that are common to all vertex
 * and
 * "attribute" data that are individual to each vertex.
 * One vertex can have several "attribute" data sources enabled.
 *
 * The vertex shader produce output used by the fragment shader.
 * gl_Position are expected to get set to the final vertex position.
 * You can also send additional user defined
 * "varying" data to the fragment shader.
 *
 * Model Translate, Scale and Rotate are done here by matrix-multiplying a
 * projection matrix against each vertex position.
 *
 * The whole vertex shader program are a String containing GLSL ES language
 * http://www.khronos.org/registry/gles/specs/2.0/GLSL_ES_Specification_1.0.17.pdf
 * sent to the GPU driver for compilation.
 */

  val vertexShader =
"""#ifdef GL_ES
#version 100
precision mediump float;
precision mediump int;
#else
#version 110
#endif
uniform mat4    uniform_Projection;
attribute vec4  attribute_Position;
attribute vec4  attribute_Color;
varying vec4    varying_Color;
void main(void)
{
  varying_Color = attribute_Color;
  gl_Position = uniform_Projection * attribute_Position;
}
"""

  /* Introducing the OpenGL ES 2 Fragment shader
   *
   * The main loop of the fragment shader gets executed for each visible
   * pixel fragment on the render buffer.
   *
   *       vertex-> *
   *      (0,1,-1) /f\
   *              /ffF\ <- This fragment F gl_FragCoord get interpolated
   *             /fffff\                   to (0.25,0.25,-1) based on the
   *   vertex-> *fffffff* <-vertex         three vertex gl_Position.
   *  (-1,-1,-1)           (1,-1,-1)
   *
   *
   * All incomming "varying" and gl_FragCoord data to the fragment shader
   * gets interpolated based on the vertex positions.
   *
   * The fragment shader produce and store the final color data output into
   * gl_FragColor.
   *
   * Is up to you to set the final colors and calculate lightning here based on
   * supplied position, color and normal data.
   *
   * The whole fragment shader program are a String containing GLSL ES language
   * http://www.khronos.org/registry/gles/specs/2.0/GLSL_ES_Specification_1.0.17.pdf
   * sent to the GPU driver for compilation.
   */
  val fragmentShader =
"""#ifdef GL_ES
#version 100
precision mediump float;
precision mediump int;
#else
#version 110
#endif

varying   vec4    varying_Color;
void main (void)
{
  gl_FragColor = varying_Color;
}
"""
  /* Introducing projection matrix helper functions
   *
   * OpenGL ES 2 vertex projection transformations gets applied inside the
   * vertex shader, all you have to do are to calculate and supply a projection matrix.
   *
   * Its recomended to use the com/jogamp/opengl/util/PMVMatrix.java
   * import com.jogamp.opengl.util.PMVMatrix;
   * To simplify all your projection model view matrix creation needs.
   *
   * These helpers here are based on PMVMatrix code and common linear
   * algebra for matrix multiplication, translate and rotations.
   */
  def glMultMatrixf(a:FloatBuffer, b:FloatBuffer, d:FloatBuffer) = {
    val aP = a.position()
    val bP = b.position()
    val dP = d.position()
    for (i <- 0 until 4) {
      val (ai0, ai1, ai2, ai3) = (a.get(aP+i+0*4),  a.get(aP+i+1*4),  a.get(aP+i+2*4),  a.get(aP+i+3*4))
      d.put(dP+i+0*4 , ai0 * b.get(bP+0+0*4) + ai1 * b.get(bP+1+0*4) + ai2 * b.get(bP+2+0*4) + ai3 * b.get(bP+3+0*4) )
      d.put(dP+i+1*4 , ai0 * b.get(bP+0+1*4) + ai1 * b.get(bP+1+1*4) + ai2 * b.get(bP+2+1*4) + ai3 * b.get(bP+3+1*4) )
      d.put(dP+i+2*4 , ai0 * b.get(bP+0+2*4) + ai1 * b.get(bP+1+2*4) + ai2 * b.get(bP+2+2*4) + ai3 * b.get(bP+3+2*4) )
      d.put(dP+i+3*4 , ai0 * b.get(bP+0+3*4) + ai1 * b.get(bP+1+3*4) + ai2 * b.get(bP+2+3*4) + ai3 * b.get(bP+3+3*4) )
    }
    ()
  }

  def multiply(a:Array[Float],b:Array[Float]):Array[Float] = {
    val tmp = new Array[Float](16)
    glMultMatrixf(FloatBuffer.wrap(a),FloatBuffer.wrap(b),FloatBuffer.wrap(tmp))
    return tmp
  }

  def translate(m:Array[Float], x:Float, y:Float, z:Float):Array[Float] = {
    val t = Array( 1.0f, 0.0f, 0.0f, 0.0f,
                  0.0f, 1.0f, 0.0f, 0.0f,
                  0.0f, 0.0f, 1.0f, 0.0f,
                  x, y, z, 1.0f )
    multiply(m, t)
  }

  def rotate(m:Array[Float],a:Float, x:Float, y:Float, z:Float):Array[Float] = {
    var (s,c) = (0.0f, 0.0f)
    s = Math.sin(Math.toRadians(a)).toFloat
    c = Math.cos(Math.toRadians(a)).toFloat
    val r = Array(
      x * x * (1.0f - c) + c,     y * x * (1.0f - c) + z * s, x * z * (1.0f - c) - y * s, 0.0f,
      x * y * (1.0f - c) - z * s, y * y * (1.0f - c) + c,     y * z * (1.0f - c) + x * s, 0.0f,
      x * z * (1.0f - c) + y * s, y * z * (1.0f - c) - x * s, z * z * (1.0f - c) + c,     0.0f,
      0.0f, 0.0f, 0.0f, 1.0f
    )
    multiply(m, r)
  }

  def scale(m:Array[Float], x:Float, y:Float, z:Float):Array[Float] = {
    val r = Array(
      x, 0.0f, 0.0f, 0.0f,
      0.0f, y, 0.0f, 0.0f,
      0.0f, 0.0f, z, 0.0f,
      0.0f, 0.0f, 0.0f, 1.0f
    )
    multiply(m, r)
  }

  def perspective(near:Float, far:Float, verticalViewAngle:Float, aspectRatio:Float) = {
    val d = 1f / (math.tan(verticalViewAngle/2)).toFloat
    Array(
      d/aspectRatio, 0f, 0f, 0f,
      0f, d, 0f, 0f,
      0f, 0f, (near+far)/(near-far), 2*near*far/(near-far),
      0f, 0f, -1f, 0f
    )
  }

  def printMatrix( m:Array[Float] ):Unit = {
    print( ("%f %f %f %f\n"*4).format(m:_*) )
  }


  /* variables */
  var (shaderProgram, vertShader, fragShader) = (0,0,0)

  var ModelViewProjectionMatrix_location = 0

  var (theta, s, c) = (0.0, 0.0, 0.0)


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
    var compiled = Array( 0 )
    gl.glGetShaderiv(vertShader, GL2ES2.GL_COMPILE_STATUS, compiled,0)
    if (compiled(0)!=0) {
      println("Horray! vertex shader compiled")
    } else {
      var logLength = Array(0)
      gl.glGetShaderiv(vertShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0)

      var log = new Array[Byte](logLength(0))
      gl.glGetShaderInfoLog(vertShader, logLength(0), null, 0, log, 0)

      println("Error compiling the vertex shader: " + new String(log))
      System.exit(1)
    }

    //Compile the fragmentShader String into a program.
    val flines = Array( fragmentShader )
    val flengths = Array( flines(0).length() )
    gl.glShaderSource(fragShader, flines.length, flines, flengths, 0)
    gl.glCompileShader(fragShader)

    //Check compile status.
    gl.glGetShaderiv(fragShader, GL2ES2.GL_COMPILE_STATUS, compiled,0)
    if(compiled(0)!=0){
      println("Horray! fragment shader compiled")
    }
    else {
      var logLength = new Array[Int](1)
      gl.glGetShaderiv(fragShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0)

      var log = new Array[Byte](logLength(0))
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

    initVBO(gl)
  }


  var circle:Circle = _
  var circle2:Circle = _

  def initVBO(gl:GL2ES2):Unit = {
    gl.glEnableVertexAttribArray(0)
    gl.glEnableVertexAttribArray(1)

    circle = new Circle(gl, 1.2f, 6)
    circle2 = new Circle(gl, 0.8f, 4)

  }

  def dispose(drawable:GLAutoDrawable) = {
    System.out.println("cleanup, remember to release shaders")
    val gl = drawable.getGL().getGL2ES2()
    gl.glUseProgram(0)
    gl.glDetachShader(shaderProgram, vertShader)
    gl.glDeleteShader(vertShader)
    gl.glDetachShader(shaderProgram, fragShader)
    gl.glDeleteShader(fragShader)
    gl.glDeleteProgram(shaderProgram)
    circle.dispose
    circle2.dispose
  }

  

  def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {
  }

  def update() = {
    theta += 0.08
    s = Math.sin(theta)
    c = Math.cos(theta)
    ()
  }

  def render(drawable:GLAutoDrawable) = {
    val gl = drawable.getGL().getGL2ES2()
    gl.glClearColor(1,0,1,1) //purple
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)

    gl.glUseProgram(shaderProgram);


    /* Change a projection matrix
     * The matrix multiplications and OpenGL ES2 code below
     * basically match this OpenGL ES1 code.
     * note that the model_view_projection matrix gets sent to the vertexShader.
     *
     * gl.glLoadIdentity();
     * gl.glTranslatef(0.0f,0.0f,-0.1f);
     * gl.glRotatef((float)30f*(float)s,1.0f,0.0f,1.0f);
     *
     */

    val identity_matrix = Array(
      1.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 1.0f, 0.0f, 0.0f,
      0.0f, 0.0f, 1.0f, 0.0f,
      0.0f, 0.0f, 0.0f, 1.0f
    )

    var view_projection = translate(identity_matrix,0.0f,0.0f, -3f + c.toFloat)
    view_projection = multiply(perspective(1f, 5f, 1.5f, 1f), view_projection)

    var model1_matrix = translate(identity_matrix, 1.0f, 0.0f, 0.0f)
    model1_matrix =  rotate(model1_matrix,(30f*s).toFloat,0.0f,0.0f,1.0f)

    var model2_matrix = translate(identity_matrix, -1.0f, 0.0f, 0.0f)
    model2_matrix =  rotate(model2_matrix,(30f*c).toFloat,0.0f,0.0f,1.0f)

    // Send the final projection matrix to the vertex shader by
    // using the uniform location id obtained during the init part.
    gl.glUniformMatrix4fv(ModelViewProjectionMatrix_location, 1, false, multiply(view_projection, model1_matrix ), 0)


    // bind the vertices
    circle.draw

    // draw some triangles

    gl.glUniformMatrix4fv(ModelViewProjectionMatrix_location, 1, false, multiply(view_projection, model2_matrix ), 0)

    circle2.draw


  }

}
