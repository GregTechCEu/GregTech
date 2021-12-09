package gregtech.api.util.input;

import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

public enum EnumKey {

    MENU("ALT Menu", Keyboard.KEY_LMENU, Key.KEYS_CATEGORY),
    MODE_SWITCH("Mode Switch", Keyboard.KEY_M, Key.KEYS_CATEGORY),
    FORWARD(null, 0, null),
    JUMP(null, 0, null),
    SHIFT(null, 0, null),
    BOOST(null, 0, null),
    FLY_KEY("Jetpack Flight", Keyboard.KEY_F, Key.KEYS_CATEGORY),
    HOVER_KEY("Jetpack Hover", Keyboard.KEY_H, Key.KEYS_CATEGORY),
    SHARE_KEY("Armor Charging", Keyboard.KEY_N, Key.KEYS_CATEGORY);

    public final String NAME;
    public final String CATEGORY;
    public final byte BUTTON_INDEX;
    private byte ID;
    private static byte idCounter = 0;

    EnumKey(String name, int button_index, String category) {
        this.NAME = name;
        this.BUTTON_INDEX = (byte) button_index;
        this.CATEGORY = category;
        setID();
    }

    @Nullable
    public static EnumKey getKeyByID(int id) {
        for (EnumKey key : EnumKey.values()) {
            if (key.ID == id) return key;
        }
        return null;
    }

    private void setID() {
        this.ID = idCounter++;
    }

    public int getID() {
        return this.ID;
    }
}
