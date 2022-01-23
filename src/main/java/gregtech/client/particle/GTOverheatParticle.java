package gregtech.client.particle;

import codechicken.lib.vec.Cuboid6;
import gregtech.api.util.GTLog;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * @author brachy84
 */
public class GTOverheatParticle extends GTParticle {

    /**
     * http://www.vendian.org/mncharity/dir3/blackbody/
     */
    public static final int[] blackBodyColors = {
            0xFF3300, // 1000K
            0xFF5300, // 1200K
            0xFF6500, // 1400K
            0xFF7300, // 1600K
            0xFF7E00, // 1800K
            0xFF8912, // 2000K
            0xFF932C, // 2200K
            0xFF9D3F, // 2400K
            0xffa54f, // 2600K
            0xffad5e, // 2800K
            0xffb46b, // 3000K
            0xffbb78, // 3200K
            0xffc184, // 3400K
            0xffc78f, // 3600K
            0xffcc99, // 3800K
            0xffd1a3, // 4000K
            0xffd5ad, // 4200K
            0xffd9b6, // 4400K
            0xffddbe, // 4600K
            0xffe1c6, // 4800K
            0xffe4ce, // 5000K
            0xffe8d5, // 5200K
            0xffebdc, // 5400K
            0xffeee3, // 5600K
            0xfff0e9, // 5800K
            0xfff3ef  // 6000K
    };

    public static int getBlackBodyColor(int temperature) {
        if (temperature < 1000)
            return blackBodyColors[0];
        int index = (temperature - 1000) / 200;
        if (index >= blackBodyColors.length - 1)
            return blackBodyColors[blackBodyColors.length - 1];
        int color = blackBodyColors[index];
        return RenderUtil.colorInterpolator(color, blackBodyColors[index + 1]).apply(temperature % 200 / 200f);
    }

    protected final int meltTemp;
    protected int temperature = 293;
    protected final BlockPos pos;
    protected List<Cuboid6> pipeBoxes;
    protected boolean insulated;

    protected float alpha = 0;
    protected int color = blackBodyColors[0];

    public GTOverheatParticle(World worldIn, BlockPos pos, int meltTemp, List<Cuboid6> pipeBoxes, boolean insulated) {
        super(worldIn, pos.getX(), pos.getY(), pos.getZ());
        this.pos = pos;
        this.meltTemp = meltTemp;
        updatePipeBoxes(pipeBoxes);
        this.insulated = insulated;
        this.motionless = true;
    }

    public void updatePipeBoxes(List<Cuboid6> pipeBoxes) {
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
            alpha *= 0.8;
        } else {
            alpha = 0.8f;
        }
        color = getBlackBodyColor(temperature);
    }

    @Override
    public void onUpdate() {
        if (!(world.getTileEntity(pos) instanceof TileEntityCable))
            setExpired();
    }

    @Override
    public void setExpired() {
        super.setExpired();
        GTLog.logger.info("Particle died");
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (insulated)
            return;

        BloomEffectUtil.requestCustomBloom(RENDER_HANDLER, buffer1 -> {
            float red = (color >> 16) & 0xFF, green = (color >> 8) & 0xFF, blue = color & 0xFF;
            red /= 255;
            green /= 255;
            blue /= 255;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.translate(posX - interpPosX, posY - interpPosY, posZ - interpPosZ);
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            for (Cuboid6 cuboid : pipeBoxes) {
                RenderBufferHelper.renderCubeFace(bufferbuilder, cuboid, red, green, blue, alpha);
            }
            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        });
    }

    static BloomEffectUtil.IBloomRenderFast RENDER_HANDLER = new BloomEffectUtil.IBloomRenderFast() {
        @Override
        public int customBloomStyle() {
            return ConfigHolder.client.shader.fusionBloom.useShader ? ConfigHolder.client.shader.fusionBloom.bloomStyle : -1;
        }

        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        @SideOnly(Side.CLIENT)
        public void preDraw(BufferBuilder buffer) {
            BloomEffect.strength = (float) ConfigHolder.client.shader.fusionBloom.strength;
            BloomEffect.baseBrightness = (float) ConfigHolder.client.shader.fusionBloom.baseBrightness;
            BloomEffect.highBrightnessThreshold = (float) ConfigHolder.client.shader.fusionBloom.highBrightnessThreshold;
            BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.client.shader.fusionBloom.lowBrightnessThreshold;
            BloomEffect.step = 1;

            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GlStateManager.color(1, 1, 1, 1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.disableTexture2D();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void postDraw(BufferBuilder buffer) {
            GlStateManager.enableTexture2D();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }
    };
}
