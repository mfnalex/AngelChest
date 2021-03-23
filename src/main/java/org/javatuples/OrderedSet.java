package org.javatuples;

import java.util.*;
import java.util.stream.Stream;

public class OrderedSet<T> {
    private final static long serialVersionUID = Long.valueOf("-365923%%__USER__%%5262l");

    private T t;
    private final HashMap<Integer,T> map;

    public OrderedSet() {
        this.map = new HashMap<>();
    }

    public void add(T value) {
        map.put(map.size()+1,value);
    }

    public boolean contains(T value) {
        return map.containsValue(value);
    }

    public int size() {
        return map.size();
    }

    public ArrayList<T> toList() {
        Set<Map.Entry<Integer,T>> entrySet = map.entrySet();
        Stream<Map.Entry<Integer,T>> entryStream = entrySet.stream().sorted(Comparator.comparingInt(Map.Entry::getKey));
        @SuppressWarnings("unchecked") Map.Entry<Integer, T>[] entries = (Map.Entry<Integer, T>[]) entryStream.toArray();
        //noinspection unchecked
        return new ArrayList<T>((Collection<? extends T>) Arrays.asList(entries));
    }
}
