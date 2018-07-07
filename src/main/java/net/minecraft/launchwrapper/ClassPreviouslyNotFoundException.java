package net.minecraft.launchwrapper;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ClassPreviouslyNotFoundException extends ClassNotFoundException {
    private final ClassNotFoundException previousException;

    public ClassPreviouslyNotFoundException(String name, ClassNotFoundException previousException) {
        super(name);
        this.previousException = previousException;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        s.println("Cached class load exception: " + this);
        previousException.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        s.println("Cached class load exception: " + this);
        previousException.printStackTrace(s);
    }
}
