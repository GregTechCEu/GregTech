package gregtech.api.statemachine;

import net.minecraft.nbt.NBTTagCompound;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

// if serializableTag is null, then the offthread worker never completed a non-transient operator and
// nextOpAfterLastSerializable is -2
@Desugar
public record GTSMWalkCompletionData(int nextOpID, NBTTagCompound data, Map<String, Object> transientData,
                                     int nextOpAfterLastSerializable, @Nullable NBTTagCompound serializableTag) {}
