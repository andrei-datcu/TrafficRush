package ro.lupii.trafficrush.objects;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.momchil_atanasov.data.front.parser.MTLLibrary;
import com.momchil_atanasov.data.front.parser.OBJDataReference;
import com.momchil_atanasov.data.front.parser.OBJFace;
import com.momchil_atanasov.data.front.parser.OBJMesh;
import com.momchil_atanasov.data.front.parser.OBJModel;
import com.momchil_atanasov.data.front.parser.OBJNormal;
import com.momchil_atanasov.data.front.parser.OBJTexCoord;
import com.momchil_atanasov.data.front.parser.OBJVertex;

import org.javatuples.Triplet;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import ro.lupii.trafficrush.Utils;
import ro.lupii.trafficrush.opengl.ShaderProgram;

/**
 * Created by andrei on 1/24/17.
 */

public class Mesh {

    final public static int VERTICES_PIPE = 0;
    final public static int NORMALS_PIPE = 1;
    final public static int TEXCOORDS_PIPE = 2;

    private int m_vao = -1;
    private int m_ibo = -1;
    private int m_verticesVBO = -1;
    private int m_normalsVBO = -1;
    private int m_texCoordsVBO = -1;
    private int m_faceCount;

    public Mesh(OBJModel objModel, OBJMesh objMesh, MTLLibrary mtlLibrary) throws IOException {

        List<OBJFace> objFaces = objMesh.getFaces();
        Utils.doAssert(objFaces.size() > 0);

        OBJFace firstFace = objFaces.get(0);
        boolean hasPositions = firstFace.getReferences().get(0).hasVertexIndex();
        boolean hasNormals = firstFace.getReferences().get(0).hasNormalIndex();
        boolean hasTexCoords = firstFace.getReferences().get(0).hasTexCoordIndex();

        Utils.doAssert(hasPositions);
        Utils.doAssert(hasNormals || hasTexCoords);

        HashMap<Triplet<Integer, Integer, Integer>, Integer> vCache = new HashMap<>();
        ExposedDataOutputStream tmpVertix = new ExposedDataOutputStream(new ExposedByteArrayOutputStream());
        ExposedDataOutputStream tmpNormals = hasNormals ? new ExposedDataOutputStream(new ExposedByteArrayOutputStream()) : null;
        ExposedDataOutputStream tmpTexCoords = hasTexCoords ? new ExposedDataOutputStream(new ExposedByteArrayOutputStream()) : null;
        ExposedDataOutputStream tmpFaces = new ExposedDataOutputStream(new ExposedByteArrayOutputStream());
        int itemsCount = 0;

        for (OBJFace face : objFaces) {
            List<OBJDataReference> refs = face.getReferences();
            Utils.doAssert(refs.size() == 3);

            for (OBJDataReference ref : refs) {
                Utils.doAssert(ref.hasVertexIndex() == hasPositions);
                Utils.doAssert(ref.hasNormalIndex() == hasNormals);
                Utils.doAssert(ref.hasTexCoordIndex() == hasTexCoords);

                Triplet<Integer, Integer, Integer> cacheEntry = Triplet.with(ref.vertexIndex, ref.normalIndex, ref.texCoordIndex);
                Integer cachedIndex = vCache.get(cacheEntry);
                if (cachedIndex == null) {

                    OBJVertex v = objModel.getVertex(ref);
                    tmpVertix.writeFloat(v.x);
                    tmpVertix.writeFloat(v.y);
                    tmpVertix.writeFloat(v.z);

                    if (hasNormals) {
                        OBJNormal n = objModel.getNormal(ref);
                        tmpNormals.writeFloat(n.x);
                        tmpNormals.writeFloat(n.y);
                        tmpNormals.writeFloat(n.z);
                    }

                    if (hasTexCoords) {
                        OBJTexCoord texCoord = objModel.getTexCoord(ref);
                        Utils.doAssert(texCoord.type == OBJTexCoord.Type.TYPE_2D);
                        tmpTexCoords.writeFloat(texCoord.u);
                        tmpTexCoords.writeFloat(texCoord.v);
                    }

                    cachedIndex = itemsCount++;
                    vCache.put(cacheEntry, cachedIndex);
                }
                tmpFaces.writeInt(cachedIndex);
            }
        }

        ByteBuffer vertices = tmpVertix.toByteBuffer();
        ByteBuffer normals = hasNormals ? tmpNormals.toByteBuffer() : null;
        ByteBuffer texCoords = hasTexCoords ? tmpTexCoords.toByteBuffer() : null;
        ByteBuffer faces = tmpFaces.toByteBuffer();

        buildGLBuffers(itemsCount, objFaces.size(), vertices, normals, texCoords, faces);
        m_faceCount = objFaces.size();

        //TODO (Andrei) handle materials and textures here
    }

