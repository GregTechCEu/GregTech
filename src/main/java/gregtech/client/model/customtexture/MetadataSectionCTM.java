package gregtech.client.model.customtexture;

import com.google.gson.*;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;

@SideOnly(Side.CLIENT)
public class MetadataSectionCTM implements IMetadataSection {
    public static final String SECTION_NAME = "ctm";

    public BlockRenderLayer layer = null;
    public int skyLight = -1;
    public int blockLight = -1;

    public static MetadataSectionCTM fromJson(JsonObject obj) throws JsonParseException {
        MetadataSectionCTM ret = new MetadataSectionCTM();

        if (!obj.has("gregtech") || !obj.get("gregtech").isJsonPrimitive() || !obj.get("gregtech").getAsBoolean()) {
            return null;
        }

        if (obj.has("layer")) {
            JsonElement layerEle = obj.get("layer");
            if (layerEle.isJsonPrimitive() && layerEle.getAsJsonPrimitive().isString()) {
                try {
                    ret.layer = BlockRenderLayer.valueOf(layerEle.getAsString());
                } catch (IllegalArgumentException e) {
                    throw new JsonParseException("Invalid block layer given: " + layerEle);
                }
            }
        }

        if (obj.has("extra") && obj.get("extra").isJsonObject()) {
            JsonObject extraData = obj.getAsJsonObject("extra");
            if (extraData.has("light")) {
                if (extraData.get("light").isJsonPrimitive()) {
                    JsonPrimitive light = extraData.getAsJsonPrimitive("light");
                    if (light.isNumber()) {
                        ret.skyLight = light.getAsInt();
                        ret.blockLight = light.getAsInt();
                    }
                } else if (extraData.get("light").isJsonObject()) {
                    JsonObject light = extraData.getAsJsonObject("light");
                    if (light.has("block") && light.get("block").isJsonPrimitive() && light.getAsJsonPrimitive("block").isNumber()) {
                        ret.blockLight = light.getAsJsonPrimitive("block").getAsInt();
                    }
                    if (light.has("sky") && light.get("sky").isJsonPrimitive() && light.getAsJsonPrimitive("sky").isNumber()) {
                        ret.blockLight = light.getAsJsonPrimitive("sky").getAsInt();
                    }
                }
            }
        }

        return ret;
    }

    public static class Serializer implements IMetadataSectionSerializer<MetadataSectionCTM> {

        @Override
        public @Nullable
        MetadataSectionCTM deserialize(@Nullable JsonElement json, @Nullable Type typeOfT, @Nullable JsonDeserializationContext context) throws JsonParseException {
            if (json != null && json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                return MetadataSectionCTM.fromJson(obj);
            }
            return null;
        }

        @Override
        public @Nonnull
        String getSectionName() {
            return SECTION_NAME;
        }
    }
}
