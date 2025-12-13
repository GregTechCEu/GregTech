package gregtech.client;

import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluidRegistration;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTLog;
import gregtech.api.util.IBlockOre;
import gregtech.api.util.Mods;
import gregtech.api.util.input.KeyBind;
import gregtech.client.model.customtexture.CustomTextureModelHandler;
import gregtech.client.model.customtexture.MetadataSectionCTM;
import gregtech.client.renderer.handler.FacadeRenderer;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.client.renderer.pipe.LaserPipeRenderer;
import gregtech.client.renderer.pipe.OpticalPipeRenderer;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.ItemRenderCompat;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.CommonProxy;
import gregtech.common.ConfigHolder;
import gregtech.common.MetaEntities;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;
import paulscode.sound.SoundSystemConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static boolean isGUIClosingPermanently = false;

    public void onPreLoad() {
        super.onPreLoad();

        SoundSystemConfig.setNumberNormalChannels(ConfigHolder.client.maxNumSounds);

        if (!Mods.CTM.isModLoaded()) {
            Minecraft.getMinecraft().metadataSerializer.registerMetadataSectionType(new MetadataSectionCTM.Serializer(),
                    MetadataSectionCTM.class);
            MinecraftForge.EVENT_BUS.register(CustomTextureModelHandler.INSTANCE);
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener(CustomTextureModelHandler.INSTANCE);
        }

        MetaTileEntityRenderer.preInit();
        CableRenderer.INSTANCE.preInit();
        FluidPipeRenderer.INSTANCE.preInit();
        ItemPipeRenderer.INSTANCE.preInit();
        OpticalPipeRenderer.INSTANCE.preInit();
        LaserPipeRenderer.INSTANCE.preInit();
        MetaEntities.initRenderers();

        MinecraftForge.EVENT_BUS.register(KeyBind.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerColors();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        ItemRenderCompat.init();
        FacadeRenderer.init();
    }

    public static void registerColors() {
        MetaBlocks.registerColors();
        MetaItems.registerColors();
        ToolItems.registerColors();
    }

    @SubscribeEvent
    public static void textureStitchPre(@NotNull TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        GTFluidRegistration.INSTANCE.registerSprites(map);
        Textures.register(map);
        PipeRenderer.initializeRestrictor(map);
        CableRenderer.INSTANCE.registerIcons(map);
        FluidPipeRenderer.INSTANCE.registerIcons(map);
        ItemPipeRenderer.INSTANCE.registerIcons(map);
        OpticalPipeRenderer.INSTANCE.registerIcons(map);
        LaserPipeRenderer.INSTANCE.registerIcons(map);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MetaBlocks.registerStateMappers();
        MetaBlocks.registerItemModels();
        MetaItems.registerModels();
        ToolItems.registerModels();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerBakedModels(ModelBakeEvent event) {
        GTLog.logger.info("Registering special item models");
        MetaItems.registerBakedModels(event);
        ToolItems.registerBakedModels(event);
    }

    @SubscribeEvent
    public static void addMaterialFormulaHandler(@NotNull ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) itemStack.getItem()).getBlock();
            if (!(block instanceof BlockFrame) && !(block instanceof BlockCompressed) &&
                    !(block instanceof IBlockOre) && !(block instanceof IFluidBlock)) {
                // Do not apply this tooltip to blocks other than:
                // - Frames
                // - Compressed Blocks
                // - Ores
                // - Fluids
                return;
            }
        }

        // Handles Item tooltips
        List<String> tooltips = new ArrayList<>();

        // Test for Items
        UnificationEntry unificationEntry = OreDictUnifier.getUnificationEntry(itemStack);

        if (itemStack.getItem() instanceof MetaOreDictItem) { // Test for OreDictItems
            MetaOreDictItem oreDictItem = (MetaOreDictItem) itemStack.getItem();
            Optional<String> oreDictName = OreDictUnifier.getOreDictionaryNames(itemStack).stream().findFirst();
            if (oreDictName.isPresent() && oreDictItem.OREDICT_TO_FORMULA.containsKey(oreDictName.get()) &&
                    !oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()).isEmpty()) {
                tooltips.add(TextFormatting.YELLOW + oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()));
            }
        } else if (unificationEntry != null && unificationEntry.material != null) {
            if (unificationEntry.material.getChemicalFormula() != null &&
                    !unificationEntry.material.getChemicalFormula().isEmpty())
                tooltips.add(TextFormatting.YELLOW + unificationEntry.material.getChemicalFormula());
        } else if (itemStack.hasTagCompound()) { // Test for Fluids
            // Vanilla bucket
            // noinspection ConstantConditions
            tooltips = FluidTooltipUtil.getFluidTooltip(itemStack.getTagCompound().getString("FluidName"));

            // GTCE Cells, Forestry cans, some other containers
            if (tooltips == null || tooltips.size() == 0) {
                // if (itemStack.getItem() instanceof ItemBlock && ((ItemBlock) itemStack.getItem()).getBlock() ==
                // GregTechAPI.MACHINE && itemStack.getItemDamage())
                NBTTagCompound compound = itemStack.getTagCompound();
                if (compound != null &&
                        compound.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
                    FluidStack fstack = FluidStack
                            .loadFluidStackFromNBT(compound.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
                    tooltips = FluidTooltipUtil.getFluidTooltip(fstack);
                }
            }
        } else if (itemStack.getItem().equals(Items.WATER_BUCKET)) { // Water and Lava buckets have a separate registry
                                                                     // name from other buckets
            tooltips = FluidTooltipUtil.getFluidTooltip(Materials.Water.getFluid());
        } else if (itemStack.getItem().equals(Items.LAVA_BUCKET)) {
            tooltips = FluidTooltipUtil.getFluidTooltip(Materials.Lava.getFluid());
        }

        if (tooltips != null) {
            for (String s : tooltips) {
                if (s == null || s.isEmpty()) continue;
                event.getToolTip().add(s);
            }
        }
    }

    private static final String[] clearRecipes = new String[] {
            "quantum_tank",
            "quantum_chest",
            "super_chest",
            "super_tank",
            "drum.",
            "_tank",
            "fluid_cell"
    };

    @SubscribeEvent
    public static void addNBTClearingTooltip(ItemTooltipEvent event) {
        // Quantum Tank/Chest NBT Clearing Recipe Tooltip
        final EntityPlayer player = event.getEntityPlayer();
        if (player != null) {
            InventoryCrafting inv = null;
            InventoryCraftResult result = null;

            if (player.openContainer instanceof ContainerWorkbench) {
                inv = ((ContainerWorkbench) player.openContainer).craftMatrix;
                result = ((ContainerWorkbench) player.openContainer).craftResult;
            } else if (player.openContainer instanceof ContainerPlayer) {
                inv = ((ContainerPlayer) player.openContainer).craftMatrix;
                result = ((ContainerPlayer) player.openContainer).craftResult;
            }

            if (inv != null) {
                ItemStack stackResult = result.getStackInSlot(0);

                if (stackResult == event.getItemStack()) {
                    if (!stackResult.isEmpty() && ItemStack.areItemsEqual(stackResult, event.getItemStack())) {
                        String unlocalizedName = stackResult.getTranslationKey();
                        // noinspection ConstantConditions
                        String namespace = stackResult.getItem().getRegistryName().getNamespace();
                        for (String key : clearRecipes) {
                            if (unlocalizedName.contains(key) && namespace.equals(GTValues.MODID)) {

                                for (int i = 0; i < inv.getSizeInventory(); i++) {
                                    ItemStack craftStack = inv.getStackInSlot(i);
                                    if (!craftStack.isEmpty()) {
                                        if (!craftStack.isItemEqual(stackResult) || !craftStack.hasTagCompound())
                                            return;
                                    }
                                }
                                event.getToolTip().add(I18n.format("gregtech.universal.clear_nbt_recipe.tooltip"));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation") // we need the deprecated I18n to match EnderCore in all cases
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cleanupDebugTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        boolean isAdvanced = event.getFlags().isAdvanced();
        List<String> tooltip = event.getToolTip();

        if (isAdvanced) {

            // Remove durability keys. These can always be removed, as GT puts one of its own in the tooltip already.
            if (stack.getItem() instanceof IGTTool) {
                // vanilla durability key
                tooltip.remove(I18n.format("item.durability", stack.getMaxDamage() - stack.getItemDamage(),
                        stack.getMaxDamage()));
                // EnderCore durability key
                tooltip.remove(
                        net.minecraft.util.text.translation.I18n.translateToLocal("endercore.tooltip.durability") +
                                " " + (stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage());
            }

            // MC and EnderCore debug tooltips. Remove these always, as we will format them differently later
            String nbtTags = null, registryName = null;
            if (stack.getTagCompound() != null) {
                nbtTags = TextFormatting.DARK_GRAY +
                        I18n.format("item.nbt_tags", stack.getTagCompound().getKeySet().size());
                tooltip.remove(nbtTags);
            }
            if (stack.getItem().getRegistryName() != null) {
                registryName = TextFormatting.DARK_GRAY + stack.getItem().getRegistryName().toString();
                tooltip.remove(registryName);
                // also remove the EnderCore one, since again we are handling it different later
                tooltip.remove(stack.getItem().getRegistryName().toString());
            }

            if (hasActuallyAdvancedInfo(tooltip)) {
                // EnderCore ore-dict names. Remove these only if AAInfo is present, otherwise leave them be
                if (TooltipHelper.isShiftDown()) {
                    int[] oreIds = OreDictionary.getOreIDs(event.getItemStack());
                    if (oreIds.length > 0) {
                        tooltip.remove(net.minecraft.util.text.translation.I18n
                                .translateToLocal("endercore.tooltip.oreDictNames"));
                        for (int i : oreIds) {
                            tooltip.remove("  - " + OreDictionary.getOreName(i));
                        }
                    }
                }
            } else {
                // Add back this information if AAInfo is not present
                if (nbtTags != null) tooltip.add(nbtTags);
                if (registryName != null) tooltip.add(registryName);
            }
        }
    }

    private static boolean hasActuallyAdvancedInfo(List<String> tooltip) {
        // Actually Additions Keys
        if (tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC +
                I18n.format("tooltip.actuallyadditions.extraInfo.desc") + ":"))
            return true;
        if (tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC +
                I18n.format("tooltip.actuallyadditions.ctrlForMoreInfo.desc")))
            return true;
        // Actually Advanced Info Keys
        if (tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Advanced Info:")) return true;
        return tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Press CTRL for Advanced Info");
    }

    @Override
    public boolean isFancyGraphics() {
        return Minecraft.getMinecraft().gameSettings.fancyGraphics;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMouseEvent(@NotNull MouseEvent event) {
        if (!ConfigHolder.client.toolbeltConfig.enableToolbeltScrollingCapture) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (event.getDwheel() != 0 && player.isSneaking()) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemGTToolbelt toolbelt) {
                // vanilla code in GuiIngame line 1235 does not copy the stack before storing it in the highlighting
                // item stack, so unless we copy the stack the tool highlight will not refresh.
                stack = stack.copy();
                toolbelt.changeSelectedToolMousewheel(event.getDwheel(), stack);
                InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
                inv.mainInventory.set(inv.currentItem, stack);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPostEvent(RenderGameOverlayEvent.Post event) {
        if (!ConfigHolder.client.toolbeltConfig.enableToolbeltHotbarDisplay) return;
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            if (Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge gui) {
                ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
                if (stack.getItem() instanceof ItemGTToolbelt toolbelt) {
                    renderToolbeltHotbar(gui, stack, toolbelt, event.getResolution(), event.getPartialTicks());
                }
            }
        }
    }

    private static void renderToolbeltHotbar(GuiIngameForge gui, ItemStack stack, ItemGTToolbelt toolbelt,
                                             ScaledResolution sr, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        int offset = 31;
        int slots = Math.min(9, toolbelt.getSlotCount(stack));
        GuiIngameForge.left_height += offset - 6;
        if (slots > 4) {
            GuiIngameForge.right_height += offset - 6;
        }
        if (mc.getRenderViewEntity() instanceof EntityPlayer entityplayer) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(GuiIngame.WIDGETS_TEX_PATH);
            int i = sr.getScaledWidth() / 2;
            float f = gui.zLevel;
            gui.zLevel = -90.0F;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            // draw the left side of the hotbar
            gui.drawTexturedModalRect(i - 91, sr.getScaledHeight() - 22 - offset, 0, 0, slots * 20 - 18, 22);
            // draw the endpiece to the hotbar
            gui.drawTexturedModalRect(i - 91 + slots * 20 - 18, sr.getScaledHeight() - 22 - offset, 162, 0, 20, 22);
            int selected = toolbelt.getSelectedSlot(stack);
            if (selected != -1) {
                gui.drawTexturedModalRect(i - 91 - 1 + selected * 20, sr.getScaledHeight() - 22 - 1 - offset, 0, 22, 24,
                        24);
            }
            gui.zLevel = -80f;
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();

            for (int l = 0; l < slots; ++l) {
                ItemStack stack1 = toolbelt.getToolInSlot(stack, l);
                if (stack1.isEmpty()) continue;
                int i1 = i - 90 + l * 20 + 2;
                int j1 = sr.getScaledHeight() - 16 - 3 - offset;
                gui.renderHotbarItem(i1, j1, partialTicks, entityplayer, stack1);
            }

            gui.zLevel = f;

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    }

    @SubscribeEvent
    public static void onGuiChange(GuiOpenEvent event) {
        isGUIClosingPermanently = (event.getGui() == null);
    }
}
