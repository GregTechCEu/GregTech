package gregtech.api.mui;

import gregtech.api.GTValues;
import gregtech.client.ClientProxy;
import gregtech.integration.jei.JustEnoughItemsModule;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerRecipeTransferHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
@SideOnly(Side.CLIENT)
public class GregTechGuiScreen extends ModularScreen implements RecipeViewerRecipeTransferHandler {

    // Stores lists of higher priority recipe receivers to the left of the tree
    private static final Int2ObjectMap<Map<String, IRecipeTransferReceiver>> registeredRecipeTransferReceivers = new Int2ObjectAVLTreeMap<>(
            IntComparators.OPPOSITE_COMPARATOR);

    public GregTechGuiScreen(ModularPanel mainPanel) {
        this(mainPanel, GTGuiTheme.STANDARD);
    }

    public GregTechGuiScreen(ModularPanel mainPanel, GTGuiTheme theme) {
        this(GTValues.MODID, mainPanel, theme);
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, GTGuiTheme theme) {
        this(owner, mainPanel, theme.getId());
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, String themeId) {
        super(owner, mainPanel);
        useTheme(themeId);
    }

    @Override
    public void onClose() {
        // Only clear all registered recipe receivers when the UI is truly closing, ie not just opening JEI over it.
        if (ClientProxy.isGUIClosingPermanently) {
            // Clear all registered recipe receivers on UI close, just in case.
            registeredRecipeTransferReceivers.clear();
        }
    }

    @Override
    public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
        // Receivers are sorted high to low on registration
        for (Map<String, IRecipeTransferReceiver> subMap : registeredRecipeTransferReceivers.values()) {
            for (IRecipeTransferReceiver receiver : subMap.values()) {
                IRecipeTransferError result = receiver.receiveRecipe(recipeLayout, maxTransfer, simulate);
                if (result != null && result.getType() == IRecipeTransferError.Type.INTERNAL) continue;
                return result;
            }
        }

        // No valid transfer handler was found
        return JustEnoughItemsModule.transferHelper.createInternalError();
    }

    /**
     * Register an {@link IRecipeTransferReceiver} to this screen. <br/>
     * Recipe transfer handlers registered through this method will have a priority of {@code 0}. <br/>
     * <b>Important:</b> ensure that you remove this handler with {@link #removeRecipeTransferHandler(String)} when it's
     * disposed of! <br/>
     * Remove it by calling {@link #removeRecipeTransferHandler(String)} from {@link Widget#dispose()} for widgets and
     * {@link SyncHandler#dispose()} for sync handlers.
     * 
     * @throws IllegalArgumentException if a receiver with the given key already exists.
     */
    public static void registerRecipeTransferHandler(@NotNull String key,
                                                     @NotNull IRecipeTransferReceiver transferReceiver) {
        registerRecipeTransferHandler(key, transferReceiver, 0);
    }

    /**
     * Register an {@link IRecipeTransferReceiver} to this screen with a certain priority. Higher numbers will be tried
     * first. <br/>
     * <b>Important:</b> ensure that you remove this handler with {@link #removeRecipeTransferHandler(String)} when it's
     * disposed of! <br/>
     * Remove it by calling {@link #removeRecipeTransferHandler(String)} from {@link Widget#dispose()} for widgets and
     * {@link SyncHandler#dispose()} for sync handlers.
     * 
     * @throws IllegalArgumentException if a receiver with the given key already exists.
     */
    public static void registerRecipeTransferHandler(@NotNull String key,
                                                     @NotNull IRecipeTransferReceiver transferReceiver,
                                                     int priority) {
        for (Map<String, IRecipeTransferReceiver> subMap : registeredRecipeTransferReceivers.values()) {
            if (subMap.containsKey(key)) {
                throw new IllegalArgumentException(
                        "Tried to register a recipe transfer receiver to a key that's already used!");
            }
        }

        registeredRecipeTransferReceivers.computeIfAbsent(priority, $ -> new Object2ObjectOpenHashMap<>())
                .put(key, transferReceiver);
    }

    /**
     * Remove a registered {@link IRecipeTransferReceiver} from this screen.
     * 
     * @throws IllegalArgumentException if no receiver exists with the given key.
     */
    public static void removeRecipeTransferHandler(@NotNull String key) {
        for (Map<String, IRecipeTransferReceiver> subMap : registeredRecipeTransferReceivers.values()) {
            if (subMap.containsKey(key)) {
                subMap.remove(key);
                return;
            }
        }

        throw new IllegalArgumentException("Tried to remove a recipe transfer receiver by a key that didn't exist!");
    }
}
