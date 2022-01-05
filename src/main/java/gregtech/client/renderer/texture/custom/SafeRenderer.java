package gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import gregtech.api.GTValues;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class SafeRenderer implements IIconRegister {

    private static final Cuboid6 mainBoxOuter = new Cuboid6(3 / 16.0, 0 / 16.0, 3 / 16.0, 13 / 16.0, 14 / 16.0, 13 / 16.0);
    private static final Cuboid6 mainBoxInner = new Cuboid6(4 / 16.0, 1 / 16.0, 3 / 16.0, 12 / 16.0, 13 / 16.0, 12 / 16.0);
    private static final Cuboid6 doorBox = new Cuboid6(4 / 16.0, 1 / 16.0, 3 / 16.0, 12 / 16.0, 13 / 16.0, 4 / 16.0);
    private static final List<EnumFacing> rotations = Arrays.asList(EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST);

    private final String basePath;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures;

    public SafeRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return textures[1];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        String formattedBase = GTValues.MODID + ":blocks/" + basePath;
        this.textures = new TextureAtlasSprite[7];
        this.textures[0] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_bottom"));
        this.textures[1] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_top"));
        this.textures[2] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_side"));
        this.textures[3] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_front"));

        this.textures[4] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/door_side"));
        this.textures[5] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/door_back"));
        this.textures[6] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/door_front"));
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing rotation, float capRotation) {
        translation.translate(0.5, 0.5, 0.5);
        translation.rotate(Math.toRadians(90.0 * rotations.indexOf(rotation)), Rotation.axes[1]);
        translation.translate(-0.5, -0.5, -0.5);

        for (EnumFacing renderSide : EnumFacing.VALUES) {
            TextureAtlasSprite baseSprite = renderSide.getAxis() == Axis.Y ?
                    textures[renderSide.getIndex()] :
                    renderSide == EnumFacing.NORTH ? textures[3] : textures[2];
            Textures.renderFace(renderState, translation, pipeline, renderSide, mainBoxOuter, baseSprite, BlockRenderLayer.CUTOUT_MIPPED);
            if (renderSide == EnumFacing.NORTH) continue;
            Textures.renderFace(renderState, translation, pipeline, renderSide, mainBoxInner, baseSprite, BlockRenderLayer.CUTOUT_MIPPED);
        }

        translation.translate(4 / 16.0, 7 / 16.0, 3 / 16.0);
        translation.rotate(Math.toRadians(capRotation), Rotation.axes[1]);
        translation.translate(-4 / 16.0, -7 / 16.0, -3 / 16.0);

        for (EnumFacing renderSide : EnumFacing.VALUES) {
            TextureAtlasSprite doorSprite =
                    renderSide == EnumFacing.NORTH ? textures[6] :
                            renderSide == EnumFacing.SOUTH ? textures[5] : textures[4];
            Textures.renderFace(renderState, translation, pipeline, renderSide, doorBox, doorSprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
    }
}
