package io.github.javiewer.util;

import android.os.Build;
import android.os.Bundle;

import java.io.Serializable;

public final class BundleCompat {

    private BundleCompat() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getSerializable(Bundle bundle, String key, Class<T> clazz) {
        if (bundle == null || clazz == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getSerializable(key, clazz);
        }
        Serializable value = bundle.getSerializable(key);
        if (value == null) {
            return null;
        }
        if (!clazz.isInstance(value)) {
            return null;
        }
        return (T) value;
    }
}
