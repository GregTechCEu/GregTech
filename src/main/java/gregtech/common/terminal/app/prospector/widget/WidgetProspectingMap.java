package gregtech.common.terminal.app.prospector.widget;

import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.core.network.packets.PacketProspecting;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.api.worldgen.filler.FillerEntry;
import gregtech.common.terminal.app.prospector.ProspectingTexture;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.common.minimap.waypoints.WaypointWorld;

import java.awt.*;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.regex.Matcher;

public class WidgetProspectingMap extends Widget {
    private final int chunkRadius;
    private final WidgetOreList oreList;
    private final int mode;
    private final int scanTick;
    private boolean darkMode = false;
    private int chunkIndex = 0;

    @SideOnly(Side.CLIENT)
    private ProspectingTexture texture;
    @SideOnly(Side.CLIENT)
    private Consumer<PacketProspecting> onPacketReceived;
    Queue<PacketProspecting> packetQueue = new LinkedBlockingQueue<>();

    public static final int ORE_PROSPECTING_MODE = 0;
    public static final int FLUID_PROSPECTING_MODE = 1;
    private long lastClicked;

    private List<String> hoveredNames;
    private int color;

    public WidgetProspectingMap(int xPosition, int yPosition, int chunkRadius, WidgetOreList widgetOreList, int mode, int scanTick) {
        super(new Position(xPosition, yPosition), new Size(16 * (chunkRadius * 2 - 1), 16 * (chunkRadius * 2 - 1)));
        this.chunkRadius = chunkRadius;
        this.mode = mode;
        this.scanTick = scanTick;
        oreList = widgetOreList;
        if (oreList != null) {
            oreList.onSelected = name -> {
                if (texture != null) {
                    texture.loadTexture(null, name);
                }
            };
        }
    }

