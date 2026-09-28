package com.dtv.dcp.epoch.common.httpclient.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Context {
  private static final ThreadLocal<Map<String, Set<String>>> holder = new ThreadLocal<>();
  
  Context(Map<String, Set<String>> other) {
    if (other == null)
      throw new IllegalArgumentException("Context holder cannot be null"); 
    holder.set(other);
  }
  
  static void initialize() {
    holder.set(new HashMap<>());
  }
  
  static void clear() {
    holder.remove();
  }
  
  public static void add(String key, String value) {
    if (get() == null)
      return; 
    ((Set<String>)get().computeIfAbsent(key, k -> new LinkedHashSet())).add(value);
  }
  
  public static Set<String> keySet() {
    if (get() == null)
      return null; 
    return ((Map)holder.get()).keySet();
  }
  
  public static boolean containsKey(String key) {
    if (get() == null)
      return false; 
    return get().containsKey(key);
  }
  
  public static Set<String> getValues(String key) {
    if (get() == null)
      return new HashSet<>(); 
    Set<String> values = get().getOrDefault(key, Collections.emptySet());
    return Collections.unmodifiableSet(values);
  }
  
  public static String getFirst(String key) {
    if (get() == null)
      return null; 
    Set<String> values = get().getOrDefault(key, Collections.emptySet());
    return values.isEmpty() ? null : values.stream().findFirst().get();
  }
  
  static Map<String, Set<String>> get() {
    return (holder != null) ? holder.get() : null;
  }
}
