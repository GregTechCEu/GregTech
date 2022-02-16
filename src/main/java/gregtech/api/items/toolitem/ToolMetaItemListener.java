package gregtech.api.items.toolitem;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolMetaItem.MetaToolValueItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = GTValues.MODID)
public class ToolMetaItemListener {

    private static final Object DUMMY_OBJECT = new Object();
    private static final ThreadLocal<Object> harvesting = new ThreadLocal<>();

    @SubscribeEvent
    public static void onXpOrbPickup(PlayerPickupXpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        EntityXPOrb xpOrb = event.getOrb();
        ItemStack itemStack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, player);

        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ToolMetaItem) {
            ToolMetaItem<?> toolMetaItem = (ToolMetaItem<?>) itemStack.getItem();
            int maxDurabilityRegain = xpToDurability(xpOrb.xpValue);
            int durabilityRegained = toolMetaItem.regainItemDurability(itemStack, maxDurabilityRegain);
            xpOrb.xpValue -= durabilityToXp(durabilityRegained);
        }
    }

    @SubscribeEvent
    public static void onAnvilChange(AnvilUpdateEvent event) {
        ItemStack firstStack = event.getLeft();
        ItemStack secondStack = event.getRight();
        if (!firstStack.isEmpty() && !secondStack.isEmpty() && firstStack.getItem() instanceof ToolMetaItem) {
            ToolMetaItem<?> toolMetaItem = (ToolMetaItem<?>) firstStack.getItem();
            MetaToolValueItem toolValueItem = toolMetaItem.getItem(firstStack);
            if (toolValueItem == null) {
                return;
            }
            Material toolMaterial = ToolMetaItem.getToolMaterial(firstStack);
            OrePrefix solidPrefix = getSolidPrefix(toolMaterial);
            UnificationEntry unificationEntry = OreDictUnifier.getUnificationEntry(secondStack);
            double toolDamage = toolMetaItem.getItemDamage(firstStack) / (toolMetaItem.getMaxItemDamage(firstStack) * 1.0);
            double materialForFullRepair = toolValueItem.getAmountOfMaterialToRepair(firstStack);
            int durabilityPerUnit = (int) Math.ceil(toolMetaItem.getMaxItemDamage(firstStack) / materialForFullRepair);
            int materialUnitsRequired = Math.min(secondStack.getCount(), (int) Math.ceil(toolDamage * materialForFullRepair));
            int repairCost = (MathHelper.clamp(toolMaterial.getToolHarvestLevel(), 2, 3) - 1) * materialUnitsRequired;

            if (toolDamage > 0.0 && materialUnitsRequired > 0 && unificationEntry != null &&
                    unificationEntry.material == toolMaterial && unificationEntry.orePrefix == solidPrefix) {
                int durabilityToRegain = durabilityPerUnit * materialUnitsRequired;
                ItemStack resultStack = firstStack.copy();
                toolMetaItem.regainItemDurability(resultStack, durabilityToRegain);
                event.setMaterialCost(materialUnitsRequired);
                event.setCost(repairCost);
                event.setOutput(resultStack);
            }
        }
    }

    private static OrePrefix getSolidPrefix(Material material) {
        if (material == Materials.Wood) {
            return OrePrefix.plank;
        } else if (material.hasProperty(PropertyKey.INGOT)) {
            return OrePrefix.ingot;
        } else if (material.hasProperty(PropertyKey.GEM)) {
            return OrePrefix.gem;
        } else return null;
    }

    private static int xpToDurability(int xp) {
        return xp * 2;
    }

    private static int durabilityToXp(int durability) {
        return durability / 2;
    }

}
