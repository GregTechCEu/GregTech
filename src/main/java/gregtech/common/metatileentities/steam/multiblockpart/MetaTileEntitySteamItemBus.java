package gregtech.common.metatileentities.steam.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntitySteamItemBus extends MetaTileEntityItemBus implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    private static final boolean IS_STEEL = ConfigHolder.machines.steelSteamMultiblocks;

    public MetaTileEntitySteamItemBus(ResourceLocation metaTileEntityId, boolean isExportHatch) {
        super(metaTileEntityId, 1, isExportHatch);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamItemBus(metaTileEntityId, isExportHatch);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return isExportHatch ? MultiblockAbility.STEAM_EXPORT_ITEMS : MultiblockAbility.STEAM_IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
        abilityList.add(isExportHatch ? this.exportItems : this.importItems);
    }

    // Override base texture to have a bus with 4 slots, but ULV textures
    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller == null)
            return IS_STEEL ? Textures.STEAM_CASING_STEEL : Textures.STEAM_CASING_BRONZE;
        return controller.getBaseTexture(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.steam_bus.tooltip"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = this.isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    // Override UI to make it a Steam UI
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(IS_STEEL), 176, 166)
                .label(6, 6, getMetaFullName());

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                int index = y * 2 + x;
                builder.slot(isExportHatch ? exportItems : importItems, index, 70 + x * 18, 31 + y * 18, true, !isExportHatch,
                        GuiTextures.SLOT_STEAM.get(IS_STEEL));
            }
        }
        builder.shouldColor(false);
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT_STEAM.get(IS_STEEL), 7, 83);
        return builder.build(getHolder(), entityPlayer);
    }
}
