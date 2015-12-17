package org.ifollowyou.saber.model;

import java.util.jar.JarEntry;

public interface JarFileFilter {
    boolean accept(JarEntry entry);
}