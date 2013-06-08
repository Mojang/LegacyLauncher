package net.minecraft.launchwrapper;

import java.io.File;
import java.util.List;

public class VanillaTweaker implements ITweaker {
    public static File workDir;
    private List<String> args;

    @Override
    public void acceptOptions(List<String> options, File workDir, String profile) {
        this.args = options;
        VanillaTweaker.workDir = workDir;
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.registerTransformer("net.minecraft.launchwrapper.injector.VanillaTweakInjector");
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.Minecraft";
    }

    @Override
    public String[] getLaunchArguments() {
        return args.toArray(new String[args.size()]);
    }
}
