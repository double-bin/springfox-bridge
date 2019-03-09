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
public class Tuple2<A, B> implements Serializable {
    private static final long serialVersionUID = 4251682107552677117L;

    private A fst;
    private B snd;

    public static <A, B> Tuple2<A, B> build(A a, B b) {
        return new Tuple2<A, B>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Tuple2)) { return false; }

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>)o;

        if (fst != null ? !fst.equals(tuple2.fst) : tuple2.fst != null) { return false; }
        return snd != null ? snd.equals(tuple2.snd) : tuple2.snd == null;

    }

    @Override
    public int hashCode() {
        int result = fst != null ? fst.hashCode() : 0;
        result = 31 * result + (snd != null ? snd.hashCode() : 0);
        return result;
    }
}
