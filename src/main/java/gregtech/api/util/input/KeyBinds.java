package gregtech.api.util.input;

import gregtech.api.items.armor.ArmorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class KeyBinds {

    // Registry of all used keys
    public static final List<Key> REGISTRY = new ArrayList<>();
    // Logical server-sided variable, where is state of any keys from players
    public static final Map<EntityPlayer, List<Key>> PLAYER_KEYS = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private static List<KeyBinding> bindings;

    @SideOnly(Side.CLIENT)
    public static void initBinds() {
        bindings = Arrays.asList(null,
                null,
                Minecraft.getMinecraft().gameSettings.keyBindForward,
                Minecraft.getMinecraft().gameSettings.keyBindBack,
                Minecraft.getMinecraft().gameSettings.keyBindLeft,
                Minecraft.getMinecraft().gameSettings.keyBindRight,
                Minecraft.getMinecraft().gameSettings.keyBindJump,
                Minecraft.getMinecraft().gameSettings.keyBindSneak,
                Minecraft.getMinecraft().gameSettings.keyBindSprint,
                null,
                null,
                null);
    }

    @SideOnly(Side.CLIENT)
    public static void registerClient() {
        for (Key key : REGISTRY) {
            if (!ArrayUtils.contains(Minecraft.getMinecraft().gameSettings.keyBindings, key.getBind()))
                ClientRegistry.registerKeyBinding(key.getBind());
        }
    }

    public static void register() {
        for (EnumKey type : EnumKey.values()) {
            if (ArmorUtils.SIDE.isClient()) {
                REGISTRY.add(new Key(type, bindings.get(type.getID())));
            } else {
                REGISTRY.add(new Key(type));
            }
        }
    }
}
