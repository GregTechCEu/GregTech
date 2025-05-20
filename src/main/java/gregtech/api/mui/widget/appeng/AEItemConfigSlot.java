package gregtech.api.mui.widget.appeng;

import gregtech.api.util.GTLog;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> implements Interactable,
                              JeiGhostIngredientSlot<ItemStack>,
                              JeiIngredientProvider {

    public AEItemConfigSlot(ExportOnlyAEItemList itemList, int index) {
        super(itemList.getInventory()[index], itemList.isStocking());
    }

    @Override
    public void onInit() {
        size(18, 18);
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        GTLog.logger.info(ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof ItemStack stack ? stack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return backingSlot.getConfig();
    }
}
