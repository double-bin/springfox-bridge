package com.github.doublebin.springfox.bridge.core.component.tuple;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tuple3<A, B, C> implements Serializable {
    private static final long serialVersionUID = 4251682107552677117L;

    private A fst;
    private B snd;
    private C trd;

    public static <A, B, C> Tuple3<A, B, C> build(A a, B b, C c) {
        return new Tuple3<A, B, C>(a, b, c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Tuple3)) { return false; }

        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>)o;

        if (fst != null ? !fst.equals(tuple3.fst) : tuple3.fst != null) { return false; }
        if (snd != null ? !snd.equals(tuple3.snd) : tuple3.snd != null) { return false; }
        return trd != null ? trd.equals(tuple3.trd) : tuple3.trd == null;

    }

    @Override
    public int hashCode() {
        int result = fst != null ? fst.hashCode() : 0;
        result = 31 * result + (snd != null ? snd.hashCode() : 0);
        result = 31 * result + (trd != null ? trd.hashCode() : 0);
        return result;
    }
}
