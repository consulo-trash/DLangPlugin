package org.dlangplugin.library;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.ui.Util;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.AttachRootButtonDescriptor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.libraries.ui.OrderRootTypePresentation;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.DefaultLibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.dlangplugin.DLangIcons;
import org.dlangplugin.DLangBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class DLangLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {
    @Override
    public OrderRootTypePresentation getRootTypePresentation(@NotNull OrderRootType type) {
        if(type.equals(LibFileRootType.getInstance())) {
            return new OrderRootTypePresentation("Lib File", DLangIcons.LIBRARY);
        }
        else {
            return DefaultLibraryRootsComponentDescriptor.getDefaultPresentation(type);
        }
    }

    @NotNull
    @Override
    public List<? extends RootDetector> getRootDetectors() {
        return Arrays.asList(
                new DLangLibRootDetector(OrderRootType.CLASSES, DLangBundle.message("sources.root.detector.sources.name")),
                new DLangLibRootDetector(LibFileRootType.getInstance(), DLangBundle.message("sources.root.detector.lib.name"))
        );
    }

    @NotNull
    @Override
    public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
        return Arrays.asList(new AttachUrlJavadocDescriptor());
    }

    private static class AttachUrlJavadocDescriptor extends AttachRootButtonDescriptor {
        private AttachUrlJavadocDescriptor() {
            super(JavadocOrderRootType.getInstance(), ProjectBundle.message("module.libraries.javadoc.url.button"));
        }

        @Override
        public VirtualFile[] selectFiles(@NotNull JComponent parent,
                                         @Nullable VirtualFile initialSelection,
                                         @Nullable Module contextModule,
                                         @NotNull LibraryEditor libraryEditor) {
            final VirtualFile vFile = Util.showSpecifyJavadocUrlDialog(parent);
            if (vFile != null) {
                return new VirtualFile[]{vFile};
            }
            return VirtualFile.EMPTY_ARRAY;
        }
    }
}
