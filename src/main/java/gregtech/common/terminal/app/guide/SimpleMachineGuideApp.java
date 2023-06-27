package gregtech.common.terminal.app.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.util.ResourceLocation;

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
        String[] valids = {"machine", "generator", "metatileentity"};
        if (json.isJsonObject()) {
            for (String valid : valids) {
                JsonElement id = json.getAsJsonObject().get(valid);
                if (id != null && id.isJsonPrimitive())
                    return GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(GTValues.MODID, id.getAsString()));
            }
        }
        return null;
    }
}
