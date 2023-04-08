package gregtech.api.util.enderlink;

import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import net.minecraft.util.EnumFacing;

import java.util.UUID;
import java.util.regex.Pattern;

public abstract class CoverEnderLinkBase extends CoverBehavior implements CoverWithUI, IControllable {
    protected static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("[0-9a-fA-F]*");
    protected int color;
    protected UUID playerUUID;
    protected boolean isPrivate;
    protected boolean workingEnabled = true;
    protected boolean ioEnabled;
    protected String tempColorStr;
    protected boolean isColorTemp;

    public CoverEnderLinkBase(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        ioEnabled = false;
        isPrivate = false;
        playerUUID = null;
        color = 0xFFFFFFFF;
    }

    protected String makeName(String identifier) {
        return identifier + Integer.toHexString(this.color).toUpperCase();
    }

    protected void updateColor(String str) {
        if (str.length() == 8) {
            isColorTemp = false;
            // stupid java not having actual unsigned ints
            long tmp = Long.parseLong(str, 16);
            if (tmp > 0x7FFFFFFF) {
                tmp -= 0x100000000L;
            }
            this.color = (int) tmp;
            updateLink();
        } else {
            tempColorStr = str;
            isColorTemp = true;
        }
    }

    private void updateLink() {
        coverHolder.markDirty();
    }
}
