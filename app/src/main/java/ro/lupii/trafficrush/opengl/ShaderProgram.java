package ro.lupii.trafficrush.opengl;

import android.content.Context;
import android.opengl.GLES30;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ro.lupii.trafficrush.Utils;

/**
 * Created by andrei on 1/24/17.
 */

public class ShaderProgram {

    private final int m_Program;

    public ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId,
                         HashMap<Integer, String> attributeBindings) throws IOException {

        // prepare shaders and OpenGL program
        int vertexShader = loadShader(
                GLES30.GL_VERTEX_SHADER,
                readTextFileFromResource(context, vertexShaderResourceId));
        int fragmentShader = loadShader(
                GLES30.GL_FRAGMENT_SHADER,
                readTextFileFromResource(context, fragmentShaderResourceId));

        m_Program = GLES30.glCreateProgram();             // create empty OpenGL Program
        GLES30.glAttachShader(m_Program, vertexShader);   // add the vertex shader to program
        GLES30.glAttachShader(m_Program, fragmentShader); // add the fragment shader to program

        // Bind attributes
        if (attributeBindings != null) {
            for (Map.Entry<Integer, String> entries : attributeBindings.entrySet()) {
                GLES30.glBindAttribLocation(m_Program, entries.getKey(), entries.getValue());
            }
        }

        GLES30.glLinkProgram(m_Program);                  // create OpenGL program executables
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);
    }

    public void useProgram() {
        GLES30.glUseProgram(m_Program);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);
    }

    public int getUniformLocation(String uniformVarName) {
        int result = GLES30.glGetUniformLocation(m_Program, uniformVarName);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);
        return result;
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    private int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);

        int[] params = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, params, 0);
        if (params[0] != GLES30.GL_TRUE) {
            String shaderInfo = GLES30.glGetShaderInfoLog(shader);
            throw new RuntimeException("Shader compilation error: " + shaderInfo);
        }

        return shader;
    }

    private String readTextFileFromResource(Context context, int resourceId) throws IOException {

        StringBuilder builder = new StringBuilder();

        InputStream inputStream = context.getResources().openRawResource(resourceId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;

        while ((nextLine = bufferedReader.readLine()) != null) {
            builder.append(nextLine);
            builder.append('\n');
        }

        bufferedReader.close();

        return builder.toString();
    }
}