    @SideOnly(Side.CLIENT)
    public WidgetProspectingMap setOnPacketReceived(Consumer<PacketProspecting> onPacketReceived) {
        this.onPacketReceived = onPacketReceived;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public void setDarkMode(boolean mode) {
        if (darkMode != mode) {
            darkMode = mode;
            if (texture != null) {
                texture.loadTexture(null, darkMode);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean getDarkMode() {
        return darkMode;
    }

    @Override
    public void detectAndSendChanges() {
        EntityPlayer player = gui.entityPlayer;
        World world = player.world;
        if (FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % scanTick == 0 && chunkIndex < (chunkRadius * 2 - 1) * (chunkRadius * 2 - 1)) {

            int playerChunkX = player.chunkCoordX;
            int playerChunkZ = player.chunkCoordZ;

            int row = chunkIndex / (chunkRadius * 2 - 1);
            int column = chunkIndex % (chunkRadius * 2 - 1);

            int ox = column - chunkRadius + 1;
            int oz = row - chunkRadius + 1;

            Chunk chunk = world.getChunk(playerChunkX + ox, playerChunkZ + oz);
            PacketProspecting packet = new PacketProspecting(playerChunkX + ox, playerChunkZ + oz, playerChunkX, playerChunkZ, (int) player.posX, (int) player.posZ, this.mode);

            switch (mode) {
                case ORE_PROSPECTING_MODE:
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int ySize = chunk.getHeightValue(x, z);
                            for (int y = 1; y < ySize; y++) {
                                pos.setPos(x, y, z);
                                IBlockState state = chunk.getBlockState(pos);
                                ItemStack itemBlock = GTUtility.toItem(state);
                                if (GTUtility.isOre(itemBlock)) {
                                    boolean added = false;
                                    String oreDictString = OreDictUnifier.getOreDictionaryNames(itemBlock).stream().findFirst().get();
                                    OrePrefix prefix = OreDictUnifier.getPrefix(itemBlock);
                                    for (StoneType type : StoneType.STONE_TYPE_REGISTRY) {
                                        if (type.processingPrefix == prefix && type.shouldBeDroppedAsItem) {
                                            packet.addBlock(x, y, z, oreDictString);
                                            added = true;
                                            break;
                                        } else if (type.processingPrefix == prefix) {
                                            MaterialStack materialStack = OreDictUnifier.getMaterial(itemBlock);
                                            if (materialStack != null) {
                                                String oreDict = "ore" + oreDictString.replaceFirst(prefix.name(), "");
                                                packet.addBlock(x, y, z, oreDict);
                                                added = true;
                                                break;
                                            }
                                        }
                                    }
                                    // Probably other mod's ores
                                    if (!added) {
                                        // Fallback
                                        packet.addBlock(x, y, z, oreDictString);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case FLUID_PROSPECTING_MODE:
                    BedrockFluidVeinHandler.FluidVeinWorldEntry fStack = BedrockFluidVeinHandler.getFluidVeinWorldEntry(world, chunk.x, chunk.z);
                    if (fStack != null && fStack.getDefinition() != null) {
                        packet.addBlock(0, 3, 0, GTUtility.formatNumbers(100.0 * BedrockFluidVeinHandler.getOperationsRemaining(world, chunk.x, chunk.z)
                                / BedrockFluidVeinHandler.MAXIMUM_VEIN_OPERATIONS));
                        packet.addBlock(0, 2, 0, "" + BedrockFluidVeinHandler.getFluidYield(world, chunk.x, chunk.z));
                        packet.addBlock(0, 1, 0, BedrockFluidVeinHandler.getFluidInChunk(world, chunk.x, chunk.z).getName());
                    }
                    break;
                default:
                    break;
            }
            writeUpdateInfo(2, packet::writePacketData);
//            if (oreList != null) {
//                oreList.addOres(packet.ores, packet.mode);
//            }
            chunkIndex++;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (texture != null) {
            GlStateManager.color(1, 1, 1, 1);
            texture.draw(this.getPosition().x, this.getPosition().y);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            PacketProspecting packet = PacketProspecting.readPacketData(buffer);
            if (packet != null) {
                if (onPacketReceived != null) {
                    onPacketReceived.accept(packet);
                }
                addPacketToQueue(packet);
            }
        }
    }

    @Override
    public void updateScreen() {
        if (packetQueue != null) {
            int max = 10;
            while (max-- > 0 && !packetQueue.isEmpty()) {
                PacketProspecting packet = packetQueue.poll();
                if (texture == null) {
                    texture = new ProspectingTexture(packet.mode, chunkRadius, darkMode);
                }
                texture.updateTexture(packet);
                if (oreList != null) {
                    oreList.addOres(packet.ores, packet.mode);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void addPacketToQueue(PacketProspecting packet) {
        packetQueue.add(packet);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        // draw tooltips
        if (this.isMouseOverElement(mouseX, mouseY) && texture != null) {
            this.hoveredNames = new ArrayList<>();
            List<String> tooltips = new ArrayList<>();
            int cX = (mouseX - this.getPosition().x) / 16;
            int cZ = (mouseY - this.getPosition().y) / 16;
            if (cX >= chunkRadius * 2 - 1 || cZ >= chunkRadius * 2 - 1)
                return;
            // draw hover layer
            Gui.drawRect(cX * 16 + this.getPosition().x,
                    cZ * 16 + this.getPosition().y,
                    (cX + 1) * 16 + this.getPosition().x,
                    (cZ + 1) * 16 + this.getPosition().y,
                    new Color(0x4B6C6C6C, true).getRGB());

            //pick the color of the highest element for the waypoint color
            final int[] maxAmount = {0};

            if (this.mode == 0) { // draw ore
                tooltips.add(I18n.format("terminal.prospector.ore"));
                HashMap<String, Integer> oreInfo = new HashMap<>();
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        if (texture.map[cX * 16 + i][cZ * 16 + j] != null) {
                            texture.map[cX * 16 + i][cZ * 16 + j].values().forEach(dict -> {
                                String name = OreDictUnifier.get(dict).getDisplayName();
                                if (texture.getSelected().equals("[all]") || texture.getSelected().equals(dict)) {
                                    oreInfo.put(name, oreInfo.getOrDefault(name, 0) + 1);
                                    if (oreInfo.get(name) > maxAmount[0]) {
                                        maxAmount[0] = oreInfo.get(name);
                                        MaterialStack m = OreDictUnifier.getMaterial(OreDictUnifier.get(dict));
                                        if (m != null) {
                                            color = m.material.getMaterialRGB();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
                oreInfo.forEach((name, count) -> {
                    tooltips.add(name + " --- " + count);
                    hoveredNames.add(name);
                });
            } else if (this.mode == 1) {
                tooltips.add(I18n.format("terminal.prospector.fluid"));
                if (texture.map[cX][cZ] != null && !texture.map[cX][cZ].isEmpty()) {
                    if (texture.getSelected().equals("[all]") || texture.getSelected().equals(texture.map[cX][cZ].get((byte) 1))) {
                        FluidStack fluidStack = FluidRegistry.getFluidStack(texture.map[cX][cZ].get((byte) 1), 1);
                        tooltips.add(I18n.format("terminal.prospector.fluid.info",
                                fluidStack.getLocalizedName(),
                                texture.map[cX][cZ].get((byte) 2),
                                texture.map[cX][cZ].get((byte) 3)));
                        hoveredNames.add(fluidStack.getLocalizedName());
                        int amount = Integer.parseInt(texture.map[cX][cZ].get((byte) 2));
                        if (amount > maxAmount[0]) {
                            maxAmount[0] = amount;
                            color = fluidStack.getFluid().getColor(fluidStack);
                        }
                    }
                }
            }
            tooltips.add(I18n.format("terminal.prospector.waypoint.add"));
            this.drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {

        int cX = (mouseX - this.getPosition().x) / 16;
        int cZ = (mouseY - this.getPosition().y) / 16;

        if (cX >= chunkRadius * 2 - 1 || cZ >= chunkRadius * 2 - 1)
            return false;

        int xDiff = cX - (chunkRadius - 1);
        int zDiff = cZ - (chunkRadius - 1);

        int xPos = ((Minecraft.getMinecraft().player.chunkCoordX + xDiff) << 4) + 8;
        int zPos = ((Minecraft.getMinecraft().player.chunkCoordZ + zDiff) << 4) + 8;

        BlockPos b = new BlockPos(xPos, Minecraft.getMinecraft().world.getHeight(xPos, zPos), zPos);
        if (System.currentTimeMillis() - lastClicked < 200) {
            boolean added = false;
            trimHoveredNames();

            if (Loader.isModLoaded("journeymap")) {
                added = addJourneymapWaypoint(b);
            } else if (Loader.isModLoaded("voxelmap")) {
                added = addVoxelMapWaypoint(b);
            } else if (Loader.isModLoaded("xaerominimap")) {
                added = addXaeroMapWaypoint(b);
            }
            if (added) {
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentTranslation("behavior.prospector.added_waypoint"), false);
            }
        }
        this.lastClicked = System.currentTimeMillis();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    List<String> trimHoveredNames() {
        List<OreDepositDefinition> oreVeins = WorldGenRegistry.getOreDeposits();
        for (OreDepositDefinition odd : oreVeins) {
            for (FillerEntry fillerEntry : odd.getBlockFiller().getAllPossibleStates()) {
                List<String> matches = new ArrayList<>();
                Collection<IBlockState> pr = fillerEntry.getPossibleResults();
                for (IBlockState bs : pr) {
                    Set<String> ores = OreDictUnifier.getOreDictionaryNames(new ItemStack(bs.getBlock()));
                    for (String dict : ores) {
                        String name = OreDictUnifier.get(dict).getDisplayName();
                        if (hoveredNames.contains(name)) {
                            matches.add(name);
                        }
                    }
                }
                if (matches.size() > pr.size() / 2) {
                    this.hoveredNames.removeAll(matches);
                    this.hoveredNames.add(makePrettyName(odd.getDepositName()));
                }
            }
        }
        return this.hoveredNames;
    }

    public String makePrettyName(String name) {
        FileSystem fs = FileSystems.getDefault();
        String separator = fs.getSeparator();

        //Remove the leading "folderName\"
        String[] tempName = name.split(Matcher.quoteReplacement(separator));
        //Take the last entry in case of nested folders
        String newName = tempName[tempName.length - 1];
        //Remove the ".json"
        tempName = newName.split("\\.");
        //Take the first entry
        newName = tempName[0];
        //Replace all "_" with a space
        newName = newName.replaceAll("_", " ");
        //Capitalize the first letter
        newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);

        return newName;
    }

    @Optional.Method(modid = "journeymap")
    boolean addJourneymapWaypoint(BlockPos b) {
        journeymap.client.model.Waypoint journeyMapWaypoint = new Waypoint(hoveredNames.toString(),
                b,
                new Color(color),
                Waypoint.Type.Normal,
                Minecraft.getMinecraft().world.provider.getDimension());
        if (!WaypointStore.INSTANCE.getAll().contains(journeyMapWaypoint)) {
            WaypointStore.INSTANCE.save(journeyMapWaypoint);
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "voxelmap")
    boolean addVoxelMapWaypoint(BlockPos b) {
        Color c = new Color(color);
        TreeSet<Integer> world = new TreeSet<>();
        world.add(Minecraft.getMinecraft().world.provider.getDimension());

        IWaypointManager waypointManager = VoxelMap.getInstance().getWaypointManager();
        com.mamiyaotaru.voxelmap.util.Waypoint voxelMapWaypoint = new com.mamiyaotaru.voxelmap.util.Waypoint(hoveredNames.toString(),
                b.getX(),
                b.getZ(),
                Minecraft.getMinecraft().world.getHeight(b.getX(), b.getZ()),
                true,
                c.getRed(),
                c.getGreen(),
                c.getBlue(),
                Minecraft.getMinecraft().world.provider.getDimensionType().getSuffix(),
                Minecraft.getMinecraft().world.provider.getDimensionType().getName(),
                world);

        if (!waypointManager.getWaypoints().contains(voxelMapWaypoint)) {
            waypointManager.addWaypoint(voxelMapWaypoint);
            waypointManager.saveWaypoints();
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "xaerominimap")
    boolean addXaeroMapWaypoint(BlockPos b) {
        int red = clamp(color >> 16 & 0xFF);
        int green = clamp(color >> 8 & 0xFF);
        int blue = clamp(color & 0xFF);

        Color wpc = new Color(red, green, blue);
        int bestColorIndex = 0;
        int closestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < xaerosColors.length; i++) {
            Color c = xaerosColors[i];
            int diffRinner = Math.abs(c.getRed() - wpc.getRed());
            int diffGinner = Math.abs(c.getGreen() - wpc.getGreen());
            int diffBinner = Math.abs(c.getBlue() - wpc.getBlue());
            int distance = diffRinner * diffRinner + diffGinner * diffGinner + diffBinner * diffBinner;
            if (distance < closestDistance) {
                closestDistance = distance;
                bestColorIndex = i;
            }

        }

        XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
        WaypointSet wps = minimapSession.getWaypointsManager().getWaypoints();
        WaypointWorld ww = minimapSession.getWaypointsManager().getCurrentWorld();
        xaero.common.minimap.waypoints.Waypoint xaeroWaypoint = new xaero.common.minimap.waypoints.Waypoint(
                b.getX(),
                Minecraft.getMinecraft().world.getHeight(b.getX(), b.getZ()),
                b.getZ(),
                hoveredNames.toString(), "", bestColorIndex);
        if (!wps.getList().contains(xaeroWaypoint)) {
            wps.getList().add(xaeroWaypoint);
            try {
                minimapSession.getModMain().getSettings().saveWaypoints(ww);
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    private int clamp(int color) {
        if (color < 32) {
            return 0;
        } else if (color < 128) {
            return 128;
        } else if (color < 192) {
            return 192;
        } else {
            return 255;
        }
    }

    private Color[] xaerosColors = new Color[]{
            new Color(0, 0, 0),
            new Color(0, 0, 128),
            new Color(0, 128, 0),
            new Color(0, 128, 128),
            new Color(128, 0, 0),
            new Color(128, 0, 128),
            new Color(128, 128, 0),
            new Color(192, 192, 192),
            new Color(128, 128, 128),
            new Color(0, 0, 255),
            new Color(0, 255, 0),
            new Color(0, 255, 255),
            new Color(255, 0, 0),
            new Color(255, 0, 255),
            new Color(255, 255, 0),
            new Color(255, 255, 255),
    };
}
