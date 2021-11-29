package io.g740.commons.utils;

import java.util.List;

public class CollectionUtils {

    public static String toString(List<? extends Object> objects, String delimiter) {
        StringBuilder sb = new StringBuilder();

        if (objects == null) {
            return null;
        }

        objects.forEach(object -> {
            sb.append(object);

            sb.append(delimiter);
        });

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
