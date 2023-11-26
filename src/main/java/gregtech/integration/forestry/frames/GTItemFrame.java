package gregtech.integration.forestry.frames;

import gregtech.api.GTValues;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IHiveFrame;
import forestry.api.core.IItemModelRegister;
import forestry.api.core.IModelManager;
import forestry.api.core.Tabs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GTItemFrame extends Item implements IHiveFrame, IItemModelRegister {

    private final GTFrameType type;

    public GTItemFrame(GTFrameType type) {
        super();
        this.type = type;
        this.setMaxDamage(this.type.maxDamage);
        this.setMaxStackSize(1);
        this.setCreativeTab(Tabs.tabApiculture);
        this.setRegistryName(GTValues.MODID, "gt.frame_" + type.getName());
        this.setTranslationKey("gt.frame_" + type.getName().toLowerCase());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        // TODO
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @NotNull
    @Override
    public ItemStack frameUsed(@NotNull IBeeHousing housing, ItemStack frame, @NotNull IBee bee, int wear) {
        frame.setItemDamage(frame.getItemDamage() + wear);
        if (frame.getItemDamage() >= frame.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return frame;
    }

    @NotNull
    @Override
    public IBeeModifier getBeeModifier() {
        return this.type;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerModel(@NotNull Item item, @NotNull IModelManager manager) {
        manager.registerItemModel(item, 0, GTValues.MODID_FR, "gt.frame_" + type.getName().toLowerCase());
    }

    public ItemStack getItemStack() {
        return new ItemStack(this);
    }
}
