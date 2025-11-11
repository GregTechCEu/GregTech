package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.mui.GTByteBufAdapters;
import gregtech.api.util.function.ByteSupplier;
import gregtech.api.util.function.FloatSupplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public interface UISyncer {

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    boolean syncBoolean(@NotNull BooleanSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    int syncInt(@NotNull IntSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    long syncLong(@NotNull LongSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    byte syncByte(@NotNull ByteSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    double syncDouble(@NotNull DoubleSupplier initial);

    /**
     * Calls the supplier server side only so there's no potential NPEs for the client
     *
     * @param initial supplier to be called on the server
     * @return synced value
     */
    float syncFloat(@NotNull FloatSupplier initial);

    default boolean syncBoolean(boolean initial) {
        return syncBoolean(() -> initial);
    }

    default int syncInt(int initial) {
        return syncInt(() -> initial);
    }

    default long syncLong(long initial) {
        return syncLong(() -> initial);
    }

    default byte syncByte(byte initial) {
        return syncByte(() -> initial);
    }

    default double syncDouble(double initial) {
        return syncDouble(() -> initial);
    }

    default float syncFloat(float initial) {
        return syncFloat(() -> initial);
    }

    default @NotNull String syncString(@NotNull String initial) {
        return syncObject(initial, ByteBufAdapters.STRING);
    }

    default BigInteger syncBigInt(BigInteger initial) {
        return syncObject(initial, ByteBufAdapters.BIG_INT);
    }

    default <T> T syncObject(T initial, IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer) {
        return syncObject((Supplier<T>) () -> initial, serializer, deserializer);
    }

    default <T> T syncObject(T initial, IByteBufAdapter<T> adapter) {
        return syncObject(initial, adapter, adapter);
    }

    <T> T syncObject(@NotNull Supplier<@NotNull T> initial, IByteBufSerializer<T> serializer,
                     IByteBufDeserializer<T> deserializer);

    default <T> T syncObject(@NotNull Supplier<@NotNull T> initial, IByteBufAdapter<T> adapter) {
        return syncObject(initial, adapter, adapter);
    }

    /**
     * Syncs the elements of the collection to the internal buffer. <br />
     * On the server, elements are serialized in the order of iteration. <br />
     * On the client, elements are deserialized in the same order and re-added to the collection.
     * The list <b>will</b> be modified on the client
     *
     * @param initial      collection whose elements should be synced
     * @param serializer   serializer to write an element to the buffer
     * @param deserializer deserializer to read the elements to add them back to the collection
     * @param <T>          collection element's type
     * @param <C>          the collection type itself
     * @return the synced collection
     */
    <T, C extends Collection<T>> C syncCollection(C initial,
                                                  IByteBufSerializer<T> serializer,
                                                  IByteBufDeserializer<T> deserializer);

    /**
     * Syncs the elements of the collection to the internal buffer. <br />
     * On the server, elements are serialized in the order of iteration. <br />
     * On the client, elements are deserialized in the same order and re-added to the collection.
     * The list <b>will</b> be modified on the client
     *
     * @param initial collection whose elements should be synced
     * @param adapter adapter for serialization/deserialization
     * @param <T>     collection element's type
     * @param <C>     the collection type itself
     * @return the synced collection
     */
    default <T, C extends Collection<T>> C syncCollection(C initial, IByteBufAdapter<T> adapter) {
        return syncCollection(initial, adapter, adapter);
    }

    /**
     * Syncs the elements of the array to the internal buffer. <br />
     * On the server, elements are serialized in the order of iteration. <br />
     * On the client, elements are deserialized in the same order. <br />
     * The array <b>will not</b> be modified, instead copied
     *
     * @param initial      array whose elements should be synced
     * @param serializer   serializer to write an element to the buffer
     * @param deserializer deserializer to read the elements to add them back to the collection
     * @param <T>          element type
     * @return on the server, the initial array, otherwise the synced array copy
     */
    <T> T[] syncArray(T[] initial, IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer);

    /**
     * Syncs the elements of the array to the internal buffer. <br />
     * On the server, elements are serialized in the order of iteration. <br />
     * On the client, elements are deserialized in the same order. <br />
     * The array <b>will not</b> be modified, instead copied
     *
     * @param initial array whose elements should be synced
     * @param adapter adapter for serialization/deserialization
     * @param <T>     element type
     * @return on the server, the initial array, otherwise the synced array copy
     */
    default <T> T[] syncArray(T[] initial, IByteBufAdapter<T> adapter) {
        return syncArray(initial, adapter, adapter);
    }

    @NotNull
    default ItemStack syncItemStack(@NotNull Supplier<@NotNull ItemStack> initial) {
        return syncObject(initial, ByteBufAdapters.ITEM_STACK);
    }

    @NotNull
    default ItemStack syncItemStack(@NotNull ItemStack initial) {
        return syncItemStack(() -> initial);
    }

    @Nullable
    default FluidStack syncFluidStack(@NotNull Supplier<@Nullable FluidStack> initial) {
        return syncObject(initial, ByteBufAdapters.FLUID_STACK);
    }

    @Nullable
    default FluidStack syncFluidStack(@Nullable FluidStack initial) {
        return syncFluidStack(() -> initial);
    }

    @Nullable
    default Fluid syncFluid(@NotNull Supplier<@Nullable Fluid> initial) {
        return syncObject(initial, GTByteBufAdapters.FLUID);
    }

    @Nullable
    default Fluid syncFluid(@Nullable Fluid initial) {
        return syncFluid(() -> initial);
    }
}
