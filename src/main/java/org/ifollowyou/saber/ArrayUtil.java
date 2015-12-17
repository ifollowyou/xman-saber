package org.ifollowyou.saber;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class ArrayUtil {
    public static Set array2Set(Object[] arr) {
        Set set = new TreeSet();
        for (Object obj : arr) {
            set.add(obj);
        }

        return set;
    }

    public static <T> List<T> array2List(T[] arr) {
        ArrayList<T> list = new ArrayList<T>();
        for (T t : arr) {
            list.add(t);
        }

        return list;
    }

    public static String set2StringByDeli(Set set, char deli) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : set) {
            sb.append(deli + "" + obj);
        }

        return sb.substring(1);
    }

    public static <T> List<T> toList(T[] arr) {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }

        return list;
    }

    public static boolean contains(String str, String[] arr) {
        for (String ele : arr) {
            if (str.contains(ele)) return true;
        }

        return false;
    }


}