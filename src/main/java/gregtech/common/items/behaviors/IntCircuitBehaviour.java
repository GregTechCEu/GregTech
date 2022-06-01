package gregtech.common.items.behaviors;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.ButtonWidget;
import com.cleanroommc.modularui.common.widget.Row;
import com.cleanroommc.modularui.common.widget.TextWidget;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.guiOld.ModularUI;
import gregtech.api.guiOld.widgets.ClickButtonWidget;
import gregtech.api.guiOld.widgets.DynamicLabelWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
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
import net.minecraft.world.World;

import java.util.List;

public class IntCircuitBehaviour implements IItemBehaviour, ItemUIFactory, ISubItemHandler {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int configuration = IntCircuitIngredient.getCircuitConfiguration(itemStack);
        lines.add(I18n.format("metaitem.int_circuit.configuration", configuration));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            GregTechUI.getPlayerItemUi(hand).open(player);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return ModularUI.builder(gregtech.api.guiOld.GuiTextures.BACKGROUND, 176, 60)
                .label(9, 8, "metaitem.circuit.integrated.gui")
                .widget(new DynamicLabelWidget(82, 30, () -> Integer.toString(IntCircuitIngredient.getCircuitConfiguration(holder.getCurrentItem())), 0x4D4040))
                .widget(new ClickButtonWidget(15, 24, 20, 20, "-5", data -> IntCircuitIngredient.adjustConfiguration(holder, -5)))
                .widget(new ClickButtonWidget(50, 24, 20, 20, "-1", data -> IntCircuitIngredient.adjustConfiguration(holder, -1)))
                .widget(new ClickButtonWidget(104, 24, 20, 20, "+1", data -> IntCircuitIngredient.adjustConfiguration(holder, +1)))
                .widget(new ClickButtonWidget(141, 24, 20, 20, "+5", data -> IntCircuitIngredient.adjustConfiguration(holder, +5)))
                .build(holder, entityPlayer);
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext, ItemStack item) {
        return ModularWindow.builder(new Size(130, 39))
                .setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new TextWidget(new Text("metaitem.circuit.integrated.gui").localise())
                        .setPos(6, 6))
                .widget(new Row()
                        .widget(new ButtonWidget()
                                .setOnClick(GuiFunctions.getIncrementer(-1, -4, -16, -32, val -> IntCircuitIngredient.adjustConfiguration(item, val)))
                                .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                .setSize(14, 14))
                        .widget(new TextFieldWidget()
                                .setGetterInt(() -> IntCircuitIngredient.getCircuitConfiguration(item))
                                .setSetterInt(val -> IntCircuitIngredient.setCircuitConfiguration(item, val))
                                .setNumbers(0, 32)
                                .setTextAlignment(Alignment.Center)
                                .setTextColor(0xFFFFFF)
                                .setBackground(GuiTextures.DISPLAY_SMALL)
                                .setSize(56, 14))
                        .widget(new ButtonWidget()
                                .setOnClick(GuiFunctions.getIncrementer(1, 4, 16, 32, val -> IntCircuitIngredient.adjustConfiguration(item, val)))
                                .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                .setSize(14, 14))
                        .setPos(23, 18))
                .build();
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
