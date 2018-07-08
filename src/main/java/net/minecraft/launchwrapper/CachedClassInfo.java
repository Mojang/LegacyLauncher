package net.minecraft.launchwrapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachedClassInfo {
    public long modsHash;
    public ConcurrentMap<String, String> transformedClassNames = new ConcurrentHashMap<>();
    public ConcurrentMap<String, String> untransformedClassNames = new ConcurrentHashMap<>();
    public ConcurrentMap<Long, Long> transformedClassHashes = new ConcurrentHashMap<>();
}
