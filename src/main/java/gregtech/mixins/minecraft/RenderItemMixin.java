package gregtech.mixins.minecraft;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.client.renderer.handler.LampItemOverlayRenderer;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.ToolChargeBarRenderer;

import gregtech.common.MetaEntities;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;

import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(RenderItem.class)
public class RenderItemMixin {

    // The easy part of translating the item render stuff
    @Inject(method = "renderItemOverlayIntoGUI", at = @At(value = "HEAD"))
    private void renderItemOverlayIntoGUIInject(FontRenderer fr, ItemStack stack, int xPosition, int yPosition,
                                                String text, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            gregTechCEu$renderLampOverlay(stack, xPosition, yPosition);
        }
    }

    @Inject(method = "renderItemOverlayIntoGUI",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lnet/minecraft/client/Minecraft;getMinecraft()Lnet/minecraft/client/Minecraft;",
                     shift = At.Shift.BEFORE,
                     ordinal = 0))
    public void showDurabilityBarMixin(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text,
                                       CallbackInfo ci) {
        if (!Mods.EnderCore.isModLoaded()) {
            gregTechCEu$renderElectricBar(stack, xPosition, yPosition);
            gregTechCEu$renderDrumBar(stack, xPosition, yPosition);
            gregTechCEu$renderQuantumTankBar(stack, xPosition, yPosition);
        }
    }

    /*
     * 如果某人觉得我这个又是抄袭https://github.com/MCTian-mi/SussyPatches/commit/e13ea32afac6d7bfd07d3c107713098e9d73a03a
     * 那我只能笑嘻了
     *
     */
    @Unique
    private static void gregTechCEu$renderDrumBar(@NotNull ItemStack stack, int xPosition, int yPosition) {
        if (stack.getCount() > 1) return; //忽视堆叠项目

        MetaTileEntity mte = GTUtility.getMetaTileEntity(stack);
        if (!(mte instanceof MetaTileEntityDrum drum)) return;

        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid == null || fluid.amount <= 0) return;

        int tankCapacity = drum.getTankSize();
        double fillRate = fluid.amount / (double) tankCapacity;

        Color color = new Color(GTUtility.convertRGBtoOpaqueRGBA_MC(RenderUtil.getFluidColor(fluid)));
        ToolChargeBarRenderer.render(fillRate, xPosition, yPosition, 0, true, color, color, false);
    }

    @Unique
    private static void gregTechCEu$renderQuantumTankBar(@NotNull ItemStack stack, int xPosition, int yPosition) {
        if (stack.getCount() > 1) return; //忽视堆叠项目

        MetaTileEntity mte = GTUtility.getMetaTileEntity(stack);
        if (!(mte instanceof MetaTileEntityQuantumTank tank)) return;

        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid == null || fluid.amount <= 0) return;

        int tankCapacity = tank.getTankSize();
        double fillRate = fluid.amount / (double) tankCapacity;

        Color color = new Color(GTUtility.convertRGBtoOpaqueRGBA_MC(RenderUtil.getFluidColor(fluid)));
        ToolChargeBarRenderer.render(fillRate, xPosition, yPosition, 0, true, color, color, false);
    }

    @Unique
    private static void gregTechCEu$renderElectricBar(@NotNull ItemStack stack, int xPosition, int yPosition) {
        if (stack.getItem() instanceof IGTTool) {
            ToolChargeBarRenderer.renderBarsTool((IGTTool) stack.getItem(), stack, xPosition, yPosition);
        } else if (stack.getItem() instanceof MetaItem) {
            ToolChargeBarRenderer.renderBarsItem((MetaItem<?>) stack.getItem(), stack, xPosition, yPosition);
        }
    }

    @Unique
    private static void gregTechCEu$renderLampOverlay(@NotNull ItemStack stack, int xPosition, int yPosition) {
        LampItemOverlayRenderer.OverlayType overlayType = LampItemOverlayRenderer.getOverlayType(stack);
        if (overlayType != LampItemOverlayRenderer.OverlayType.NONE) {
            LampItemOverlayRenderer.renderOverlay(overlayType, xPosition, yPosition);
        }
    }
}
