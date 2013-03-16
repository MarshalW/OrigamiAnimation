package com.example.origami;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-16
 * Time: 下午4:29
 * To change this template use File | Settings | File Templates.
 */
public class TitleMesh {
    private RectF rect;

    private Bitmap texture;

    private Shader shader;

    private FloatBuffer vertexBuffer, textureCoordBuffer;

    private int[] textureIds;

    public TitleMesh() {
        //设置buffer
        //4个顶点，每个3个坐标，每个float 4个字节
        vertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        //4个顶点，每个顶点2个坐标，每个float 4个字节
        textureCoordBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureCoordBuffer.put(new float[]{
                0, 0,
                0, 1,
                1, 0,
                1, 1
        });

        //设置shader
        String vertexShader =
                "        uniform mat4 uProjectionM;\n" +
                        "attribute vec3 aPosition;\n" +
                        "attribute vec4 aColor;\n" +
                        "attribute vec2 aTextureCoord;\n" +
                        "varying vec4 vColor;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "  gl_Position = uProjectionM * vec4(aPosition, 1.0);\n" +
                        "  vColor = aColor;\n" +
                        "  vTextureCoord = aTextureCoord;\n" +
                        "}\n";
        String fragmentShader =
                "        precision mediump float;\n" +
                        "varying vec4 vColor;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform sampler2D sTexture;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n";

        shader = new Shader();
        shader.setProgram(vertexShader, fragmentShader);

    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }

    public void setTexture(Bitmap texture) {
        this.texture = texture;
    }

    public void draw(float[] projectionMatrix) {
        this.setData();

        this.shader.useProgram();
        GLES20.glUniformMatrix4fv(shader.getHandle("uProjectionM"), 1, false, projectionMatrix, 0);

        int aPosition = this.shader.getHandle("aPosition");
        int aColor = this.shader.getHandle("aColor");
        int aTextureCoord = this.shader.getHandle("aTextureCoord");

        //设置标题顶点
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 0,
                vertexBuffer);
        glEnableVertexAttribArray(aPosition);

        glEnable(GL_TEXTURE_2D);

        GLES20.glVertexAttribPointer(aTextureCoord, 2, GLES20.GL_FLOAT, false,
                0, textureCoordBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoord);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

//        Log.d("origami.new","draw title mesh "+this);

    }

    private void setData() {
        //设置顶点
        vertexBuffer.clear();
        vertexBuffer.put(new float[]{
                rect.left, rect.top, 0,
                rect.left, rect.bottom, 0,
                rect.right, rect.top, 0,
                rect.right, rect.bottom, 0
        });
        vertexBuffer.position(0);

        textureCoordBuffer.position(0);

        if(this.textureIds==null){
            this.textureIds=new int[1];
            GLES20.glGenTextures(1, this.textureIds, 0);
        }
    }
}
