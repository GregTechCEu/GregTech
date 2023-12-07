package gregtech.common.items.behaviors;

import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class IntCircuitBehaviour implements IItemBehaviour, ItemUIFactory, ISubItemHandler {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int configuration = IntCircuitIngredient.getCircuitConfiguration(itemStack);
        lines.add(1, I18n.format("metaitem.int_circuit.configuration", configuration));
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        MetaTileEntity mte = GTUtility.getMetaTileEntity(world, pos);
        ItemStack stack = player.getHeldItem(hand);

        if (!(mte instanceof IGhostSlotConfigurable)) {
            return ActionResult.newResult(EnumActionResult.FAIL, stack);
        } else if (!world.isRemote) {
            ((IGhostSlotConfigurable) mte).setGhostCircuitConfig(IntCircuitIngredient.getCircuitConfiguration(stack));
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
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
                .widget(new DynamicLabelWidget(82, 30,
                        () -> Integer.toString(IntCircuitIngredient.getCircuitConfiguration(holder.getCurrentItem())),
                        0x4D4040))
                .widget(new ClickButtonWidget(15, 24, 20, 20, "-5",
                        data -> IntCircuitIngredient.adjustConfiguration(holder, -5)))
                .widget(new ClickButtonWidget(50, 24, 20, 20, "-1",
                        data -> IntCircuitIngredient.adjustConfiguration(holder, -1)))
                .widget(new ClickButtonWidget(104, 24, 20, 20, "+1",
                        data -> IntCircuitIngredient.adjustConfiguration(holder, +1)))
                .widget(new ClickButtonWidget(141, 24, 20, 20, "+5",
                        data -> IntCircuitIngredient.adjustConfiguration(holder, +5)))
                .build(holder, entityPlayer);
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return Integer.toString(IntCircuitIngredient.getCircuitConfiguration(itemStack));
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack.copy());
    }
}
