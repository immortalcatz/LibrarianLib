package com.teamwizardry.librarianlib.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

// Boilerplate code taken with love from Vazkii's Quark mod
// Quark is distrubted at https://github.com/Vazkii/Quark

public class LibLibTransformer implements IClassTransformer, Opcodes {

    private static final String ASM_HOOKS = "com/teamwizardry/librarianlib/asm/LibLibAsmHooks";

    private static final Map<String, Transformer> transformers = new HashMap<>();

    public static final ClassnameMap CLASS_MAPPINGS = new ClassnameMap(
            "net/minecraft/item/ItemStack", "afj",
            "net/minecraft/client/renderer/block/model/IBakedModel", "cbh",
            "net/minecraft/client/renderer/RenderItem", "bve",
            "net/minecraft/client/renderer/entity/layers/LayerArmorBase", "bww",
            "net/minecraft/entity/EntityLivingBase", "sw",
            "net/minecraft/client/renderer/entity/RenderLivingBase", "bvl",
            "net/minecraft/client/model/ModelBase", "blv",
            "net/minecraft/client/renderer/BlockRendererDispatcher", "bqy",
            "net/minecraft/client/renderer/BlockModelRenderer", "bra",
            "net/minecraft/block/state/IBlockState", "atj",
            "net/minecraft/util/math/BlockPos", "co",
            "net/minecraft/world/IBlockAccess", "aju",
            "net/minecraft/client/renderer/VertexBuffer", "bpw",
            "net/minecraft/client/particle/Particle", "bos",
            "net/minecraft/client/particle/ParticleSpell", "bpb"
    );


    static {
        transformers.put("net.minecraft.client.renderer.RenderItem", LibLibTransformer::transformRenderItem);
        transformers.put("net.minecraft.client.renderer.entity.layers.LayerArmorBase", LibLibTransformer::transformLayerArmorBase);
        transformers.put("net.minecraft.client.renderer.BlockRendererDispatcher", LibLibTransformer::transformBlockRenderDispatcher);
        transformers.put("net.minecraft.client.particle.Particle", LibLibTransformer::transformParticle);
    }

