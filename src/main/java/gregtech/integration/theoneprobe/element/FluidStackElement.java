package gregtech.integration.theoneprobe.element;

import gregtech.client.utils.RenderUtil;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.IElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class FluidStackElement implements IElement {

    private static final int ID = TheOneProbe.theOneProbeImp.registerElementFactory(FluidStackElement::new);

    private final String location;
    private final int color;

    private TextureAtlasSprite sprite = null;

    public FluidStackElement(@NotNull FluidStack stack) {
        this(stack.getFluid().getStill(stack), stack.getFluid().getColor(stack));
    }

    public FluidStackElement(@NotNull ResourceLocation location, int color) {
        this.location = location.toString();
        this.color = color;
    }

    public FluidStackElement(@NotNull ByteBuf buf) {
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        this.location = new String(bytes, StandardCharsets.UTF_8);
        this.color = buf.readInt();
    }

    @Override
    public void render(int x, int y) {
        if (sprite == null) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location);
        }

        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        RenderUtil.setGlColorFromInt(color, 0xFF);
        RenderUtil.drawFluidTexture(x, y, sprite, 0, 0, 0);

        GlStateManager.disableBlend();
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf) {
        byte[] bytes = location.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeInt(color);
    }

    @Override
    public int getID() {
        return ID;
    }
}
