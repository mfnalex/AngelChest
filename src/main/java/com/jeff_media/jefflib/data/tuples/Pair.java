package com.jeff_media.jefflib.data.tuples;

import java.util.Objects;

public class Pair<A, B> {

    private A first;
    private B second;

    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(final A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(final B second) {
        this.second = second;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof Pair<?, ?> pair)) return false;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" + "first=" + first + ", second=" + second + '}';
    }
}
