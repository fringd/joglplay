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
import java.awt.event.WindowListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.Window

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

/* Introducing the GL2ES demo
 *
 * How to render a triangle using 424 lines of code using the RAW
 * OpenGL ES 2 API.
 * The Programmable pipeline in OpenGL ES 2 are both fast and flexible
 * yet it do take some extra lines of code to setup.
 *
 */



  def main(args:Array[String]):Unit = {

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

    val caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES2))
    val canvas = GLWindow.create(caps)

    val newtCanvas = new NewtCanvasAWT(canvas)
    val frame = new JFrame("RAW GL2ES2 Demo")
    frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
    frame.setSize(300,300)
    frame.add(newtCanvas)
    //add some swing code if you like.
    /* javax.swing.JButton b = new javax.swing.JButton();
    b.setText("Hi");
    frame.add(b); */

    frame.setVisible(true)

    frame.addWindowListener(new WindowAdapter {
      override def windowClosing(e:WindowEvent):Unit = {
        val  w = e.getWindow()
        w.setVisible(false)
        w.dispose()
        System.exit(0)
      }
    });

    canvas.addGLEventListener(MyScene)
    val animator = new FPSAnimator(canvas,60)
    animator.add(canvas)
    animator.start()
  }

}
