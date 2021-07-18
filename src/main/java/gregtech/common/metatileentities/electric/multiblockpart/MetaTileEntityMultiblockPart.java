package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public abstract class MetaTileEntityMultiblockPart extends MetaTileEntity implements IMultiblockPart, ITieredMetaTileEntity {

    private final int tier;
    private BlockPos controllerPos;
    private MultiblockControllerBase controllerTile;
    private MultiblockControllerBase controllerTile2;
    private MultiblockControllerBase controllerTile3;
    private MultiblockControllerBase controllerTile4;

    public MetaTileEntityMultiblockPart(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        initializeInventory();
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture().getParticleSprite(), getPaintingColor());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        getBaseTexture().render(renderState, translation, ArrayUtils.add(pipeline,
            new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
    }

    @Override
    public int getActualLightValue() {
        MultiblockControllerBase controller = getController();
        return controller == null ? 0 : controller.getLightValueForPart(this);
    }

    public int getTier() {
        return tier;
    }

    public MultiblockControllerBase getController() {
        if (getWorld() != null && getWorld().isRemote) { //check this only clientside
            if (controllerTile4 == null && controllerPos != null) {
                this.controllerTile4 = (MultiblockControllerBase) BlockMachine.getMetaTileEntity(getWorld(), controllerPos);
            }
            else if (controllerTile3 == null && controllerPos != null) {
                 this.controllerTile3 = (MultiblockControllerBase) BlockMachine.getMetaTileEntity(getWorld(), controllerPos);
            }
            else if (controllerTile2 == null && controllerPos != null) {
                 this.controllerTile2 = (MultiblockControllerBase) BlockMachine.getMetaTileEntity(getWorld(), controllerPos);
            }
            else if (controllerTile == null && controllerPos != null) {
                 this.controllerTile = (MultiblockControllerBase) BlockMachine.getMetaTileEntity(getWorld(), controllerPos);
            }
        }
        if (controllerTile != null && (controllerTile.getHolder() == null ||
                controllerTile.getHolder().isInvalid() || !(getWorld().isRemote || controllerTile.getMultiblockParts().contains(this)))) {
            return this.controllerTile = null;
        }
        else if (controllerTile2 != null && (controllerTile2.getHolder() == null ||
                controllerTile2.getHolder().isInvalid() || !(getWorld().isRemote || controllerTile2.getMultiblockParts().contains(this)))) {
            return this.controllerTile2 = null;
        }
            else if (controllerTile3 != null && (controllerTile3.getHolder() == null ||
                controllerTile3.getHolder().isInvalid() || !(getWorld().isRemote || controllerTile3.getMultiblockParts().contains(this)))) {
            return this.controllerTile3 = null;
            }
        else if (controllerTile4 != null && (controllerTile4.getHolder() == null ||
                controllerTile4.getHolder().isInvalid() || !(getWorld().isRemote || controllerTile4.getMultiblockParts().contains(this)))) {
            return this.controllerTile4 = null;
        }

if (controllerTile4 != null)
    return controllerTile4;
if(controllerTile3 != null)
    return controllerTile3;
if (controllerTile2 != null)
    return controllerTile2;
else
    return controllerTile;
    }


    public ICubeRenderer getBaseTexture() {

        MultiblockControllerBase controller = getController();
        if (controller == null || !controller.isStructureFormed()) {

            return controller == null ? Textures.VOLTAGE_CASINGS[tier] : controller.getBaseTexture(this);

        }
        this.setPaintingColor(0xFFFFFF);
        return controller.getBaseTexture(this);
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
            this.controllerTile = null;
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            if (buf.readBoolean()) {
                this.controllerPos = buf.readBlockPos();
                this.controllerTile = null;
            } else {
                this.controllerPos = null;
                this.controllerTile = null;
            }
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    private void setController(MultiblockControllerBase controller1) {
        this.controllerTile = controller1;
        if (!getWorld().isRemote) {
            writeCustomData(100, writer -> {
                writer.writeBoolean(controllerTile != null);
                if (controllerTile != null) {
                    writer.writeBlockPos(controllerTile.getPos());
                }
            });
        }
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
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        setController(controllerBase);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        setController(null);
    }

    @Override
    public boolean isAttachedToMultiBlock() {
        return getController() != null;
    }
}
