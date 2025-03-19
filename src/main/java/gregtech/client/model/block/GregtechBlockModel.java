package gregtech.client.model.block;

import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.IVertexProducer;

import org.jetbrains.annotations.NotNull;

public class GregtechBlockModel {

    final BlockFace[] faces = new BlockFace[6];

    static class BlockFace implements IVertexProducer {

        Vertex[] vertices = new Vertex[4];

        @Override
        public void pipe(@NotNull IVertexConsumer consumer) {
            var fmt = consumer.getVertexFormat();
            // for each vertex
            for (var vertex : vertices) {
                // for each element
                for (int e = 0; e < fmt.getElementCount(); e++) {
                    float[] data = new float[0];
                    var element = fmt.getElement(e);

                    switch (fmt.getElement(e).getUsage()) {
                        case POSITION -> data = new float[] {
                                vertex.x,
                                vertex.y,
                                vertex.z
                        };
                        case UV -> {
                            if (element.getIndex() == 0) {
                                data = new float[] {
                                        vertex.u,
                                        vertex.v
                                };
                            } else {
                                // lightmaps
                            }
                        }
                        case NORMAL -> {
                            data = vertex.normals.clone();
                        }
                        case COLOR -> data = new float[] {
                                vertex.r,
                                vertex.g,
                                vertex.b
                        };
                        default -> {}
                    }
                    consumer.put(e, data);
                }
            }
        }
    }

    static class Vertex {

        float x, y, z;
        float u, v;
        float r, g, b;
        float[] normals = new float[3];
    }
}
