package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.recipes.logic.workable.RecipeWorkable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipeWorkableDataProvider extends CapabilityDataProvider<RecipeWorkable> {

    public static final RecipeWorkableDataProvider INSTANCE = new RecipeWorkableDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MOD_NAME, "gregtech.recipe_logic");
    }

    @Override
    protected @NotNull Capability<RecipeWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_RECIPE_WORKABLE;
    }

    @Override
    protected NBTTagCompound getNBTData(RecipeWorkable capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setBoolean("Working", capability.isRunning());
        // TODO multiple recipe display
        // if (capability.isRunning() && capability.getCurrent() != null &&
        // !(capability instanceof PrimitiveRecipeLogic)) {
        // subTag.setLong("RecipeEUt", capability.getInfoProviderEUt());
        // subTag.setBoolean("Generating", capability.getCurrent().isGenerating());
        // }
        tag.setTag("gregtech.RecipeWorkable", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.recipe_logic") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.RecipeWorkable")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.RecipeWorkable");
            if (tag.getBoolean("Working")) {
                // TODO multiple recipe display
                // long eut = tag.getLong("RecipeEUt");
                // boolean consumer = !tag.getBoolean("Generating");
                // String endText = null;
                //
                // if (accessor.getTileEntity() instanceof IGregTechTileEntity gtte) {
                // MetaTileEntity mte = gtte.getMetaTileEntity();
                // if (mte instanceof SteamMetaTileEntity || mte instanceof MetaTileEntityLargeBoiler ||
                // mte instanceof RecipeMapSteamMultiblockController) {
                // endText = ": " + TextFormattingUtil.formatNumbers(eut) + TextFormatting.RESET + " L/t " +
                // I18n.format(Materials.Steam.getUnlocalizedName());
                // }
                // }
                // if (endText == null) {
                // endText = ": " + TextFormattingUtil.formatNumbers(eut) + TextFormatting.RESET + " EU/t (" +
                // GTValues.VOCNF[GTUtility.getOCTierByVoltage(eut)] + TextFormatting.RESET + ")";
                // }
                //
                // if (eut == 0) return tooltip;
                //
                // if (consumer) {
                // tooltip.add(I18n.format("gregtech.top.energy_consumption") + endText);
                // } else {
                // tooltip.add(I18n.format("gregtech.top.energy_production") + endText);
                // }
            }
        }
        return tooltip;
    }
}
