package gregtech.client;

import codechicken.lib.texture.TextureUtils;
import gregtech.api.GTValues;
import gregtech.api.fluids.MetaFluids;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.IBlockOre;
import gregtech.api.util.ModCompatibility;
import gregtech.client.model.customtexture.CustomTextureModelHandler;
import gregtech.client.model.customtexture.MetadataSectionCTM;
import gregtech.client.renderer.handler.FacadeRenderer;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.CommonProxy;
import gregtech.common.ConfigHolder;
import gregtech.common.MetaEntities;
import gregtech.common.blocks.*;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import paulscode.sound.SoundSystemConfig;

import javax.annotation.Nonnull;
import java.util.*;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final IBlockColor COMPRESSED_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            state.getValue(((BlockCompressed) state.getBlock()).variantProperty).getMaterialRGB();

    public static final IItemColor COMPRESSED_ITEM_COLOR = (stack, tintIndex) -> {
        BlockCompressed block = (BlockCompressed) ((ItemBlock) stack.getItem()).getBlock();
        IBlockState state = block.getStateFromMeta(stack.getItemDamage());
        return state.getValue(block.variantProperty).getMaterialRGB();
    };

    public static final IBlockColor FRAME_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            state.getValue(((BlockFrame) state.getBlock()).variantProperty).getMaterialRGB();

    public static final IItemColor FRAME_ITEM_COLOR = (stack, tintIndex) -> {
        BlockFrame block = (BlockFrame) ((ItemBlock) stack.getItem()).getBlock();
        IBlockState state = block.getStateFromMeta(stack.getItemDamage());
        return state.getValue(block.variantProperty).getMaterialRGB();
    };

    public static final IBlockColor ORE_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            tintIndex == 1 ? ((BlockOre) state.getBlock()).material.getMaterialRGB() : 0xFFFFFF;

    public static final IItemColor ORE_ITEM_COLOR = (stack, tintIndex) ->
            tintIndex == 1 ? ((BlockOre) ((ItemBlock) stack.getItem()).getBlock()).material.getMaterialRGB() : 0xFFFFFF;

    public static final IBlockColor FOAM_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            state.getValue(BlockColored.COLOR).colorValue;

    public static final IBlockColor SURFACE_ROCK_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            tintIndex == 1 ? state.getValue(((BlockSurfaceRock) state.getBlock()).variantProperty).getMaterialRGB() : -1;

    public static final IBlockColor RUBBER_LEAVES_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            ColorizerFoliage.getFoliageColorBirch();

    public static final IItemColor RUBBER_LEAVES_ITEM_COLOR = (stack, tintIndex) -> ColorizerFoliage.getFoliageColorBirch();

    public static final IBlockColor MACHINE_CASING_BLOCK_COLOR = (state, world, pos, tintIndex) ->
        state.getBlock() instanceof BlockMachineCasing && MetaBlocks.MACHINE_CASING.getMetaFromState(state) == 0 ? 0xFFFFFF : ConfigHolder.client.defaultPaintingColor;

    public static final IItemColor MACHINE_CASING_ITEM_COLOR = (stack, tintIndex) ->
        stack.getItemDamage() == 0 && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockMachineCasing ? 0xFFFFFF : ConfigHolder.client.defaultPaintingColor;

    public void onPreLoad() {
        super.onPreLoad();

        SoundSystemConfig.setNumberNormalChannels(ConfigHolder.client.maxNumSounds);

        if (!Loader.isModLoaded(GTValues.MODID_CTM)) {
            Minecraft.getMinecraft().metadataSerializer.registerMetadataSectionType(new MetadataSectionCTM.Serializer(), MetadataSectionCTM.class);
            MinecraftForge.EVENT_BUS.register(CustomTextureModelHandler.INSTANCE);
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(CustomTextureModelHandler.INSTANCE);
        }

        MetaTileEntityRenderer.preInit();
        CableRenderer.INSTANCE.preInit();
        FluidPipeRenderer.INSTANCE.preInit();
        ItemPipeRenderer.INSTANCE.preInit();
        MetaEntities.initRenderers();
        MetaFluids.initIconFluidSprites();
        TextureUtils.addIconRegister(MetaFluids::registerSprites);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerColors();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        TerminalRegistry.initTerminalFiles();
        ModCompatibility.initCompat();
        FacadeRenderer.init();
    }

    public static void registerColors() {
        MetaBlocks.registerColors();
        MetaItems.registerColors();
        ToolItems.registerColors();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MetaBlocks.registerStateMappers();
        MetaBlocks.registerItemModels();
        MetaItems.registerModels();
        ToolItems.registerModels();
    }

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Pre event) {
        for (MaterialIconSet set : MaterialIconSet.ICON_SETS.values()) {
            event.getMap().registerSprite(MaterialIconType.ore.getBlockTexturePath(set));
            event.getMap().registerSprite(MaterialIconType.block.getBlockTexturePath(set));
        }
        MetaBlocks.COMPRESSED.values().stream().distinct().forEach(c -> c.onTextureStitch(event));
        MetaBlocks.FRAMES.values().stream().distinct().forEach(f -> f.onTextureStitch(event));
        MetaBlocks.ORES.forEach(o -> o.onTextureStitch(event));
    }

    @SubscribeEvent
    public static void addMaterialFormulaHandler(@Nonnull ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) itemStack.getItem()).getBlock();
            if (!(block instanceof BlockFrame) && !(block instanceof BlockCompressed) && !(block instanceof IBlockOre) && !(block instanceof IFluidBlock)) {
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
            if (oreDictName.isPresent() && oreDictItem.OREDICT_TO_FORMULA.containsKey(oreDictName.get()) && !oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()).isEmpty()) {
                tooltips.add(TextFormatting.YELLOW + oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()));
            }
        } else if (unificationEntry != null && unificationEntry.material != null) {
            if (unificationEntry.material.getChemicalFormula() != null && !unificationEntry.material.getChemicalFormula().isEmpty())
                tooltips.add(TextFormatting.YELLOW + unificationEntry.material.getChemicalFormula());
        } else if (itemStack.hasTagCompound()) { // Test for Fluids
            // Vanilla bucket
            //noinspection ConstantConditions
            tooltips = FluidTooltipUtil.getFluidTooltip(itemStack.getTagCompound().getString("FluidName"));

            // GTCE Cells, Forestry cans, some other containers
            if (tooltips == null || tooltips.size() == 0) {
                //if (itemStack.getItem() instanceof ItemBlock && ((ItemBlock) itemStack.getItem()).getBlock() == GregTechAPI.MACHINE && itemStack.getItemDamage())
                NBTTagCompound compound = itemStack.getTagCompound();
                if (compound != null && compound.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
                    FluidStack fstack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
                    tooltips = FluidTooltipUtil.getFluidTooltip(fstack);
                }
            }
        } else if (itemStack.getItem().equals(Items.WATER_BUCKET)) { // Water and Lava buckets have a separate registry name from other buckets
            tooltips = FluidTooltipUtil.getWaterTooltip();
        } else if (itemStack.getItem().equals(Items.LAVA_BUCKET)) {
            tooltips = FluidTooltipUtil.getLavaTooltip();
        }

        if (tooltips != null) {
            for (String s : tooltips) {
                if (s == null || s.isEmpty()) continue;
                event.getToolTip().add(s);
            }
        }
    }

    private static final String[] clearRecipes = new String[]{
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
                        //noinspection ConstantConditions
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
                tooltip.remove(I18n.format("item.durability", stack.getMaxDamage() - stack.getItemDamage(), stack.getMaxDamage()));
                // EnderCore durability key
                tooltip.remove(net.minecraft.util.text.translation.I18n.translateToLocal("endercore.tooltip.durability") + " " + (stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage());
            }

            // MC and EnderCore debug tooltips. Remove these always, as we will format them differently later
            String nbtTags = null, registryName = null;
            if (stack.getTagCompound() != null) {
                nbtTags = TextFormatting.DARK_GRAY + I18n.format("item.nbt_tags", stack.getTagCompound().getKeySet().size());
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
                        tooltip.remove(net.minecraft.util.text.translation.I18n.translateToLocal("endercore.tooltip.oreDictNames"));
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
        if (tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("tooltip.actuallyadditions.extraInfo.desc") + ":")) return true;
        if (tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format("tooltip.actuallyadditions.ctrlForMoreInfo.desc"))) return true;
        // Actually Advanced Info Keys
        if (tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Advanced Info:")) return true;
        return tooltip.contains(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Press CTRL for Advanced Info");
    }

    @Override
    public boolean isFancyGraphics() {
        return Minecraft.getMinecraft().gameSettings.fancyGraphics;
    }
}
