package org.ifollowyou.saber;


import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public final class Objects {

    /**
     * 检查目标对象是否为null。
     *
     * @param object 待检查对象
     * @return 空（true），非空（false）
     */
    public static boolean isNull(Object object) {
        boolean isNull = true;
        Optional<Object> optional = Optional.ofNullable(object);
        if (optional.isPresent()) {
            isNull = false;
        }

        return isNull;
    }

    /**
     * 检查目标对象是否非空。
     *
     * @param object 待检查对象
     * @return 非空（true），空（false）
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * 检查字符串是否为空，或者长度为0。
     *
     * @param object 待检查对象
     * @return 空（true），非空（false）
     */
    public static boolean isEmpty(String object) {
        return isNull(object) || object.trim().length() == 0;
    }

    /**
     * 检查字符串是否非空，或者长度大于0。
     *
     * @param object 待检查对象
     * @return 非空（true），空（false）
     */
    public static boolean isNotEmpty(String object) {
        return !isEmpty(object);
    }

    /**
     * 两个同类型对象之间的数据拷贝，可排除不拷贝的属性。
     *
     * @param from     源对象。
     * @param to       待更新对象。
     * @param excludes 排除更新的属性。
     */
    public static void clone(Object from, Object to, String[] excludes) {

        Set<String> outSet = new HashSet<>(Arrays.asList(excludes));

        Class<?> clazz = from.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        String methodName;
        String propertyName;

        for (Method getMethod : methods) {
            methodName = getMethod.getName();
            if (!methodName.startsWith("get")) {
                // 仅查找get方法，减少嵌套深度
                continue;
            }
            try {
                methodName = "s" + methodName.substring(1);

                Class<?> parameterType = getMethod.getReturnType();
                Object value = getMethod.invoke(from);

                propertyName = capitalize(methodName.substring(3));
                if (!outSet.contains(propertyName)) {
                    clazz.getDeclaredMethod(methodName, parameterType).invoke(to, value);
                }
            } catch (Exception e) {
                log.error("Failed to clone property: " + methodName, e);
            }
        }
    }

    /**
     * 首字母大写，其它字符不会改变。
     *
     * @param str 目标串
     * @return 首字母大写字符串
     */
    public static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            // 已经大写
            return str;
        }

        return new StringBuilder(strLen)
                .append(Character.toTitleCase(firstChar))
                .append(str.substring(1))
                .toString();
    }

    public static Map sortMapByValue(Map<Comparable, Comparable> unsortedMap) {
        Map<Comparable, Comparable> sortedMap = new TreeMap<>(new ValueComparator<>(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    public static Map sortMapByKey(Map<Comparable, Comparable> unsortedMap) {
        Map<Comparable, Comparable> sortedMap = new TreeMap<>();
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    private static class ValueComparator<K, V extends Comparable> implements Comparator<K> {
        private Map<K, V> map;

        public ValueComparator(Map<K, V> map) {
            this.map = map;
        }

        @SuppressWarnings("unchecked")
        public int compare(K keyA, K keyB) {
            V valueA = map.get(keyA);
            V valueB = map.get(keyB);

            System.out.println(valueA + " - " + valueB);

            return valueB.compareTo(valueA);
        }
    }
}
