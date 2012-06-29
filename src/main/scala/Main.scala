/**
 * Copyright 2012 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

import javax.media.opengl.GL
import javax.media.opengl.GL2ES2
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLContext
import com.jogamp.newt.opengl.GLWindow

import com.jogamp.newt.awt.NewtCanvasAWT
import com.jogamp.opengl.util.GLArrayDataServer
import com.jogamp.opengl.util.glsl.ShaderCode
import com.jogamp.opengl.util.glsl.ShaderState
import com.jogamp.opengl.util.glsl.ShaderProgram
import com.jogamp.opengl.util._

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.Buffer

import javax.swing.JFrame
import javax.media.opengl._
import javax.media.opengl.awt.GLCanvas

/**
 * <pre>
 *   __ __|_  ___________________________________________________________________________  ___|__ __
 *  //    /\                                           _                                  /\    \\
 * //____/  \__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \____\\
 *  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /
 *   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/
 *  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\
 * /  \____\                       http://jogamp.org  |_|                              /____/  \
 * \  /   "' _________________________________________________________________________ `"   \  /
 *  \/____.                                                                             .____\/
 * </pre>
 *
 * <p>
 * JOGL2 OpenGL ES 2 demo to expose and learn what the RAW OpenGL ES 2 API looks like.
 *
 * Compile, run and enjoy:
   wget http://jogamp.org/deployment/jogamp-test/archive/jogamp-all-platforms.7z
   7z x jogamp-all-platforms.7z
   cd jogamp-all-platforms
   wget https://raw.github.com/xranby/jogl/master/src/test/com/jogamp/opengl/test/junit/jogl/demos/es2/RawGL2ES2demo.java
   javac -cp jar/jogl.all.jar:jar/gluegen-rt.jar RawGL2ES2demo.java
   java -cp jar/jogl.all.jar:jar/gluegen-rt.jar:. RawGL2ES2demo
 * </p>
 *
 *
 * @author Xerxes Rånby (xranby)
 */

object Main {

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
static final String fragmentShader =
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
    private void glMultMatrixf(final FloatBuffer a, final FloatBuffer b, FloatBuffer d) {
        final int aP = a.position();
        final int bP = b.position();
        final int dP = d.position();
        for (int i = 0; i < 4; i++) {
            final float ai0=a.get(aP+i+0*4),  ai1=a.get(aP+i+1*4),  ai2=a.get(aP+i+2*4),  ai3=a.get(aP+i+3*4);
            d.put(dP+i+0*4 , ai0 * b.get(bP+0+0*4) + ai1 * b.get(bP+1+0*4) + ai2 * b.get(bP+2+0*4) + ai3 * b.get(bP+3+0*4) );
            d.put(dP+i+1*4 , ai0 * b.get(bP+0+1*4) + ai1 * b.get(bP+1+1*4) + ai2 * b.get(bP+2+1*4) + ai3 * b.get(bP+3+1*4) );
            d.put(dP+i+2*4 , ai0 * b.get(bP+0+2*4) + ai1 * b.get(bP+1+2*4) + ai2 * b.get(bP+2+2*4) + ai3 * b.get(bP+3+2*4) );
            d.put(dP+i+3*4 , ai0 * b.get(bP+0+3*4) + ai1 * b.get(bP+1+3*4) + ai2 * b.get(bP+2+3*4) + ai3 * b.get(bP+3+3*4) );
        }
    }

    private float[] multiply(float[] a,float[] b){
        float tmp[] = new float[16];
        glMultMatrixf(FloatBuffer.wrap(a),FloatBuffer.wrap(b),FloatBuffer.wrap(tmp));
        return tmp;
    }

    private float[] translate(float[] m,float x,float y,float z){
        float t[] = { 1.0f, 0.0f, 0.0f, 0.0f,
                      0.0f, 1.0f, 0.0f, 0.0f,
                      0.0f, 0.0f, 1.0f, 0.0f,
                      x, y, z, 1.0f };
        return multiply(m, t);
    }

