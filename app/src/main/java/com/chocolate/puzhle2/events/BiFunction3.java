package com.chocolate.puzhle2.events;

/**
 * Created by mahdi on 9/26/15.
 */
public interface BiFunction3<T, T1, T2, R> {
    R run(T data, T1 data1, T2 data2);
}
