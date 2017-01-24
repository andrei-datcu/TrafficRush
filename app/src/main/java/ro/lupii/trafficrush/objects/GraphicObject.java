package ro.lupii.trafficrush.objects;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.momchil_atanasov.data.front.parser.MTLLibrary;
import com.momchil_atanasov.data.front.parser.MTLParser;
import com.momchil_atanasov.data.front.parser.OBJMesh;
import com.momchil_atanasov.data.front.parser.OBJModel;
import com.momchil_atanasov.data.front.parser.OBJObject;
import com.momchil_atanasov.data.front.parser.OBJParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ro.lupii.trafficrush.Utils;
import ro.lupii.trafficrush.opengl.ShaderProgram;

/**
 * Created by andrei on 1/23/17.
 */

public class GraphicObject {

    private ArrayList<Mesh> m_meshes = new ArrayList<>();
    private float[] m_modelMatrix = new float[16];
    private int m_modelMatrixHandle;

    public GraphicObject(Context context, int resourceId) throws IOException {

        Matrix.setIdentityM(m_modelMatrix, 0);

        Resources resources = context.getResources();
        InputStream objFile = resources.openRawResource(resourceId);
        OBJParser parser = new OBJParser();
        OBJModel model = parser.parse(objFile);

        List<String> materials = model.getMaterialLibraries();
        Utils.doAssert(materials.size() <= 1);

        MTLLibrary mtlLibrary = null;

        if (materials.size() > 0) {
            MTLParser mtlParser = new MTLParser();
            mtlLibrary = mtlParser.parse(resources.openRawResource(resources.getIdentifier(materials.get(0), "raw", null)));
        }

        for (OBJObject object : model.getObjects()) {
            for (OBJMesh objMesh : object.getMeshes()) {
                m_meshes.add(new Mesh(model, objMesh, mtlLibrary));
            }
        }
    }

    public void setProgram(ShaderProgram program) {
        m_modelMatrixHandle = program.getUniformLocation("model_matrix");
        for (Mesh m : m_meshes) {
            m.setProgram(program);
        }
    }

    public void draw() {
        GLES30.glUniformMatrix4fv(m_modelMatrixHandle, 1, false, m_modelMatrix, 0);
        for (Mesh m : m_meshes) {
            m.draw();
        }
    }
}
