package org.javatuples;

import java.util.*;
import java.util.stream.Stream;

public class OrderedHashSet<T> {
    //private final static long serialVersionUID = -36592371275262l;

    private T t;
    private HashMap<Integer,T> map;

    public OrderedHashSet() {
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
        Stream<Map.Entry<Integer,T>> entryStream = entrySet.stream().sorted(new Comparator<Map.Entry<Integer, T>>() {
            @Override
            public int compare(Map.Entry<Integer, T> o1, Map.Entry<Integer, T> o2) {
                return Integer.compare(o1.getKey(), o2.getKey());
            }
        });
        Map.Entry<Integer,T> entries[] = (Map.Entry<Integer, T>[]) entryStream.toArray();
        return new ArrayList<T>((Collection<? extends T>) Arrays.asList(entries));
    }
}
