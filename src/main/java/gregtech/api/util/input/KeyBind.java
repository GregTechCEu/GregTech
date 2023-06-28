package gregtech.api.util.input;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.core.network.packets.PacketKeyPressed;
import gregtech.core.network.packets.PacketKeysDown;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public enum KeyBind {

    /* Held keys */
    VANILLA_JUMP(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindJump),
    VANILLA_SNEAK(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindSneak),
    VANILLA_FORWARD(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindForward),
    VANILLA_BACKWARD(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindBack),
    VANILLA_LEFT(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindLeft),
    VANILLA_RIGHT(() -> () -> Minecraft.getMinecraft().gameSettings.keyBindRight),

    /* Pressed keys */
    ARMOR_MODE_SWITCH("gregtech.key.armor_mode_switch", KeyConflictContext.IN_GAME, Keyboard.KEY_M),
    ARMOR_HOVER("gregtech.key.armor_hover", KeyConflictContext.IN_GAME, Keyboard.KEY_H),
    ARMOR_CHARGING("gregtech.key.armor_charging", KeyConflictContext.IN_GAME, Keyboard.KEY_N),
    TOOL_AOE_CHANGE("gregtech.key.tool_aoe_change", KeyConflictContext.IN_GAME, Keyboard.KEY_V);

    public static final KeyBind[] VALUES = values();

    public static void init() {
        GTLog.logger.info("Registering KeyBinds");
        if (FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(KeyBind.class);
        }
    }

    /**
     * Handle Keys which we track for "holds" on the server, meaning if a key is being pressed
     * down for a prolonged period of time. This is a state which gets saved on the server.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Int2BooleanMap updatingKeyDown = new Int2BooleanOpenHashMap();
            for (KeyBind keybind : VALUES) {
                boolean previousKeyDown = keybind.isKeyDown;
                keybind.isKeyDown = keybind.isKeyDown();
                if (previousKeyDown != keybind.isKeyDown) {
                    updatingKeyDown.put(keybind.ordinal(), keybind.isKeyDown);
                }
            }
            if (!updatingKeyDown.isEmpty()) {
                GregTechAPI.networkHandler.sendToServer(new PacketKeysDown(updatingKeyDown));
            }
        }
    }

    /**
     * Handle Keys which we track for "presses" on the server, meaning a single input which
     * sends a packet to the server which informs all listeners.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onInputEvent(InputEvent.KeyInputEvent event) {
        IntList updatingPressed = new IntArrayList();
        for (KeyBind keybind : VALUES) {
            if (keybind.isPressed()) {
                updatingPressed.add(keybind.ordinal());
            }
        }
        if (!updatingPressed.isEmpty()) {
            GregTechAPI.networkHandler.sendToServer(new PacketKeyPressed(updatingPressed));
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean scrollingUp() {
        return Mouse.getEventDWheel() > 0;
    }

    @SideOnly(Side.CLIENT)
    public static boolean notScrolling() {
        return Mouse.getEventDWheel() == 0;
    }

    @SideOnly(Side.CLIENT)
    public static boolean scrollingDown() {
        return Mouse.getEventDWheel() < 0;
    }

    @SideOnly(Side.CLIENT)
    private KeyBinding keybinding;
    @SideOnly(Side.CLIENT)
    private boolean isKeyDown;

    private final WeakHashMap<EntityPlayerMP, Boolean> mapping = new WeakHashMap<>();
    private final WeakHashMap<EntityPlayerMP, Set<IKeyPressedListener>> listeners = new WeakHashMap<>();

    // For Vanilla/Other Mod keybinds
    // Double Supplier to keep client classes from loading
    KeyBind(Supplier<Supplier<KeyBinding>> keybindingGetter) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = keybindingGetter.get().get();
        }
    }

    KeyBind(String langKey, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    KeyBind(String langKey, IKeyConflictContext ctx, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, ctx, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    @SideOnly(Side.CLIENT)
    public KeyBinding toMinecraft() {
        return this.keybinding;
    }

    @SideOnly(Side.CLIENT)
    public boolean isPressed() {
        return this.keybinding.isPressed();
    }

    @SideOnly(Side.CLIENT)
    public boolean isKeyDown() {
        return this.keybinding.isKeyDown();
    }

    public void updateKeyDown(boolean keyDown, EntityPlayerMP player) {
        this.mapping.put(player, keyDown);
    }

    public void onKeyPressed(EntityPlayerMP player) {
        Set<IKeyPressedListener> listenerSet = listeners.get(player);
        if (listenerSet != null && !listenerSet.isEmpty()) {
            for (var listener : listenerSet) {
                listener.onKeyPressed(player, this);
            }
        }
    }

    public boolean isKeyDown(EntityPlayer player) {
        if (player.world.isRemote) return isKeyDown();
        return mapping.get((EntityPlayerMP) player);
    }

    public void registerListener(EntityPlayerMP player, IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = listeners.computeIfAbsent(player, k -> new HashSet<>());
        listenerSet.add(listener);
    }

    public void removeListener(EntityPlayerMP player, IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = listeners.get(player);
        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }
}
