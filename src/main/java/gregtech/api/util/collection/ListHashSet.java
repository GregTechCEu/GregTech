package gregtech.api.util.collection;

import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;

/**
 * A primitive implementation of a set and a list in parallel, with iteration order determined by the list
 * while using the set for approximate {@code O(1)} time for {@link #contains(Object)}
 */
public class ListHashSet<E> extends AbstractObjectSortedSet<E> implements List<E> {

    protected final Object2IntMap<E> indexMap;
    protected final ObjectList<E> list;

    protected static final int DEFAULT_SIZE = 16;

    public ListHashSet() {
        this(DEFAULT_SIZE);
    }

    public ListHashSet(Collection<? extends E> c) {
        this(c.size());
        addAll(c);
    }

    public ListHashSet(int size) {
        indexMap = new Object2IntOpenHashMap<>(size);
        list = new ObjectArrayList<>();
        indexMap.defaultReturnValue(-1);
    }

    protected void updateIndices(List<E> sublist, int by) {
        for (int i = 0; i < sublist.size(); i++) {
            E e = sublist.get(i);
            int index = indexMap.getInt(e);
            if (index == -1) continue;
            indexMap.put(e, index + by);
        }
    }

    @Override
    public ObjectBidirectionalIterator<E> iterator(E fromElement) {
        int index = indexOf(fromElement);
        if (index == -1) throw new IllegalArgumentException("ListHashSet must contain the specified element!");
        return list.listIterator(index);
    }

    @Override
    public ObjectListIterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public boolean addAll(final int index, @NotNull Collection<? extends E> c) {
        if (index == list.size()) {
            return addAll(c);
        }
        int shuffle = 0;
        for (E e : c) {
            if (indexMap.containsKey(e)) continue;
            indexMap.put(e, index + shuffle);
            list.add(index + shuffle, e);
            shuffle++;
        }
        updateIndices(list.subList(index + shuffle, list.size()), shuffle);
        return shuffle > 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexMap.containsKey(o);
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        E prev = list.set(index, element);
        indexMap.remove(prev);
        indexMap.put(element, index);
        return prev;
    }

    @Override
    public boolean add(E e) {
        return addSensitive(size(), e);
    }

    @Override
    public void add(int index, E element) {
        addSensitive(index, element);
    }

    /**
     * Equivalent to {@link #add(int, Object)}, but reports whether the addition was successful or not.
     * 
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @return whether the element was inserted.
     */
    public boolean addSensitive(int index, E element) {
        if (indexMap.containsKey(element)) return false;
        indexMap.put(element, index);
        list.add(index, element);
        // update if we didn't add to the end of the list
        if (index != list.size() - 1) updateIndices(list.subList(index + 1, list.size()), 1);
        return true;
    }

    @Override
    public E remove(int index) {
        E element = list.remove(index);
        indexMap.remove(element);
        return element;
    }

    @Override
    public boolean rem(Object o) {
        int index = indexOf(o);
        if (index == -1) return false;
        list.remove(index);
        indexMap.remove(o);
        return true;
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Override
    public E first() {
        return list.get(0);
    }

    @Override
    public E last() {
        return list.get(list.size() - 1);
    }

    @Override
    public Spliterator<E> spliterator() {
        return super.spliterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public int indexOf(Object o) {
        return indexMap.getInt(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        // no duplicates because set
        return indexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectSortedSet<E> subSet(E fromElement, E toElement) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectSortedSet<E> headSet(E toElement) {
        return subSet(first(), toElement);
    }

    @Override
    public ObjectSortedSet<E> tailSet(E fromElement) {
        return subSet(fromElement, null);
    }
}
