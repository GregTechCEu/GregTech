package gregtech.common.metatileentities.steam.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.SimpleOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityItemBus;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntitySteamItemBus extends MetaTileEntityItemBus implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final boolean isExportHatch;

    public MetaTileEntitySteamItemBus(ResourceLocation metaTileEntityId, boolean isExportHatch) {
        super(metaTileEntityId, 1, isExportHatch);
        this.isExportHatch = isExportHatch;
        initializeInventory();
        this.setPaintingColor(0xFFFFFF);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
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
            return ConfigHolder.steelSteamMultiblocks ? Textures.SOLID_STEEL_CASING : Textures.BRONZE_PLATED_BRICKS;
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
}
