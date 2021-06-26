package gregtech.common.items.behaviors;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.ICircuitConfigurationItem;
import gregtech.api.capability.impl.ConfigCircuitItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public class IntCircuitBehaviour implements IItemBehaviour, ItemUIFactory, ISubItemHandler, IItemCapabilityProvider {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int configuration = IntCircuitIngredient.getCircuitConfiguration(itemStack);
        lines.add(I18n.format("metaitem.int_circuit.configuration", configuration));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 60)
            .label(9, 8, "metaitem.circuit.integrated.gui")
            .widget(new DynamicLabelWidget(82, 30, () -> Integer.toString(IntCircuitIngredient.getCircuitConfiguration(holder.getCurrentItem())), 0x404040))
            .widget(new ClickButtonWidget(15, 24, 20, 20, "-5", data -> adjustConfiguration(holder, -5)))
            .widget(new ClickButtonWidget(50, 24, 20, 20, "-1", data -> adjustConfiguration(holder, -1)))
            .widget(new ClickButtonWidget(104, 24, 20, 20, "+1", data -> adjustConfiguration(holder, +1)))
            .widget(new ClickButtonWidget(141, 24, 20, 20, "+5", data -> adjustConfiguration(holder, +5)))
            .build(holder, entityPlayer);
    }

    private static void adjustConfiguration(PlayerInventoryHolder holder, int amount) {
        ItemStack stack = holder.getCurrentItem();
        int configuration = IntCircuitIngredient.getCircuitConfiguration(stack);
        configuration += amount;
        configuration = MathHelper.clamp(configuration, 0, IntCircuitIngredient.CIRCUIT_MAX);
        IntCircuitIngredient.setCircuitConfiguration(stack, configuration);
        ICircuitConfigurationItem configCircuitItem = stack.getCapability(GregtechCapabilities.CAPABILITY_CIRCUIT_CONFIGURATION, null);
        if (configCircuitItem != null)
            configCircuitItem.changeConfiguration(amount, false);

        holder.markAsDirty();
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return Integer.toString(IntCircuitIngredient.getCircuitConfiguration(itemStack));
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack.copy());
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new ConfigCircuitItem(itemStack, 32);
    }
}
