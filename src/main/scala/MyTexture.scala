import java.nio.{ByteBuffer, IntBuffer}
import javax.media.opengl._

class MyTexture( val gl:GL2ES2, val data:Array[Integer], val width:Integer, val height:Integer ) {

  val textureNameBuffer = IntBuffer.allocate(1)
  val textureName = {
    gl.glGenTextures(1, textureNameBuffer)

    textureNameBuffer.get(0)
  }


  val dataBuffer = {
    ByteBuffer.wrap(data.map (_.toByte))
  }

  /* initialize */
  {
    bind

    gl.glTexImage2D(
      GL.GL_TEXTURE_2D, 0 /*no mipmap*/, GL.GL_RGBA/*uncompressed format*/,
      width, height, 0/*no border*/, GL.GL_RGBA/*internally also RGBA*/,
      GL.GL_UNSIGNED_BYTE, dataBuffer
    )

    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
  }


  def bind:Unit = {
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureName)
  }

  def dispose:Unit = {
    gl.glDeleteTextures(1, textureNameBuffer)
  }

}
