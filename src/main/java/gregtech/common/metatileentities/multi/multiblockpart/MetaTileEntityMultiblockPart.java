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
import java.util.Collections;
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

    /**
     * Returns the last controller, which is the one that controls the texture of the part. If the world is null, return
     * null.
     * If this is called client side, the controller is fetched if not already and returned, this can lead to null. If
     * this is called on server,
     * returns the last controller if it is valid, or null if it is not. Note that calling this multiple times in
     * succession can
     * have different results, due to invalid controllers being removed after detected in this method.
     */
    @Nullable
    public MultiblockControllerBase getController() {
        tryInitControllers();

        if (getWorld() == null) {
            this.controllers.clear();
            lastController = null;
            return null;
        }

        if (getWorld().isRemote) { // client check, on client controllers is always empty
            if (lastController == null) {
                if (controllerPos != null) {
                    this.lastController = (MultiblockControllerBase) GTUtility.getMetaTileEntity(getWorld(),
                            controllerPos);
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
            return null;
        }

        return controller;
    }

    /**
     * Gets a list of all the controllers owning the part. Calling this when the world is null returns an empty list.
     * Calling this on client side is unsupported(as the client has no knowledge of this) and logs an error(and returns
     * an empty list).
     * Calling this on server side returns the controllers list, with all invalid controllers removed.
     */
    @NotNull
    public List<MultiblockControllerBase> getControllers() {
        tryInitControllers();

        if (getWorld() == null) {
            this.controllers.clear();
            lastController = null;
            return Collections.emptyList();
        }

        if (getWorld().isRemote) {
            GTLog.logger.error("getControllers() was called on client side on " + getClass().getName() +
                    ", the author probably intended to use getController()! Ignoring and returning empty list.");
            return Collections.emptyList();
        }

        // empty list check
        if (controllers.isEmpty()) return controllers;

        // last controller in list
        MultiblockControllerBase last = controllers.get(controllers.size() - 1);

        // remove all invalid controllers
        controllers.removeIf(controller -> !controller.isValid());

        // check again
        if (controllers.isEmpty()) {
            syncLastController();
            return controllers;
        }

        // only sync if last controller changed
        if (last != controllers.get(controllers.size() - 1)) syncLastController();

        return controllers;
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
            }
            this.lastController = null;
            scheduleRenderUpdate();
        }
    }

    private void addController(@NotNull MultiblockControllerBase controller) {
        tryInitControllers();

        // this should be called after canPartShare has already checked the class, just a safeguard
        if (controllerClass != null && controller.getClass() != controllerClass) {
            GTLog.logger.error("addContr oller(MultiblockControllerBase) was called on " + getClass().getName() +
                    " with a mismatched class(original: " + controllerClass.getName() + ", new: " +
                    controller.getClass().getName() + ")! Ignoring the call.");
            return;
        }

        if (controllers.isEmpty()) controllerClass = controller.getClass();

        this.controllers.add(controller);

        // controllers always add at end of list, so always sync
        syncLastController();
    }

    private void removeController(@NotNull MultiblockControllerBase controller) {
        tryInitControllers();

        int index = controllers.indexOf(controller);

        if (index == -1) {
            GTLog.logger.error("removeController(MultiblockControllerBase) was called on " + getClass().getName() +
                    " while the given controller wasn't in the list!");
            return;
        }

        controllers.remove(index);

        // if the last controller changed, sync it
        if (index == controllers.size()) {
            syncLastController();
        }

        if (controllers.isEmpty()) {
            controllerClass = null;
            attachedSubstructureName = null;
        }
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
        if (getWorld().isRemote) return;

        List<MultiblockControllerBase> controllers = getControllers();
        for (int i = 0; i < controllers.size(); i++) {
            controllers.get(controllers.size() - 1).invalidateStructure("main");
        }
    }

    @Override
    public void addToMultiBlock(@NotNull MultiblockControllerBase controllerBase, @NotNull String substructureName) {
        // canPartShare() should
        if (!substructureName.equals(attachedSubstructureName) && attachedSubstructureName != null) {
            GTLog.logger.error("addToMultiBlock(MultiblockControllerBase, String) was called on " +
                    getClass().getName() + " with a mismatched name(original: " + attachedSubstructureName + ", new: " +
                    substructureName + ")! Ignoring the call.");
            return;
        }

        attachedSubstructureName = substructureName;

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

        return canPartShare() && target.getClass() == controllerClass &&
                (substructureName.equals(attachedSubstructureName) || attachedSubstructureName == null);
    }

    @Override
    public int getWallshareCount() {
        return wallshareCount;
    }

    @Override
    public String getSubstructureName() {
        return attachedSubstructureName;
    }

    @Override
    public boolean isAttachedToMultiBlock() {
        return controllers != null && !controllers.isEmpty();
    }

    @Override
    public int getDefaultPaintingColor() {
        return getController() == null && hatchTexture == null ? super.getDefaultPaintingColor() : 0xFFFFFF;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        MultiblockControllerBase controllerBase = getController();
        if (controllerBase == null) return super.getIsWeatherOrTerrainResistant();
        return controllerBase.isMultiblockPartWeatherResistant(this);
    }
}
