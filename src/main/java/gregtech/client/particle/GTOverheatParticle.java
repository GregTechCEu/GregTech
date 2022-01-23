package gregtech.client.particle;

import codechicken.lib.vec.Cuboid6;
import gregtech.api.util.GTLog;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author brachy84
 */
public class GTOverheatParticle extends GTParticle {

    /**
     * http://www.vendian.org/mncharity/dir3/blackbody/
     * 1000 K  #ff3800
     * 1200 K  #ff5300
     * 1400 K  #ff6500
     * 1600 K  #ff7300
     * 1800 K  #ff7e00
     * 2000 K  #ff8912
     * 2200 K  #ff932c
     * 2400 K  #ff9d3f
     * 2600 K  #ffa54f
     * 2800 K  #ffad5e
     * 3000 K  #ffb46b
     * 3200 K  #ffbb78
     * 3400 K  #ffc184
     * 3600 K  #ffc78f
     * 3800 K  #ffcc99
     * 4000 K  #ffd1a3
     * 4200 K  #ffd5ad
     * 4400 K  #ffd9b6
     * 4600 K  #ffddbe
     * 4800 K  #ffe1c6
     * 5000 K  #ffe4ce
     * 5200 K  #ffe8d5
     * 5400 K  #ffebdc
     * 5600 K  #ffeee3
     * 5800 K  #fff0e9
     * 6000 K  #fff3ef
     */
    public static final int[] blackBodyColors = {
            0xFF3300,
            0xFF5300,
            0xFF6500,
            0xFF7300,
            0xFF7E00,
            0xFF8912,
            0xFF932C,
            0xFF9D3F,
            0xffa54f,
            0xffad5e,
            0xffb46b,
            0xffbb78,
            0xffc184,
            0xffc78f,
            0xffcc99,
            0xffd1a3,
            0xffd5ad,
            0xffd9b6,
            0xffddbe,
            0xffe1c6,
            0xffe4ce,
            0xffe8d5,
            0xffebdc,
            0xffeee3,
            0xfff0e9,
            0xfff3ef
    };

    public static int getBlackBodyColor(int temperature) {
        if (temperature < 1000)
            return blackBodyColors[0];
        int index = (temperature - 1000) / 200;
        if (index >= blackBodyColors.length - 1)
            return blackBodyColors[blackBodyColors.length - 1];
        int color = blackBodyColors[index];
        return interpolateColors(color, blackBodyColors[index + 1], temperature % 200 / 200f);
    }

    private static int interpolateColors(int c1, int c2, float blend) {
        int color = (int) (((c2 >> 16) & 0xFF) * blend + ((c1 >> 16) & 0xFF) * (1 - blend)) << 16;
        color |= (int) (((c2 >> 8) & 0xFF) * blend + ((c1 >> 8) & 0xFF) * (1 - blend)) << 8;
        color |= (int) ((c2 & 0xFF) * blend + (c1 & 0xFF) * (1 - blend));
        return color;
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
    }
}
