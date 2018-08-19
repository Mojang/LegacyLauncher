package net.minecraft.launchwrapper;

import java.io.File;
import java.util.List;

/**
 * A tweaker that changes parts of Minecraft.
 * 
 * @author Erik Broes
 *
 */
public interface ITweaker {
	/**
	 * Provides the tweaker with options that may influence the behavior.
	 * 
	 * @param args The arguments that were passed, excluding the gameDir, assetsDir, profile, and tweakClass arguments.
	 * @param gameDir The game directory that Minecraft is being launched in.
	 * @param assetsDir The directory that contains Minecraft's assets.
	 * @param profile The version that Minecraft is being launched in.
	 */
    void acceptOptions(List<String> args, File gameDir, final File assetsDir, String profile);
    
    /**
     * Performs injection into the class loader. Transformers are registered with {@link LaunchClassLoader#registerTransformer}.
     * 
     * @param classLoader The class loader where transformers should be injected. The tweak class was not loaded with this class loader.
     */
    void injectIntoClassLoader(LaunchClassLoader classLoader);
    
    /**
     * Gets the main class that should be launched. This method is only invoked on the primary tweaker (the tweaker that appears first).
     * 
     * @return The main class.
     */
    String getLaunchTarget();
    
    /**
     * Gets a list of arguments. This list will be combined with arguments from other tweakers to form an uber list. Please note that the original launch args are not added automatically.
     * 
     * @return A list of arguments to add.
     */
    String[] getLaunchArguments();
}
