package gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DrumRenderer implements IIconRegister {
    private final String basePath;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures;

    public DrumRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        String formattedBase = GTValues.MODID + ":blocks/" + basePath;
        this.textures = new TextureAtlasSprite[3];
        this.textures[0] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/top"));
        this.textures[1] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/side"));
        this.textures[2] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/bottom"));
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing rotation) {

        for (EnumFacing renderSide : EnumFacing.VALUES) {
            TextureAtlasSprite baseSprite = renderSide == EnumFacing.UP ? textures[0] : renderSide == EnumFacing.DOWN ? textures[2] : textures[1];
            Textures.renderFace(renderState, translation, pipeline, renderSide, Cuboid6.full, baseSprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return textures[0];
    }

}
