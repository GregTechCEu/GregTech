package gregtech.client.particle;

import gregtech.api.GTValues;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.EffectRenderContext;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.vec.Cuboid6;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * @author brachy84
 */
public class GTOverheatParticle extends GTBloomParticle {

    /**
     * <a href="http://www.vendian.org/mncharity/dir3/blackbody/">Source</a>
     */
    private static final int[] blackBodyColors = {
            0xFF3300, // 1000 K
            0xFF5300, // 1200 K
            0xFF6500, // 1400 K
            0xFF7300, // 1600 K
            0xFF7E00, // 1800 K
            0xFF8912, // 2000 K
            0xFF932C, // 2200 K
            0xFF9D3F, // 2400 K
            0xffa54f, // 2600 K
            0xffad5e, // 2800 K
            0xffb46b, // 3000 K
            0xffbb78, // 3200 K
            0xffc184, // 3400 K
            0xffc78f, // 3600 K
            0xffcc99, // 3800 K
            0xffd1a3, // 4000 K
            0xffd5ad, // 4200 K
            0xffd9b6, // 4400 K
            0xffddbe, // 4600 K
            0xffe1c6, // 4800 K
            0xffe4ce, // 5000 K
            0xffe8d5, // 5200 K
            0xffebdc, // 5400 K
            0xffeee3, // 5600 K
            0xfff0e9, // 5800 K
            0xfff3ef, // 6000 K
            0xfff5f5, // 6200 K
            0xfff8fb, // 6400 K
            0xfef9ff, // 6600 K
            0xf9f6ff, // 6800 K
            0xf5f3ff, // 7000 K
            0xf0f1ff, // 7200 K
            0xedefff, // 7400 K
            0xe9edff, // 7600 K
            0xe6ebff, // 7800 K
            0xe3e9ff, // 8000 K
            0xe0e7ff, // 8200 K
            0xdde6ff, // 8400 K
            0xdae4ff, // 8600 K
            0xd8e3ff, // 8800 K
            0xd6e1ff, // 9000 K
            0xd3e0ff, // 9200 K
            0xd1dfff, // 9400 K
            0xcfddff, // 9600 K
            0xcedcff, // 9800 K
            0xccdbff, // 10000 K
            0xcadaff, // 10200 K
            0xc9d9ff, // 10400 K
            0xc7d8ff, // 10600 K
            0xc6d8ff, // 10800 K
            0xc4d7ff, // 11000 K
            0xc3d6ff, // 11200 K
            0xc2d5ff, // 11400 K
            0xc1d4ff, // 11600 K
            0xc0d4ff, // 11800 K
            0xbfd3ff, // 12000 K
            0xbed2ff, // 12200 K
            0xbdd2ff, // 12400 K
            0xbcd1ff, // 12600 K
            0xbbd1ff, // 12800 K
            0xbad0ff, // 13000 K
            0xb9d0ff, // 13200 K
            0xb8cfff, // 13400 K
            0xb7cfff, // 13600 K
            0xb7ceff, // 13800 K
            0xb6ceff, // 14000 K
            0xb5cdff, // 14200 K
            0xb5cdff, // 14400 K
            0xb4ccff, // 14600 K
            0xb3ccff, // 14800 K
            0xb3ccff, // 15000 K
            0xb2cbff, // 15200 K
            0xb2cbff, // 15400 K
            0xb1caff, // 15600 K
            0xb1caff, // 15800 K
            0xb0caff, // 16000 K
            0xafc9ff, // 16200 K
            0xafc9ff, // 16400 K
            0xafc9ff, // 16600 K
            0xaec9ff, // 16800 K
            0xaec8ff, // 17000 K
            0xadc8ff, // 17200 K
            0xadc8ff, // 17400 K
            0xacc7ff, // 17600 K
            0xacc7ff, // 17800 K
            0xacc7ff, // 18000 K
            0xabc7ff, // 18200 K
            0xabc6ff, // 18400 K
            0xaac6ff, // 18600 K
            0xaac6ff, // 18800 K
            0xaac6ff, // 19000 K
            0xa9c6ff, // 19200 K
            0xa9c5ff, // 19400 K
            0xa9c5ff, // 19600 K
            0xa9c5ff, // 19800 K
            0xa8c5ff, // 20000 K
            // color doesn't really change onwards
    };

