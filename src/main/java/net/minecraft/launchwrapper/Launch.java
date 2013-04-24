package net.minecraft.launchwrapper;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.logging.Level;

public class Launch {
    static String minecraftHome;

    public static void main(String[] args) {
        System.out.println("HELLO FROM WRAPPER");
        new Launch().launch(args);
    }

    private LaunchClassLoader classLoader;

    private Launch() {
        URLClassLoader ucl = (URLClassLoader) getClass().getClassLoader();
        classLoader = new LaunchClassLoader(ucl.getURLs());
    }

    private void launch(String[] args) {
        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<String> profileArg = parser.accepts("profile", "The profile we launched with").withRequiredArg().ofType(String.class);
        ArgumentAcceptingOptionSpec<File> workDirArg = parser.accepts("workDir", "Alternative work directory").withRequiredArg().ofType(File.class);
        OptionSet options = parser.parse(args);
        File workDir = options.valueOf(workDirArg);
//        String profileName = options.valueOf(profileArg);
//        if (profileName == null)
//        {
//            profileName = "";
//        }
//        File profile = new File(workDir, profileName);
        // read profile here for extra data
        ITweaker tweaker = new VanillaTweaker(); // Read from the profile, please!
        tweaker.acceptOptions(options.nonOptionArguments(), workDir, "");
        tweaker.injectIntoClassLoader(classLoader);

        try {
            Class<?> clazz = Class.forName(tweaker.getLaunchTarget(), false, classLoader);
            Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});
            mainMethod.invoke(null, (Object) tweaker.getLaunchArguments());
        } catch (Exception e) {
            LogWrapper.log(Level.SEVERE, e, "Unable to launch %s", tweaker.getLaunchTarget());
        }
    }

}
