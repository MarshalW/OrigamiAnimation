package com.example.origami;

import android.opengl.GLES20;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-11
 * Time: 上午10:45
 * To change this template use File | Settings | File Templates.
 */
public class Shader {
    private int program;
    private final HashMap<String, Integer> shaderHandleMap = new HashMap<String, Integer>();
    String vertexShader;

    public int getHandle(String name) {
        if (shaderHandleMap.containsKey(name)) {
            return shaderHandleMap.get(name);
        }
        int handle = GLES20.glGetAttribLocation(program, name);
        if (handle == -1) {
            handle = GLES20.glGetUniformLocation(program, name);
        }
        if (handle == -1) {
            throw new RuntimeException("get handle error, handle name:" + name + "\nshader:" + this.vertexShader);
        } else {
            shaderHandleMap.put(name, handle);
        }
        return handle;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            throw new RuntimeException("can not load shader");
        }
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException(error);
        }
        return shader;
    }

    public void setProgram(String vertexSource, String fragmentSource) {
        this.vertexShader = vertexSource;

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentSource);
        int program = GLES20.glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("can not create shader program.");
        }
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException(error);
        }
        this.program = program;
        shaderHandleMap.clear();
    }

    public void useProgram() {
        GLES20.glUseProgram(program);
    }
}
