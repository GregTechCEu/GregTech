package gregtech.tools.enchants;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.util.GTUtility;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class EnchantmentHardHammer extends Enchantment {

    public static final EnchantmentHardHammer INSTANCE = new EnchantmentHardHammer();

    private EnchantmentHardHammer() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.DIGGER, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        this.setRegistryName(GTUtility.gregtechId("hard_hammer"));
        this.setName("hard_hammer");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 20;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return 60;
    }

    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) &&
                stack.getItem().getToolClasses(stack).contains(ToolClasses.PICKAXE) &&
                !stack.getItem().getToolClasses(stack).contains(ToolClasses.HARD_HAMMER);
    }

    @Override
    protected boolean canApplyTogether(@NotNull Enchantment ench) {
        return super.canApplyTogether(ench) && ench != Enchantments.SILK_TOUCH && ench != Enchantments.FORTUNE;
    }
}
