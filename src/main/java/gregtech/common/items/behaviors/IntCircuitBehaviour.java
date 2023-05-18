package gregtech.common.items.behaviors;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregtech.api.gui.GTGuis;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class IntCircuitBehaviour implements IItemBehaviour, ItemUIFactory, ISubItemHandler {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int configuration = IntCircuitIngredient.getCircuitConfiguration(itemStack);
        lines.add(I18n.format("metaitem.int_circuit.configuration", configuration));
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        MetaTileEntity mte = GTUtility.getMetaTileEntity(world, pos);
        ItemStack stack = player.getHeldItem(hand);

        if (!(mte instanceof SimpleMachineMetaTileEntity)) {
            return ActionResult.newResult(EnumActionResult.FAIL, stack);
        } else if (!world.isRemote) {
            ((SimpleMachineMetaTileEntity) mte).setGhostCircuitConfig(IntCircuitIngredient.getCircuitConfiguration(stack));
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            GTGuis.getMetaItemUiInfo(hand).open(player);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 60)
                .label(9, 8, "metaitem.circuit.integrated.gui")
                .widget(new DynamicLabelWidget(82, 30, () -> Integer.toString(IntCircuitIngredient.getCircuitConfiguration(holder.getCurrentItem())), 0x4D4040))
                .widget(new ClickButtonWidget(15, 24, 20, 20, "-5", data -> IntCircuitIngredient.adjustConfiguration(holder, -5)))
                .widget(new ClickButtonWidget(50, 24, 20, 20, "-1", data -> IntCircuitIngredient.adjustConfiguration(holder, -1)))
                .widget(new ClickButtonWidget(104, 24, 20, 20, "+1", data -> IntCircuitIngredient.adjustConfiguration(holder, +1)))
                .widget(new ClickButtonWidget(141, 24, 20, 20, "+5", data -> IntCircuitIngredient.adjustConfiguration(holder, +5)))
                .build(holder, entityPlayer);
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer, ItemStack var3) {
        for (int i = 0; i <= 32; i++) {
            int finalI = i;
            guiSyncHandler.syncValue("config", i, new InteractionSyncHandler()
                    .setOnMousePressed(b -> IntCircuitIngredient.setCircuitConfiguration(var3, finalI)));
        }
    }

    @Override
    public ModularPanel createUIPanel(GuiContext context, EntityPlayer player, ItemStack stack) {
        List<List<IWidget>> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            options.add(new ArrayList<>());
            for (int j = 0; j < 9; j++) {
                int index = i * 9 + j;
                if (index > 32) break;
                options.get(i).add(new ButtonWidget<>()
                        .size(18)
                        .background(com.cleanroommc.modularui.drawable.GuiTextures.SLOT, new ItemDrawable(IntCircuitIngredient.getIntegratedCircuit(index)).asIcon().size(16))
                        .setSynced("config", index));
            }
        }
        return ModularPanel.defaultPanel(context, 176, 120)
                .child(IKey.lang("metaitem.circuit.integrated.gui").asWidget().pos(5, 5))
                .child(new ItemDrawable(stack).asWidget()
                        .size(16)
                        .top(19).alignX(0.5f)
                        .background(com.cleanroommc.modularui.drawable.GuiTextures.SLOT.asIcon().size(18)))
                .child(new Grid()
                        .left(7).right(7).top(41).height(4 * 18)
                        .matrix(options)
                        .minColWidth(18).minRowHeight(18)
                        .minElementMargin(0, 0));
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
