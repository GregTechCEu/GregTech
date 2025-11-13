package gregtech.common.items.behaviors;

import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemModelDispatcher;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;

import java.util.ArrayList;
import java.util.List;

public class IntCircuitBehaviour implements IItemBehaviour, ItemUIFactory, ISubItemHandler, IItemModelDispatcher {

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
            MetaItemGuiFactory.open(player, hand);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        var panel = GTGuis.createPanel(guiData.getUsedItemStack(), 176, 120);
        ItemDrawable circuitPreview = new ItemDrawable(guiData.getUsedItemStack());
        for (int i = 0; i <= 32; i++) {
            int finalI = i;
            guiSyncManager.syncValue("config", i, new InteractionSyncHandler()
                    .setOnMousePressed(b -> {
                        ItemStack item = IntCircuitIngredient.getIntegratedCircuit(finalI);
                        item.setCount(guiData.getUsedItemStack().getCount());
                        circuitPreview.setItem(item);
                        if (Interactable.hasShiftDown()) panel.animateClose();
                        guiData.getPlayer().setHeldItem(guiData.getHand(), item);
                    }));
        }

        List<List<IWidget>> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            options.add(new ArrayList<>());
            for (int j = 0; j < 9; j++) {
                int index = i * 9 + j;
                if (index > 32) break;
                options.get(i).add(new ButtonWidget<>()
                        .size(18)
                        .background(GTGuiTextures.SLOT,
                                new ItemDrawable(IntCircuitIngredient.getIntegratedCircuit(index)).asIcon().size(16))
                        .disableHoverBackground()
                        .syncHandler("config", index));
            }
        }
        return panel.child(IKey.lang("metaitem.circuit.integrated.gui").asWidget().pos(5, 5))
                .child(circuitPreview.asIcon().size(16).asWidget()
                        .size(18)
                        .top(19).alignX(0.5f)
                        .background(GTGuiTextures.SLOT))
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

    @Override
    public int getModelIndex(ItemStack itemStack, int maxIndex) {
        return IntCircuitIngredient.getCircuitConfiguration(itemStack);
    }
}
