package gregtech.common.metatileentities.steam;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SteamFurnace extends SteamMetaTileEntity {

    public SteamFurnace(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeMaps.FURNACE_RECIPES, Textures.FURNACE_OVERLAY, isHighPressure);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamFurnace(metaTileEntityId, isHighPressure);
    }

    @Override
    protected boolean isBrickedCasing() {
        return true;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return createUITemplate(player)
                .slot(this.importItems, 0, 53, 25, GuiTextures.SLOT_STEAM.get(isHighPressure),
                        GuiTextures.FURNACE_OVERLAY_STEAM.get(isHighPressure))
                // TODO multiple recipe display
                .progressBar(/* workableHandler::getProgressPercent */() -> 0, 79, 26, 20, 16,
                        GuiTextures.PROGRESS_BAR_ARROW_STEAM.get(isHighPressure), MoveType.HORIZONTAL,
                        getRecipeMap())
                .slot(this.exportItems, 0, 107, 25, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure))
                .build(getHolder(), player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            EnumParticleTypes smokeParticle = isHighPressure ? EnumParticleTypes.SMOKE_LARGE :
                    EnumParticleTypes.SMOKE_NORMAL;
            VanillaParticleEffects.defaultFrontEffect(this, 0.5F, smokeParticle, EnumParticleTypes.FLAME);

            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                BlockPos pos = getPos();
                getWorld().playSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }
}
