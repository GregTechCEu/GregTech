package gtqt.common.items.behaviors;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;

import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTUtility;

import gtqt.common.items.GTQTMetaItems;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ProgrammableCircuit implements ItemUIFactory, IItemBehaviour {
    String name;
    int type;
    MetaItem<?>.MetaValueItem item;

    public ProgrammableCircuit(int type, String name) {
        this.name = name;
        this.type = type;

    }

    public ProgrammableCircuit(MetaItem<?>.MetaValueItem item, String name) {
        this.name = name;
        this.item = item;
    }

    public static ProgrammableCircuit getInstanceFor(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof MetaItem)) return null;
        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) itemStack.getItem()).getItem(itemStack);
        if (valueItem == null) return null;
        for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
            if (behaviour instanceof ProgrammableCircuit) {
                return (ProgrammableCircuit) behaviour;
            }
        }

        return null;
    }

    public void addInformation(ItemStack stack, List<String> lines) {
        if (name.equals("programmable_circuit")) {
            lines.add(I18n.format("请配合可编程覆盖板使用"));
            lines.add(I18n.format("安装有可编程覆盖板的输入总线/输入总成/单方块机器接收到本物品会自动将虚拟电路槽设置为当前电路值并消耗本物品"));
            lines.add(I18n.format("如果该多方块结构拥有输出总线/总成且输出总线/总成有空槽位，则会尝试返还编程电路"));
            lines.add(I18n.format("如果该单方块机器输出有空槽位，则会尝试返还编程电路"));
        }
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public MetaItem<?>.MetaValueItem getItem() {
        return item;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        MetaTileEntity mte = GTUtility.getMetaTileEntity(world, pos);
        ItemStack stack = player.getHeldItem(hand);

        if (!(mte instanceof IGhostSlotConfigurable)) {
            return ActionResult.newResult(EnumActionResult.FAIL, stack);
        } else if (!world.isRemote) {
            ((IGhostSlotConfigurable) mte).setGhostCircuitConfig(type);
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
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(guiData.getUsedItemStack(), 176, 120);
        ItemDrawable circuitPreview = new ItemDrawable(guiData.getUsedItemStack());
        for (int i = 0; i <= 32; i++) {
            int finalI = i;
            guiSyncManager.syncValue("config", i, new InteractionSyncHandler()
                    .setOnMousePressed(b -> {
                        ItemStack item = new ItemStack(GTQTMetaItems.PROGRAMMABLE_CIRCUIT_0.getMetaItem(), 1, 20+finalI);
                        item.setCount(guiData.getUsedItemStack().getCount());
                        circuitPreview.setItem(item);
                        guiData.getPlayer().setHeldItem(guiData.getHand(), item);
                        if (Interactable.hasShiftDown()) panel.animateClose();
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
                                new ItemDrawable(new ItemStack(GTQTMetaItems.PROGRAMMABLE_CIRCUIT_0.getMetaItem(), 1, 20+index)).asIcon().size(16))
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

}
