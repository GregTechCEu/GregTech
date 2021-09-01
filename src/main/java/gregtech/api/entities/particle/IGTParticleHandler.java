package gregtech.api.entities.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description: copyright Created by brandon3055 on 30/11/2016.
 */

public interface IGTParticleHandler {

    /**
     * Run any pre render gl code here.
     * You can also start drawing quads.
     */
    void preDraw(int layer, BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ);

    /**
     * Run any post render gl code here.
     * This is where you would draw if you started drawing in preDraw
     */
    void postDraw(int layer, BufferBuilder buffer, Tessellator tessellator);

    IGTParticleHandler DEFAULT_FX_HANDLER = new IGTParticleHandler() {
        @Override
        public void preDraw(int layer, BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        }

        @Override
        public void postDraw(int layer, BufferBuilder buffer, Tessellator tessellator) {
        }
    };
}