    private float[] rotate(float[] m,float a,float x,float y,float z){
        float s, c;
        s = (float)Math.sin(Math.toRadians(a));
        c = (float)Math.cos(Math.toRadians(a));
        float r[] = {
            x * x * (1.0f - c) + c,     y * x * (1.0f - c) + z * s, x * z * (1.0f - c) - y * s, 0.0f,
            x * y * (1.0f - c) - z * s, y * y * (1.0f - c) + c,     y * z * (1.0f - c) + x * s, 0.0f,
            x * z * (1.0f - c) + y * s, y * z * (1.0f - c) - x * s, z * z * (1.0f - c) + c,     0.0f,
            0.0f, 0.0f, 0.0f, 1.0f };
            return multiply(m, r);
        }

    private void printMatrix(float[] m){
        System.out.println(m[0]+" "+m[1]+" "+m[2]+" "+m[3]+"\n"+
                           m[4]+" "+m[5]+" "+m[6]+" "+m[7]+"\n"+
                           m[8]+" "+m[9]+" "+m[10]+" "+m[11]+"\n"+
                           m[12]+" "+m[13]+" "+m[14]+" "+m[15]+"\n");
    }

/* Introducing the GL2ES demo
 *
 * How to render a triangle using 424 lines of code using the RAW
 * OpenGL ES 2 API.
 * The Programmable pipeline in OpenGL ES 2 are both fast and flexible
 * yet it do take some extra lines of code to setup.
 *
 */
    private double theta=0;
    private double s=0;
    private double c=0;

    private int shaderProgram;
    private int vertShader;
    private int fragShader;
    private int ModelViewProjectionMatrix_location;

