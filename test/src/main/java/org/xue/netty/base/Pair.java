package org.xue.netty.base;

public class Pair<T1, T2> {
    private final T1 obj1;
    private final T2 obj2;

    public Pair(T1 obj1, T2 obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public T1 getObj1() {
        return obj1;
    }

    public T2 getObj2() {
        return obj2;
    }
}
