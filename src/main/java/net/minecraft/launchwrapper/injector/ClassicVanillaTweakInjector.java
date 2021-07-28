package net.minecraft.launchwrapper.injector;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LogWrapper;

// Minecraft Classic Vanilla Tweak Injector
// - Adding support for server and port options.
// - Adding support for mppass option ( uuid )
// - Making window not resizable
// - Adding a tweak to enable survival mode.
// - Making game don't freeze on close.
// - Fixing world saving and loading.
// - Fixing resources loading.
// - Adding support for custom resolution.

// Implementation
// You need to add ${auth_uuid} as third argument.
// Add --survival for survival mode.
// Add --server 127.0.0.1:25565 (example) for connecting to a server.

public class ClassicVanillaTweakInjector implements IClassTransformer {
	public static boolean enableSurvivalPatch;
	public static File minecraftPath;
	private static final String ONLINE_WORLD_SAVING_DISABLED = "Online world saving disabled.";

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(basicClass);
		reader.accept(classNode, ClassReader.EXPAND_FRAMES);

		// Allow game to play sounds
		if ("com.mojang.minecraft.c".equals(name)) {
			LogWrapper.log(Level.INFO, "Found Minecraft resources downloader class.");

			MethodNode aMethod = null;
			for (MethodNode method : classNode.methods) {
				if ("run".equals(method.name)) {
					aMethod = method;
					break;
				}
			}

			if (aMethod != null) {
				// Ensuring that it's really the resources downloader class and not another
				// class with the same name.
				if (aMethod.instructions.size() >= 2 && aMethod.instructions.get(1).getOpcode() == Opcodes.NEW) {
					TypeInsnNode toTest = (TypeInsnNode) aMethod.instructions.get(1);
					if ("java/util/ArrayList".equals(toTest.desc)) {
						LogWrapper.log(Level.INFO, "Found run method.");
						InsnList instructions = new InsnList();
						instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/minecraft/c", "c",
								"Lcom/mojang/minecraft/l;"));
						instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
								"net/minecraft/launchwrapper/injector/ClassicVanillaTweakInjector", "downloadResources",
								"(Ljava/lang/Object;)V"));
						instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						instructions.add(new InsnNode(Opcodes.ICONST_1));
						instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/mojang/minecraft/c", "a", "Z"));
						instructions.add(new InsnNode(Opcodes.RETURN));
						aMethod.instructions.insert(instructions);
					} else {
						LogWrapper.log(Level.WARN,
								"Found class run method but not having same behavior as Minecraft resources downloader class (bad content). Patch won't be applied.");
					}
				} else {
					LogWrapper.log(Level.WARN,
							"Found class run method but not having same behavior as Minecraft resources downloader class (bad opcode). Patch won't be applied.");
				}
			} else {
				LogWrapper.log(Level.WARN, "Failed to find class run method. Patch won't be applied.");
			}

		}

		// Allow save and load of worlds.
		if ("com.mojang.minecraft.e.q".equals(name)) {
			LogWrapper.log(Level.INFO, "Found Minecraft world loader class.");
			MethodNode runmethod = null;
			for (MethodNode method : classNode.methods) {
				if ("run".equals(method.name)) {
					runmethod = method;
					break;
				}
			}
			if (runmethod != null) {
				// Ensuring that it's really the world loader class and not another class with
				// the same name.
				if (runmethod.instructions.size() >= 16 && runmethod.instructions.get(15).getOpcode() == Opcodes.LDC) {
					LdcInsnNode insnNode = (LdcInsnNode) runmethod.instructions.get(15);
					if ("Getting level list..".equals(insnNode.cst)) {
						LogWrapper.log(Level.INFO, "Found run method.");
						InsnList instructionsNew = new InsnList();

						// Setting string q to ""
						instructionsNew.add(new VarInsnNode(Opcodes.ALOAD, 0));
						instructionsNew.add(new LdcInsnNode(ONLINE_WORLD_SAVING_DISABLED));
						instructionsNew.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/mojang/minecraft/e/q", "q",
								"Ljava/lang/String;"));

						// Setting string array p to an empty array.
						instructionsNew.add(new VarInsnNode(Opcodes.ALOAD, 0));

						instructionsNew.add(new InsnNode(Opcodes.ICONST_5));
						instructionsNew.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"));

						instructionsNew.add(new InsnNode(Opcodes.DUP));
						instructionsNew.add(new InsnNode(Opcodes.ICONST_0));
						instructionsNew.add(new LdcInsnNode("-"));
						instructionsNew.add(new InsnNode(Opcodes.AASTORE));

						instructionsNew.add(new InsnNode(Opcodes.DUP));
						instructionsNew.add(new InsnNode(Opcodes.ICONST_1));
						instructionsNew.add(new LdcInsnNode("-"));
						instructionsNew.add(new InsnNode(Opcodes.AASTORE));

						instructionsNew.add(new InsnNode(Opcodes.DUP));
						instructionsNew.add(new InsnNode(Opcodes.ICONST_2));
						instructionsNew.add(new LdcInsnNode("-"));
						instructionsNew.add(new InsnNode(Opcodes.AASTORE));

						instructionsNew.add(new InsnNode(Opcodes.DUP));
						instructionsNew.add(new InsnNode(Opcodes.ICONST_3));
						instructionsNew.add(new LdcInsnNode("-"));
						instructionsNew.add(new InsnNode(Opcodes.AASTORE));

						instructionsNew.add(new InsnNode(Opcodes.DUP));
						instructionsNew.add(new InsnNode(Opcodes.ICONST_4));
						instructionsNew.add(new LdcInsnNode("-"));
						instructionsNew.add(new InsnNode(Opcodes.AASTORE));

						instructionsNew.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/mojang/minecraft/e/q", "p",
								"[Ljava/lang/String;"));
						instructionsNew.add(new VarInsnNode(Opcodes.ALOAD, 0));
						instructionsNew.add(new VarInsnNode(Opcodes.ALOAD, 0));
						instructionsNew.add(new FieldInsnNode(Opcodes.GETFIELD, "com/mojang/minecraft/e/q", "p",
								"[Ljava/lang/String;"));
						instructionsNew.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/mojang/minecraft/e/q", "a",
								"([Ljava/lang/String;)V"));

						// Setting boolean variable o to true
						instructionsNew.add(new VarInsnNode(Opcodes.ALOAD, 0));
						instructionsNew.add(new InsnNode(Opcodes.ICONST_1));
						instructionsNew.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/mojang/minecraft/e/q", "o", "Z"));

						// Return
						instructionsNew.add(new InsnNode(Opcodes.RETURN));
						runmethod.instructions.insert(instructionsNew);
					} else {
						LogWrapper.log(Level.WARN,
								"Found class run method but not having same behavior as Minecraft world loader class (bad content). Patch won't be applied.");
					}
				} else {
					LogWrapper.log(Level.WARN,
							"Found class run method but not having same behavior as Minecraft world loader class (bad opcode). Patch won't be applied.");
				}
			} else {
				LogWrapper.log(Level.WARN, "Failed to find class run method. Patch won't be applied.");
			}

		}

		if (enableSurvivalPatch) {

			// In this class we need to recreate the missing constructor.
			if ("com.mojang.minecraft.d.c".equals(name)) {
				LogWrapper.log(Level.INFO, "Found Minecraft gamemode class to patch.");
				MethodNode constructor = new MethodNode();
				constructor.name = "<init>";
				constructor.desc = "(Lcom/mojang/minecraft/l;)V";
				constructor.access = Opcodes.ACC_PUBLIC;
				constructor.exceptions = new ArrayList<String>();
				constructor.visitVarInsn(Opcodes.ALOAD, 0);
				constructor.visitVarInsn(Opcodes.ALOAD, 1);
				constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/mojang/minecraft/d/b", "<init>",
						"(Lcom/mojang/minecraft/l;)V");
				constructor.visitVarInsn(Opcodes.ALOAD, 0);
				constructor.visitInsn(Opcodes.ICONST_M1);
				constructor.visitFieldInsn(Opcodes.PUTFIELD, "com/mojang/minecraft/d/c", "c", "I");
				constructor.visitVarInsn(Opcodes.ALOAD, 0);
				constructor.visitInsn(Opcodes.ICONST_M1);
				constructor.visitFieldInsn(Opcodes.PUTFIELD, "com/mojang/minecraft/d/c", "d", "I");
				constructor.visitVarInsn(Opcodes.ALOAD, 0);
				constructor.visitInsn(Opcodes.ICONST_M1);
				constructor.visitFieldInsn(Opcodes.PUTFIELD, "com/mojang/minecraft/d/c", "e", "I");
				constructor.visitVarInsn(Opcodes.ALOAD, 0);
				constructor.visitInsn(Opcodes.ICONST_0);
				constructor.visitFieldInsn(Opcodes.PUTFIELD, "com/mojang/minecraft/d/c", "f", "I");
				constructor.visitVarInsn(Opcodes.ALOAD, 0);
				constructor.visitInsn(Opcodes.ICONST_0);
				constructor.visitFieldInsn(Opcodes.PUTFIELD, "com/mojang/minecraft/d/c", "g", "I");
				constructor.visitVarInsn(Opcodes.ALOAD, 0);
				constructor.visitInsn(Opcodes.ICONST_0);
				constructor.visitFieldInsn(Opcodes.PUTFIELD, "com/mojang/minecraft/d/c", "h", "I");
				constructor.visitInsn(Opcodes.RETURN);

				classNode.methods.add(constructor);
			}

			// In this class, we need to patch the constructor to use the survival gamemode
			// instead of the creative gamemode.
			// com.mojang.minecraft.d.c is for the survival gamemode
			// com.mojang.minecraft.d.a is for the creative gamemode
			if ("com.mojang.minecraft.l".equals(name)) {
				LogWrapper.log(Level.INFO, "Found Minecraft main class.");
				MethodNode constructor = null;
				for (MethodNode method : classNode.methods) {
					if ("<init>".equals(method.name)) {
						constructor = method;
						break;
					}
				}
				if (constructor != null) {
					LogWrapper.log(Level.INFO, "Found constructor.");
					for (int iterator = 0; iterator < constructor.instructions.size(); iterator++) {
						AbstractInsnNode instruction = constructor.instructions.get(iterator);
						AbstractInsnNode replacedInstruction = instruction;
						if (instruction.getOpcode() == Opcodes.ANEWARRAY) {
							System.out.println(instruction.getClass().getName());
						}

						if (instruction.getOpcode() == Opcodes.NEW) {
							TypeInsnNode typeNode = (TypeInsnNode) instruction;
							TypeInsnNode replaceTypeNode = (TypeInsnNode) replacedInstruction;
							if ("com/mojang/minecraft/d/a".equals(typeNode.desc)) {
								LogWrapper.log(Level.INFO, "Found instance creation instruction (new).");
								replaceTypeNode.desc = "com/mojang/minecraft/d/c";
							}
						}
						if (instruction.getOpcode() == Opcodes.INVOKESPECIAL) {
							MethodInsnNode methodNode = (MethodInsnNode) instruction;
							MethodInsnNode replaceMethodNode = (MethodInsnNode) replacedInstruction;
							if ("<init>".equals(methodNode.name)
									&& "com/mojang/minecraft/d/a".equals(methodNode.owner)) {
								LogWrapper.log(Level.INFO, "Found instance creation instruction (invokespecial).");
								replaceMethodNode.owner = "com/mojang/minecraft/d/c";
							}
						}
						if (instruction != replacedInstruction) {
							constructor.instructions.set(instruction, replacedInstruction);
							LogWrapper.log(Level.INFO, "Instruction replaced.");
						}
					}

				} else {
					LogWrapper.log(Level.WARN, "Failed to find class constructor. Patch won't be applied.");
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public static void main(String[] args) {

		// Trying to get custom server option.
		String host = null;
		String port = null;

		// Trying to get custom resolution
		int resolutionX = 854;
		int resolutionY = 480;
		for (int i = 0; i < args.length; i++) {
			if ("--server".equals(args[i])) {
				if (args.length >= i + 2) {
					host = args[i + 1].split(":")[0];
					port = args[i + 1].split(":")[1];
				}
			}
			if ("--width".equals(args[i])) {
				if (args.length >= i + 2) {
					resolutionX = Integer.parseInt(args[i + 1]);
				}
			}
			if ("--height".equals(args[i])) {
				if (args.length >= i + 2) {
					resolutionY = Integer.parseInt(args[i + 1]);
				}
			}
		}

		if (resolutionX != 854 || resolutionY != 480) {
			LogWrapper.log(Level.INFO, "Custom resolution %dx%d", resolutionX, resolutionY);
		}

		// Obtaining Minecraft Applet Class and creating an instance.
		Class<?> minecraftClass = obtainMinecraftClass();
		if (minecraftClass == null) {
			LogWrapper.log(Level.FATAL, "Failed to load Minecraft Applet class.");
			return;
		}
		Applet minecraftApplet;
		try {
			minecraftApplet = (Applet) minecraftClass.newInstance();
		} catch (Exception e) {
			LogWrapper.log(Level.FATAL, "Failed to create Minecraft Applet instance.");
			return;
		}

		MinecraftAppletStub appletStub = new MinecraftAppletStub();

		// Setting various settings.
		LogWrapper.log(Level.INFO, "Setting username: %s", args[0]);
		appletStub.parameters.put("username", args[0]);
		appletStub.parameters.put("sessionid", args[1]);
		appletStub.parameters.put("haspaid", "true");
		LogWrapper.log(Level.INFO, "Setting UUID: %s", args[2]);
		appletStub.parameters.put("mppass", args[2]);

		if (host != null && port != null) {
			LogWrapper.log(Level.INFO, "Will connect to host %s and port %s", host, port);
			appletStub.parameters.put("server", host);
			appletStub.parameters.put("port", port);
		}

		minecraftApplet.setStub(appletStub);

		// Creating window and starting applet
		JFrame appletWrapper = new JFrame("Minecraft");
		appletWrapper.setSize(resolutionX, resolutionY);
		appletWrapper.setResizable(false);
		appletWrapper.setLocationRelativeTo(null);
		appletWrapper.addWindowListener(new WrapperWindowListener(minecraftApplet, appletWrapper));
		appletWrapper.add(minecraftApplet);
		appletWrapper.setVisible(true);
		VanillaTweakInjector.loadIconsOnFrames();
		minecraftApplet.setSize(appletWrapper.getSize());
		appletWrapper.revalidate();
		minecraftApplet.init();
		minecraftApplet.start();

	}

	private static class MinecraftAppletStub implements AppletStub {

		private static final String MINECRAFT_BASE_URL = "https://minecraft.net/game";
		public Map<String, String> parameters;

		public MinecraftAppletStub() {
			parameters = new HashMap<>();
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public URL getDocumentBase() {
			try {
				return new URL(MINECRAFT_BASE_URL);
			} catch (MalformedURLException ignored) {

			}
			return null;
		}

		@Override
		public URL getCodeBase() {
			try {
				return new URL(MINECRAFT_BASE_URL);
			} catch (MalformedURLException ignored) {

			}
			return null;
		}

		@Override
		public String getParameter(String name) {
			if (parameters.containsKey(name))
				return parameters.get(name);
			return null;
		}

		@Override
		public AppletContext getAppletContext() {
			return null;
		}

		@Override
		public void appletResize(int width, int height) {

		}

	}

	private static class WrapperWindowListener implements WindowListener {

		private Applet applet;
		private JFrame wrapper;

		public WrapperWindowListener(Applet applet, JFrame wrapper) {
			this.applet = applet;
			this.wrapper = wrapper;
		}

		@Override
		public void windowOpened(WindowEvent e) {

		}

		@Override
		public void windowClosing(WindowEvent e) {
			if (applet != null) {
				applet.stop();
				applet.destroy();
			}
			wrapper.dispose();
			System.exit(0);

		}

		@Override
		public void windowClosed(WindowEvent e) {

		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {

		}

		@Override
		public void windowActivated(WindowEvent e) {

		}

		@Override
		public void windowDeactivated(WindowEvent e) {

		}

	}

	public static void downloadResources(Object o) {
		LogWrapper.log(Level.INFO, "Game downloading resources ...");
		try {
			Field sField = Launch.classLoader.findClass("com.mojang.minecraft.l").getDeclaredField("s");
			Object sObject = sField.get(o);
			Method soundLoadMethod = Launch.classLoader.findClass("com.mojang.minecraft.c.k").getDeclaredMethod("a",
					File.class, String.class);
			Method musicLoadMethod = Launch.classLoader.findClass("com.mojang.minecraft.c.k").getDeclaredMethod("a",
					String.class, File.class);
			File resourcesDirectory = new File(minecraftPath, "resources");
			if (resourcesDirectory.exists() & resourcesDirectory.isDirectory()) {
				Path path = Paths.get(resourcesDirectory.toURI());
				Files.walkFileTree(path, new FileVisitor<Path>() {

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.toFile().getName().endsWith(".ogg")) {
							if ("music".equals(file.toFile().getParentFile().getName())) {
								Path relativizeToMusic = new File(minecraftPath, "resources/music").toPath()
										.relativize(file);
								String toRegister = relativizeToMusic.toString().replace("\\", "/");

								try {
									musicLoadMethod.invoke(sObject, toRegister, file.toFile());
								} catch (Exception e) {
									LogWrapper.log(Level.WARN, "Failed to load resource %s.", file.toString());
									e.printStackTrace();
								}
							} else if (file.toFile().getAbsolutePath().replace("\\", "/").contains("/sound/")) {
								Path relativizeToSound = new File(minecraftPath, "resources/sound/").toPath()
										.relativize(file);
								String toRegister = relativizeToSound.toString().replace("\\", "/");
								try {
									soundLoadMethod.invoke(sObject, file.toFile(), toRegister);
								} catch (Exception e) {
									LogWrapper.log(Level.WARN, "Failed to load resource %s.", file.toString());
									e.printStackTrace();
								}
							}
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});
			}

			LogWrapper.log(Level.INFO, "Resources loading done.");
		} catch (Exception e) {
			LogWrapper.log(Level.WARN, "Failed to patch resources.");
		}
	}

	private static Class<?> obtainMinecraftClass() {
		try {
			return Launch.classLoader.findClass("com.mojang.minecraft.MinecraftApplet");
		} catch (Exception ignored) {
		}
		return null;
	}

}
