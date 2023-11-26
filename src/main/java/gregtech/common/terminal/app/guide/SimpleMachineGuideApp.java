package gregtech.common.terminal.app.guide;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.MetaTileEntities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SimpleMachineGuideApp extends GuideApp<MetaTileEntity> {

    public SimpleMachineGuideApp() {
        super("machines", new ItemStackTexture(MetaTileEntities.CHEMICAL_REACTOR[GTValues.LV].getStackForm()));
    }

    @Override
    protected IGuiTexture itemIcon(MetaTileEntity item) {
        return new ItemStackTexture(item.getStackForm());
    }

    @Override
    protected String itemName(MetaTileEntity item) {
        return item.getStackForm().getDisplayName();
    }

    @Override
    protected String rawItemName(MetaTileEntity item) {
        return item.metaTileEntityId.getPath();
    }

    @Override
    public MetaTileEntity ofJson(JsonObject json) {
        String[] valids = { "machine", "generator", "metatileentity" };
        if (json.isJsonObject()) {
            for (String valid : valids) {
                JsonElement id = json.getAsJsonObject().get(valid);
                if (id != null && id.isJsonPrimitive())
                    return GregTechAPI.MTE_REGISTRY.getObject(GTUtility.gregtechId(id.getAsString()));
            }
        }
        return null;
    }
}
