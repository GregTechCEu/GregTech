package gregtech.api.util.input;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Key {

    public final EnumKey KEY;
    public boolean state;
    public static final String KEYS_CATEGORY = "gregtech";

    @SideOnly(Side.CLIENT)
    private KeyBinding binding;

    public Key(EnumKey type) {
        this.KEY = type;
        this.state = false;
    }

    public Key(EnumKey type, boolean state) {
        this.KEY = type;
        this.state = state;
    }

    @SideOnly(Side.CLIENT)
    public Key(EnumKey type, KeyBinding bind) {
        this.KEY = type;
        this.state = false;
        if (bind != null) {
            this.binding = bind;
        } else {
            this.binding = new KeyBinding(type.NAME, type.BUTTON_INDEX, type.CATEGORY);
        }
    }

    @SideOnly(Side.CLIENT)
    public KeyBinding getBind() {
        return this.binding;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Key) {
            return this.KEY == ((Key) object).KEY;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s", KEY.toString(), state);
    }
}
