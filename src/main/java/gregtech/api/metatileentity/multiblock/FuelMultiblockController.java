package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class FuelMultiblockController extends RecipeMapMultiblockController {

    public FuelMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new MultiblockFuelRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (!isStructureFormed()) {
            ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            textList.add(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                    .setStyle(new Style().setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        } else {
            if (recipeMapWorkable.getPreviousRecipe() != null) {
                String[] recipeInput = ((MultiblockFuelRecipeLogic) recipeMapWorkable).getRecipeFluidInputInfo();
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_needed",
                        recipeInput[0], recipeInput[1], TextFormatting.AQUA + TextFormattingUtil.formatNumbers(recipeMapWorkable.getPreviousRecipeDuration())));
            }

            if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics())
                addMaintenanceText(textList);

            if (hasMufflerMechanics() && !isMufflerFaceFree())
                textList.add(new TextComponentTranslation("gregtech.multiblock.universal.muffler_obstructed")
                        .setStyle(new Style().setColor(TextFormatting.RED)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponentTranslation("gregtech.multiblock.universal.muffler_obstructed.tooltip")))));

            IEnergyContainer energyContainer = recipeMapWorkable.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                if (maxVoltage < -recipeMapWorkable.getRecipeEUt()) {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy_output"));
                } else {
                    String voltageName = GTValues.VNF[GTUtility.getFloorTierByVoltage(maxVoltage)];
                    textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", TextFormattingUtil.formatNumbers(maxVoltage), voltageName));
                }
            }

            if (!recipeMapWorkable.isWorkingEnabled()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            } else if (recipeMapWorkable.isActive()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
                int currentProgress = (int) (recipeMapWorkable.getProgressPercent() * 100);
                textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }
        }
    }
    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 176 + 90, 216);
        builder.image(7, 4, 162 + 90, 121 + 6, GuiTextures.DISPLAY);
        builder.label(13, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(13, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(156 + 90)
                .setClickHandler(this::handleDisplayClick));
        if (shouldShowVoidingModeButton()) {
            builder.widget(new ImageCycleButtonWidget(149, 121 - 17, 18, 18, GuiTextures.BUTTON_VOID_MULTIBLOCK,
                    4, this::getVoidingMode, this::setVoidingMode)
                    .setTooltipHoverString(MultiblockWithDisplayBase::getVoidingModeTooltip));
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7 + 45, 134);
        return builder;
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        if (recipeMapWorkable.getMaxProgress() > 0) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(recipeMapWorkable.getProgress() / 20)).setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(recipeMapWorkable.getMaxProgress() / 20)).setStyle(new Style().setColor(TextFormatting.YELLOW))
            ));
        }

        list.add(new TextComponentTranslation("behavior.tricorder.energy_container_storage",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyStored())).setStyle(new Style().setColor(TextFormatting.GREEN)),
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity())).setStyle(new Style().setColor(TextFormatting.YELLOW))
        ));

        if (recipeMapWorkable.getRecipeEUt() < 0) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_production",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(recipeMapWorkable.getRecipeEUt() * -1)).setStyle(new Style().setColor(TextFormatting.RED)),
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(recipeMapWorkable.getRecipeEUt() == 0 ? 0 : 1)).setStyle(new Style().setColor(TextFormatting.RED))
            ));
        }

        list.add(new TextComponentTranslation("behavior.tricorder.multiblock_energy_output",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getOutputVoltage())).setStyle(new Style().setColor(TextFormatting.YELLOW)),
                new TextComponentTranslation(GTValues.VN[GTUtility.getTierByVoltage(energyContainer.getOutputVoltage())]).setStyle(new Style().setColor(TextFormatting.YELLOW))
        ));

        if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
            list.add(new TextComponentTranslation("behavior.tricorder.multiblock_maintenance",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(getNumMaintenanceProblems())).setStyle(new Style().setColor(TextFormatting.RED))
            ));
        }

        return list;
    }
}
