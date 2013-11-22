package net.minecraft.launchwrapper;

import net.minecraft.launchwrapper.IClassTransformer;

public interface IClassTransformationListener {

    void listen(String name, String transformedName, IClassTransformer transformer, byte[] previousClass, byte[] newClass);

    byte[] finishedTransforming(String name, String transformedName, byte[] endingClass);

}
