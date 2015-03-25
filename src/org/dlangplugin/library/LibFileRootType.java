package org.dlangplugin.library;

import com.intellij.openapi.roots.PersistentOrderRootType;
import org.jetbrains.annotations.NotNull;

public class LibFileRootType extends PersistentOrderRootType {
    @NotNull
    public static LibFileRootType getInstance()
    {
			return getOrderRootType(LibFileRootType.class);
    }

    protected LibFileRootType() {
        super("LIBRARY_FILE", "libFile", "lib-file", null);
    }
}
