package gregtech.api.mui;

import net.minecraft.client.resources.Locale;

// todo remove in next mui2 version
public interface LocaleAccessor {

    String gregtech$getRawKey(String s);

    ThreadLocal<LocaleAccessor> accessor = new ThreadLocal<>();

    static String getRawKey(String s) {
        if (accessor.get() == null) return s;
        return accessor.get().gregtech$getRawKey(s);
    }

    static void setLocale(Locale locale) {
        accessor.set((LocaleAccessor) locale);
    }
}
