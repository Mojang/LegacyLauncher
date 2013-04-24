package net.minecraft.launchwrapper;

import java.io.File;
import java.util.List;

import joptsimple.OptionSet;

public interface ITweaker {

    void acceptOptions(List<String> list, File workDir, String profile);

    void injectIntoClassLoader(LaunchClassLoader classLoader);

    String getLaunchTarget();

    String[] getLaunchArguments();

}
