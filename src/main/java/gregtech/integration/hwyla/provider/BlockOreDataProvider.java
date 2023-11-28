package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.BlockOre;
import gregtech.integration.hwyla.HWYLAModule;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import mcp.mobius.waila.api.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockOreDataProvider implements IWailaDataProvider {

    public static final BlockOreDataProvider INSTANCE = new BlockOreDataProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerStackProvider(this, BlockOre.class);
        registrar.registerHeadProvider(this, BlockOre.class);
        registrar.registerBodyProvider(this, BlockOre.class);
        registrar.addConfig(GTValues.MODID, "gregtech.block_ore");
    }

    @NotNull
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (accessor.getBlock() instanceof BlockOre ore && ore.isSmallOre) {
            return OreDictUnifier.get(OrePrefix.crushed, ore.material);
        }
        return accessor.getStack();
    }

    @NotNull
    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (accessor.getBlock() instanceof BlockOre ore && ore.isSmallOre) {
            tooltip.set(0, TextFormatting.WHITE + OrePrefix.oreSmall.getLocalNameForItem(ore.material));
        }
        return tooltip;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.block_ore")) {
            return tooltip;
        }

        if (accessor.getBlock() instanceof BlockOre ore) {
            StoneType type = accessor.getBlockState().getValue(ore.STONE_TYPE);
            if (accessor.getPlayer().isSneaking() && !type.shouldBeDroppedAsItem) {
                tooltip.add(I18n.format("gregtech.top.block_drops") + ":");
                ItemStack itemDropped = ore.getItem(accessor.getWorld(), accessor.getPosition(),
                        accessor.getBlockState());
                tooltip.add(HWYLAModule.wailaStackWithName(itemDropped));
            }
        }
        return tooltip;
    }
}
