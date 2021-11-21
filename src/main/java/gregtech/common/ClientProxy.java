package gregtech.common;

import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.ItemNBTUtils;
import codechicken.lib.util.ResourceUtils;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.realmsclient.gui.ChatFormatting;
import gregtech.api.GTValues;
import gregtech.api.model.customtexture.CustomTextureModelHandler;
import gregtech.api.model.customtexture.MetadataSectionCTM;
import gregtech.api.render.MetaTileEntityRenderer;
import gregtech.api.render.Textures;
import gregtech.api.render.ToolRenderHandler;
import gregtech.api.render.shader.Shaders;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.*;
import gregtech.api.util.input.KeyBinds;
import gregtech.common.blocks.*;
import gregtech.common.covers.facade.FacadeRenderer;
import gregtech.common.items.MetaItems;
import gregtech.common.render.CableRenderer;
import gregtech.common.render.FluidPipeRenderer;
import gregtech.common.render.ItemPipeRenderer;
import gregtech.common.render.StoneRenderer;
import gregtech.common.terminal.app.capeselector.CapeSelectorApp;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private static ResourceLocation defaultPlayerCape;

    public static final IBlockColor COMPRESSED_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            state.getValue(((BlockCompressed) state.getBlock()).variantProperty).getMaterialRGB();

    public static final IItemColor COMPRESSED_ITEM_COLOR = (stack, tintIndex) -> {
        BlockCompressed block = (BlockCompressed) ((ItemBlock) stack.getItem()).getBlock();
        IBlockState state = block.getStateFromMeta(stack.getItemDamage());
        return state.getValue(block.variantProperty).getMaterialRGB();
    };

    public static final IBlockColor FRAME_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) -> {
        Material material = ((BlockFrame) state.getBlock()).frameMaterial;
        return material.getMaterialRGB();
    };

    public static final IItemColor FRAME_ITEM_COLOR = (stack, tintIndex) -> {
        IBlockState frameState = ((FrameItemBlock) stack.getItem()).getBlockState(stack);
        BlockFrame block = (BlockFrame) frameState.getBlock();
        return block.frameMaterial.getMaterialRGB();
    };

    public static final IBlockColor ORE_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            tintIndex == 1 ? ((BlockOre) state.getBlock()).material.getMaterialRGB() : 0xFFFFFF;

    public static final IItemColor ORE_ITEM_COLOR = (stack, tintIndex) ->
            tintIndex == 1 ? ((BlockOre) ((ItemBlock) stack.getItem()).getBlock()).material.getMaterialRGB() : 0xFFFFFF;

    public static final IBlockColor FOAM_BLOCK_COLOR = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) ->
            state.getValue(BlockColored.COLOR).colorValue;

    public void onPreLoad() {
        super.onPreLoad();

        if (!GTValues.isModLoaded(GTValues.MODID_CTM)) {
            Minecraft.getMinecraft().metadataSerializer.registerMetadataSectionType(new MetadataSectionCTM.Serializer(), MetadataSectionCTM.class);
            MinecraftForge.EVENT_BUS.register(CustomTextureModelHandler.INSTANCE);
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(CustomTextureModelHandler.INSTANCE);
        }

        KeyBinds.initBinds();
        MetaTileEntityRenderer.preInit();
        CableRenderer.preInit();
        FluidPipeRenderer.preInit();
        ItemPipeRenderer.preInit();
        StoneRenderer.preInit();
        MetaEntities.initRenderers();
        TextureUtils.addIconRegister(MetaFluids::registerSprites);
        MinecraftForge.EVENT_BUS.register(ToolRenderHandler.INSTANCE);

    }

    @Override
    public void onLoad() {
        KeyBinds.registerClient();
        super.onLoad();
        if (ConfigHolder.debug) {
            ClientCommandHandler.instance.registerCommand(new Shaders.ShaderCommand());
        }
        registerColors();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
        TerminalRegistry.initTerminalFiles();
        ResourceUtils.registerReloadListener(ToolRenderHandler.INSTANCE);
        ModCompatibility.initCompat();
        FacadeRenderer.init();
        this.registerAutomaticCapes();
    }

    private void registerAutomaticCapes() {
        CapesRegistry.unlockCapeEverywhere(UUID.fromString("2fa297a6-7803-4629-8360-7059155cf43e"), Textures.GREGTECH_CAPE_TEXTURE); // KilaBash
        CapesRegistry.unlockCapeEverywhere(UUID.fromString("a82fb558-64f9-4dd6-a87d-84040e84bb43"), Textures.GREGTECH_CAPE_TEXTURE); // Dan
        CapesRegistry.unlockCapeEverywhere(UUID.fromString("5c2933b3-5340-4356-81e7-783c53bd7845"), Textures.GREGTECH_CAPE_TEXTURE); // Tech22
        CapesRegistry.unlockCapeEverywhere(UUID.fromString("56bd41d0-06ef-4ed7-ab48-926ce45651f9"), Textures.GREGTECH_CAPE_TEXTURE); // Zalgo239
        CapesRegistry.unlockCapeEverywhere(UUID.fromString("aaf70ec1-ac70-494f-9966-ea5933712750"), Textures.GREGTECH_CAPE_TEXTURE); // Bruberu
        CapesRegistry.unlockCapeEverywhere(UUID.fromString("a24a9108-23d2-43fc-8db7-43f809d017db"), Textures.GREGTECH_CAPE_TEXTURE); // ALongString
    }

    public void registerColors() {
        MetaBlocks.registerColors();
        MetaItems.registerColors();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MetaBlocks.registerStateMappers();
        MetaBlocks.registerItemModels();
        MetaItems.registerModels();
    }

    @SubscribeEvent
    public static void addMaterialFormulaHandler(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();

        // Handles Item tooltips
        String chemicalFormula = null;

        // Test for Items
        UnificationEntry unificationEntry = OreDictUnifier.getUnificationEntry(itemStack);
        if (unificationEntry != null && unificationEntry.material != null) {
            chemicalFormula = unificationEntry.material.getChemicalFormula();

            // Test for Fluids
        } else if (ItemNBTUtils.hasTag(itemStack)) {

            // Vanilla bucket
            chemicalFormula = FluidTooltipUtil.getFluidTooltip(ItemNBTUtils.getString(itemStack, "FluidName"));

            // GTCE Cells, Forestry cans, some other containers
            if (chemicalFormula == null) {
                NBTTagCompound compound = itemStack.getTagCompound();
                if (compound != null && compound.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
                    chemicalFormula = FluidTooltipUtil.getFluidTooltip(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY)));
                }
            }

            // Water buckets have a separate registry name from other buckets
        } else if (itemStack.getItem().equals(Items.WATER_BUCKET)) {
            chemicalFormula = FluidTooltipUtil.getWaterTooltip();
        }
        if (chemicalFormula != null && !chemicalFormula.isEmpty()) {
            event.getToolTip().add(1, ChatFormatting.YELLOW.toString() + chemicalFormula);
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

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) event.getEntityPlayer();
        if (clientPlayer.hasPlayerInfo()) {
            NetworkPlayerInfo playerInfo = ObfuscationReflectionHelper.getPrivateValue(AbstractClientPlayer.class, clientPlayer, 0);
            Map<Type, ResourceLocation> playerTextures = ObfuscationReflectionHelper.getPrivateValue(NetworkPlayerInfo.class, playerInfo, 1);
            if (defaultPlayerCape == null && playerTextures.get(Type.CAPE) != null)
                defaultPlayerCape = playerTextures.get(Type.CAPE);

            if (CapesRegistry.wornCapes.get(event.getEntityPlayer().getPersistentID()) != null)
                playerTextures.put(Type.CAPE, CapesRegistry.wornCapes.get(event.getEntityPlayer().getPersistentID()));
            else
                playerTextures.put(Type.CAPE, defaultPlayerCape);
        }
    }
}
