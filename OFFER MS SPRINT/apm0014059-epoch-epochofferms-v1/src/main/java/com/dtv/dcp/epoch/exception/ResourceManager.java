package com.dtv.dcp.epoch.exception;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceManager {
  private static final String BAD_ARGUMENTS = "EELF9997E Invalid arguments to format resource id [%s]!\n";
  
  private static final String BAD_RESOURCE = "EELF9998E Resource id [%s] cannot be formatted - no resource with that id exists!\n";
  
  private static final String CALLED_FROM = "Request for resource was made from:\n";
  
  private static List<String> descriptionBaseNames = new ArrayList<>();
  
  private static Map<String, Map<String, ResourceBundle>> descriptionBundles = new HashMap<>();
  
  private static List<String> messageBaseNames = new ArrayList<>();
  
  private static Map<String, Map<String, ResourceBundle>> messageBundles = new HashMap<>();
  
  private static final String NO_BUNDLE = "EELF9999E Resource id [%s] cannot be formatted - no bundle loaded!\n";
  
  private static List<String> resolutionBaseNames = new ArrayList<>();
  
  private static Map<String, Map<String, ResourceBundle>> resolutionBundles = new HashMap<>();
  
  private static String delimiReqularExp = "\\|";
  
  private enum RESOURCE_TYPES {
    HTTPCODE, CODE, MSG, DESC, RESOLUTION;
  }
  
  private static Logger log = LoggerFactory.getLogger(ResourceManager.class);
  
  private static String format(Locale locale, String identifier, String resourceId, Throwable exception, String... arguments) {
    String skeleton = getMessage(locale, resourceId);
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append(MessageFormat.format(skeleton, (Object[])arguments));
    if (exception != null) {
      sBuilder.append(String.format("EELF9997E Invalid arguments to format resource id [%s]!\n", new Object[] { resourceId }));
      sBuilder.append(
          String.format("%nException %s: %s", new Object[] { exception.getClass().getSimpleName(), exception.getMessage() }));
      StackTraceElement[] stack = exception.getStackTrace();
      sBuilder.append("\n" + formatPartialStackTrace(stack, 0, 5));
    } 
    return sBuilder.toString().trim();
  }
  
  private static void formatCause(Throwable t, StringBuilder sBuilder) {
    if (t != null) {
      sBuilder.append("Caused by: " + t.getClass().getName() + ": " + t.getMessage() + "\n");
      formatStackFrames(t, sBuilder);
      Throwable cause = t.getCause();
      formatCause(cause, sBuilder);
    } 
  }
  
  private static void formatStackFrames(Throwable t, StringBuilder sBuilder) {
    StackTraceElement[] stack = t.getStackTrace();
    sBuilder.append(formatPartialStackTrace(stack, 0, 12));
  }
  
  private static String getResourceTemplateFromBundle(String resourceId, Map<String, ResourceBundle> bundles, RESOURCE_TYPES type) {
    String output = null;
    if (bundles.isEmpty()) {
      StackTraceElement[] arrayOfStackTraceElement = Thread.currentThread().getStackTrace();
      return String.format("EELF9999E Resource id [%s] cannot be formatted - no bundle loaded!\n", new Object[] { resourceId }) + "Request for resource was made from:\n" + String.format("EELF9999E Resource id [%s] cannot be formatted - no bundle loaded!\n", new Object[] { resourceId });
    } 
    for (Map.Entry<String, ResourceBundle> entry : bundles.entrySet()) {
      try {
        output = setResourceTypes(((ResourceBundle)entry.getValue()).getString(resourceId), type);
        return output;
      } catch (MissingResourceException e) {
        log.trace(String.format("%s%nUnable to locate resource %s in bundle %s for type %s", new Object[] { e
                .getLocalizedMessage(), resourceId, entry.toString(), type }));
      } 
    } 
    log.warn("Unable to locate resource with resourceId: {} in bundles: {}", resourceId, bundles);
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    return String.format("EELF9998E Resource id [%s] cannot be formatted - no resource with that id exists!\n", new Object[] { resourceId }) + "Request for resource was made from:\n" + String.format("EELF9998E Resource id [%s] cannot be formatted - no resource with that id exists!\n", new Object[] { resourceId });
  }
  
  private static String setResourceTypes(String str, RESOURCE_TYPES type) {
    String output = null;
    if (str != null) {
      String[] values = str.split(delimiReqularExp);
      switch (type) {
        case HTTPCODE:
          output = setValue(values, 1, 0);
          return output;
        case CODE:
          output = setValue(values, 1, 1);
          return output;
        case MSG:
          output = setValue(values, 2, 2);
          return output;
        case RESOLUTION:
          output = setValue(values, 3, 3);
          return output;
        case DESC:
          output = setValue(values, 4, 4);
          return output;
      } 
      output = str;
    } 
    return output;
  }
  
  private static String setValue(String[] values, int chkLen, int index) {
    return (values.length >= chkLen) ? values[index] : null;
  }
  
  public static String asList(String... values) {
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append("[");
    for (String value : values) {
      sBuilder.append(value);
      sBuilder.append(",");
    } 
    if (sBuilder.length() > 1)
      sBuilder.delete(sBuilder.length() - 1, sBuilder.length()); 
    sBuilder.append("]");
    return sBuilder.toString();
  }
  
  public static String format(Locale locale, ResolvableErrorEnum resourceId, String... arguments) {
    return format(locale, getIdentifier(resourceId), resourceId.toString(), null, arguments);
  }
  
  public static String format(Locale locale, ResolvableErrorEnum resourceId, Throwable exception, String... arguments) {
    return format(locale, getIdentifier(resourceId), resourceId.toString(), exception, arguments);
  }
  
  public static String format(ResolvableErrorEnum resourceId, String... arguments) {
    Locale locale = Locale.getDefault();
    return format(locale, getIdentifier(resourceId), resourceId.toString(), null, arguments);
  }
  
  public static String format(ResolvableErrorEnum resourceId, Throwable exception, String... arguments) {
    Locale locale = Locale.getDefault();
    return format(locale, resourceId, exception, arguments);
  }
  
  public static String format(Throwable t) {
    StringBuilder sBuilder = new StringBuilder();
    Thread currentThread = Thread.currentThread();
    if (t != null) {
      sBuilder.append("Exception in thread " + currentThread.getName() + " " + t.getClass().getName() + ": " + t
          .getMessage() + "%n");
      formatStackFrames(t, sBuilder);
      Throwable cause = t.getCause();
      formatCause(cause, sBuilder);
    } 
    return sBuilder.toString();
  }
  
  public static String format(Throwable t, String contextMsg) {
    StringBuilder sBuilder = new StringBuilder();
    Thread currentThread = Thread.currentThread();
    if (t != null) {
      sBuilder.append("Exception in thread " + currentThread.getName() + " " + t.getClass().getName() + ": " + t
          .getMessage() + " [contextMsg:" + contextMsg + "]");
      formatStackFrames(t, sBuilder);
      Throwable cause = t.getCause();
      formatCause(cause, sBuilder);
    } 
    return sBuilder.toString();
  }
  
  private static String formatPartialStackTrace(StackTraceElement[] trace, int start, int limit) {
    StringBuilder sBuilder = new StringBuilder();
    if (trace != null && trace.length > start) {
      for (int i = start; i < trace.length && i <= limit; i++) {
        StackTraceElement frame = trace[i];
        sBuilder.append("    at " + frame.getClassName() + "." + frame.getMethodName() + "(" + frame
            .getFileName() + ":" + frame.getLineNumber() + ")%n");
      } 
      if (limit < trace.length)
        sBuilder.append("    ... " + Integer.toString(trace.length - limit) + " more frames%n"); 
    } 
    return sBuilder.toString();
  }
  
  public static String getDescription(Locale locale, ResolvableErrorEnum resourceId) {
    Map<String, ResourceBundle> localBundles = hasBundles(descriptionBundles, locale) ? getMessageBundle(locale, descriptionBundles, descriptionBaseNames) : getMessageBundle(locale, descriptionBaseNames);
    return getResourceTemplateFromBundle(resourceId.toString(), localBundles, RESOURCE_TYPES.DESC);
  }
  
  public static String getDescription(ResolvableErrorEnum resourceId) {
    return getDescription(Locale.getDefault(), resourceId);
  }
  
  public static String getMessage(Locale locale, String resourceId) {
    Map<String, ResourceBundle> localBundles = hasBundles(messageBundles, locale) ? getMessageBundle(locale, messageBundles, messageBaseNames) : getMessageBundle(locale, messageBaseNames);
    return getResourceTemplateFromBundle(resourceId, localBundles, RESOURCE_TYPES.MSG);
  }
  
  public static String getMessage(String resourceId) {
    return getMessage(Locale.getDefault(), resourceId);
  }
  
  public static String getMessage(ResolvableErrorEnum resourceId) {
    return getMessage(Locale.getDefault(), resourceId.toString());
  }
  
  public static String getMessage(ResolvableErrorEnum resourceId, String... args) {
    return format(Locale.getDefault(), resourceId, args);
  }
  
  private static Map<String, ResourceBundle> getMessageBundle(Locale locale, Map<String, Map<String, ResourceBundle>> bundles, List<String> baseNames) {
    synchronized (messageBundles) {
      List<String> failed = new ArrayList<>();
      Map<String, ResourceBundle> bundleMap = bundles.get(locale.toLanguageTag());
      if (!bundleMap.keySet().containsAll(baseNames)) {
        HashSet<String> differences = new HashSet<>(baseNames);
        differences.removeAll(bundleMap.keySet());
        for (String baseName : differences) {
          if (!loadResourceBundle(bundleMap, baseName, locale))
            failed.add(baseName); 
        } 
      } 
      if (!failed.isEmpty())
        for (String baseName : failed)
          messageBaseNames.remove(baseName);  
      return bundleMap;
    } 
  }
  
  private static Map<String, ResourceBundle> getMessageBundle(Locale locale, List<String> baseNames) {
    synchronized (messageBundles) {
      List<String> failed = new ArrayList<>();
      Map<String, ResourceBundle> bundleMap = new HashMap<>();
      if (null == locale)
        return bundleMap; 
      String languageTag = locale.toLanguageTag();
      messageBundles.put(languageTag, bundleMap);
      for (String baseName : baseNames) {
        if (!loadResourceBundle(bundleMap, baseName, locale))
          failed.add(baseName); 
      } 
      if (!failed.isEmpty())
        for (String baseName : failed)
          messageBaseNames.remove(baseName);  
      return bundleMap;
    } 
  }
  
  private static boolean hasBundles(Map<String, Map<String, ResourceBundle>> bundles, Locale locale) {
    if (null == locale)
      return false; 
    Map<String, ResourceBundle> bundleMap = bundles.get(locale.toLanguageTag());
    return !(bundleMap == null);
  }
  
  public static String getResolution(Locale locale, ResolvableErrorEnum resource) {
    Map<String, ResourceBundle> localBundles = hasBundles(resolutionBundles, locale) ? getMessageBundle(locale, resolutionBundles, resolutionBaseNames) : getMessageBundle(locale, resolutionBaseNames);
    return getResourceTemplateFromBundle(resource.toString(), localBundles, RESOURCE_TYPES.RESOLUTION);
  }
  
  public static String getResolution(ResolvableErrorEnum resource) {
    return getResolution(Locale.getDefault(), resource);
  }
  
  public static void loadDescriptionBundle(String baseName) {
    if (!descriptionBaseNames.contains(baseName))
      descriptionBaseNames.add(baseName); 
  }
  
  public static void loadMessageBundle(String baseName) {
    if (!messageBaseNames.contains(baseName))
      messageBaseNames.add(baseName); 
    loadDescriptionBundle(baseName);
    loadResolutionBundle(baseName);
  }
  
  public static void loadResolutionBundle(String baseName) {
    if (!resolutionBaseNames.contains(baseName))
      resolutionBaseNames.add(baseName); 
  }
  
  private static boolean loadResourceBundle(Map<String, ResourceBundle> bundleMap, String baseName, Locale locale) {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
      bundleMap.put(baseName, bundle);
      return true;
    } catch (MissingResourceException e) {
      log.warn(String.format("Unable to load resource bundle %s for locale %s", new Object[] { baseName, locale
              .toLanguageTag() }));
      return false;
    } 
  }
  
  public static String getIdentifier(ResolvableErrorEnum resourceId) {
    return getIdentifier(Locale.getDefault(), resourceId);
  }
  
  public static String getIdentifier(Locale locale, ResolvableErrorEnum resourceId) {
    Map<String, ResourceBundle> localBundles = hasBundles(messageBundles, locale) ? getMessageBundle(locale, messageBundles, messageBaseNames) : getMessageBundle(locale, messageBaseNames);
    return getResourceTemplateFromBundle(resourceId.toString(), localBundles, RESOURCE_TYPES.CODE);
  }
  
  public static int getHttpCode(ResolvableErrorEnum resourceId) {
    return getHttpCode(Locale.getDefault(), resourceId);
  }
  
  public static int getHttpCode(Locale locale, ResolvableErrorEnum resourceId) {
    Map<String, ResourceBundle> localBundles = hasBundles(messageBundles, locale) ? getMessageBundle(locale, messageBundles, messageBaseNames) : getMessageBundle(locale, messageBaseNames);
    if (null == locale)
      return 500; 
    return 
      Integer.parseInt(getResourceTemplateFromBundle(resourceId.toString(), localBundles, RESOURCE_TYPES.HTTPCODE));
  }
  
  public static String formatMessage(ResolvableErrorEnum resourceId, Throwable cause, String message, String... args) {
    int httpCode = getHttpCode(resourceId);
    String errorId = getIdentifier(resourceId);
    String errMsg = getMessage(resourceId, args);
    Map<String, String> msgMap = new HashMap<>();
    msgMap.put("resourceId", resourceId.toString());
    msgMap.put("resourceMsg", errMsg);
    msgMap.put("userMessage", message);
    msgMap.put("errorId", errorId);
    msgMap.put("httpCode", (new Integer(httpCode)).toString());
    if (cause != null)
      msgMap.put("exceptionMsg", cause.getMessage()); 
    return msgMap.toString();
  }
}
