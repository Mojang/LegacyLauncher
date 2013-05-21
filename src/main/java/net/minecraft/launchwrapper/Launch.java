package net.minecraft.launchwrapper;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.logging.Level;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class Launch {
    static String minecraftHome;
    public static JsonRootNode profile;

    public static void main(String[] args) {
        new Launch().launch(args);
    }

    private LaunchClassLoader classLoader;

    private Launch() {
        URLClassLoader ucl = (URLClassLoader) getClass().getClassLoader();
        classLoader = new LaunchClassLoader(ucl.getURLs());
    }

    private void launch(String[] args) {
        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<String> profileArg = parser.accepts("profile", "The profile we launched with").withRequiredArg().ofType(String.class).defaultsTo("versions/1.5.1-test/1.5.1-test.json");
        ArgumentAcceptingOptionSpec<File> workDirArg = parser.accepts("workDir", "Alternative work directory").withRequiredArg().ofType(File.class);
        OptionSet options = parser.parse(args);
        File workDir = options.valueOf(workDirArg);
        String profileName = options.valueOf(profileArg);
        File profile = new File(workDir, profileName);
        String tweakClassName = "net.minecraft.launchwrapper.VanillaTweaker";
        // read profile here for extra data
        if (profile.exists())
        {
            try
            {
                JsonRootNode rootNode = new JdomParser().parse(new FileReader(profile));
                Launch.profile = rootNode;
                System.out.println(rootNode);
                tweakClassName = rootNode.isStringValue("launchwrapper", "tweakclass") ? rootNode.getStringValue("launchwrapper", "tweakclass") : tweakClassName;
            }
            catch (Exception e)
            {
                // NOOP - probably missing our bit
            }
        }
        try {
            LogWrapper.log(Level.INFO, "Using tweak class name %s",tweakClassName);
            ITweaker tweaker = (ITweaker)Class.forName(tweakClassName,true,classLoader).newInstance();
            tweaker.acceptOptions(options.nonOptionArguments(), workDir, profileName);
            tweaker.injectIntoClassLoader(classLoader);
            Class<?> clazz = Class.forName(tweaker.getLaunchTarget(), false, classLoader);
            Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});
            LogWrapper.info("Launching wrapped minecraft");
            mainMethod.invoke(null, (Object) tweaker.getLaunchArguments());
        } catch (Exception e) {
            LogWrapper.log(Level.SEVERE, e, "Unable to launch");
        }
    }

}
