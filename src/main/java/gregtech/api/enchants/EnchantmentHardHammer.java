package gregtech.api.enchants;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.common.tools.ToolPickaxe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

import javax.annotation.Nonnull;

public class EnchantmentHardHammer extends Enchantment {
    public static final EnchantmentHardHammer INSTANCE = new EnchantmentHardHammer();

    private EnchantmentHardHammer() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.DIGGER, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
    }

    public void register(RegistryEvent.Register<Enchantment> event) {
        this.setRegistryName(new ResourceLocation(GTValues.MODID, "hard_hammer"));
        setName("hard_hammer");
        event.getRegistry().register(this);
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
    public boolean canApply(ItemStack stack) {
        return this.canApplyAtEnchantingTable(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        boolean isPick = false;

        if(stack.getItem() instanceof ItemPickaxe) {
            isPick = true;
        }
        else if(stack.getItem() instanceof ToolMetaItem) {
            ToolMetaItem<?> toolMetaItem = (ToolMetaItem<?>) stack.getItem();
            ToolMetaItem<?>.MetaToolValueItem toolValueItem = toolMetaItem.getItem(stack);
            if(toolValueItem.getToolStats() instanceof ToolPickaxe) {
                isPick = true;
            }
        }

        return isPick && stack.getItem().canApplyAtEnchantingTable(stack, this);
    }

    @Override
    protected boolean canApplyTogether(@Nonnull Enchantment ench) {
            return ench != Enchantments.SILK_TOUCH && super.canApplyTogether(ench);
    }
}

