package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipeLogicDataProvider extends CapabilityDataProvider<AbstractRecipeLogic> {

    public static final RecipeLogicDataProvider INSTANCE = new RecipeLogicDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.recipe_logic");
    }

    @Override
    protected @NotNull Capability<AbstractRecipeLogic> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC;
    }

    @Override
    protected NBTTagCompound getNBTData(AbstractRecipeLogic capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setBoolean("Working", capability.isWorking());
        if (capability.isWorking()) {
            subTag.setInteger("RecipeEUt", capability.getInfoProviderEUt());
        }
        tag.setTag("gregtech.AbstractRecipeLogic", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.recipe_logic") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.AbstractRecipeLogic")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.AbstractRecipeLogic");
            if (tag.getBoolean("Working")) {
                int EUt = tag.getInteger("RecipeEUt");
                int absEUt = Math.abs(EUt);
                boolean consumer = EUt > 0;
                String endText = null;

                if (accessor.getTileEntity() instanceof IGregTechTileEntity gtte) {
                    MetaTileEntity mte = gtte.getMetaTileEntity();
                    if (mte instanceof SteamMetaTileEntity || mte instanceof MetaTileEntityLargeBoiler ||
                            mte instanceof RecipeMapSteamMultiblockController) {
                        endText = ": " + absEUt + TextFormatting.RESET + " L/t " +
                                I18n.format(Materials.Steam.getUnlocalizedName());
                    }
                    AbstractRecipeLogic arl = mte.getRecipeLogic();
                    if (arl != null) {
                        consumer = arl.consumesEnergy();
                    }
                }
                if (endText == null) {
                    endText = ": " + absEUt + TextFormatting.RESET + " EU/t (" +
                            GTValues.VNF[GTUtility.getTierByVoltage(absEUt)] + TextFormatting.RESET + ")";
                }

                if (EUt == 0) return tooltip;

                if (consumer) {
                    tooltip.add(I18n.format("gregtech.top.energy_consumption") + endText);
                } else {
                    tooltip.add(I18n.format("gregtech.top.energy_production") + endText);
                }
            }
        }
        return tooltip;
    }
}
