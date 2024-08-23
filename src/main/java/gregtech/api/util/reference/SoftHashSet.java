package gregtech.api.util.reference;

import java.lang.ref.SoftReference;
import java.util.Objects;

public class SoftHashSet<T> extends ExpiringReferenceHashSet<T> {

    @Override
    protected ExpiringReference<T> wrapObject(T obj) {
        return new Reference(obj);
    }

    protected class Reference extends SoftReference<T> implements ExpiringReference<T> {

        public Reference(T referent) {
            super(referent);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Reference that;
            try {
                // noinspection unchecked
                that = (Reference) obj;
            } catch (ClassCastException e) {
                return false;
            }

            T contained = this.get();
            T other = that.get();
            if (this.expired() || that.expired()) return false;
            return Objects.equals(contained, other);
        }

        @Override
        public int hashCode() {
            T contained = this.get();
            if (contained == null) {
                return 0;
            }
            return contained.hashCode();
        }
    }
}
