package gregtech.common.terminal.app.prospector;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.client.utils.RenderUtil;
import gregtech.core.network.packets.PacketProspecting;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;

public class ProspectingTexture extends AbstractTexture {

    public static final String SELECTED_ALL = "[all]";

    private String selected = SELECTED_ALL;
    private boolean darkMode;
    private int imageWidth = -1;
    private int imageHeight = -1;
    public final HashMap<Byte, String>[][] map;
    public static HashMap<Byte, String> emptyTag = new HashMap<>();
    private int playerXGui;
    private int playerYGui;
    private final ProspectorMode mode;
    private final int radius;

    public ProspectingTexture(ProspectorMode mode, int radius, boolean darkMode) {
        this.darkMode = darkMode;
        this.radius = radius;
        this.mode = mode;
        if (this.mode == ProspectorMode.FLUID) {
            // noinspection unchecked
            map = new HashMap[(radius * 2 - 1)][(radius * 2 - 1)];
        } else {
            // noinspection unchecked
            map = new HashMap[(radius * 2 - 1) * 16][(radius * 2 - 1) * 16];
        }
    }

    public void updateTexture(PacketProspecting packet) {
        int playerChunkX = packet.playerChunkX;
        int playerChunkZ = packet.playerChunkZ;
        playerXGui = packet.posX - (playerChunkX - this.radius + 1) * 16 + (packet.posX > 0 ? 1 : 0);
        playerYGui = packet.posZ - (playerChunkZ - this.radius + 1) * 16 + (packet.posX > 0 ? 1 : 0);

        int ox;
        if ((packet.chunkX > 0 && playerChunkX > 0) || (packet.chunkX < 0 && playerChunkX < 0)) {
            ox = Math.abs(Math.abs(packet.chunkX) - Math.abs(playerChunkX));
        } else {
            ox = Math.abs(playerChunkX) + Math.abs(packet.chunkX);
        }
        if (playerChunkX > packet.chunkX) {
            ox = -ox;
        }

        int oy;
        if ((packet.chunkZ > 0 && playerChunkZ > 0) || (packet.chunkZ < 0 && playerChunkZ < 0)) {
            oy = Math.abs(Math.abs(packet.chunkZ) - Math.abs(playerChunkZ));
        } else {
            oy = Math.abs(playerChunkZ) + Math.abs(packet.chunkZ);
        }
        if (playerChunkZ > packet.chunkZ) {
            oy = -oy;
        }

        int currentColumn = (this.radius - 1) + ox;
        int currentRow = (this.radius - 1) + oy;
        if (currentRow < 0) {
            return;
        }

        if (this.mode == ProspectorMode.FLUID) {
            map[currentColumn][currentRow] = packet.map[0][0] == null ?
                    emptyTag : packet.map[0][0];
        } else {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    map[x + currentColumn * 16][z + currentRow * 16] = packet.map[x][z] == null ?
                            emptyTag : packet.map[x][z];
                }
            }
        }
        loadTexture(null);
    }

    private BufferedImage getImage() {
        int wh = (this.radius * 2 - 1) * 16;
        BufferedImage image = new BufferedImage(wh, wh, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = image.getRaster();

        for (int i = 0; i < wh; i++) {
            for (int j = 0; j < wh; j++) {
                HashMap<Byte, String> data = this.map[this.mode == ProspectorMode.ORE ? i : i / 16][this.mode ==
                        ProspectorMode.ORE ? j : j / 16];
                // draw bg
                image.setRGB(i, j, ((data == null) ^ darkMode) ? Color.darkGray.getRGB() : Color.WHITE.getRGB());
                // draw ore
                if (this.mode == ProspectorMode.ORE && data != null) {
                    for (String orePrefix : data.values()) {
                        if (!selected.equals(SELECTED_ALL) && !selected.equals(orePrefix)) continue;
                        MaterialStack mterialStack = OreDictUnifier.getMaterial(OreDictUnifier.get(orePrefix));
                        image.setRGB(i, j, mterialStack == null ? orePrefix.hashCode() :
                                mterialStack.material.getMaterialRGB() | 0XFF000000);
                        break;
                    }
                }
                // draw grid
                if ((i) % 16 == 0 || (j) % 16 == 0) {
                    raster.setSample(i, j, 0, raster.getSample(i, j, 0) / 2);
                    raster.setSample(i, j, 1, raster.getSample(i, j, 1) / 2);
                    raster.setSample(i, j, 2, raster.getSample(i, j, 2) / 2);
                }
            }
        }
        return image;
    }

    @Override
    public void loadTexture(@Nullable IResourceManager resourceManager) {
        this.deleteGlTexture();
        int tId = getGlTextureId();
        if (tId < 0) return;
        TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), getImage(), false, false);
        imageWidth = (radius * 2 - 1) * 16;
        imageHeight = (radius * 2 - 1) * 16;
    }

    public void loadTexture(@Nullable IResourceManager resourceManager, String selected) {
        this.selected = selected;
        loadTexture(resourceManager);
    }

    public void loadTexture(@Nullable IResourceManager resourceManager, boolean darkMode) {
        this.darkMode = darkMode;
        loadTexture(resourceManager);
    }

    public String getSelected() {
        return selected;
    }

    public void draw(int x, int y) {
        if (this.glTextureId < 0) return;
        GlStateManager.bindTexture(this.getGlTextureId());
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        if (this.mode == ProspectorMode.FLUID) { // draw fluids in grid
            for (int cx = 0; cx < this.radius * 2 - 1; cx++) {
                for (int cz = 0; cz < this.radius * 2 - 1; cz++) {
                    if (this.map[cx][cz] != null && !this.map[cx][cz].isEmpty()) {
                        Fluid fluid = FluidRegistry.getFluid(this.map[cx][cz].get((byte) 1));
                        if (selected.equals(SELECTED_ALL) || selected.equals(fluid.getName())) {
                            RenderUtil.drawFluidForGui(new FluidStack(fluid, 1), 1, x + cx * 16 + 1, y + cz * 16 + 1,
                                    16, 16);
                        }
                    }
                }
            }
        }
        // draw red vertical line
        if (playerXGui % 16 > 7 || playerXGui % 16 == 0) {
            Gui.drawRect(x + playerXGui - 1, y, x + playerXGui, y + imageHeight, Color.RED.getRGB());
        } else {
            Gui.drawRect(x + playerXGui, y, x + playerXGui + 1, y + imageHeight, Color.RED.getRGB());
        }
        // draw red horizontal line
        if (playerYGui % 16 > 7 || playerYGui % 16 == 0) {
            Gui.drawRect(x, y + playerYGui - 1, x + imageWidth, y + playerYGui, Color.RED.getRGB());
        } else {
            Gui.drawRect(x, y + playerYGui, x + imageWidth, y + playerYGui + 1, Color.RED.getRGB());
        }
    }
}