    private static byte[] transformRenderItem(byte[] basicClass) {
        MethodSignature sig1 = new MethodSignature("renderItem", "func_180454_a", "a",
                "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V");

        MethodSignature sig2 = new MethodSignature("renderEffect", "func_180451_a", "a",
                "(Lnet/minecraft/client/renderer/block/model/IBakedModel;)V");

        MethodSignature target = new MethodSignature("renderModel", "func_175045_a", "a",
                "(Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/item/ItemStack;)V");

        byte[] transformedClass = transform(basicClass, sig1, "Item render hook",
                combine((AbstractInsnNode node) -> (node.getOpcode() == INVOKESPECIAL || node.getOpcode() == INVOKEVIRTUAL) // Filter
                        && target.matches((MethodInsnNode) node),
                (MethodNode method, AbstractInsnNode node) -> { // Action
                    InsnList newInstructions = new InsnList();

                    newInstructions.add(new VarInsnNode(ALOAD, 1));
                    newInstructions.add(new VarInsnNode(ALOAD, 2));
                    newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "renderHook",
                            "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", false));

                    method.instructions.insert(node, newInstructions);
                    return true;
                }));

        transformedClass = transform(transformedClass, sig2, "Enchantment glint glow activation", (MethodNode method) -> { // Action
            InsnList instructions = method.instructions;

            InsnList newInstructions = new InsnList();
            newInstructions.add(new FieldInsnNode(GETSTATIC, ASM_HOOKS, "INSTANCE", "L" + ASM_HOOKS + ";"));
            newInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, ASM_HOOKS, "maximizeGlowLightmap", "()V", false));

            instructions.insertBefore(instructions.getFirst(), newInstructions);
            return true;
        });

        return transform(transformedClass, sig2, "Enchantment glint glow return", combine((AbstractInsnNode node) -> node.getOpcode() == RETURN,
                (MethodNode method, AbstractInsnNode node) -> {
                    InsnList newInstructions = new InsnList();
                    newInstructions.add(new FieldInsnNode(GETSTATIC, ASM_HOOKS, "INSTANCE", "L" + ASM_HOOKS + ";"));
                    newInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, ASM_HOOKS, "returnGlowLightmap", "()V", false));
                    method.instructions.insertBefore(node, newInstructions);
                    return false;
                }));
    }

    private static byte[] transformLayerArmorBase(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("renderEnchantedGlint", "func_188364_a", "a",
                "(Lnet/minecraft/client/renderer/entity/RenderLivingBase;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/model/ModelBase;FFFFFFF)V");

        byte[] transformedClass = transform(basicClass, sig, "Enchantment glint glow activation", (MethodNode method) -> { // Action
                InsnList instructions = method.instructions;

                InsnList newInstructions = new InsnList();
                newInstructions.add(new FieldInsnNode(GETSTATIC, ASM_HOOKS, "INSTANCE", "L" + ASM_HOOKS + ";"));
                newInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, ASM_HOOKS, "maximizeGlowLightmap", "()V", false));

                instructions.insertBefore(instructions.getFirst(), newInstructions);
                return true;
            });

        return transform(transformedClass, sig, "Enchantment glint glow return", combine((AbstractInsnNode node) -> node.getOpcode() == RETURN,
                (MethodNode method, AbstractInsnNode node) -> {
                    InsnList newInstructions = new InsnList();
                    newInstructions.add(new FieldInsnNode(GETSTATIC, ASM_HOOKS, "INSTANCE", "L" + ASM_HOOKS + ";"));
                    newInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, ASM_HOOKS, "returnGlowLightmap", "()V", false));
                    method.instructions.insertBefore(node, newInstructions);
                    return false;
                }));
    }

    private static byte[] transformBlockRenderDispatcher(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("renderBlock", "func_175018_a", "a",
                "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/VertexBuffer;)Z");

        MethodSignature target1 = new MethodSignature("renderModel", "func_178267_a", "a",
                "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/VertexBuffer;Z)Z");
        MethodSignature target2 = new MethodSignature("renderFluid", "func_178270_a", "a",
                "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/VertexBuffer;)Z");

        byte[] transformedClass = transform(basicClass, sig, "Block render hook",
                combine((AbstractInsnNode node) -> node.getOpcode() == INVOKEVIRTUAL &&
                        target1.matches((MethodInsnNode) node), // Filter
                (MethodNode method, AbstractInsnNode node) -> { // Action
                    InsnList newInstructions = new InsnList();

                    newInstructions.add(new VarInsnNode(ALOAD, 0));
                    newInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/BlockRendererDispatcher",
                            "blockModelRenderer", "Lnet/minecraft/client/renderer/BlockModelRenderer;"));
                    // BlockModelRenderer

                    newInstructions.add(new VarInsnNode(ALOAD, 3));
                    newInstructions.add(new VarInsnNode(ALOAD, 6));
                    newInstructions.add(new VarInsnNode(ALOAD, 1));
                    newInstructions.add(new VarInsnNode(ALOAD, 2));
                    newInstructions.add(new VarInsnNode(ALOAD, 4));
                    // BlockModelRenderer, IBlockAccess, IBakedModel, IBlockState, BlockPos, VertexBuffer

                    newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "renderHook",
                            "(Lnet/minecraft/client/renderer/BlockModelRenderer;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/VertexBuffer;)V", false));

                    method.instructions.insert(node, newInstructions);

                    return true;
                }));

        return transform(transformedClass, sig, "Fluid render hook",
                combine((AbstractInsnNode node) -> node.getOpcode() == INVOKEVIRTUAL &&
                        target2.matches((MethodInsnNode) node), // Filter
                (MethodNode method, AbstractInsnNode node) -> { // Action
                    InsnList newInstructions = new InsnList();

                    newInstructions.add(new VarInsnNode(ALOAD, 0));
                    newInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/BlockRendererDispatcher",
                            "blockModelRenderer", "Lnet/minecraft/client/renderer/BlockModelRenderer;"));
                    // BlockModelRenderer

                    newInstructions.add(new VarInsnNode(ALOAD, 3));
                    newInstructions.add(new VarInsnNode(ALOAD, 1));
                    newInstructions.add(new VarInsnNode(ALOAD, 2));
                    newInstructions.add(new VarInsnNode(ALOAD, 4));
                    // BlockModelRenderer, IBlockAccess, IBlockState, BlockPos, VertexBuffer

                    newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "renderHook",
                            "(Lnet/minecraft/client/renderer/BlockModelRenderer;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/VertexBuffer;)V", false));

                    method.instructions.insert(node, newInstructions);

                    return true;
                }));
    }

    private static byte[] transformParticle(byte[] basicClass) {
        MethodSignature sig = new MethodSignature("getBrightnessForRender", "func_189214_a", "a",
                "(F)I");

        return transform(basicClass, sig, "Potion particle glow", (MethodNode method) -> { // Action
            InsnList instructions = method.instructions;

            InsnList newInstructions = new InsnList();

            LabelNode node = new LabelNode();

            newInstructions.add(new VarInsnNode(ALOAD, 0));
            newInstructions.add(new TypeInsnNode(INSTANCEOF, "net/minecraft/client/particle/ParticleSpell"));
            newInstructions.add(new FieldInsnNode(GETSTATIC, ASM_HOOKS, "INSTANCE", "L" + ASM_HOOKS + ";"));
            newInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, ASM_HOOKS, "usePotionGlow", "()Z", false));
            newInstructions.add(new InsnNode(IAND));
            newInstructions.add(new JumpInsnNode(IFEQ, node));

            newInstructions.add(new LdcInsnNode(0xf000f0));
            newInstructions.add(new InsnNode(IRETURN));
            newInstructions.add(node);

            instructions.insertBefore(instructions.getFirst(), newInstructions);
            instructions.resetLabels();
            return true;
        });
    }


    // BOILERPLATE =====================================================================================================

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformers.containsKey(transformedName)) {
            String[] arr = transformedName.split("\\.");
            log("Transforming " + arr[arr.length - 1]);
            return transformers.get(transformedName).apply(basicClass);
        }

        return basicClass;
    }

    public static byte[] transform(byte[] basicClass, MethodSignature sig, String simpleDesc, MethodAction action) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        log("Applying Transformation to method (" + sig + ")");
        log("Attempting to insert: " + simpleDesc);
        boolean didAnything = findMethodAndTransform(node, sig, action);

        if (didAnything) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }


    public static boolean findMethodAndTransform(ClassNode node, MethodSignature sig, MethodAction pred) {
        for (MethodNode method : node.methods) {
            if (sig.matches(method)) {

                boolean finish = pred.test(method);
                log("Patch result: " + (finish ? "Success" : "!!!!!!! Failure !!!!!!!"));

                return finish;
            }
        }

        log("Patch result: !!!!!!! Couldn't locate method! !!!!!!!");

        return false;
    }

    public static MethodAction combine(NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNode(node, filter, action);
    }

    public static boolean applyOnNode(MethodNode method, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        Iterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes);

        boolean didAny = false;
        while (iterator.hasNext()) {
            AbstractInsnNode anode = iterator.next();
            if (filter.test(anode)) {
                didAny = true;
                if (action.test(method, anode))
                    break;
            }
        }

        return didAny;
    }

    public static MethodAction combineByLast(NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeByLast(node, filter, action);
    }

    public static boolean applyOnNodeByLast(MethodNode method, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes, method.instructions.size());

        boolean didAny = false;
        while (iterator.hasPrevious()) {
            AbstractInsnNode anode = iterator.previous();
            if (filter.test(anode)) {
                didAny = true;
                if (action.test(method, anode))
                    break;
            }
        }

        return didAny;
    }

    public static MethodAction combineFrontPivot(NodeFilter pivot, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeFrontPivot(node, pivot, filter, action);
    }

    public static boolean applyOnNodeFrontPivot(MethodNode method, NodeFilter pivot, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes);

        int pos = 0;

        boolean didAny = false;
        while (iterator.hasNext()) {
            pos++;
            AbstractInsnNode pivotTest = iterator.next();
            if (pivot.test(pivotTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasPrevious()) {
                    AbstractInsnNode anode = internal.previous();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static MethodAction combineBackPivot(NodeFilter pivot, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeBackPivot(node, pivot, filter, action);
    }

    public static boolean applyOnNodeBackPivot(MethodNode method, NodeFilter pivot, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes, method.instructions.size());

        int pos = method.instructions.size();

        boolean didAny = false;
        while (iterator.hasPrevious()) {
            pos--;
            AbstractInsnNode pivotTest = iterator.previous();
            if (pivot.test(pivotTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasNext()) {
                    AbstractInsnNode anode = internal.next();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static MethodAction combineFrontFocus(NodeFilter focus, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeFrontFocus(node, focus, filter, action);
    }

    public static boolean applyOnNodeFrontFocus(MethodNode method, NodeFilter focus, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes);

        int pos = method.instructions.size();

        boolean didAny = false;
        while (iterator.hasNext()) {
            pos++;
            AbstractInsnNode focusTest = iterator.next();
            if (focus.test(focusTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasNext()) {
                    AbstractInsnNode anode = internal.next();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static MethodAction combineBackFocus(NodeFilter focus, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeBackFocus(node, focus, filter, action);
    }

    public static boolean applyOnNodeBackFocus(MethodNode method, NodeFilter focus, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes, method.instructions.size());

        int pos = method.instructions.size();

        boolean didAny = false;
        while (iterator.hasPrevious()) {
            pos--;
            AbstractInsnNode focusTest = iterator.previous();
            if (focus.test(focusTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasPrevious()) {
                    AbstractInsnNode anode = internal.previous();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static void log(String str) {
        FMLLog.info("[LibrarianLib ASM] %s", str);
    }

    public static void prettyPrint(MethodNode node) {
        Printer printer = new Textifier();

        TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
        node.accept(visitor);

        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();

        log(sw.toString());
    }

    public static void prettyPrint(AbstractInsnNode node) {
        Printer printer = new Textifier();

        TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
        node.accept(visitor);

        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();

        log(sw.toString());
    }

    private static class InsnArrayIterator implements ListIterator<AbstractInsnNode> {

        private int index;
        private final AbstractInsnNode[] array;

        public InsnArrayIterator(AbstractInsnNode[] array) {
            this(array, 0);
        }

        public InsnArrayIterator(AbstractInsnNode[] array, int index) {
            this.array = array;
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return array.length > index + 1 && index >= 0;
        }

        @Override
        public AbstractInsnNode next() {
            if (hasNext())
                return array[++index];
            return null;
        }

        @Override
        public boolean hasPrevious() {
            return index > 0 && index <= array.length;
        }

        @Override
        public AbstractInsnNode previous() {
            if (hasPrevious())
                return array[--index];
            return null;
        }

        @Override
        public int nextIndex() {
            return hasNext() ? index + 1 : array.length;
        }

        @Override
        public int previousIndex() {
            return hasPrevious() ? index - 1 : 0;
        }

        @Override
        public void remove() {
            throw new Error("Unimplemented");
        }

        @Override
        public void set(AbstractInsnNode e) {
            throw new Error("Unimplemented");
        }

        @Override
        public void add(AbstractInsnNode e) {
            throw new Error("Unimplemented");
        }
    }

    public static class MethodSignature {
        private final String funcName, srgName, obfName, funcDesc, obfDesc;

        public MethodSignature(String funcName, String srgName, String obfName, String funcDesc) {
            this.funcName = funcName;
            this.srgName = srgName;
            this.obfName = obfName;
            this.funcDesc = funcDesc;
            this.obfDesc = obfuscate(funcDesc);
        }

        @Override
        public String toString() {
            return "Names [" + funcName + ", " + srgName + ", " + obfName + "] Descriptor " + funcDesc + " / " + obfDesc;
        }

        private static String obfuscate(String desc) {
            for (String s : CLASS_MAPPINGS.keySet())
                if (desc.contains(s))
                    desc = desc.replaceAll(s, CLASS_MAPPINGS.get(s));

            return desc;
        }

        public boolean matches(String methodName, String methodDesc) {
            return (methodName.equals(funcName) || methodName.equals(obfName) || methodName.equals(srgName))
                    && (methodDesc.equals(funcDesc) || methodDesc.equals(obfDesc));
        }

        public boolean matches(MethodNode method) {
            return matches(method.name, method.desc);
        }

        public boolean matches(MethodInsnNode method) {
            return matches(method.name, method.desc);
        }

    }

    // Basic interface aliases to not have to clutter up the code with generics over and over again

    public interface Transformer extends Function<byte[], byte[]> {
        // NO-OP
    }

    public interface MethodAction extends Predicate<MethodNode> {
        // NO-OP
    }

    public interface NodeFilter extends Predicate<AbstractInsnNode> {
        // NO-OP
    }

    public interface NodeAction extends BiPredicate<MethodNode, AbstractInsnNode> {
        // NO-OP
    }
}
