package gregtech.client.particle;

import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.IBloomEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GTBloomParticle extends GTParticle implements IBloomEffect {

    public GTBloomParticle(double posX, double posY, double posZ) {
        super(posX, posY, posZ);
        BloomEffectUtil.registerBloomRender(getBloomRenderSetup(), getBloomType(), this, this);
    }

    @Nullable
    protected abstract IRenderSetup getBloomRenderSetup();

    @NotNull
    protected abstract BloomType getBloomType();
}
