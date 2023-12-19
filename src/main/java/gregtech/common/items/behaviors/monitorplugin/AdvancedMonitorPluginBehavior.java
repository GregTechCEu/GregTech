package gregtech.common.items.behaviors.monitorplugin;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.api.items.behavior.ProxyHolderPluginBehavior;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.scene.FBOWorldSceneRenderer;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.TrackedDummyWorld;
import gregtech.common.gui.widget.WidgetScrollBar;
import gregtech.common.gui.widget.monitor.WidgetPluginConfig;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;

import javax.vecmath.Vector3f;

public class AdvancedMonitorPluginBehavior extends ProxyHolderPluginBehavior {

    @SideOnly(Side.CLIENT)
    private static Framebuffer FBO;
    private static final int RESOLUTION = 1080;

    private float scale;
    private int rotationPitch;
    private int rotationYaw;
    private float spin;
    private boolean connect;

    // run-time
    @SideOnly(Side.CLIENT)
    private FBOWorldSceneRenderer worldSceneRenderer;
    @SideOnly(Side.CLIENT)
    private Map<BlockPos, Pair<List<MetaTileEntityMonitorScreen>, Vector3f>> connections;
    @SideOnly(Side.CLIENT)
    private Vector3f center;
    @SideOnly(Side.CLIENT)
    private double[] lastMouse;
    private boolean isValid;
    Set<BlockPos> validPos;

