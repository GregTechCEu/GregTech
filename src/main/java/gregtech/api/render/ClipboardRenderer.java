package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.fakegui.FakeModularGui;
import gregtech.api.gui.impl.fakegui.FakeModularUIContainer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTLog;
import gregtech.api.util.GregFakePlayer;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ClipboardRenderer implements TextureUtils.IIconRegister {

    private static final Cuboid6 pageBox = new Cuboid6(3 / 16.0, 0.25 / 16.0, 0.25 / 16.0, 13 / 16.0, 14.25 / 16.0, 0.3 / 16.0);
    private static final Cuboid6 boardBox = new Cuboid6(2.75 / 16.0, 0 / 16.0, 0 / 16.0, 13.25 / 16.0, 15.25 / 16.0, 0.25 / 16.0);
    private static final Cuboid6 clipBox = new Cuboid6(5.75 / 16.0, 14.75 / 16.0, 0.25 / 16.0, 10.25 / 16.0, 15.5 / 16.0, 0.4 / 16.0);
    private static final Cuboid6 graspBox = new Cuboid6(7 / 16.0, 15.25 / 16.0, 0.1 / 16.0, 9 / 16.0, 16 / 16.0, 0.35 / 16.0);

    private static final List<EnumFacing> rotations = Arrays.asList(EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST);

    private static HashMap<Cuboid6, TextureAtlasSprite> boxTextureMap = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures = new TextureAtlasSprite[3];


    public ClipboardRenderer() {
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        this.textures[0] = textureMap.registerSprite(new ResourceLocation("gregtech:blocks/clipboard/wood"));
        boxTextureMap.put(boardBox, this.textures[0]);
        this.textures[1] = textureMap.registerSprite(new ResourceLocation("gregtech:blocks/clipboard/clip"));
        boxTextureMap.put(clipBox, this.textures[1]);
        boxTextureMap.put(graspBox, this.textures[1]);
        this.textures[2] = textureMap.registerSprite(new ResourceLocation("gregtech:blocks/clipboard/page"));
        boxTextureMap.put(pageBox, this.textures[2]);
    }

    @SideOnly(Side.CLIENT)
    public void renderBoard(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing rotation, MetaTileEntityClipboard clipboard, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();


        renderState.bind(buffer);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        }
        else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        translation.translate(0.5, 0.5, 0.5);
        translation.rotate(Math.toRadians(90.0 * rotations.indexOf(rotation)), Rotation.axes[1]);
        translation.translate(-0.5, -0.5, -0.5);



        // Render Clipboard
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            boxTextureMap.forEach((box, sprite) -> Textures.renderFace(renderState, translation, pipeline, renderSide, box, sprite));
        }

        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();


    }


    @SideOnly(Side.CLIENT)
    public void renderGUI(Matrix4 translation, MetaTileEntityClipboard clipboard, float partialTicks) {

        translation.translate((1 - 1) * 0.5, (1 - 1) * 0.5, 0);
        translation.scale(1, 1, 1);

        FakeModularGui fakeGui = createFakeGui(clipboard);
        if (fakeGui != null) {
            Pair<Double, Double> result = clipboard.checkLookingAt(partialTicks);
            if (result == null)
                fakeGui.drawScreen(0, 0, partialTicks);
            else
                fakeGui.drawScreen(result.getKey(), result.getValue(), partialTicks);

        }

    }

    public FakeModularGui createFakeGui(MetaTileEntityClipboard mte) {
        try {
            // Get a FakePlayer in the client's current dimension through this roundabout function
            FakePlayer fakePlayer = GregFakePlayer.get(Minecraft.getMinecraft().getIntegratedServer().getWorld(Minecraft.getMinecraft().player.dimension));
            ModularUI ui = mte.createUI(fakePlayer);
            List<Widget> widgets = new ArrayList<>();
            // Don't worry about SlotWidgets, since the clipboard literally can't have them
            boolean hasPlayerInventory = false;

            ModularUI.Builder builder = new ModularUI.Builder(ui.backgroundPath, ui.getWidth(), ui.getHeight() - (hasPlayerInventory ? 80 : 0));
            for (Widget widget : widgets) {
                builder.widget(widget);
            }
            ui = builder.build(ui.holder, ui.entityPlayer);
            FakeModularUIContainer fakeModularUIContainer = new FakeModularUIContainer(ui);
            if (mte.getWorld().isRemote) {
                return new FakeModularGui(ui, fakeModularUIContainer);
            }
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
        return null;
    }


    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return textures[0];
    }
}
