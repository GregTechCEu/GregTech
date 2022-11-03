package gregtech.client.model.pipeline;

import gregtech.client.shader.Shaders;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;

import javax.vecmath.Vector3f;
import java.util.Objects;

public class VertexLighterFlatSpecial extends VertexLighterFlat {

    public int tint = -1;
    public boolean diffuse = true;

    public VertexLighterFlatSpecial(BlockColors colors) {
        super(colors);
    }

    @Override
    public void setVertexFormat(VertexFormat format) {

        if (!Objects.equals(format, baseFormat)) {
            baseFormat = format;

            if (!format.hasNormal()) {
                format = new VertexFormat(format).addElement(NORMAL_4F);
            }

            this.format = format;
            dataLength = new byte[format.getElementCount()];
            quadData = new float[format.getElementCount()][4][4];

            updateIndices();
        }

    }

    //This was copied over from VertexLighterFlat because it was private and thus inaccessible from this extended implementation
    private void updateIndices() {
        for (int i = 0; i < getVertexFormat().getElementCount(); i++) {
            switch (getVertexFormat().getElement(i).getUsage()) {
                case POSITION:
                    posIndex = i;
                    break;
                case NORMAL:
                    normalIndex = i;
                    break;
                case COLOR:
                    colorIndex = i;
                    break;
                case UV:
                    if (getVertexFormat().getElement(i).getIndex() == 1) {
                        lightmapIndex = i;
                    }
                    break;
                default:
            }
        }
        if (posIndex == -1) {
            throw new IllegalArgumentException("vertex lighter needs format with position");
        }
        if (lightmapIndex == -1) {
            throw new IllegalArgumentException("vertex lighter needs format with lightmap");
        }
        if (colorIndex == -1) {
            throw new IllegalArgumentException("vertex lighter needs format with color");
        }
    }

    //This was copied over from VertexLighterFlat because it needed tweaks to the color handling
    @Override
    protected void processQuad() {

        float[][] position = quadData[posIndex];
        float[][] normal = null;
        float[][] lightmap = quadData[lightmapIndex];
        float[][] color = quadData[colorIndex];

        if (dataLength[normalIndex] >= 3
                && (quadData[normalIndex][0][0] != -1
                || quadData[normalIndex][0][1] != -1
                || quadData[normalIndex][0][2] != -1)) {
            normal = quadData[normalIndex];
        } else { // normals must be generated
            normal = new float[4][4];
            Vector3f v1 = new Vector3f(position[3]);
            Vector3f t = new Vector3f(position[1]);
            Vector3f v2 = new Vector3f(position[2]);
            v1.sub(t);
            t.set(position[0]);
            v2.sub(t);
            v1.cross(v2, v1);
            v1.normalize();
            for (int v = 0; v < 4; v++) {
                normal[v][0] = v1.x;
                normal[v][1] = v1.y;
                normal[v][2] = v1.z;
                normal[v][3] = 0;
            }
        }

        int multiplier = 0xFFFFFFFF;//white
        if (tint != -1) {
            multiplier = blockInfo.getColorMultiplier(tint);
        }

        VertexFormat format = parent.getVertexFormat();
        int count = format.getElementCount();

        for (int v = 0; v < 4; v++) {
            position[v][0] += blockInfo.getShx();
            position[v][1] += blockInfo.getShy();
            position[v][2] += blockInfo.getShz();

            float x = position[v][0] - .5f;
            float y = position[v][1] - .5f;
            float z = position[v][2] - .5f;

            x += normal[v][0] * .5f;
            y += normal[v][1] * .5f;
            z += normal[v][2] * .5f;

            color[v][0] = color[v][1] = color[v][2] = color[v][3] = 1.0f;//Default to white

            float blockLight = lightmap[v][0];
            float skyLight = lightmap[v][1];
            updateLightmap(normal[v], lightmap[v], x, y, z);
            if (dataLength[lightmapIndex] > 1) {
                if (blockLight > lightmap[v][0]) lightmap[v][0] = blockLight;
                if (skyLight > lightmap[v][1]) lightmap[v][1] = skyLight;
            }

            updateColor(normal[v], color[v], x, y, z, tint, multiplier);

            //When enabled this causes the rendering to be black with Optifine
            if (!Shaders.isOptiFineShaderPackLoaded() && diffuse) {
                float d = LightUtil.diffuseLight(normal[v][0], normal[v][1], normal[v][2]);
                for (int i = 0; i < 3; i++) {
                    color[v][i] *= d;
                }
            }

            // no need for remapping cause all we could've done is add 1 element to the end
            for (int e = 0; e < count; e++) {
                VertexFormatElement element = format.getElement(e);
                switch (element.getUsage()) {
                    case POSITION:
                        parent.put(e, position[v]);
                        break;
                    case NORMAL:
                        if (normalIndex != -1) {
                            parent.put(e, normal[v]);
                            break;
                        }
                    case COLOR:
                        //color[v][0] = color[v][1] = color[v][2] = color[v][3] = 1.0f;//Default to white
                        parent.put(e, color[v]);
                        break;
                    case UV:
                        if (element.getIndex() == 1) {
                            parent.put(e, lightmap[v]);
                            break;
                        }
                    default:
                        parent.put(e, quadData[e][v]);
                }
            }
        }
        tint = -1;
    }

    //This was copied over from VertexLighterFlat because the tint parameter shouldn't be a float
    protected void updateColor(float[] normal, float[] color, float x, float y, float z, int tint, int multiplier) {
        if (tint != -1) {
            color[0] *= (float) (multiplier >> 0x10 & 0xFF) / 0xFF;
            color[1] *= (float) (multiplier >> 0x8 & 0xFF) / 0xFF;
            color[2] *= (float) (multiplier & 0xFF) / 0xFF;
        }
    }

    @Override
    public void setQuadTint(int tint) {
        this.tint = tint;
    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        this.diffuse = diffuse;
    }

}
