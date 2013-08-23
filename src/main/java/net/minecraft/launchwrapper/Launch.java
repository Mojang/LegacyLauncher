package net.minecraft.launchwrapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Launch {
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;
    public static File assetsDir;
    public static Map<String,Object> blackboard;
    
    public static void main(String[] args) {
        new Launch().launch(args);
    }

    public static LaunchClassLoader classLoader;

    private Launch() {
        final URLClassLoader ucl = (URLClassLoader) getClass().getClassLoader();
        classLoader = new LaunchClassLoader(ucl.getURLs());
        blackboard = new HashMap<String,Object>();
    }

    private void launch(String[] args) {
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        final OptionSpec<String> profileOption = parser.accepts("version", "The version we launched with").withRequiredArg();
        final OptionSpec<File> gameDirOption = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().ofType(File.class);
        final OptionSpec<File> assetsDirOption = parser.accepts("assetsDir", "Assets directory").withRequiredArg().ofType(File.class);
        final OptionSpec<String> tweakClassOption = parser.accepts("tweakClass", "Tweak class(es) to load").withRequiredArg().defaultsTo(DEFAULT_TWEAK);
        final OptionSpec<String> nonOption = parser.nonOptions();

        final OptionSet options = parser.parse(args);
        minecraftHome = options.valueOf(gameDirOption);
        assetsDir = options.valueOf(assetsDirOption);
        final String profileName = options.valueOf(profileOption);
        final List<String> tweakClassNames = new ArrayList<String>(options.valuesOf(tweakClassOption));

        final List<String> argumentList = new ArrayList<String>();
        
        blackboard.put("TweakClasses", tweakClassNames);
        blackboard.put("ArgumentList", argumentList);

        final List<ITweaker> allTweakers = new ArrayList<ITweaker>();
        try {
            final List<ITweaker> tweakers = new ArrayList(tweakClassNames.size() + 1);
            blackboard.put("Tweaks", tweakers);
            ITweaker primaryTweaker = null;
            do {
                for (final Iterator<String> it = tweakClassNames.iterator(); it.hasNext(); ) {
                    final String tweakName = it.next();
                    LogWrapper.log(Level.INFO, "Loading tweak class name %s", tweakName);
        
                    // Ensure we allow the tweak class to load with the parent classloader
                    classLoader.addClassLoaderExclusion(tweakName.substring(0,tweakName.lastIndexOf('.')));
                    final ITweaker tweaker = (ITweaker) Class.forName(tweakName, true, classLoader).newInstance();
                    tweakers.add(tweaker);
                    it.remove();
                    if (primaryTweaker == null) {
                        LogWrapper.log(Level.INFO, "Using primary tweak class name %s", tweakName);
                        primaryTweaker = tweaker;
                    }
                }
                for (final Iterator<ITweaker> it = tweakers.iterator(); it.hasNext(); ) {
                    final ITweaker tweaker = it.next();
                    LogWrapper.log(Level.INFO, "Calling tweak class %s", tweaker.getClass().getName());
                    tweaker.acceptOptions(options.valuesOf(nonOption), minecraftHome, assetsDir, profileName);
                    tweaker.injectIntoClassLoader(classLoader);
                    allTweakers.add(tweaker);
                    it.remove();
                }
            } while (!tweakClassNames.isEmpty());

            for (final ITweaker tweaker : allTweakers) {
                argumentList.addAll(Arrays.asList(tweaker.getLaunchArguments()));
            }
            
            final String launchTarget = primaryTweaker.getLaunchTarget();
            final Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            final Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});

            LogWrapper.info("Launching wrapped minecraft {%s}", launchTarget);
            mainMethod.invoke(null, (Object) argumentList.toArray(new String[argumentList.size()]));
        } catch (Exception e) {
            LogWrapper.log(Level.SEVERE, e, "Unable to launch");
        }
    }
}
