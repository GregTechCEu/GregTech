package gregtech.client.particle;

import gregtech.client.renderer.ICustomRenderFast;
import net.minecraft.client.renderer.BufferBuilder;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description: copyright Created by brandon3055 on 30/11/2016.
 */

public interface IGTParticleHandler extends ICustomRenderFast {
    IGTParticleHandler DEFAULT_FX_HANDLER = new IGTParticleHandler() {
        @Override
        public void preDraw(BufferBuilder buffer) {

        }

        @Override
        public void postDraw(BufferBuilder buffer) {
        }
    };
}
