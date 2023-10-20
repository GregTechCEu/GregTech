package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class MultiblockDisplayText {

    public static Builder builder(List<ITextComponent> textList, boolean isStructureFormed) {
        return new Builder(textList, isStructureFormed);
    }

    public static class Builder {

        private final List<ITextComponent> textList;
        private final boolean isStructureFormed;

        private boolean isWorkingEnabled, isActive;

        private Builder(List<ITextComponent> textList, boolean isStructureFormed) {
            this.textList = textList;
            this.isStructureFormed = isStructureFormed;
        }

        /** Set the current working enabled and active status of this multiblock, used by many line addition calls. */
        public Builder setWorkingStatus(boolean isWorkingEnabled, boolean isActive) {
            this.isWorkingEnabled = isWorkingEnabled;
            this.isActive = isActive;
            return this;
        }

        /**
         * Adds the "Structure Incomplete" line if the multiblock is not formed.
         * <br>
         * Added if the structure is not formed.
         */
        public Builder addStructureLine() {
            if (!isStructureFormed) {
                ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
                tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
                textList.add(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                        .setStyle(new Style().setColor(TextFormatting.RED)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the passed energy container has greater than zero capacity.
         */
        public Builder addEnergyUsageLine(IEnergyContainer energyContainer) {
            if (!isStructureFormed) return this;
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VNF[GTUtility.getFloorTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", TextFormattingUtil.formatNumbers(maxVoltage), voltageName));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can produce.
         * <br>
         * Added if the structure is formed and if the max voltage is greater than zero and the recipe EU/t.
         */
        public Builder addEnergyProductionLine(long maxVoltage, long recipeEUt) {
            if (!isStructureFormed) return this;
            if (maxVoltage != 0 && maxVoltage >= -recipeEUt) {
                String voltageName = GTValues.VNF[GTUtility.getFloorTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", TextFormattingUtil.formatNumbers(maxVoltage), voltageName));
            }
            return this;
        }

        /**
         * Adds a three-state indicator line, showing if the machine is running, paused, or idling.
         * <br>
         * Added if the structure is formed.
         */
        public Builder addWorkingStatusLine() {
            if (!isStructureFormed) return this;

            if (!isWorkingEnabled) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            } else if (isActive) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }
            return this;
        }

        /**
         * Adds a simple progress line that displays progress as a percentage.
         * <br>
         * Added if structure is formed and the machine is active.
         *
         * @param progressPercent Progress formatted as a range of [0,1] representing the progress of the recipe.
         */
        public Builder addProgressLine(double progressPercent) {
            if (!isStructureFormed || !isActive) return this;
            int currentProgress = (int) (progressPercent * 100);
            textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
            return this;
        }

        /** Adds a line indicating how many parallels this multi can potentially perform.
         * <br>
         * Added if structure is formed and the number of parallels is greater than one.
         */
        public Builder addParallelsLine(int numParallels) {
            if (!isStructureFormed) return this;
            if (numParallels > 1) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.parallel", numParallels));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine is low on power.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowPowerLine(boolean isLowPower) {
            if (!isStructureFormed) return this;
            if (isLowPower) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
            }
            return this;
        }

        /**
         * Adds a line showing the fuel's name and the amount available for use in this multiblock.
         * <br>
         * Added if structure is formed and if the passed parameter is not null and has an amount greater than zero.
         */
        public Builder addFuelAmountLine(FluidStack fuelStack) {
            if (!isStructureFormed) return this;
            if (fuelStack != null && fuelStack.amount > 0) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_amount", TextFormattingUtil.formatNumbers(fuelStack.amount), fuelStack.getLocalizedName()));
            }
            return this;
        }

        /**
         * Adds a fuel consumption line showing the fuel name and the number of ticks per recipe run.
         * <br>
         * Added if structure is formed, the machine is active, and the passed fuelName parameter is not null.
         */
        public Builder addFuelNeededLine(String fuelName, int previousRecipeDuration) {
            if (!isStructureFormed || !isActive) return this;
            textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_needed",
                    fuelName, TextFormatting.AQUA + TextFormattingUtil.formatNumbers(previousRecipeDuration)));
            return this;
        }

        /** Add custom text more dynamically, allowing for custom application logic. */
        public Builder addCustom(Consumer<List<ITextComponent>> customConsumer) {
            customConsumer.accept(textList);
            return this;
        }

        /** Adds a custom text line only when the multiblock is not formed. */
        public Builder addCustomUnformedLine(ITextComponent custom) {
            if (!isStructureFormed) {
                textList.add(custom);
            }
            return this;
        }

        /** Adds a custom text line only when the multiblock is formed. */
        public Builder addCustomFormedLine(ITextComponent custom) {
            if (isStructureFormed) {
                textList.add(custom);
            }
            return this;
        }

        /** Adds a custom text line that is always shown, no matter if the multiblock is formed or not formed. */
        public Builder addCustomLine(ITextComponent custom) {
            textList.add(custom);
            return this;
        }
    }
}
