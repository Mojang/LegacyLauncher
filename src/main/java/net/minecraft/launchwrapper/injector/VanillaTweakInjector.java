package net.minecraft.launchwrapper.injector;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.VanillaTweaker;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

public class VanillaTweakInjector implements IClassTransformer {

    private static String workDirFieldName;

    public VanillaTweakInjector() {
    }

    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (!"net.minecraft.client.Minecraft".equals(name)) {
            return bytes;
        }

        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        MethodNode mainMethod = null;
        for (final MethodNode methodNode : classNode.methods) {
            if ("main".equals(methodNode.name)) {
                mainMethod = methodNode;
                break;
            }
        }
        if (mainMethod == null) {
            // WTF? We got no main method
            return bytes;
        }

        FieldNode workDirCache = null;
        for (final FieldNode fieldNode : classNode.fields) {
            final String fileTypeDescriptor = Type.getDescriptor(File.class);
            if (fileTypeDescriptor.equals(fieldNode.desc) && (fieldNode.access & ACC_STATIC) == ACC_STATIC) {
                workDirCache = fieldNode;
                break;
            }
        }

        // Prepere our injection code
        final MethodNode methodNode = new MethodNode();
        final Label label = new Label();
        methodNode.visitLabel(label);
        methodNode.visitLineNumber(9001, label); // Linenumber which shows up in the stacktrace
        // Call the method below
        methodNode.visitMethodInsn(INVOKESTATIC, "net/minecraft/launchwrapper/injector/VanillaTweakInjector", "inject", "()Ljava/io/File;");
        // Store the result in the workDir variable.
        methodNode.visitFieldInsn(PUTSTATIC, "net/minecraft/client/Minecraft", workDirCache.name, "Ljava/io/File;");

        // Find the injection point and insert our code
        final ListIterator<AbstractInsnNode> iterator = mainMethod.instructions.iterator();
        while (iterator.hasNext()) {
            final AbstractInsnNode insn = iterator.next();
            if (insn.getOpcode() == INVOKEVIRTUAL) {
                final MethodInsnNode mins = (MethodInsnNode) insn;
                if (mins.owner.equals("java/awt/Frame") && mins.name.equals("validate")) {
                    mainMethod.instructions.insert(insn, methodNode.instructions);
                    break;
                }
            }
        }

        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static File inject() {
        // Speed up imageloading
        ImageIO.setUseCache(false);

        // Load icon from disk
        try {
            Display.setIcon(new ByteBuffer[]{
                    loadIcon(new File(VanillaTweaker.workDir, "assets/icons/icon_16x16.png")),
                    loadIcon(new File(VanillaTweaker.workDir, "assets/icons/icon_32x32.png"))
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set the workdir, return value will get assigned
        return VanillaTweaker.workDir;
    }

    private static ByteBuffer loadIcon(final File iconFile) throws IOException {
        final BufferedImage icon = ImageIO.read(iconFile);

        final int[] rgb = icon.getRGB(0, 0, icon.getWidth(), icon.getHeight(), null, 0, icon.getWidth());

        final ByteBuffer buffer = ByteBuffer.allocate(4 * rgb.length);
        for (int color : rgb) {
            buffer.putInt(color << 8 | ((color >> 24) & 0xFF));
        }
        buffer.flip();
        return buffer;
    }
}
