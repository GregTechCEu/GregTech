package gregtech.api.enchants;

import gregtech.api.GTValues;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;

import javax.annotation.Nonnull;

public class EnchantmentHardHammer extends Enchantment {
    public static final EnchantmentHardHammer INSTANCE = new EnchantmentHardHammer();

    private EnchantmentHardHammer() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.DIGGER, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
    }

    public void register(RegistryEvent.Register<Enchantment> event) {
        this.setRegistryName(new ResourceLocation(GTValues.MODID, "HardHammer"));
        setName("hard.hammer");
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
    protected boolean canApplyTogether(@Nonnull Enchantment ench) {
            return ench != Enchantments.SILK_TOUCH && super.canApplyTogether(ench);
    }
}

