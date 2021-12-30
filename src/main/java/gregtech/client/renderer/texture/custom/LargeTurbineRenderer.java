package gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

public class LargeTurbineRenderer implements IIconRegister {

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite baseRingSprite;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite baseBackgroundSprite;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite idleBladeSprite;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite activeBladeSprite;

    public LargeTurbineRenderer() {
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.baseRingSprite = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/multiblock/large_turbine/base_ring"));
        this.baseBackgroundSprite = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/multiblock/large_turbine/base_bg"));
        this.idleBladeSprite = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/multiblock/large_turbine/rotor_idle"));
        this.activeBladeSprite = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/multiblock/large_turbine/rotor_spinning"));
    }

    @SideOnly(Side.CLIENT)
    public void renderSided(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing side, boolean hasBase, boolean hasRotor, boolean isActive, int rotorRGB) {
        Matrix4 cornerOffset = null;
        switch (side.getAxis()) {
            case X:
                cornerOffset = translation.copy().translate(0.01 * side.getXOffset(), -1.0, -1.0);
                cornerOffset.scale(1.0, 3.0, 3.0);
                break;
            case Z:
                cornerOffset = translation.copy().translate(-1.0, -1.0, 0.01 * side.getZOffset());
                cornerOffset.scale(3.0, 3.0, 1.0);
                break;
            case Y:
                cornerOffset = translation.copy().translate(-1.0, 0.01 * side.getYOffset(), -1.0);
                cornerOffset.scale(3.0, 1.0, 3.0);
                break;
        }
        if (hasBase) {
            Textures.renderFace(renderState, cornerOffset, ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240)), side, Cuboid6.full, baseRingSprite, BlockRenderLayer.CUTOUT_MIPPED);
            Textures.renderFace(renderState, cornerOffset, ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourOperation(0xFFFFFFFF)), side, Cuboid6.full, baseBackgroundSprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
        if (hasRotor) {
            TextureAtlasSprite sprite = isActive ? activeBladeSprite : idleBladeSprite;
            IVertexOperation[] color = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(rotorRGB)));
            Textures.renderFace(renderState, cornerOffset, color, side, Cuboid6.full, sprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

}
