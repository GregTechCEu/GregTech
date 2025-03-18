package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MetaTileEntityCraftingInputBus extends MetaTileEntityMultiblockNotifiablePart implements IControllable,
                                            IMultiblockAbilityPart<ItemStack> {

    boolean isWorking = true;
    private GhostCircuitItemStackHandler circuitInventory;
    private PatternHandler patternHandler;

    public MetaTileEntityCraftingInputBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.IV, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCraftingInputBus(this.metaTileEntityId);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.circuitInventory.addNotifiableMetaTileEntity(this);
        this.patternHandler = new PatternHandler(this, 9, getController(), false);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        AtomicReference<IPanelHandler> panel = new AtomicReference<>();
        return GTGuis.defaultPanel(this)
                .child(SlotGroupWidget.builder()
                        .row("xxxxxxxxx")
                        .row("rrrrrrrrr")
                        .key('x', value -> new ItemSlot().slot(patternHandler, value))
                        .key('r', value -> new ButtonWidget<>()
                                .onUpdateListener(w -> {
                                    IItemHandler h = patternHandler.patterns[value];
                                    w.overlay(h.getSlots() == 0 ? GTGuiTextures.BUTTON_X : IDrawable.NONE);
                                    if (panel.get() == null) {
                                        panel.set(IPanelHandler.simple(w.getPanel(), (parentPanel, player) -> {
                                            int rowSize = h.getSlots() > 9 ? 4 : 3;
                                            var item = new ItemDrawable();
                                            return GTGuis.defaultPopupPanel("pattern_" + value)
                                                    .child(new Grid().mapTo(rowSize, h.getSlots(),
                                                            value1 -> new DynamicDrawable(
                                                                    () -> item.setItem(h.getStackInSlot(value1)))
                                                                            .asWidget()
                                                                            .size(18)
                                                                            .background(GTGuiTextures.SLOT)));
                                        }, true));
                                    }
                                })
                                .onMousePressed(mouseButton -> {
                                    if (patternHandler.patterns[value].getSlots() != 0) {
                                        IPanelHandler p = panel.get();
                                        if (p.isPanelOpen()) {
                                            p.deleteCachedPanel();
                                            p.closePanel();
                                        } else {
                                            p.openPanel();
                                        }
                                        return true;
                                    }
                                    return false;
                                }))
                        .build()
                        .margin(4))
                .bindPlayerInventory();
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorking;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorking = isWorkingAllowed;
    }

    @Override
    public @NotNull List<MultiblockAbility<?>> getAbilities() {
        return Collections.singletonList(MultiblockAbility.IMPORT_ITEMS);
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        Collections.addAll(abilityInstances, this.patternHandler.patterns);
    }

    static class DelegateHandler implements IItemHandlerModifiable {

        private static final IItemHandler EMPTY = new ItemStackHandler(0);
        private IItemHandler currentHandler = EMPTY;

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (getCurrentHandler() instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, stack);
            }
        }

        public IItemHandler getCurrentHandler() {
            return currentHandler;
        }

        @Override
        public int getSlots() {
            return getCurrentHandler().getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return getCurrentHandler().getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return getCurrentHandler().insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return getCurrentHandler().extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getCurrentHandler().getSlotLimit(slot);
        }

        public void setCurrentHandler(IItemHandler handler) {
            this.currentHandler = handler == null ? EMPTY : handler;
        }
    }

    public static class PatternHandler extends NotifiableItemStackHandler {

        final DelegateHandler[] patterns;
        final World world;

        public PatternHandler(MetaTileEntity metaTileEntity, int slots, MetaTileEntity entityToNotify,
                              boolean isExport) {
            super(metaTileEntity, slots, entityToNotify, isExport);
            world = metaTileEntity.getWorld();
            patterns = new DelegateHandler[slots];
            Arrays.setAll(patterns, value -> new DelegateHandler());
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.isEmpty() || stack.getItem() instanceof ICraftingPatternItem;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            if (!simulate) {
                updateSlot(slot, stack);
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!simulate) {
                updateSlot(slot, ItemStack.EMPTY);
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!isItemValid(slot, stack)) return;
            updateSlot(slot, stack);
            super.setStackInSlot(slot, stack);
        }

        public void updateSlot(int slot, ItemStack stack) {
            IItemHandler handler;
            if (stack.isEmpty()) {
                handler = null;
            } else {
                ICraftingPatternItem patternItem = (ICraftingPatternItem) stack.getItem();
                handler = createHandlerFrom(patternItem.getPatternForItem(stack, world));
            }
            this.patterns[slot].setCurrentHandler(handler);
        }

        @Override
        protected Object getHandler(int slot) {
            return this.patterns[slot];
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        public @NotNull IItemHandler createHandlerFrom(@Nullable ICraftingPatternDetails details) {
            if (details == null) return DelegateHandler.EMPTY;
            return new IItemHandler() {

                final IAEItemStack[] inputs = details.getInputs();
                final ItemStack[] cache = new ItemStack[inputs.length];

                @Override
                public int getSlots() {
                    return inputs.length;
                }

                @Override
                public @NotNull ItemStack getStackInSlot(int slot) {
                    if (cache[slot] != null) return cache[slot];
                    if (inputs[slot] == null) return ItemStack.EMPTY;
                    return cache[slot] = inputs[slot].createItemStack();
                }

                @Override
                public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    return stack;
                }

                @Override
                public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return ItemStack.EMPTY;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return Integer.MAX_VALUE;
                }
            };
        }
    }
}
