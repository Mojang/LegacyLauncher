package net.minecraft.launchwrapper;

import java.io.File;
import java.util.List;

public class VanillaTweaker implements ITweaker {
    public static File gameDir;
    public static File assetsDir;

    private List<String> args;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.args = args;
        VanillaTweaker.gameDir = gameDir;
        VanillaTweaker.assetsDir = assetsDir;
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
