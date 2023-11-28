package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.BlockOre;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.config.Config;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import mcjty.theoneprobe.api.*;

public class BlockOreInfoProvider implements IProbeInfoProvider, IBlockDisplayOverride {

    @Override
    public String getID() {
        return GTValues.MODID + ":ore_block_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState blockState, IProbeHitData probeHitData) {
        if (blockState.getBlock() instanceof BlockOre blockOre) {
            if (blockOre.isSmallOre) return;

            StoneType stoneType = blockState.getValue(blockOre.STONE_TYPE);
            if (entityPlayer.isSneaking() && !stoneType.shouldBeDroppedAsItem) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.block_drops*}:");
                ItemStack itemDropped = blockState.getBlock().getItem(world, probeHitData.getPos(), blockState);
                IProbeInfo horizontalInfo = probeInfo
                        .horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                horizontalInfo.item(itemDropped);
                horizontalInfo.itemLabel(itemDropped);
            }
        }
    }

    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer entityPlayer,
                                        World world, @NotNull IBlockState state, IProbeHitData iProbeHitData) {
        if (state.getBlock() instanceof BlockOre ore) {
            if (!ore.isSmallOre) return false;

            ItemStack crushedStack = OreDictUnifier.get(OrePrefix.crushed, ore.material);

            if (Tools.show(mode, Config.getRealConfig().getShowModName())) {
                probeInfo.horizontal()
                        .item(crushedStack)
                        .vertical()
                        .text(OrePrefix.oreSmall.getLocalNameForItem(ore.material))
                        .text(TextStyleClass.MODNAME + WordUtils.capitalize(ore.material.getModid()));
            } else {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                        .item(crushedStack)
                        .text(OrePrefix.oreSmall.getLocalNameForItem(ore.material));
            }
            return true;
        }
        return false;
    }
}
