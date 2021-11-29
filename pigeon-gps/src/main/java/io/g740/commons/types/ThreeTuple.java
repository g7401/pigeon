package io.g740.commons.types;

/**
 * @author bbottong
 */
public class ThreeTuple<F, S, T> {
    public F f;
    public S s;
    public T t;

    public ThreeTuple(F f, S s, T t) {
        this.f = f;
        this.s = s;
        this.t = t;
    }
}