    public void deallocate() {

        int[] buffers = new int[4];
        int activeBuffers = 2;

        Utils.doAssert(m_verticesVBO != -1);
        buffers[0] = m_verticesVBO;
        Utils.doAssert(m_ibo != -1);
        buffers[1] = m_ibo;

        if (m_normalsVBO != -1) {
            buffers[activeBuffers++] = m_normalsVBO;
        }
        if (m_texCoordsVBO != -1) {
            buffers[activeBuffers++] = m_texCoordsVBO;
        }

        GLES30.glDeleteBuffers(activeBuffers, buffers, 0);
    }

    public void setProgram(ShaderProgram program) {
        //TODO matrial uniforms and textures here
    }

    public void draw() {
        GLES30.glBindVertexArray(m_vao);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, m_faceCount * 3, GLES30.GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
    }

    private void buildGLBuffers(int itemsCount, int faceCount, Buffer vertices, Buffer normals,
                                Buffer texCoords, Buffer faces) {

        int[] tmpVaoArray = new int[1];
        GLES30.glGenVertexArrays(1, tmpVaoArray, 0);
        m_vao = tmpVaoArray[0];
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);

        GLES30.glBindVertexArray(m_vao);

        // generate IBO
        GLES30.glGenBuffers(1, tmpVaoArray, 0);
        m_ibo = tmpVaoArray[0];

        Utils.doAssert(faces.limit() == 3 * 4 * faceCount);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, m_ibo);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, 3 * 4 * faceCount,
                faces, GLES30.GL_STATIC_DRAW);


        m_verticesVBO = generateVBO(vertices, itemsCount, 3, VERTICES_PIPE);
        if (normals != null) {
            m_normalsVBO = generateVBO(normals, itemsCount, 3, NORMALS_PIPE);
        }
        if (texCoords != null) {
            m_texCoordsVBO = generateVBO(texCoords, itemsCount, 2, TEXCOORDS_PIPE);
        }

        GLES30.glBindVertexArray(0);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);

    }

    /**
     * Generate a VBO for a buffer containing count tuples of floats each tuple having floatCount items
     * Also, buffers the data and binds it on pipe. Call this only after the VAO has been bound
     */
    private int generateVBO(Buffer buffer, int count, int floatCount, int pipe) {

        Utils.doAssert(buffer.limit() == count * floatCount * 4);

        int[] tmpVBOArray = new int[1];
        GLES30.glGenBuffers(1, tmpVBOArray, 0);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);
        int vbo = tmpVBOArray[0];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);
        // No sizeof in java :(
        GLES30.glBufferData(GLES20.GL_ARRAY_BUFFER, count * floatCount * 4, buffer, GLES20.GL_STATIC_DRAW);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glEnableVertexAttribArray(pipe);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glVertexAttribPointer(pipe, floatCount, GLES20.GL_FLOAT, false, 0, 0);
        Utils.doAssert(GLES30.glGetError() == GLES30.GL_NO_ERROR);

        return vbo;
    }
}
