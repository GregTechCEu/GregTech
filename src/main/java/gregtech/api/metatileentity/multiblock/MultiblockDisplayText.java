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

import java.util.List;

public class MultiblockDisplayText {

    public static Builder builder(List<ITextComponent> textList) {
        return new Builder(textList);
    }

    public static class Builder {

        private final List<ITextComponent> textList;

        private Builder(List<ITextComponent> textList) {
            this.textList = textList;
        }

        /** Adds the "Structure Incomplete" line if the multiblock is not formed. */
        public Builder addStructureLine(boolean isFormed) {
            if (!isFormed) {
                ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
                tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
                textList.add(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                        .setStyle(new Style().setColor(TextFormatting.RED)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
            }
            return this;
        }

        /** Adds the max EU/t that this multiblock can use. */
        public Builder addEnergyLine(IEnergyContainer energyContainer) {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VNF[GTUtility.getFloorTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", TextFormattingUtil.formatNumbers(maxVoltage), voltageName));
            }
            return this;
        }

        /** Adds a three-state indicator line, showing if the machine is running, paused, or idling. */
        public Builder addWorkingStatusLine(boolean isWorkingEnabled, boolean isActive) {
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
         *
         * @param progressPercent Progress formatted as a range of [0,1] representing the progress of the recipe.
         */
        public Builder addProgressLine(boolean isActiveAndWorking, double progressPercent) {
            int currentProgress = (int) (progressPercent * 100);
            textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
            return this;
        }

        /** Adds a line indicating how many parallels this multi can potentially perform, if more than 1. */
        public Builder addParallelsLine(int numParallels) {
            if (numParallels > 1) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.parallel", numParallels));
            }
            return this;
        }

        public Builder addLowPowerLine(boolean isLowPower) {
            if (isLowPower) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
            }
            return this;
        }

        public Builder addCustomLine(ITextComponent custom) {
            textList.add(custom);
            return this;
        }
    }
}
