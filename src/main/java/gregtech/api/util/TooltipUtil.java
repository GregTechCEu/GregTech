package gregtech.api.util;

import gregtech.api.unification.material.Materials;
import gregtech.common.items.ToolItems;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import org.jetbrains.annotations.NotNull;

public class TooltipUtil {

    public static void addMaintenanceProblems(@NotNull IRichTextBuilder<?> richText, byte maintenanceProblems) {
        richText.addLine(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.universal.has_problems"))
                .spaceLine(2);

        // Wrench
        if ((maintenanceProblems & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.WRENCH.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.universal.problem.wrench"))
                    .newLine();
        }

        // Screwdriver
        if (((maintenanceProblems >> 1) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.SCREWDRIVER.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.universal.problem.screwdriver"))
                    .newLine();
        }

        // Soft Mallet
        if (((maintenanceProblems >> 2) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.SOFT_MALLET.get(Materials.Wood)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.universal.problem.soft_mallet"))
                    .newLine();
        }

        // Hammer
        if (((maintenanceProblems >> 3) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.HARD_HAMMER.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.universal.problem.hard_hammer"))
                    .newLine();
        }

        // Wire Cutters
        if (((maintenanceProblems >> 4) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.WIRE_CUTTER.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.universal.problem.wire_cutter"))
                    .newLine();
        }

        // Crowbar
        if (((maintenanceProblems >> 5) & 1) == 0) {
            richText.add(new ItemDrawable(ToolItems.CROWBAR.get(Materials.Iron)))
                    .add(IKey.SPACE)
                    .add(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.universal.problem.crowbar"))
                    .newLine();
        }
    }
}