    public static void main(String[] s){

        /* This demo are based on the GL2ES2 GLProfile that allows hardware acceleration
         * on both desktop OpenGL 2 and mobile OpenGL ES 2 devices.
         * JogAmp JOGL will probe all the installed libGL.so, libEGL.so and libGLESv2.so librarys on
         * the system to find which one provide hardware acceleration for your GPU device.
         * Its common to find more than one version of these librarys installed on a system.
         * For example on a ARM Linux system JOGL may find
         * Hardware accelerated Nvidia tegra GPU drivers in: /usr/lib/nvidia-tegra/libEGL.so
         * Software rendered Mesa Gallium driver in: /usr/lib/arm-linux-gnueabi/mesa-egl/libEGL.so.1
         * Software rendered Mesa X11 in: /usr/lib/arm-linux-gnueabi/mesa/libGL.so
         * Good news!: JOGL does all this probing for you all you have to do are to ask for
         * the GLProfile you want to use.
         */

        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES2));
        GLWindow canvas = GLWindow.create(caps);

        NewtCanvasAWT newtCanvas = new NewtCanvasAWT(canvas);
        JFrame frame = new JFrame("RAW GL2ES2 Demo");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300,300);
        frame.add(newtCanvas);
        //add some swing code if you like.
        /* javax.swing.JButton b = new javax.swing.JButton();
        b.setText("Hi");
        frame.add(b); */
        frame.setVisible(true);

        canvas.addGLEventListener(new RawGL2ES2demo());
        FPSAnimator animator = new FPSAnimator(canvas,60);
        animator.add(canvas);
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        GL2ES2 gl = drawable.getGL().getGL2ES2();

        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        //Create shaders
        //OpenGL ES retuns a index id to be stored for future reference.
        vertShader = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        fragShader = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);

        //Compile the vertexShader String into a program.
        String[] vlines = new String[] { vertexShader };
        int[] vlengths = new int[] { vlines[0].length() };
        gl.glShaderSource(vertShader, vlines.length, vlines, vlengths, 0);
        gl.glCompileShader(vertShader);

        //Check compile status.
        int[] compiled = new int[1];
        gl.glGetShaderiv(vertShader, GL2ES2.GL_COMPILE_STATUS, compiled,0);
        if(compiled[0]!=0){System.out.println("Horray! vertex shader compiled");}
        else {
            int[] logLength = new int[1];
            gl.glGetShaderiv(vertShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(vertShader, logLength[0], (int[])null, 0, log, 0);

            System.err.println("Error compiling the vertex shader: " + new String(log));
            System.exit(1);
        }

        //Compile the fragmentShader String into a program.
        String[] flines = new String[] { fragmentShader };
        int[] flengths = new int[] { flines[0].length() };
        gl.glShaderSource(fragShader, flines.length, flines, flengths, 0);
        gl.glCompileShader(fragShader);

        //Check compile status.
        gl.glGetShaderiv(fragShader, GL2ES2.GL_COMPILE_STATUS, compiled,0);
        if(compiled[0]!=0){System.out.println("Horray! fragment shader compiled");}
        else {
            int[] logLength = new int[1];
            gl.glGetShaderiv(fragShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(fragShader, logLength[0], (int[])null, 0, log, 0);

            System.err.println("Error compiling the fragment shader: " + new String(log));
            System.exit(1);
        }

        //Each shaderProgram must have
        //one vertex shader and one fragment shader.
        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, vertShader);
        gl.glAttachShader(shaderProgram, fragShader);

        //Associate attribute ids with the attribute names inside
        //the vertex shader.
        gl.glBindAttribLocation(shaderProgram, 0, "attribute_Position");
        gl.glBindAttribLocation(shaderProgram, 1, "attribute_Color");

        gl.glLinkProgram(shaderProgram);

        //Get a id number to the uniform_Projection matrix
        //so that we can update it.
        ModelViewProjectionMatrix_location = gl.glGetUniformLocation(shaderProgram, "uniform_Projection");
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int z, int h) {
    }

    public void display(GLAutoDrawable drawable) {
        // Update variables used in animation
        theta += 0.08;
        s = Math.sin(theta);
        c = Math.cos(theta);

        // Get gl
        GL2ES2 gl = drawable.getGL().getGL2ES2();

        // Set viewport
        //gl.glViewport(0,0,300,300);

        // Clear screen
        gl.glClearColor(1, 0, 1, 1);  //Purple
        gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT | GL2ES2.GL_DEPTH_BUFFER_BIT);

        // Use the shaderProgram that got linked during the init part.
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

        float model_view_projection[];
        float identity_matrix[] = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        };
        model_view_projection =  translate(identity_matrix,0.0f,0.0f, -0.1f);
        model_view_projection =  rotate(model_view_projection,(float)30f*(float)s,1.0f,0.0f,1.0f);

        // Send the final projection matrix to the vertex shader by
        // using the uniform location id obtained during the init part.
        gl.glUniformMatrix4fv(ModelViewProjectionMatrix_location, 1, false, model_view_projection, 0);

        /*
         *  Render a triangle:
         *  The OpenGL ES2 code below basically match this OpenGL code.
         *
         *    gl.glBegin(GL_TRIANGLES);                      // Drawing Using Triangles
         *    gl.glVertex3f( 0.0f, 1.0f, 0.0f);              // Top
         *    gl.glVertex3f(-1.0f,-1.0f, 0.0f);              // Bottom Left
         *    gl.glVertex3f( 1.0f,-1.0f, 0.0f);              // Bottom Right
         *    gl.glEnd();                            // Finished Drawing The Triangle
         */

        float vertices[] = {  0.0f,  1.0f, 0.0f, //Top
                             -1.0f, -1.0f, 0.0f, //Bottom Left
                              1.0f, -1.0f, 0.0f  //Bottom Right
                                              };

        gl.glVertexAttribPointer(0, 3, GL2ES2.GL_FLOAT, false, 0, FloatBuffer.wrap(vertices));
        gl.glEnableVertexAttribArray(0);

        float colors[] = {    1.0f, 0.0f, 0.0f, //Top color (red)
                              0.0f, 0.0f, 0.0f, //Bottom Left color (black)
                              1.0f, 1.0f, 0.0f  //Bottom Right color (yellow)
                                             };
                                             
        gl.glVertexAttribPointer(1, 3, GL2ES2.GL_FLOAT, false, 0, FloatBuffer.wrap(colors));
        gl.glEnableVertexAttribArray(1);

        gl.glDrawArrays(GL2ES2.GL_TRIANGLES, 0, 3); //Draw the vertices as triangle
        
        gl.glDisableVertexAttribArray(0); // Allow release of vertex position memory
        gl.glDisableVertexAttribArray(1); // Allow release of vertex color memory		
    }

    public void dispose(GLAutoDrawable drawable){
        System.out.println("cleanup, remember to release shaders");
        GL2ES2 gl = drawable.getGL().getGL2ES2();
        gl.glUseProgram(0);
        gl.glDetachShader(shaderProgram, vertShader);
        gl.glDeleteShader(vertShader);
        gl.glDetachShader(shaderProgram, fragShader);
        gl.glDeleteShader(fragShader);
        gl.glDeleteProgram(shaderProgram);
        System.exit(0);
    }
}
