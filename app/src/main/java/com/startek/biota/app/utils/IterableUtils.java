package com.startek.biota.app.utils;

import java.util.Iterator;

/**
 * Created by skt90u on 2016/4/24.
 */
public class IterableUtils {

    public static <T> boolean equals(Iterable<T> lhs, Iterable<T> rhs)
    {
        if(lhs == rhs)return true;

        if(lhs == null) throw new IllegalArgumentException("lhs");
        if(rhs == null) throw new IllegalArgumentException("rhs");

        Iterator<T> lIterator = lhs.iterator();
        Iterator<T> rIterator = rhs.iterator();

        while (true)
        {
            boolean lHasNext = lIterator.hasNext();
            boolean rHasNext = rIterator.hasNext();

            if(!lHasNext && !rHasNext)break;

            if(lHasNext != rHasNext) return false;

            T lNext = lIterator.next();
            T rNext = rIterator.next();

            if(!IterableUtils.equals(lNext, rNext)) return false;
        }

        return true;
    }

    private static <T> boolean equals(T lhs, T rhs)
    {
        if(lhs == rhs) return true;

        return lhs == null
                ? false
                : lhs.getClass().equals(String.class)
                    ? StrUtils.equals((String)lhs, (String)rhs)
                    : lhs.equals(rhs);
    }

    private static <T> int indexOf(Iterable<T> iterable, T obj)
    {
        if(iterable == null) throw new IllegalArgumentException("iterable");

        Iterator<T> iterator = iterable.iterator();

        int i = 0;

        while (true)
        {
            boolean hasNext = iterator.hasNext();

            if(!hasNext)break;

            T lNext = iterator.next();
            T rNext = obj;

            if(IterableUtils.equals(lNext, rNext))
                return i;

            i++;
        }

        return -1;
    }
}
