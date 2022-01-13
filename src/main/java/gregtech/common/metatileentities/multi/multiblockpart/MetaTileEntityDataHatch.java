package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.PipelineUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

public class MetaTileEntityDataHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable> {
    private final NotifiableItemStackHandler dataStickInventory;
    private final int slotAmount;

    public MetaTileEntityDataHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        int[] invSizes = {1, 1, 1, 1, 4, 8, 16, 32, 64};
        this.slotAmount = invSizes[tier];
        this.dataStickInventory = new NotifiableItemStackHandler(slotAmount+1, this, false);
        dataStickInventory.onContentsChanged(2);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityDataHatch(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(slotAmount);
        return createUITemplate(entityPlayer, rowSize, rowSize == 10 ? 9 : 0)
                .build(getHolder(), entityPlayer);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player, int rowSize, int xOffset) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176 + xOffset * 2,
                        18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.slot(dataStickInventory, index, (88 - rowSize * 9 + x * 18) + xOffset, 18 + y * 18, GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY);
            }
        }
        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7 + xOffset, 18 + 18 * rowSize + 12);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.RESEARCH_DATA;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
        abilityList.add(dataStickInventory);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SimpleOverlayRenderer overlay;
        if (this.shouldRenderOverlay()) {
            overlay = Textures.DATA_ACCESS_HATCH;
            overlay.renderSided(this.getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, 0xffffff));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("DataStickInventory", dataStickInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.dataStickInventory.deserializeNBT(data.getCompoundTag("DataStickInventory"));
    }
}
