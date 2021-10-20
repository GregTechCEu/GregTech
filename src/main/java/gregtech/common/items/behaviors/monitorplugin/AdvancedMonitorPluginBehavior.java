package gregtech.common.items.behaviors.monitorplugin;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.PluginWorldSceneRenderer;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.api.items.behavior.ProxyHolderPluginBehavior;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.BlockPatternChecker;
import gregtech.api.util.RenderUtil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.gui.widget.WidgetScrollBar;
import gregtech.common.gui.widget.monitor.WidgetPluginConfig;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedMonitorPluginBehavior extends ProxyHolderPluginBehavior {

    private float scale;
    private int rY;
    private int rX;
    private int rZ;
    private float spin;
    private boolean connect;

    //run-time
    @SideOnly(Side.CLIENT)
    private PluginWorldSceneRenderer worldSceneRenderer;
    @SideOnly(Side.CLIENT)
    private Map<BlockPos, Pair<List<MetaTileEntityMonitorScreen>, Vector3f>> connections;
    @SideOnly(Side.CLIENT)
    private BlockPos minPos;
    @SideOnly(Side.CLIENT)
    private int teCount;
    private boolean isValid;
    List<BlockPos> validPos;


    private void createWorldScene() {
        if (this.screen == null || this.screen.getWorld() == null) return;
        isValid = true;
        Map<BlockPos, BlockInfo> renderedBlocks = new HashMap<>();
        World world = this.screen.getWorld();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (BlockPos pos : validPos) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
        }
        minPos = new BlockPos(minX, minY, minZ);
        int rte = 0;
        for (BlockPos pos : validPos) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity != null) rte++;
            if (tileEntity instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) tileEntity).getMetaTileEntity() != null) {
                MetaTileEntityHolder holder = (MetaTileEntityHolder) tileEntity;
                MetaTileEntityHolder newHolder = new MetaTileEntityHolder();
                newHolder.setMetaTileEntity(holder.getMetaTileEntity().createMetaTileEntity(newHolder));
                newHolder.getMetaTileEntity().setFrontFacing(holder.getMetaTileEntity().getFrontFacing());
                renderedBlocks.put(pos.subtract(minPos), new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), newHolder));
            } else {
                renderedBlocks.put(pos.subtract(minPos), new BlockInfo(world.getBlockState(pos)));
            }
        }
        if (rte != teCount) {
            isValid = false;
            worldSceneRenderer = null;
            return;
        }
        worldSceneRenderer = new PluginWorldSceneRenderer(renderedBlocks);
        worldSceneRenderer.world.updateEntities();
        worldSceneRenderer.setBeforeWorldRender(() -> {
            Vector3f size = worldSceneRenderer.getSceneSize();
            Vector3f minPos = worldSceneRenderer.world.getMinPos();
            minPos = new Vector3f(minPos);
            minPos.add(new Vector3f(0f, 0f, 0f));

            GlStateManager.translate(-minPos.x, -minPos.y, -minPos.z);
            Vector3 centerPosition = new Vector3(size.x / 2.0f, size.y / 2.0f, size.z / 2.0f);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(-centerPosition.x, -centerPosition.y, -centerPosition.z);

            GlStateManager.translate(centerPosition.x, centerPosition.y, centerPosition.z);
            GlStateManager.rotate(rZ, 0, 0.0f, 1.0f);
            GlStateManager.rotate(rX, 1.0f, 0.0f, 0);
            GlStateManager.rotate(rY + (float) ((System.currentTimeMillis() / 20.0) * spin % 360.0f), 0.0f, 1.0f, 0.0f);
            GlStateManager.translate(-centerPosition.x, -centerPosition.y, -centerPosition.z);
        });
        worldSceneRenderer.setAfterWorldRender(() -> {
            if (connect && connections != null) {
                for (BlockPos pos : connections.keySet()) {
                    Vector3f winPos = worldSceneRenderer.project(pos, true);
                    connections.get(pos).setValue(winPos);
                    if (winPos != null) {
                        renderBlockOverLay(pos);
                    }
                }
            }
        });
        worldSceneRenderer.setOnLookingAt(this::renderBlockOverLay);
    }

    private void renderBlockOverLay(BlockPos pos) {
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        CCRenderState renderState = CCRenderState.instance();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR, tessellator.getBuffer());
        ColourMultiplier multiplier = new ColourMultiplier(0);
        renderState.setPipeline(new Translation(pos), multiplier);
        BlockRenderer.BlockFace blockFace = new BlockRenderer.BlockFace();
        renderState.setModel(blockFace);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            float diffuse = LightUtil.diffuseLight(renderSide);
            int color = (int) (255 * diffuse);
            multiplier.colour = gregtech.api.util.RenderUtil.packColor(color, color, color, 100);
            blockFace.loadCuboidFace(Cuboid6.full, renderSide.getIndex());
            renderState.render();
        }
        renderState.draw();
        GlStateManager.enableTexture2D();
    }

    public void setConfig(float scale, int rY, int rX, int rZ, float spin, boolean connect) {
        if (this.scale == scale && this.rY == rY && this.rX == rX && this.rZ == rZ && this.spin == spin && this.connect == connect)
            return;
        if (scale < 0.3 || scale > 2 || rY < 0 || rY > 360 || rX < 0 || rX > 360 || rZ < 0 || rZ > 360 || spin < 0 || spin > 2)
            return;
        this.scale = scale;
        this.rY = rY;
        this.rX = rX;
        this.rZ = rZ;
        this.spin = spin;
        this.connect = connect;
        writePluginData(1, buffer -> {
            buffer.writeFloat(scale);
            buffer.writeVarInt(rY);
            buffer.writeVarInt(rX);
            buffer.writeVarInt(rZ);
            buffer.writeFloat(spin);
            buffer.writeBoolean(connect);
        });
        markAsDirty();
    }

    @Override
    public void update() {
        super.update();
        if (this.screen.getOffsetTimer() % 20 == 0) {
            if (this.screen.getWorld().isRemote) { // check connections
                if (worldSceneRenderer == null && validPos != null && validPos.size() > 0) {
                    createWorldScene();
                }
                if (this.connect && worldSceneRenderer != null && this.screen.getController() instanceof MetaTileEntityCentralMonitor) {
                    if (connections == null) connections = new HashMap<>();
                    connections.clear();
                    for (MetaTileEntityMonitorScreen[] monitorScreens : ((MetaTileEntityCentralMonitor) this.screen.getController()).screens) {
                        for (MetaTileEntityMonitorScreen screen : monitorScreens) {
                            if (screen != null && screen.plugin instanceof FakeGuiPluginBehavior && ((FakeGuiPluginBehavior) screen.plugin).getHolder() == this.holder) {
                                MetaTileEntity met = ((FakeGuiPluginBehavior) screen.plugin).getRealMTE();
                                if (met != null) {
                                    BlockPos pos = met.getPos().subtract(minPos);
                                    Pair<List<MetaTileEntityMonitorScreen>, Vector3f> tuple = connections.getOrDefault(pos, Pair.of(new ArrayList<>(), null));
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
                            PatternMatchContext result = BlockPatternChecker.checkPatternAt(entity);
                            if (result != null && result.get("validPos") != null) {
                                validPos = result.get("validPos");
                                writePluginData(0, buf -> {
                                    int te = 0;
                                    buf.writeVarInt(validPos.size());
                                    for (BlockPos pos : validPos) {
                                        buf.writeBlockPos(pos);
                                        if (this.screen.getWorld().getTileEntity(pos) != null)
                                            te++;
                                    }
                                    buf.writeVarInt(te);
                                });
                                isValid = true;
                            } else {
                                validPos.clear();
                            }
                        }
                    } else if (isValid) {
                        writePluginData(0, buf -> {
                            buf.writeVarInt(0);
                        });
                        isValid = false;
                    }
                }
            }
        }
    }

    @Override
    public WidgetPluginConfig customUI(WidgetPluginConfig widgetGroup, IUIHolder holder, EntityPlayer entityPlayer) {
        return widgetGroup.setSize(260, 170)
                .widget(new WidgetScrollBar(25, 20, 210, 0.3f, 2, 0.1f, value -> {
                    setConfig(value, this.rY, this.rX, this.rZ, this.spin, this.connect);
                }).setTitle("scale", 0XFFFFFFFF).setInitValue(this.scale))
                .widget(new WidgetScrollBar(25, 40, 210, 0, 360, 1, value -> {
                    setConfig(this.scale, value.intValue(), this.rX, this.rZ, this.spin, this.connect);
                }).setTitle("rotationY", 0XFFFFFFFF).setInitValue(this.rY))
                .widget(new WidgetScrollBar(25, 60, 210, 0, 360, 1, value -> {
                    setConfig(this.scale, this.rY, value.intValue(), this.rZ, this.spin, this.connect);
                }).setTitle("rotationX", 0XFFFFFFFF).setInitValue(this.rX))
                .widget(new WidgetScrollBar(25, 80, 210, 0, 360, 1, value -> {
                    setConfig(this.scale, this.rY, this.rX, value.intValue(), this.spin, this.connect);
                }).setTitle("rotationZ", 0XFFFFFFFF).setInitValue(this.rZ))
                .widget(new WidgetScrollBar(25, 100, 210, 0, 2, 0.1f, value -> {
                    setConfig(this.scale, this.rY, this.rX, this.rZ, value, this.connect);
                }).setTitle("spinDur", 0XFFFFFFFF).setInitValue(this.spin))
                .widget(new LabelWidget(25, 135, "Fake GUI:", 0XFFFFFFFF))
                .widget(new ToggleButtonWidget(80, 130, 20, 20, () -> this.connect, state -> {
                    setConfig(this.scale, this.rY, this.rX, this.rZ, this.spin, state);
                }));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (validPos != null && validPos.size() > 0) {
            int te = 0;
            buf.writeVarInt(validPos.size());
            for (BlockPos pos : validPos) {
                buf.writeBlockPos(pos);
                if (this.screen.getWorld().getTileEntity(pos) != null) {
                    te++;
                }
            }
            buf.writeVarInt(te);
        } else {
            buf.writeVarInt(0);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        loadValidPos(buf);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setFloat("scale", this.scale);
        data.setInteger("rY", this.rY);
        data.setInteger("rX", this.rX);
        data.setInteger("rZ", this.rZ);
        data.setFloat("spin", this.spin);
        data.setBoolean("connect", this.connect);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.scale = data.hasKey("scale") ? data.getFloat("scale") : 0.6f;
        this.rY = data.hasKey("rY") ? data.getInteger("rY") : 45;
        this.rX = data.hasKey("rX") ? data.getInteger("rX") : 0;
        this.rZ = data.hasKey("rZ") ? data.getInteger("rZ") : 0;
        this.spin = data.hasKey("spin") ? data.getFloat("spin") : 0f;
        this.connect = data.hasKey("connect") && data.getBoolean("connect");
    }

    @Override
    public void readPluginData(int id, PacketBuffer buf) {
        super.readPluginData(id, buf);
        if (id == 0) {
            loadValidPos(buf);
        } else if (id == 1) {
            this.scale = buf.readFloat();
            this.rY = buf.readVarInt();
            this.rX = buf.readVarInt();
            this.rZ = buf.readVarInt();
            this.spin = buf.readFloat();
            this.connect = buf.readBoolean();
        }
    }

    private void loadValidPos(PacketBuffer buf) {
        int size = buf.readVarInt();
        if (size > 0) {
            validPos = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                validPos.add(buf.readBlockPos());
            }
            teCount = buf.readVarInt();
            createWorldScene();
        } else {
            validPos = null;
            worldSceneRenderer = null;
            isValid = false;
        }
    }

    @Override
    public void readPluginAction(EntityPlayerMP player, int id, PacketBuffer buf) {
        super.readPluginAction(player, id, buf);
        if (id == 1) { //open GUI
            BlockPos pos = buf.readBlockPos();
            TileEntity tileEntity = this.screen.getWorld().getTileEntity(pos);
            if (tileEntity instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) tileEntity).isValid()) {
                ((MetaTileEntityHolder) tileEntity).getMetaTileEntity().onRightClick(player, EnumHand.MAIN_HAND, ((MetaTileEntityHolder) tileEntity).getMetaTileEntity().getFrontFacing(), null);
            }
        }
    }

    @Override
    public boolean onClickLogic(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, boolean isRight, double x, double y) {
        if (this.screen.getWorld().isRemote) {
            if (this.worldSceneRenderer != null) {
                BlockPos pos = this.worldSceneRenderer.screenPos2BlockPos(
                        (int) (x * PluginWorldSceneRenderer.getWidth()),
                        (int) ((1 - y) * PluginWorldSceneRenderer.getHeight()));
                if (pos != null) {
                    writePluginAction(1, buf -> buf.writeBlockPos(pos.add(minPos)));
                }
            }
        }
        return true;
    }

    @Override
    protected void onHolderChanged(MetaTileEntityHolder lastHolder) {
        if (this.screen.getWorld().isRemote) {
            teCount = 0;
            worldSceneRenderer = null;
        }
        validPos = null;
        isValid = false;
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
            Tuple<Double, Double> mousePos = this.screen.checkLookingAt(rayTraceResult);
            if (mousePos != null) {
                worldSceneRenderer.render(0, 0, 1, 1, (int) (mousePos.getFirst() * 1080), (int) (1080 - mousePos.getSecond() * 1080));
            } else {
                worldSceneRenderer.render(0, 0, 1, 1, 0, 0);
            }

            if (this.connect && connections != null) {
                GlStateManager.scale(1 / this.screen.scale, 1 / this.screen.scale, 1);
                int sW = PluginWorldSceneRenderer.getWidth();
                int sH = PluginWorldSceneRenderer.getHeight();
                for (Pair<List<MetaTileEntityMonitorScreen>, Vector3f> tuple : connections.values()) {
                    Vector3f origin = tuple.getRight();
                    List<MetaTileEntityMonitorScreen> screens = tuple.getLeft();
                    if (origin != null) {
                        float oX = (origin.x / sW - 0.025f) * this.screen.scale;
                        float oY = (1 - origin.y / sH) * this.screen.scale;
                        RenderUtil.renderRect(oX, oY, 0.05f, 0.05f, 0.002f, 0XFFFFFF00);
                        for (MetaTileEntityMonitorScreen screen : screens) {
                            float dX = screen.getX() - this.screen.getX() - 0.025f;
                            float dY = screen.getY() - this.screen.getY() + screen.scale / 2 - 0.025f;
                            float rX = screen.getX() - this.screen.getX() + screen.scale - 0.025f;
                            float rY = screen.getY() - this.screen.getY() + screen.scale / 2 - 0.025f;
                            if ((oX - dX) * (oX - dX) + (oY - dY) * (oY - dY) > (oX - rX) * (oX - rX) + (oY - rY) * (oY - rY)) {
                                dX = rX;
                                dY = rY;
                            }
                            RenderUtil.renderRect(dX, dY, 0.05f, 0.05f, 0.002f, screen.frameColor);
                            RenderUtil.renderLine(oX + 0.025f, oY + 0.025f, dX + 0.025f, dY + 0.025f, 0.01f, screen.frameColor);
                        }
                    }

                }
            }
            GlStateManager.popMatrix();
        }
    }
}
