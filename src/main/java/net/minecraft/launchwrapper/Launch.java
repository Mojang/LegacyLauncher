package net.minecraft.launchwrapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.logging.Level;

public class Launch {
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;

    public static void main(String[] args) {
        new Launch().launch(args);
    }

    private final LaunchClassLoader classLoader;

    private Launch() {
        final URLClassLoader ucl = (URLClassLoader) getClass().getClassLoader();
        classLoader = new LaunchClassLoader(ucl.getURLs());
    }

    private void launch(String[] args) {
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        final OptionSpec<String> profileOption = parser.accepts("version", "The version we launched with").withRequiredArg();
        final OptionSpec<File> gameDirOption = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().ofType(File.class);
        final OptionSpec<String> tweakClassOption = parser.accepts("tweakClass", "Tweak class to load").withRequiredArg().defaultsTo(DEFAULT_TWEAK);
        final OptionSpec<String> nonOption = parser.nonOptions();

        final OptionSet options = parser.parse(args);
        minecraftHome = options.valueOf(gameDirOption);
        final String profileName = options.valueOf(profileOption);
        final String tweakClassName = options.valueOf(tweakClassOption);

        try {
            LogWrapper.log(Level.INFO, "Using tweak class name %s", tweakClassName);

            final ITweaker tweaker = (ITweaker) Class.forName(tweakClassName, true, classLoader).newInstance();
            tweaker.acceptOptions(options.valuesOf(nonOption), minecraftHome, profileName);
            tweaker.injectIntoClassLoader(classLoader);

            final Class<?> clazz = Class.forName(tweaker.getLaunchTarget(), false, classLoader);
            final Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});

            LogWrapper.info("Launching wrapped minecraft");
            mainMethod.invoke(null, (Object) tweaker.getLaunchArguments());
        } catch (Exception e) {
            LogWrapper.log(Level.SEVERE, e, "Unable to launch");
        }
    }
}
