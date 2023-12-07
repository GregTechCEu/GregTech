package gregtech.integration.hwyla;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.hwyla.provider.*;
import gregtech.modules.GregTechModules;

import net.minecraft.item.ItemStack;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
@GregTechModule(
                moduleID = GregTechModules.MODULE_HWYLA,
                containerID = GTValues.MODID,
                modDependencies = GTValues.MODID_HWYLA,
                name = "GregTech HWYLA Integration",
                description = "HWYLA (WAILA) Integration Module")
public class HWYLAModule extends IntegrationSubmodule implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar) {
        ElectricContainerDataProvider.INSTANCE.register(registrar);
        WorkableDataProvider.INSTANCE.register(registrar);
        ControllableDataProvider.INSTANCE.register(registrar);
        TransformerDataProvider.INSTANCE.register(registrar);
        DiodeDataProvider.INSTANCE.register(registrar);
        MultiblockDataProvider.INSTANCE.register(registrar);
        MaintenanceDataProvider.INSTANCE.register(registrar);
        MultiRecipeMapDataProvider.INSTANCE.register(registrar);
        ConverterDataProvider.INSTANCE.register(registrar);
        RecipeLogicDataProvider.INSTANCE.register(registrar);
        SteamBoilerDataProvider.INSTANCE.register(registrar);
        PrimitivePumpDataProvider.INSTANCE.register(registrar);
        // one day, if cover provider is ported to waila, register it right here
        BlockOreDataProvider.INSTANCE.register(registrar);
        LampDataProvider.INSTANCE.register(registrar);
    }

    /** Render an ItemStack. */
    public static String wailaStack(ItemStack stack) {
        String name = stack.getItem().getRegistryName().toString();
        String count = String.valueOf(stack.getCount());
        String damage = String.valueOf(stack.getItemDamage());
        String nbt = stack.hasTagCompound() ? stack.getTagCompound().toString() : "";
        return SpecialChars.getRenderString("waila.stack", "1", name, count, damage, nbt);
    }

    /** Render a string with an X/Y offset. */
    public static String offsetText(String s, int x, int y) {
        return SpecialChars.getRenderString("gregtech.text", s, Integer.toString(x), Integer.toString(y));
    }

    /** Render an ItemStack with its display name offset to the right. */
    public static String wailaStackWithName(ItemStack stack) {
        return wailaStackWithName(stack, stack.getDisplayName());
    }

    /** Render an ItemStack with a custom String displayed offset to the right. */
    public static String wailaStackWithName(ItemStack stack, String name) {
        return wailaStack(stack) + offsetText(name, 0, 4);
    }
}
