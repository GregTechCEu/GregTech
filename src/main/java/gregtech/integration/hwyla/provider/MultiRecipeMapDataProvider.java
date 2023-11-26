package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleRecipeMaps;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiRecipeMapDataProvider extends CapabilityDataProvider<IMultipleRecipeMaps> {

    public static final MultiRecipeMapDataProvider INSTANCE = new MultiRecipeMapDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.multi_recipemap");
    }

    @Override
    protected @NotNull Capability<IMultipleRecipeMaps> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS;
    }

    @Override
    protected NBTTagCompound getNBTData(IMultipleRecipeMaps capability, NBTTagCompound tag) {
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.multi_recipemap") || accessor.getTileEntity() == null) {
            return tooltip;
        }
        IMultipleRecipeMaps cap = accessor.getTileEntity().getCapability(getCapability(), null);
        if (cap != null) {
            tooltip.add(I18n.format("gregtech.multiblock.multiple_recipemaps.header"));
            for (var recipeMap : cap.getAvailableRecipeMaps()) {
                if (recipeMap.equals(cap.getCurrentRecipeMap())) {
                    tooltip.add(" > " + I18n.format(recipeMap.getTranslationKey()));
                } else {
                    tooltip.add("   " + I18n.format(recipeMap.getTranslationKey()));
                }
            }
        }
        return tooltip;
    }
}
