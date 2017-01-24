/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.lupii.trafficrush.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ro.lupii.trafficrush.R;
import ro.lupii.trafficrush.objects.GraphicObject;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] m_projectionMatrix = new float[16];
    private final float[] m_viewMatrix = new float[16];
    private int m_viewMatrixHandle, m_projectionMatrixHandle;
    private ShaderProgram m_objectsShader;
    private Context m_context;
    private ArrayList<GraphicObject> m_objects = new ArrayList<>();

    public MyGLRenderer(Context m_context) {
        this.m_context = m_context;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        Matrix.setLookAtM(m_viewMatrix, 0, 0, 0, 50, 0, 0, 0, 0, 1, 0);

        HashMap<Integer, String> shaderBindings = new HashMap<>();
        shaderBindings.put(0, "in_position");
        shaderBindings.put(1, "in_normal");
        shaderBindings.put(2, "in_texcoord");

        try {
            m_objectsShader = new ShaderProgram(m_context, R.raw.shader_vertex, R.raw.shader_fragment, shaderBindings);
            m_objects.add(new GraphicObject(m_context, R.raw.bunny));
            m_objects.get(0).setProgram(m_objectsShader);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create shader program or asset objects");
        }

        m_objectsShader.useProgram();
        m_projectionMatrixHandle = m_objectsShader.getUniformLocation("projection_matrix");
        m_viewMatrixHandle = m_objectsShader.getUniformLocation("view_matrix");
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        // Draw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glUniformMatrix4fv(m_viewMatrixHandle, 1, false, m_viewMatrix, 0);
        GLES30.glUniformMatrix4fv(m_projectionMatrixHandle, 1, false, m_projectionMatrix, 0);

        // Draw objects
        for (GraphicObject object : m_objects) {
            object.draw();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES30.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(m_projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return 0;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
    }

}