    @SideOnly(Side.CLIENT)
    private void createWorldScene() {
        if (this.screen == null || this.screen.getWorld() == null) return;
        isValid = true;
        World world = this.screen.getWorld();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : validPos) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        if (FBO == null) {
            FBO = new Framebuffer(RESOLUTION, RESOLUTION, true);
        }
        TrackedDummyWorld dummyWorld = new TrackedDummyWorld(world);
        dummyWorld.setRenderFilter(pos -> validPos.contains(pos));
        worldSceneRenderer = new FBOWorldSceneRenderer(dummyWorld, FBO);
        worldSceneRenderer.addRenderedBlocks(validPos, null);
        center = new Vector3f((minX + maxX) / 2f + 0.5f, (minY + maxY) / 2f + 0.5f, (minZ + maxZ) / 2f + 0.5f);
        worldSceneRenderer.setCameraLookAt(center, 10 / scale, Math.toRadians(rotationPitch),
                Math.toRadians(rotationYaw));
        worldSceneRenderer.setBeforeWorldRender(this::renderBefore);
        worldSceneRenderer.setAfterWorldRender(this::renderAfter);
        worldSceneRenderer.setOnLookingAt(rayTrace -> renderBlockOverLay(rayTrace.getBlockPos()));
    }

    @SideOnly(Side.CLIENT)
    private void renderBefore(WorldSceneRenderer renderer) {
        if (spin > 0 && lastMouse == null) {
            worldSceneRenderer.setCameraLookAt(center, 10 / scale, Math.toRadians(rotationPitch),
                    Math.toRadians(rotationYaw));
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderAfter(WorldSceneRenderer renderer) {
        if (lastMouse != null) {
            int mouseX = (int) (RESOLUTION * lastMouse[0]);
            int mouseY = (int) (RESOLUTION * (1 - (lastMouse[1])));
            Vector3f hitPos = WorldSceneRenderer.unProject(mouseX, mouseY);
            Vec3d eyePos = new Vec3d(renderer.getEyePos().x, renderer.getEyePos().y, renderer.getEyePos().z);
            hitPos.scale(2); // Double view range to ensure pos can be seen.
            Vec3d endPos = new Vec3d((hitPos.x - eyePos.x), (hitPos.y - eyePos.y), (hitPos.z - eyePos.z));
            double min = Float.MAX_VALUE;
            BlockPos pos = null;
            for (BlockPos core : validPos) {
                IBlockState blockState = renderer.world.getBlockState(core);
                if (blockState.getBlock() == Blocks.AIR) {
                    continue;
                }
                RayTraceResult hit = blockState.collisionRayTrace(renderer.world, core, eyePos, endPos);
                if (hit != null && hit.typeOfHit != RayTraceResult.Type.MISS) {
                    double dist = eyePos.distanceTo(new Vec3d(hit.getBlockPos()));
                    if (dist < min) {
                        min = dist;
                        pos = hit.getBlockPos();
                    }
                }
            }
            if (pos != null) {
                renderBlockOverLay(pos);
            }
        }
        if (connect && connections != null) {
            for (Map.Entry<BlockPos, Pair<List<MetaTileEntityMonitorScreen>, Vector3f>> entry : connections
                    .entrySet()) {
                BlockPos pos = entry.getKey();
                Vector3f winPos = WorldSceneRenderer.project(pos);
                entry.getValue().setValue(winPos);
                if (winPos != null) {
                    renderBlockOverLay(pos);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderBlockOverLay(BlockPos pos) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.translate((pos.getX() + 0.5), (pos.getY() + 0.5), (pos.getZ() + 0.5));
        GlStateManager.scale(1.01, 1.01, 1.01);
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        CCRenderState renderState = CCRenderState.instance();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR, tessellator.getBuffer());
        ColourMultiplier multiplier = new ColourMultiplier(0);
        renderState.setPipeline(new Translation(-0.5, -0.5, -0.5), multiplier);
        BlockRenderer.BlockFace blockFace = new BlockRenderer.BlockFace();
        renderState.setModel(blockFace);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            multiplier.colour = RenderUtil.packColor(255, 255, 255, 100);
            blockFace.loadCuboidFace(Cuboid6.full, renderSide.getIndex());
            renderState.render();
        }
        renderState.draw();
        GlStateManager.scale(1 / 1.01, 1 / 1.01, 1 / 1.01);
        GlStateManager.translate(-(pos.getX() + 0.5), -(pos.getY() + 0.5), -(pos.getZ() + 0.5));
        GlStateManager.enableTexture2D();

        GlStateManager.color(1, 1, 1, 1);
    }

    public void setConfig(float scale, int rY, int rX, float spin, boolean connect) {
        if (this.scale == scale && this.rotationPitch == rY && this.rotationYaw == rX && this.spin == spin &&
                this.connect == connect)
            return;
        if (scale < 0.3 || scale > 2 || rY < 0 || rY > 360 || rX < -90 || rX > 90 || spin < 0 || spin > 2)
            return;
        this.scale = scale;
        this.rotationPitch = rY;
        this.rotationYaw = rX;
        this.spin = spin;
        this.connect = connect;
        if (this.screen.getWorld().isRemote) {
            if (worldSceneRenderer != null) {
                worldSceneRenderer.setCameraLookAt(center, 10 / scale, Math.toRadians(rY), Math.toRadians(rX));
            }
        } else {
            writePluginData(GregtechDataCodes.UPDATE_PLUGIN_CONFIG, buffer -> {
                buffer.writeFloat(scale);
                buffer.writeVarInt(rY);
                buffer.writeVarInt(rX);
                buffer.writeFloat(spin);
                buffer.writeBoolean(connect);
            });
            markAsDirty();
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.screen.getOffsetTimer() % 20 == 0) {
            if (this.screen.getWorld().isRemote) { // check connections
                if (worldSceneRenderer == null && validPos != null && validPos.size() > 0) {
                    createWorldScene();
                }
                if (this.connect && worldSceneRenderer != null &&
                        this.screen.getController() instanceof MetaTileEntityCentralMonitor) {
                    if (connections == null) connections = new HashMap<>();
                    connections.clear();
                    for (MetaTileEntityMonitorScreen[] monitorScreens : ((MetaTileEntityCentralMonitor) this.screen
                            .getController()).screens) {
                        for (MetaTileEntityMonitorScreen screen : monitorScreens) {
                            if (screen != null && screen.plugin instanceof FakeGuiPluginBehavior &&
                                    ((FakeGuiPluginBehavior) screen.plugin).getHolder() == this.holder) {
                                MetaTileEntity met = ((FakeGuiPluginBehavior) screen.plugin).getRealMTE();
                                if (met != null) {
                                    BlockPos pos = met.getPos();
                                    Pair<List<MetaTileEntityMonitorScreen>, Vector3f> tuple = connections
                                            .getOrDefault(pos, new MutablePair<>(new ArrayList<>(), null));
                                    tuple.getLeft().add(screen);
                                    connections.put(pos, tuple);
                                }
                            }
                        }
                    }
                }
            } else { // check multi-block valid
                if (holder != null && holder.getMetaTileEntity() instanceof MultiblockControllerBase) {
                    MultiblockControllerBase entity = (MultiblockControllerBase) holder.getMetaTileEntity();
                    if (entity.isStructureFormed()) {
                        if (!isValid) {
                            PatternMatchContext result = entity.structurePattern.checkPatternFastAt(
                                    entity.getWorld(), entity.getPos(), entity.getFrontFacing().getOpposite(),
                                    entity.getUpwardsFacing(), entity.allowsFlip());
                            if (result != null) {
                                validPos = entity.structurePattern.cache.keySet().stream().map(BlockPos::fromLong)
                                        .collect(Collectors.toSet());
                                writePluginData(GregtechDataCodes.UPDATE_ADVANCED_VALID_POS, buf -> {
                                    buf.writeVarInt(validPos.size());
                                    for (BlockPos pos : validPos) {
                                        buf.writeBlockPos(pos);
                                    }
                                });
                                isValid = true;
                            } else {
                                validPos = Collections.emptySet();
                            }
                        }
                    } else if (isValid) {
                        writePluginData(GregtechDataCodes.UPDATE_ADVANCED_VALID_POS, buf -> buf.writeVarInt(0));
                        isValid = false;
                    }
                }
            }
        }
        if (this.screen.getWorld().isRemote && spin > 0 && lastMouse == null) {
            rotationPitch = (int) ((rotationPitch + spin * 4) % 360);
        }
    }

    @Override
    public WidgetPluginConfig customUI(WidgetPluginConfig widgetGroup, IUIHolder holder, EntityPlayer entityPlayer) {
        return widgetGroup.setSize(260, 170)
                .widget(new WidgetScrollBar(25, 20, 210, 0.3f, 2, 0.1f,
                        value -> setConfig(value, this.rotationPitch, this.rotationYaw, this.spin, this.connect))
                                .setTitle("zoom", 0XFFFFFFFF).setInitValue(this.scale))
                .widget(new WidgetScrollBar(25, 40, 210, 0, 360, 1,
                        value -> setConfig(this.scale, value.intValue(), this.rotationYaw, this.spin, this.connect))
                                .setTitle("rotationPitch", 0XFFFFFFFF).setInitValue(this.rotationPitch))
                .widget(new WidgetScrollBar(25, 60, 210, -90, 90, 1,
                        value -> setConfig(this.scale, this.rotationPitch, value.intValue(), this.spin, this.connect))
                                .setTitle("rotationYaw", 0XFFFFFFFF).setInitValue(this.rotationYaw))
                .widget(new WidgetScrollBar(25, 100, 210, 0, 2, 0.1f,
                        value -> setConfig(this.scale, this.rotationPitch, this.rotationYaw, value, this.connect))
                                .setTitle("spinDur", 0XFFFFFFFF).setInitValue(this.spin))
                .widget(new LabelWidget(25, 135, "Fake GUI:", 0XFFFFFFFF))
                .widget(new ToggleButtonWidget(80, 130, 20, 20, () -> this.connect,
                        state -> setConfig(this.scale, this.rotationPitch, this.rotationYaw, this.spin, state)));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (validPos != null && validPos.size() > 0) {
            buf.writeVarInt(validPos.size());
            for (BlockPos pos : validPos) {
                buf.writeBlockPos(pos);
            }
        } else {
            buf.writeVarInt(0);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        loadValidPos(buf);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setFloat("scale", this.scale);
        data.setInteger("rY", this.rotationPitch);
        data.setInteger("rX", this.rotationYaw);
        data.setFloat("spin", this.spin);
        data.setBoolean("connect", this.connect);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.scale = data.hasKey("scale") ? data.getFloat("scale") : 0.6f;
        this.rotationPitch = data.hasKey("rY") ? data.getInteger("rY") : 45;
        this.rotationYaw = data.hasKey("rX") ? data.getInteger("rX") : 0;
        this.spin = data.hasKey("spin") ? data.getFloat("spin") : 0f;
        this.connect = data.hasKey("connect") && data.getBoolean("connect");
    }

    @Override
    public void readPluginData(int id, PacketBuffer buf) {
        if (id == GregtechDataCodes.UPDATE_ADVANCED_VALID_POS) {
            loadValidPos(buf);
        } else if (id == GregtechDataCodes.UPDATE_PLUGIN_CONFIG) {
            this.scale = buf.readFloat();
            this.rotationPitch = buf.readVarInt();
            this.rotationYaw = buf.readVarInt();
            this.spin = buf.readFloat();
            this.connect = buf.readBoolean();
            if (worldSceneRenderer != null) {
                worldSceneRenderer.setCameraLookAt(center, 10 / scale, Math.toRadians(rotationPitch),
                        Math.toRadians(rotationYaw));
            }
        }
    }

    private void loadValidPos(PacketBuffer buf) {
        int size = buf.readVarInt();
        if (size > 0) {
            validPos = new HashSet<>();
            for (int i = 0; i < size; i++) {
                validPos.add(buf.readBlockPos());
            }
            createWorldScene();
        } else {
            validPos = null;
            worldSceneRenderer = null;
            isValid = false;
        }
    }

    @Override
    public void readPluginAction(EntityPlayerMP player, int id, PacketBuffer buf) {
        if (id == GregtechDataCodes.ACTION_PLUGIN_CONFIG) { // open GUI
            BlockPos pos = buf.readBlockPos();
            TileEntity tileEntity = this.screen.getWorld().getTileEntity(pos);
            if (tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity).isValid()) {
                ((IGregTechTileEntity) tileEntity).getMetaTileEntity().onRightClick(player, EnumHand.MAIN_HAND,
                        ((IGregTechTileEntity) tileEntity).getMetaTileEntity().getFrontFacing(), null);
            }
        }
    }

    @Override
    public boolean onClickLogic(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, boolean isRight, double x,
                                double y) {
        if (this.screen.getWorld().isRemote) {
            if (this.worldSceneRenderer != null) {
                RayTraceResult rayTrace = this.worldSceneRenderer.screenPos2BlockPosFace((int) (x * RESOLUTION),
                        (int) ((1 - y) * RESOLUTION));
                if (rayTrace != null && isRight) {
                    writePluginAction(GregtechDataCodes.ACTION_PLUGIN_CONFIG,
                            buf -> buf.writeBlockPos(rayTrace.getBlockPos()));
                }
            }
        }
        return true;
    }

    @Override
    protected void onHolderChanged(IGregTechTileEntity lastHolder) {
        if (!this.screen.getWorld().isRemote) {
            validPos = null;
            isValid = false;
            writePluginData(GregtechDataCodes.UPDATE_ADVANCED_VALID_POS, buf -> buf.writeVarInt(0));
        }
    }

    @Override
    public MonitorPluginBaseBehavior createPlugin() {
        return new AdvancedMonitorPluginBehavior();
    }

    @Override
    public void renderPlugin(float partialTicks, RayTraceResult rayTraceResult) {
        if (worldSceneRenderer != null && this.screen != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5, -0.5, 0.01);
            double[] currentMouse = this.screen.checkLookingAt(rayTraceResult);
            if (currentMouse != null) {
                worldSceneRenderer.render(0, 0, 1, 1, (float) currentMouse[0], (float) currentMouse[1]);
                if (lastMouse != null) {
                    if (Mouse.isButtonDown(0)) {
                        rotationPitch += (currentMouse[0] - lastMouse[0]) * 300;
                        rotationPitch = rotationPitch % 360;
                        rotationYaw = (int) MathHelper.clamp(rotationYaw + (currentMouse[1] - lastMouse[1]) * 300,
                                -89.9, 89.9);
                    }
                    worldSceneRenderer.setCameraLookAt(center, 10 / scale, Math.toRadians(rotationPitch),
                            Math.toRadians(rotationYaw));
                }
            } else {
                worldSceneRenderer.render(0, 0, 1, 1, 0, 0);
            }

            lastMouse = currentMouse;

            if (this.connect && connections != null) {
                GlStateManager.scale(1 / this.screen.scale, 1 / this.screen.scale, 1);
                for (Pair<List<MetaTileEntityMonitorScreen>, Vector3f> tuple : connections.values()) {
                    Vector3f origin = tuple.getRight();
                    List<MetaTileEntityMonitorScreen> screens = tuple.getLeft();
                    if (origin != null) {
                        float oX = (origin.x / RESOLUTION - 0.025f) * this.screen.scale;
                        float oY = (1 - origin.y / RESOLUTION) * this.screen.scale;
                        RenderUtil.renderRect(oX, oY, 0.05f, 0.05f, 0.002f, 0XFFFFFF00);
                        for (MetaTileEntityMonitorScreen screen : screens) {
                            float dX = screen.getX() - this.screen.getX() - 0.025f;
                            float dY = screen.getY() - this.screen.getY() + screen.scale / 2 - 0.025f;
                            float rX = screen.getX() - this.screen.getX() + screen.scale - 0.025f;
                            float rY = screen.getY() - this.screen.getY() + screen.scale / 2 - 0.025f;
                            if ((oX - dX) * (oX - dX) + (oY - dY) * (oY - dY) >
                                    (oX - rX) * (oX - rX) + (oY - rY) * (oY - rY)) {
                                dX = rX;
                                dY = rY;
                            }
                            RenderUtil.renderRect(dX, dY, 0.05f, 0.05f, 0.002f, screen.frameColor);
                            RenderUtil.renderLine(oX + 0.025f, oY + 0.025f, dX + 0.025f, dY + 0.025f, 0.01f,
                                    screen.frameColor);
                        }
                    }

                }
            }
            GlStateManager.popMatrix();
        }
    }
}
