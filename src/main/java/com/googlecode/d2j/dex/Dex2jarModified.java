//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.googlecode.d2j.dex;

import com.googlecode.d2j.converter.IR2JConverter;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class Dex2jarModified {
    private DexExceptionHandler exceptionHandler;
    private final DexFileReader reader;
    private int readerConfig;
    private int v3Config;

    public static Dex2jarModified from(byte[] in) throws IOException {
        return from(new DexFileReader(ZipUtil.readDex(in)));
    }

    public static Dex2jarModified from(ByteBuffer in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Dex2jarModified from(DexFileReader reader) {
        return new Dex2jarModified(reader);
    }

    public static Dex2jarModified from(File in) throws IOException {
        return from(Files.readAllBytes(in.toPath()));
    }

    public static Dex2jarModified from(InputStream in) throws IOException {
        return from(new DexFileReader(in));
    }

    public static Dex2jarModified from(String in) throws IOException {
        return from(new File(in));
    }

    private Dex2jarModified(DexFileReader reader) {
        this.reader = reader;
        this.readerConfig |= 1;
    }

    private void doTranslate(final Path dist) throws IOException {
        DexFileNode fileNode = new DexFileNode();

        try {
            this.reader.accept(fileNode, this.readerConfig | 32);
        } catch (Exception var4) {
            this.exceptionHandler.handleFileException(var4);
        }

        ClassVisitorFactory cvf = new ClassVisitorFactory() {
            public ClassVisitor create(final String name) {
                return new ClassVisitor(262144, new ClassWriter(1)) {
                    public void visitEnd() {
                        super.visitEnd();
                        ClassWriter cw = (ClassWriter)super.cv;

                        byte[] data;
                        try {
                            data = cw.toByteArray();
                        } catch (Exception var6) {
                            System.err.println(String.format("ASM fail to generate .class file: %s", name));
                            Dex2jarModified.this.exceptionHandler.handleFileException(var6);
                            return;
                        }

                        try {
                            Path dist1 = dist.resolve(name + ".class");
                            Path parent = dist1.getParent();
                            if (parent != null && !Files.exists(parent, new LinkOption[0])) {
                                Files.createDirectories(parent);
                            }

                            Files.write(dist1, data, new OpenOption[0]);
                        } catch (IOException var5) {
                            var5.printStackTrace(System.err);
                        }

                    }
                };
            }
        };
        (new ExDex2Asm(this.exceptionHandler) {
            public void convertCode(DexMethodNode methodNode, MethodVisitor mv) {
                if ((Dex2jarModified.this.readerConfig & 4) == 0 || !methodNode.method.getName().equals("<clinit>")) {
                    super.convertCode(methodNode, mv);
                }
            }

            public void optimize(IrMethod irMethod) {
                T_exceptionFix.transform(irMethod);
                T_endRemove.transform(irMethod);
                T_cleanLabel.transform(irMethod);
                T_ssa.transform(irMethod);
                T_removeLocal.transform(irMethod);
                T_removeConst.transform(irMethod);
                T_zero.transform(irMethod);
                if (T_npe.transformReportChanged(irMethod)) {
                    T_deadCode.transform(irMethod);
                    T_removeLocal.transform(irMethod);
                    T_removeConst.transform(irMethod);
                }

                T_new.transform(irMethod);
                T_fillArray.transform(irMethod);
                T_agg.transform(irMethod);
                T_voidInvoke.transform(irMethod);
                if ((Dex2jarModified.this.v3Config & 4) != 0) {
                    int i = 0;
                    Iterator var4 = irMethod.stmts.iterator();

                    while(var4.hasNext()) {
                        Stmt p = (Stmt)var4.next();
                        if (p.st == ST.LABEL) {
                            LabelStmt labelStmt = (LabelStmt)p;
                            labelStmt.displayName = "L" + i++;
                        }
                    }

                    System.out.println(irMethod);
                }

                T_type.transform(irMethod);
                T_unssa.transform(irMethod);
                T_ir2jRegAssign.transform(irMethod);
                T_trimEx.transform(irMethod);
            }

            public void ir2j(IrMethod irMethod, MethodVisitor mv) {
                (new IR2JConverter((8 & Dex2jarModified.this.v3Config) != 0)).convert(irMethod, mv);
            }
        }).convertDex(fileNode, cvf);
    }

    public DexExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public DexFileReader getReader() {
        return this.reader;
    }

    public Dex2jarModified reUseReg(boolean b) {
        if (b) {
            this.v3Config |= 1;
        } else {
            this.v3Config &= -2;
        }

        return this;
    }

    public Dex2jarModified topoLogicalSort(boolean b) {
        if (b) {
            this.v3Config |= 2;
        } else {
            this.v3Config &= -3;
        }

        return this;
    }

    public Dex2jarModified noCode(boolean b) {
        if (b) {
            this.readerConfig |= 132;
        } else {
            this.readerConfig &= -133;
        }

        return this;
    }

    public Dex2jarModified optimizeSynchronized(boolean b) {
        if (b) {
            this.v3Config |= 8;
        } else {
            this.v3Config &= -9;
        }

        return this;
    }

    public Dex2jarModified printIR(boolean b) {
        if (b) {
            this.v3Config |= 4;
        } else {
            this.v3Config &= -5;
        }

        return this;
    }

    public Dex2jarModified reUseReg() {
        this.v3Config |= 1;
        return this;
    }

    public Dex2jarModified optimizeSynchronized() {
        this.v3Config |= 8;
        return this;
    }

    public Dex2jarModified printIR() {
        this.v3Config |= 4;
        return this;
    }

    public Dex2jarModified topoLogicalSort() {
        this.v3Config |= 2;
        return this;
    }

    public void setExceptionHandler(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public Dex2jarModified skipDebug(boolean b) {
        if (b) {
            this.readerConfig |= 1;
        } else {
            this.readerConfig &= -2;
        }

        return this;
    }

    public Dex2jarModified skipDebug() {
        this.readerConfig |= 1;
        return this;
    }

    public void to(Path file) throws IOException {
        if (Files.exists(file, new LinkOption[0]) && Files.isDirectory(file, new LinkOption[0])) {
            this.doTranslate(file);
        } else {
            Throwable var2 = null;
            Object var3 = null;

            try {
                FileSystem fs = createZip(file);

                try {
                    this.doTranslate(fs.getPath("/"));
                } finally {
                    if (fs != null) {
                        fs.close();
                    }

                }
            } catch (Throwable var10) {
                if (var2 == null) {
                    var2 = var10;
                } else if (var2 != var10) {
                    var2.addSuppressed(var10);
                }
            }
        }

    }

    private static FileSystem createZip(Path output) throws IOException {
        Map<String, Object> env = new HashMap();
        env.put("create", "true");
        Files.deleteIfExists(output);
        Path parent = output.getParent();
        if (parent != null && !Files.exists(parent, new LinkOption[0])) {
            Files.createDirectories(parent);
        }

        Iterator var4 = FileSystemProvider.installedProviders().iterator();

        FileSystemProvider p;
        String s;
        do {
            if (!var4.hasNext()) {
                throw new IOException("cant find zipfs support");
            }

            p = (FileSystemProvider)var4.next();
            s = p.getScheme();
        } while(!"jar".equals(s) && !"zip".equalsIgnoreCase(s));

        return p.newFileSystem(output, env);
    }

    public Dex2jarModified withExceptionHandler(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }
}
