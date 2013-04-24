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
        System.out.println("Hello from the tweak injector");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (!"net.minecraft.client.Minecraft".equals(name)) {
            return bytes;
        }

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        MethodNode mainMethod = null;
        for (MethodNode m : classNode.methods) {
            if ("main".equals(m.name)) {
                mainMethod = m;
                break;
            }
        }
        if (mainMethod == null) {
            // WTF? We got no main method
            return bytes;
        }

        FieldNode workDirCache = null;
        for (FieldNode n : classNode.fields) {
            String fileTypeDescriptor = Type.getDescriptor(File.class);
            if (fileTypeDescriptor.equals(n.desc) && (n.access & ACC_STATIC) != 0) {
                System.out.printf("Found static field %s of type File\n", n.name);
                workDirCache = n;
                break;
            }
        }
        MethodNode mn = new MethodNode();
        Label l66 = new Label();
        mn.visitLabel(l66);
        mn.visitLineNumber(666, l66);
        mn.visitVarInsn(ALOAD, 12);
        mn.visitMethodInsn(INVOKESTATIC, "net/minecraft/launchwrapper/injector/VanillaTweakInjector", "inject", "(Ljava/lang/Object;)Ljava/io/File;");
        mn.visitFieldInsn(PUTSTATIC, "net/minecraft/client/Minecraft", workDirCache.name, "Ljava/io/File;");

        ListIterator<AbstractInsnNode> iterator = mainMethod.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode insn = iterator.next();
            if (insn.getOpcode() == INVOKEVIRTUAL) {
                MethodInsnNode mins = (MethodInsnNode) insn;
                if (mins.owner.equals("java/awt/Frame") && mins.name.equals("validate")) {
                    System.out.printf("methodinsn : %s %s %s\n", mins.owner, mins.name, mins.desc);
                    mainMethod.instructions.insert(insn, mn.instructions);
                    System.out.println("Injected extra call into method");
                    break;
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static File inject(Object minecraftfake) {

        System.out.println("Injector called in the middle of main with object " + minecraftfake);
        try {
            Display.setIcon(new ByteBuffer[]{
                    loadIcon(new File(VanillaTweaker.workDir, "assets/icons/icon_16x16.png")),
                    loadIcon(new File(VanillaTweaker.workDir, "assets/icons/icon_32x32.png"))
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
