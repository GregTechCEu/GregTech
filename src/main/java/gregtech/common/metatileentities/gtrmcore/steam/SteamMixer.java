package gregtech.common.metatileentities.gtrmcore.steam;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GTRMGuiTextures;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SteamMixer extends SteamMetaTileEntity {

    public SteamMixer(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.MIXER_RECIPES, Textures.MIXER_OVERLAY, isHighPressure);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamMixer(metaTileEntityId, isHighPressure);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 6, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 2, this, true);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return createUITemplate(player)
                .slot(this.importItems, 0, 16, 17, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .slot(this.importItems, 1, 34, 17, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .slot(this.importItems, 2, 52, 17, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .slot(this.importItems, 3, 16, 35, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .slot(this.importItems, 4, 34, 35, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .slot(this.importItems, 5, 52, 35, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .progressBar(workableHandler::getProgressPercent, 77, 35, 20, 20,
                        GTRMGuiTextures.PROGRESS_BAR_PRIMITIVE_MIXER_STEAM.get(isHighPressure),
                        ProgressWidget.MoveType.CIRCULAR,
                        workableHandler.getRecipeMap())
                .slot(this.exportItems, 0, 106, 35, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .slot(this.exportItems, 1, 124, 35, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .build(getHolder(), player);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.steam_mixer.tooltip"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {}
}
