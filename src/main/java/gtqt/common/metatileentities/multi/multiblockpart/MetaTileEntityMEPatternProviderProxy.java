package gtqt.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.DualHandler;
import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.factory.MetaTileEntityGuiFactory;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

public class MetaTileEntityMEPatternProviderProxy extends MetaTileEntityMultiblockNotifiablePart
        implements IMultiblockAbilityPart<DualHandler>,
                   IDataStickIntractable {

    private MetaTileEntityMEPatternProvider main;
    private BlockPos mainPos;
    private boolean checkForMain = true;

    public MetaTileEntityMEPatternProviderProxy(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 5, false);
    }

    private void tryToSetMain() {
        if (getWorld() == null || mainPos == null) return;

        TileEntity tileEntity = getWorld().getTileEntity(mainPos);
        if (!(tileEntity instanceof IGregTechTileEntity iGregTechTileEntity)) {
            this.checkForMain = true;
            return;
        }

        MetaTileEntity metaTileEntity = iGregTechTileEntity.getMetaTileEntity();
        if (!(metaTileEntity instanceof MetaTileEntityMEPatternProvider budgetCRIB)) {
            this.checkForMain = true;
            return;
        }

        this.main = budgetCRIB;
        this.checkForMain = false;

        MultiblockControllerBase controllerBase = getController();
        if (controllerBase != null) {
            addNotifiedInput(getMain().getImportItems());
            addNotifiedInput(getMain().getImportFluids());

            if (hasMain() && getMain().hasGhostCircuitInventory() && getMain().getActualImportItems() instanceof ItemHandlerList) {
                for (IItemHandler handler : ((ItemHandlerList) getMain().getActualImportItems()).getBackingHandlers()) {
                    if (handler instanceof INotifiableHandler notifiable) {
                        notifiable.addNotifiableMetaTileEntity(controllerBase);
                        notifiable.addToNotifiedList(this, handler, false);
                    }
                }
            }
        }
    }

    @Override
    public MultiblockAbility<DualHandler> getAbility() {
        return MultiblockAbility.DUAL_IMPORT;
    }

    private MetaTileEntityMEPatternProvider getMain() {
        return main;
    }

    public boolean hasMain() {
        return main != null && main.isValid();
    }

    @Override
    public void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {}

    @Override
    public boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = dataStick.getTagCompound();
        if (tag == null || !tag.hasKey("BudgetCRIB")) return false;

        readLocationFromTag(tag.getCompoundTag("BudgetCRIB"));
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.budget_crib_proxy.data_stick_use",
                TextFormattingUtil.formatNumbers(mainPos.getX()),
                TextFormattingUtil.formatNumbers(mainPos.getY()),
                TextFormattingUtil.formatNumbers(mainPos.getZ())), true);

        tryToSetMain();

        return true;
    }

    private void readLocationFromTag(NBTTagCompound tag) {
        this.mainPos = new BlockPos(tag.getInteger("MainX"), tag.getInteger("MainY"), tag.getInteger("MainZ"));
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        if (data.getBoolean("HasMain")) {
            readLocationFromTag(data);
        }

        tryToSetMain();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        if (hasMain()) {
            data.setBoolean("HasMain", true);
            data.setInteger("MainX", mainPos.getX());
            data.setInteger("MainY", mainPos.getY());
            data.setInteger("MainZ", mainPos.getZ());
        } else {
            data.setBoolean("HasMain", false);
        }

        return super.writeToNBT(data);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {

            SimpleOverlayRenderer overlay = Textures.DUAL_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        if (main != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(mainPos);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        if (buf.readBoolean()) {
            mainPos = buf.readBlockPos();

            tryToSetMain();
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMEPatternProviderProxy(metaTileEntityId);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (hasMain() && getMain().hasGhostCircuitInventory() && getMain().getActualImportItems() instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) getMain().getActualImportItems()).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.addNotifiableMetaTileEntity(controllerBase);
                    notifiable.addToNotifiedList(getMain(), handler, false);
                }
            }
        }
    }
    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        if (hasMain()&&getMain().hasGhostCircuitInventory() && getMain().getActualImportItems() instanceof ItemHandlerList) {
            for (IItemHandler handler : ((ItemHandlerList) getMain().getActualImportItems()).getBackingHandlers()) {
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.removeNotifiableMetaTileEntity(controllerBase);
                }
            }
        }
    }
    @Override
    public void update() {
        super.update();

        if (!getWorld().isRemote && getOffsetTimer() % 100 == 0) {
            if(checkForMain && !hasMain())tryToSetMain();
        }
    }


    @Override
    protected boolean openGUIOnRightClick() {
        return getMain() != null;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {

        if (!playerIn.isSneaking() && openGUIOnRightClick()) {
            if (getWorld() != null && !getWorld().isRemote) {
                if (usesMui2()) {
                    MetaTileEntityGuiFactory.open(playerIn, getMain());
                }
            }
            return true;
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        if(hasMain())abilityInstances.add(new DualHandler(getMain().getActualImportItems(), getMain().getImportFluids(), false));
    }
}
