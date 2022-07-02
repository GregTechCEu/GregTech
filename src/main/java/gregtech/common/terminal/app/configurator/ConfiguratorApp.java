package gregtech.common.terminal.app.configurator;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.common.covers.*;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConfiguratorApp extends AbstractApplication {
    public static final String APP_NAME = "configurator";
    public static final String APP_NBT_TAG = APP_NAME + "_config";

    public ConfiguratorApp() {
        super(APP_NAME);
    }

    @Override
    public ConfiguratorApp initApp() {
        // Choose to apply, copy, or create a machine configuration
        if (os.clickPos != null && IsValidTileEntity(os.clickPos)) {
            MetaTileEntity entity = ((IGregTechTileEntity) gui.entityPlayer.world.getTileEntity(os.clickPos)).getMetaTileEntity();
            // 1. Apply Config
            if (!os.tabletNBT.getCompoundTag(APP_NBT_TAG).isEmpty()) {
                applyMachineConfiguration(entity);
            }
            // 2. Copy Config
            else {
                writeSettingsToOsNBT(entity);
            }
            os.shutdown(isClient);
        } else {
            // 3. Clear settings if opened while sneaking
            if (os.tabletNBT.getBoolean("_sneak")) {
                writeSettingsToOsNBT(null);
                os.shutdown(isClient);
            } // 4. Open the app to create a configuration
            else {
                writeSettingsToOsNBT(null);
                LaunchApp();
            }
        }
        return this;
    }

    private void writeSettingsToOsNBT(MetaTileEntity entity) {
        if (isClient) {
            return;
        }
        NBTTagCompound data = new NBTTagCompound();
        if (entity != null) {
            os.tabletNBT.setTag(APP_NBT_TAG, entity.writeToNBT(data));
        } else {
            os.tabletNBT.removeTag(APP_NBT_TAG);
        }
    }

    private void LaunchApp() {
        // A settings UI will go here
        // os.shutdown(isClient);
    }

    private boolean IsValidTileEntity(BlockPos TileEntityLocation) {
        TileEntity te = gui.entityPlayer.world.getTileEntity(TileEntityLocation);
        if (te instanceof IGregTechTileEntity) {
            IGregTechTileEntity IGTTE = (IGregTechTileEntity) te;
            return IGTTE.getMetaTileEntity() != null;
        }
        return false;
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }

    public static void applyMachineConfiguration(EntityPlayer entityPlayer, NBTTagCompound TerminalNBT, MetaTileEntity entity) {
        // Make a copy of the existing entity to overwrite with the template
        MetaTileEntity templateEntity = entity.createMetaTileEntity(entity.getHolder());
        templateEntity.readFromNBT(TerminalNBT.getCompoundTag(APP_NBT_TAG));
        // Apply the template
        applyMachineConfiguration(entityPlayer, templateEntity, entity);
    }

    private void applyMachineConfiguration(MetaTileEntity entity) {
        if (isClient) {
            entity.scheduleRenderUpdate();
            return;
        }
        // Make a copy of the existing entity to overwrite with the template
        MetaTileEntity templateEntity = entity.createMetaTileEntity(entity.getHolder());
        templateEntity.readFromNBT(os.tabletNBT.getCompoundTag(APP_NBT_TAG));
        // Apply the template
        applyMachineConfiguration(gui.entityPlayer, templateEntity, entity);
    }

    private static void applyMachineConfiguration(EntityPlayer entityPlayer, MetaTileEntity templateTileEntity, MetaTileEntity existingTileEntity) {
        boolean logging = true;
        List<String> log = new ArrayList<>();

        // Meta Tile Entity
        if (existingTileEntity.isValidFrontFacing(templateTileEntity.getFrontFacing())) {
            existingTileEntity.setFrontFacing(templateTileEntity.getFrontFacing());
        }
        if (existingTileEntity.isMuffled() != templateTileEntity.isMuffled()) {
            existingTileEntity.toggleMuffled();
        }

        // Simple Machine Tile Entity
        if (templateTileEntity instanceof SimpleMachineMetaTileEntity && existingTileEntity instanceof SimpleMachineMetaTileEntity) {
            SimpleMachineMetaTileEntity templateSMTE = (SimpleMachineMetaTileEntity) templateTileEntity;
            SimpleMachineMetaTileEntity existingSMTE = (SimpleMachineMetaTileEntity) existingTileEntity;
            // Check for item capabilities
            if (existingSMTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[0]) != null) {
                existingSMTE.setOutputFacingItems(templateSMTE.getOutputFacingItems());
                existingSMTE.setAutoOutputItems(templateSMTE.isAutoOutputItems());
                existingSMTE.setAllowInputFromOutputSideItems(templateSMTE.isAllowInputFromOutputSideItems());
            }
            // Check for fluid capabilities
            if (existingSMTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[0]) != null) {
                existingSMTE.setOutputFacingFluids(templateSMTE.getOutputFacingFluids());
                existingSMTE.setAutoOutputFluids(templateSMTE.isAutoOutputFluids());
                existingSMTE.setAllowInputFromOutputSideFluids(templateSMTE.isAllowInputFromOutputSideFluids());
            }
        }
        // Everything else
        else {
            copyMetaTileEntity(templateTileEntity, existingTileEntity);
        }

        // Covers
        ArrayList<CoverMachineController> ControlCovers = new ArrayList<>();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (templateTileEntity.getCoverAtSide(side) != null) {
                // Retrieve cover and cover's ItemStack
                CoverBehavior templateCover = templateTileEntity.getCoverAtSide(side);
                ItemStack templateCoverItemStack = templateCover.getPickItem();

                // If player is creative, do not check for cover possession
                if (entityPlayer.isCreative()) {
                    existingTileEntity.placeCoverOnSide(side, templateCoverItemStack, templateCover.getCoverDefinition(), entityPlayer);
                } else {
                    // Check if player inventory contains valid cover or existing cover is the same as template cover
                    // if there is no existing cover and the player doesn't have the cover OR
                    // if there is an existing cover, and the covers are not equal, and the player doesn't have the cover THEN
                    // Move on to the next cover
                    boolean preexistingCover = existingTileEntity.getCoverAtSide(side) != null;
                    boolean playerHasCover = InventoryContains(entityPlayer.inventory.mainInventory, templateCoverItemStack);
                    boolean coversAreEqual = preexistingCover && templateCoverItemStack.isItemEqual(existingTileEntity.getCoverAtSide(side).getPickItem());
                    if ((!preexistingCover && !playerHasCover) || (preexistingCover && !coversAreEqual && !playerHasCover)) {
                        log.add("- Missing (" + templateCover.getPickItem().getDisplayName() + ")");
                        continue;
                    }

                    // If current machine has no cover or has a different cover
                    if (!preexistingCover || !coversAreEqual) {
                        // Create template cover NBT and paste onto existing cover
                        if (existingTileEntity.placeCoverOnSide(side, templateCoverItemStack, templateCover.getCoverDefinition(), entityPlayer)) {
                            // If filter is present, remove from player inventory or else remove from cover
                            if (!SubtractFilterIfMissing(entityPlayer.inventory.mainInventory, templateCover, existingTileEntity.getCoverAtSide(side))) {
                                log.add("- Missing filter missing for (" + templateCover.getPickItem().getDisplayName() + ")");
                            }
                            if (!RemoveItemFromInventory(entityPlayer.inventory.mainInventory, templateCoverItemStack)) {
                                log.add("- Missing (" + templateCover.getPickItem().getDisplayName() + ")");
                            }
                        } else {
                            log.add("- Missing (" + templateCover.getPickItem().getDisplayName() + ")");
                            continue;
                        }
                    } else {
                        if (!SubtractFilterIfMissing(entityPlayer.inventory.mainInventory, templateCover, existingTileEntity.getCoverAtSide(side))) {
                            log.add("- Missing filter for (" + templateCover.getPickItem().getDisplayName() + ")");
                        }
                    }
                }

                // Finally copy settings onto the cover
                NBTTagCompound templateCoverNBT = new NBTTagCompound();
                templateCover.writeToNBT(templateCoverNBT);
                CoverBehavior newCover = existingTileEntity.getCoverAtSide(side);
                newCover.readFromNBT(templateCoverNBT);

                // Special Cases
                if (newCover instanceof CoverMachineController) {
                    // Save control covers for later
                    ControlCovers.add((CoverMachineController) newCover);
                } else if (newCover instanceof CoverCraftingTable) {
                    // Clear machine inventory of crafting table to avoid duping items
                    ((CoverCraftingTable) newCover).clearMachineInventory(NonNullList.create());
                }
            }
        }
        // TODO: Redundant?
        // Ensure the covers being controlled still exist
        for (CoverMachineController controlCover : ControlCovers) {
            if (existingTileEntity.getCoverAtSide(controlCover.getControllerMode().side) == null) {
                existingTileEntity.removeCover(controlCover.attachedSide);
            }
        }

        // Try everything under the sun to get the ***damn block to update visually***
        existingTileEntity.notifyBlockUpdate();
        existingTileEntity.markDirty();
        existingTileEntity.scheduleRenderUpdate();

        // Print logging info
        if (logging && !log.isEmpty()) {
            entityPlayer.sendMessage(new TextComponentString("Not all settings were applied:"));
            entityPlayer.sendMessage(new TextComponentString(String.join("\n", log)));
        }
    }

    private static void copyMetaTileEntity(MetaTileEntity templateTileEntity, MetaTileEntity existingTileEntity) {
        // Create NBT Tag Compound to paste onto the existing entity
        NBTTagCompound MTE = new NBTTagCompound();
        templateTileEntity.clearMachineInventory(NonNullList.create());
        templateTileEntity.writeToNBT(MTE);
        // Remove tags that should not be transferred
        MTE.removeTag("Covers");
        // All Inventories
        HashSet<String> keySet = new HashSet<>(MTE.getKeySet());
        for (String key : keySet) {
            if (key.toLowerCase().contains("inventory")) {
                MTE.removeTag(key);
            }
        }
        // TODO: Front facing may not be necessary as it uses the default MTE method
        // Conflicting Front and Output facings
        if (!existingTileEntity.isValidFrontFacing(EnumFacing.VALUES[MTE.getInteger("FrontFacing")])) {
            // If new front facing is not valid, discard it
            MTE.removeTag("FrontFacing");
        }
        // Also, if new output facing conflicts with the existing unchanged front facing, remove it too
        if (MTE.getInteger("OutputFacing") == existingTileEntity.getFrontFacing().getIndex()) {
            MTE.removeTag("OutputFacing");
        }
        // Special Cases
        if (templateTileEntity instanceof MetaTileEntityQuantumChest && existingTileEntity instanceof MetaTileEntityQuantumChest) {
            MTE.removeTag("ItemAmount");
            MTE.removeTag("ItemStack");
        }
        // Finally, transfer the sanitized NBT Compound Tag
        existingTileEntity.readFromNBT(MTE);
    }

    private static boolean SubtractFilterIfMissing(List<ItemStack> inventory, CoverBehavior templateCover, CoverBehavior existingCover) {
        if (templateCover instanceof CoverPump) {
            ItemStack templateFilter = ((CoverPump) templateCover).getFluidFilterContainer().getFilterInventory().getStackInSlot(0);
            ItemStack existingFilter = existingCover == null ? ItemStack.EMPTY : ((CoverPump) existingCover).getFluidFilterContainer().getFilterInventory().getStackInSlot(0);
            return SubtractFilterIfMissing(inventory, templateFilter, existingFilter);
        } else if (templateCover instanceof CoverEnderFluidLink) {
            ItemStack templateFilter = ((CoverEnderFluidLink) templateCover).getFluidFilterContainer().getFilterInventory().getStackInSlot(0);
            ItemStack existingFilter = existingCover == null ? ItemStack.EMPTY : ((CoverEnderFluidLink) existingCover).getFluidFilterContainer().getFilterInventory().getStackInSlot(0);
            return SubtractFilterIfMissing(inventory, templateFilter, existingFilter);
        } else if (templateCover instanceof CoverConveyor) {
            ItemStack templateFilter = ((CoverConveyor) templateCover).getItemFilterContainer().getFilterInventory().getStackInSlot(0);
            ItemStack existingFilter = existingCover == null ? ItemStack.EMPTY : ((CoverConveyor) existingCover).getItemFilterContainer().getFilterInventory().getStackInSlot(0);
            return SubtractFilterIfMissing(inventory, templateFilter, existingFilter);
        }
        return false;
    }

    private static boolean SubtractFilterIfMissing(List<ItemStack> inventory, ItemStack templateFilter, ItemStack existingFilter) {
        // Does template cover have a filter?
        if (!templateFilter.isEmpty()) {
            // If both covers are the same, delete existing filter so that it is not dropped on the ground
            if (!existingFilter.isEmpty() && templateFilter.isItemEqual(existingFilter)) {
                existingFilter.setCount(0);
                return true;
            } else if (existingFilter.isEmpty() || !templateFilter.isItemEqual(existingFilter)) {
                return RemoveItemFromInventory(inventory, templateFilter);
            }
        }
        return true;
    }

    private static boolean CheckInventory(List<ItemStack> inventory, ItemStack coverItemStack, boolean removeItem) {
        for (ItemStack playerStack : inventory) {
            if (playerStack.isItemEqual(coverItemStack) && !playerStack.isEmpty()) {
                if (removeItem) {
                    playerStack.setCount(playerStack.getCount() - 1);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean RemoveItemFromInventory(List<ItemStack> inventory, ItemStack coverItemStack) {
        return CheckInventory(inventory, coverItemStack, true);
    }

    private static boolean InventoryContains(List<ItemStack> inventory, ItemStack coverItemStack) {
        return CheckInventory(inventory, coverItemStack, false);
    }

}
