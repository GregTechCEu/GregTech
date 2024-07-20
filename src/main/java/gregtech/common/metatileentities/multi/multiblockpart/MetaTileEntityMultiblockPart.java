package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOrientedCubeRenderer;
import gregtech.client.renderer.texture.custom.FireboxActiveRenderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.SYNC_CONTROLLER;

public abstract class MetaTileEntityMultiblockPart extends MetaTileEntity
                                                   implements IMultiblockPart, ITieredMetaTileEntity {

    private final int tier;
    private BlockPos controllerPos;
    private Class<? extends MultiblockControllerBase> controllerClass;
    private List<@NotNull MultiblockControllerBase> controllers;

    /**
     * Client side, used for rendering.
     */
    private MultiblockControllerBase lastController = null;
    private int wallshareCount = 0;
    protected String attachedSubstructureName;
    protected ICubeRenderer hatchTexture = null;

    public MetaTileEntityMultiblockPart(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        initializeInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (controllerPos != null) GTLog.logger.info("pos: " + controllerPos);
        ICubeRenderer baseTexture = getBaseTexture();
        pipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));

        if (baseTexture instanceof FireboxActiveRenderer || baseTexture instanceof SimpleOrientedCubeRenderer) {
            baseTexture.renderOriented(renderState, translation, pipeline, getFrontFacing());
        } else {
            baseTexture.render(renderState, translation, pipeline);
        }
    }

    public int getTier() {
        return tier;
    }

    public @Nullable MultiblockControllerBase getController() {
        tryInitControllers();

        if (getWorld() == null) {
            this.controllers.clear();
            lastController = null;
            return null;
        }

        if (getWorld().isRemote) { // client check, on client controllers is always empty
            if (lastController == null) {
                if (controllerPos != null) {
                    this.lastController = (MultiblockControllerBase) GTUtility.getMetaTileEntity(getWorld(), controllerPos);
                }
            } else if (!lastController.isValid()) {
                this.lastController = null;
            }

            return lastController;
        }

        if (controllers.isEmpty()) return null; // server check, remove controller if it is no longer valid

        MultiblockControllerBase controller = controllers.get(controllers.size() - 1);

        if (!controller.isValid()) {
            removeController(controller);
        }

        return controller;
    }

    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            this.hatchTexture = controller.getInactiveTexture(this);
            return controller.getBaseTexture(this);
        } else if (hatchTexture != null) return hatchTexture;
        return Textures.VOLTAGE_CASINGS[tier];
    }

    public boolean shouldRenderOverlay() {
        MultiblockControllerBase controller = getController();
        return controller == null || controller.shouldRenderOverlay(this);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        MultiblockControllerBase controller = getController();
        buf.writeBoolean(controller != null);
        if (controller != null) {
            buf.writeBlockPos(controller.getPos());
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            this.controllerPos = buf.readBlockPos();
            this.lastController = null;
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SYNC_CONTROLLER) {
            long data = buf.readLong();
            if (data != Long.MAX_VALUE) {
                this.controllerPos = BlockPos.fromLong(data);
            } else {
                controllerPos = null;
                GTLog.logger.info("hi");
            }
            this.lastController = null;
            scheduleRenderUpdate();
        }
    }

    private void addController(@NotNull MultiblockControllerBase controller) {
        tryInitControllers();

        // this should be called after canPartShare has already checked the class, just a safeguard
        if (controllerClass != null && controller.getClass() != controllerClass) {
            GTLog.logger.error("addController(MultiblockControllerBase) was called on " + getClass().getName() + " with a mismatched name(original: " + controllerClass.getName() + " new: " + controller.getClass().getName() +")! Ignoring the call.");
            return;
        }

        if (controllers.isEmpty()) controllerClass = controller.getClass();

        this.controllers.add(controller);

        syncLastController();
    }

    private void removeController(@NotNull MultiblockControllerBase controller) {
        tryInitControllers();

        if (!controllers.remove(controller)) {
            GTLog.logger.error("removeController(MultiblockControllerBase) was called on " + getClass().getName() + " while the given controller wasn't in the list!");
        }

        if (controllers.isEmpty()) {
            controllerClass = null;
        }

        syncLastController();
    }

    private void syncLastController() {
        if (getWorld().isRemote) return;

        if (controllers.isEmpty()) {
            writeCustomData(SYNC_CONTROLLER, buf -> buf.writeLong(Long.MAX_VALUE));
            return;
        }

        MultiblockControllerBase controller = controllers.get(controllers.size() - 1);

        writeCustomData(SYNC_CONTROLLER, buf -> buf.writeBlockPos(controller.getPos()));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        MultiblockControllerBase controller = getController();
        if (!getWorld().isRemote && controller != null) {
            controller.invalidateStructure();
        }
    }

    @Override
    public void addToMultiBlock(@NotNull MultiblockControllerBase controllerBase) {
        addController(controllerBase);
        scheduleRenderUpdate();
        wallshareCount++;
    }

    @Override
    public void removeFromMultiBlock(@NotNull MultiblockControllerBase controllerBase) {
        removeController(controllerBase);
        scheduleRenderUpdate();
        wallshareCount--;
    }

    private void tryInitControllers() {
        // can't just init the variable in ctor because we need this init before super ctor finishes
        if (controllers == null) controllers = new ArrayList<>(1);
    }

    @Override
    public boolean canPartShare(MultiblockControllerBase target, String substructureName) {
        // when this is called normally isAttachedToMultiBlock has already been called and returned true
        // so we know controllerClass is notnull

        return canPartShare() && target.getClass() == controllerClass && substructureName.equals(attachedSubstructureName);
    }

    @Override
    public int getWallshareCount() {
        return wallshareCount;
    }

    @Override
    public boolean isAttachedToMultiBlock() {
        return controllers != null && !controllers.isEmpty();
    }

    @Override
    public int getDefaultPaintingColor() {
        return !isAttachedToMultiBlock() && hatchTexture == null ? super.getDefaultPaintingColor() : 0xFFFFFF;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        MultiblockControllerBase controllerBase = getController();
        if (controllerBase == null) return super.getIsWeatherOrTerrainResistant();
        return controllerBase.isMultiblockPartWeatherResistant(this);
    }
}