    public static int getBlackBodyColor(int temperature) {
        if (temperature < 1000)
            return blackBodyColors[0];
        int index = (temperature - 1000) / 200;
        if (index >= blackBodyColors.length - 1)
            return blackBodyColors[blackBodyColors.length - 1];
        int color = blackBodyColors[index];
        return RenderUtil.interpolateColor(color, blackBodyColors[index + 1], temperature % 200 / 200f);
    }

    private final TileEntityCable tileEntity;

    protected final int meltTemp;
    protected int temperature = 293;
    protected List<Cuboid6> pipeBoxes;
    protected boolean insulated;

    protected float alpha = 0;
    protected int color = blackBodyColors[0];

    public GTOverheatParticle(@NotNull TileEntityCable tileEntity, int meltTemp, @NotNull List<Cuboid6> pipeBoxes,
                              boolean insulated) {
        super(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ());
        this.tileEntity = tileEntity;
        this.meltTemp = meltTemp;
        this.pipeBoxes = pipeBoxes;
        updatePipeBoxes(pipeBoxes);
        this.insulated = insulated;
    }

    public void updatePipeBoxes(@NotNull List<Cuboid6> pipeBoxes) {
        this.pipeBoxes = pipeBoxes;
        for (Cuboid6 cuboid : this.pipeBoxes) {
            cuboid.expand(0.001);
        }
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        if (temperature <= 293 || temperature > meltTemp) {
            setExpired();
            return;
        }
        if (temperature < 500) {
            alpha = 0f;
        } else if (temperature < 1000) {
            alpha = (temperature - 500f) / 500f;
            alpha *= 0.8f;
        } else {
            alpha = 0.8f;
        }
        color = getBlackBodyColor(temperature);
    }

    @Override
    public void onUpdate() {
        if (tileEntity.isInvalid() || !tileEntity.isParticleAlive()) {
            setExpired();
            return;
        }

        if (temperature > 400 && GTValues.RNG.nextFloat() < 0.04) {
            spawnSmoke();
        }
    }

    private void spawnSmoke() {
        BlockPos pos = tileEntity.getPos();
        float xPos = pos.getX() + 0.5F;
        float yPos = pos.getY() + 0.9F;
        float zPos = pos.getZ() + 0.5F;

        float ySpd = 0.3F + 0.1F * GTValues.RNG.nextFloat();
        tileEntity.getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, 0, ySpd, 0);
    }

    @Override
    public String toString() {
        return "GTOverheatParticle{" +
                "tileEntity=" + tileEntity +
                ", meltTemp=" + meltTemp +
                ", temperature=" + temperature +
                ", pipeBoxes=" + pipeBoxes +
                ", insulated=" + insulated +
                ", alpha=" + alpha +
                ", color=" + color +
                '}';
    }

    @Nullable
    @Override
    protected IRenderSetup getBloomRenderSetup() {
        return SETUP;
    }

    @NotNull
    @Override
    protected BloomType getBloomType() {
        ConfigHolder.HeatEffectBloom heatEffectBloom = ConfigHolder.client.shader.heatEffectBloom;
        return BloomType.fromValue(heatEffectBloom.useShader ? heatEffectBloom.bloomStyle : -1);
    }

    @Override
    public void renderBloomEffect(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;

        buffer.setTranslation(posX - context.cameraX(), posY - context.cameraY(), posZ - context.cameraZ());
        for (Cuboid6 cuboid : pipeBoxes) {
            RenderBufferHelper.renderCubeFace(buffer, cuboid, red, green, blue, alpha, true);
        }
    }

    @Override
    public boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
        return !this.insulated;
    }

    private static final IRenderSetup SETUP = new IRenderSetup() {

        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        @SideOnly(Side.CLIENT)
        public void preDraw(@NotNull BufferBuilder buffer) {
            BloomEffect.strength = (float) ConfigHolder.client.shader.heatEffectBloom.strength;
            BloomEffect.baseBrightness = (float) ConfigHolder.client.shader.heatEffectBloom.baseBrightness;
            BloomEffect.highBrightnessThreshold = (float) ConfigHolder.client.shader.heatEffectBloom.highBrightnessThreshold;
            BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.client.shader.heatEffectBloom.lowBrightnessThreshold;
            BloomEffect.step = 1;

            this.lastBrightnessX = OpenGlHelper.lastBrightnessX;
            this.lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GlStateManager.color(1, 1, 1, 1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void postDraw(@NotNull BufferBuilder buffer) {
            buffer.setTranslation(0, 0, 0);
            Tessellator.getInstance().draw();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }
    };
}
