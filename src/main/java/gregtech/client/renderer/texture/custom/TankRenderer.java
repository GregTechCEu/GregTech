package gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TankRenderer implements IIconRegister {

    private final String basePath;

    public TankRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }


    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] ctSprites;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.ctSprites = new TextureAtlasSprite[16];
        for (int i = 0; i < 16; i++) {
            this.ctSprites[i] = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/" + basePath + "_" + i));
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return ctSprites[ctSprites.length - 1];
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, int connectionMask) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            if (hasFaceBit(connectionMask, renderSide)) {
                continue; //do not render faces occluded by connections
            }
            int resultTextureMask = 0b1111;
            if (renderSide.getAxis().isHorizontal()) {
                if (hasFaceBit(connectionMask, EnumFacing.UP)) resultTextureMask &= ~TextureDirection.TOP;
                if (hasFaceBit(connectionMask, EnumFacing.DOWN)) resultTextureMask &= ~TextureDirection.BOTTOM;
                EnumFacing leftFacing = renderSide.rotateY();
                EnumFacing rightFacing = renderSide.rotateYCCW();
                if (hasFaceBit(connectionMask, leftFacing)) resultTextureMask &= ~TextureDirection.LEFT;
                if (hasFaceBit(connectionMask, rightFacing)) resultTextureMask &= ~TextureDirection.RIGHT;
            } else {
                if (hasFaceBit(connectionMask, EnumFacing.NORTH)) resultTextureMask &= ~TextureDirection.TOP;
                if (hasFaceBit(connectionMask, EnumFacing.SOUTH)) resultTextureMask &= ~TextureDirection.BOTTOM;
                if (hasFaceBit(connectionMask, EnumFacing.WEST)) resultTextureMask &= ~TextureDirection.LEFT;
                if (hasFaceBit(connectionMask, EnumFacing.EAST)) resultTextureMask &= ~TextureDirection.RIGHT;
            }
            TextureAtlasSprite sideSprite = ctSprites[resultTextureMask];
            Textures.renderFace(renderState, translation, pipeline, renderSide, Cuboid6.full, sideSprite);
            Matrix4 backTranslation = translation.copy();
            backTranslation.translate(renderSide.getXOffset(), renderSide.getYOffset() * 0.999, renderSide.getZOffset());
            int backFaceTextureMask;
            if (renderSide.getAxis().isHorizontal()) {
                backFaceTextureMask = resultTextureMask & ~(TextureDirection.RIGHT | TextureDirection.LEFT);
                if ((resultTextureMask & TextureDirection.RIGHT) > 0)
                    backFaceTextureMask |= TextureDirection.LEFT;
                if ((resultTextureMask & TextureDirection.LEFT) > 0)
                    backFaceTextureMask |= TextureDirection.RIGHT;
            } else {
                backFaceTextureMask = resultTextureMask;
            }
            TextureAtlasSprite backSideSprite = ctSprites[backFaceTextureMask];
            Textures.renderFace(renderState, backTranslation, pipeline, renderSide.getOpposite(), Cuboid6.full, backSideSprite);
        }
    }

    @SideOnly(Side.CLIENT)
    private static class TextureDirection {
        private static final int TOP = 1;
        private static final int BOTTOM = 2;
        private static final int LEFT = 4;
        private static final int RIGHT = 8;
    }

    @SideOnly(Side.CLIENT)
    public static boolean hasFaceBit(int mask, EnumFacing side) {
        return (mask & 1 << side.getIndex()) > 0;
    }

    @SideOnly(Side.CLIENT)
    public void renderFluid(CCRenderState renderState, Matrix4 translation, int connectionMask, double fillPercent, FluidStack fluidStack) {
        if (fluidStack != null) {
            int fluidStackColor = fluidStack.getFluid().getColor(fluidStack);
            double fluidLevelOffset = (offset(EnumFacing.UP, connectionMask) + offset(EnumFacing.DOWN, connectionMask));
            double fluidLevel = fillPercent * (1.0 - fluidLevelOffset);

            Cuboid6 resultFluidCuboid = createFullOffsetCuboid(connectionMask);
            int resultFluidColor;
            if (fluidStack.getFluid().isGaseous(fluidStack)) {
                int opacity = (int) (fillPercent * 255);
                resultFluidColor = GTUtility.convertRGBtoRGBA_CL(fluidStackColor, opacity);
            } else {
                resultFluidCuboid.max.y = resultFluidCuboid.min.y + fluidLevel;
                resultFluidColor = GTUtility.convertRGBtoOpaqueRGBA_CL(fluidStackColor);
            }

            ColourMultiplier multiplier = new ColourMultiplier(resultFluidColor);
            IVertexOperation[] fluidPipeline = new IVertexOperation[]{multiplier};
            TextureAtlasSprite fluidSprite = TextureUtils.getTexture(fluidStack.getFluid().getStill(fluidStack));

            for (EnumFacing renderSide : EnumFacing.VALUES) {
                if (hasFaceBit(connectionMask, renderSide)) continue;
                Textures.renderFace(renderState, translation, fluidPipeline, renderSide, resultFluidCuboid, fluidSprite);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static Cuboid6 createFullOffsetCuboid(int connectionMask) {
        Cuboid6 cuboid6 = new Cuboid6();
        for (EnumFacing side : EnumFacing.VALUES) {
            double offset = offset(side, connectionMask);
            double value = side.getAxisDirection() == AxisDirection.POSITIVE ? 1.0 - offset : offset;
            cuboid6.setSide(side, value);
        }
        return cuboid6;
    }

    @SideOnly(Side.CLIENT)
    private static double offset(EnumFacing side, int connectionMask) {
        return hasFaceBit(connectionMask, side) ? 0.0 : 0.003;
    }

}
