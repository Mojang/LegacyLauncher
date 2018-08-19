package net.minecraft.launchwrapper;

/**
 * A transformer that transforms the names of classes. Please note that even though this interface does not extend {@link IClassTransformer}, name transformers registered to the class loader must implement it.
 * 
 * @author Erik Broes
 *
 */
public interface IClassNameTransformer {
	/**
	 * Unmaps a class name.
	 * 
	 * @param name The original name of the class.
	 * @return The unmapped name, which will become the name passed to {@link IClassTransformer#transform}.
	 */
    String unmapClassName(String name);
    
    /**
     * Remaps a class name.
     * 
     * @param name The original name of the class.
     * @return The remapped name, which will become the transformedName passed tp {@link IClassTransformer#transform}.
     */
    String remapClassName(String name);
}
