package net.minecraft.launchwrapper;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Level;

import net.minecraft.launchwrapper.injector.ClassicVanillaTweakInjector;

public class ClassicVanillaTweaker implements ITweaker {

	private static final String TWEAK_INJECTOR_CLASS = ClassicVanillaTweakInjector.class.getName();
	private List<String> arguments;

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		arguments = args;
		ClassicVanillaTweakInjector.minecraftPath = gameDir;
		// Checking if we have enabled survival mode and announcing it.
		ClassicVanillaTweakInjector.enableSurvivalPatch = args.contains("--survival");
		
		if (ClassicVanillaTweakInjector.enableSurvivalPatch)
			LogWrapper.log(Level.INFO, "Will enable surival patch.");
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		classLoader.registerTransformer(TWEAK_INJECTOR_CLASS);

	}

	@Override
	public String getLaunchTarget() {
		return TWEAK_INJECTOR_CLASS;
	}

	@Override
	public String[] getLaunchArguments() {
		return arguments.toArray(new String[arguments.size()]);
	}

}
