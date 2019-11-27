/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class ZipUtils {

//    private static final Logger LOG = Logger.getLogger(ZipUtils.class.getName());

    public static void zip(NutsWorkspace ws,String target, ZipOptions options, String... source) throws IOException {
        if (options == null) {
            options = new ZipOptions();
        }
        File targetFile = new File(target);
        File f = options.isTempFile() ? File.createTempFile("zip", ".zip") : targetFile;
        f.getParentFile().mkdirs();
        ZipOutputStream zip = null;
        FileOutputStream fW = null;
        try {
            fW = new FileOutputStream(f);
            try {
                zip = new ZipOutputStream(fW);
                if (options.isSkipRoot()) {
                    for (String s : source) {
                        File file1 = new File(s);
                        if (file1.isDirectory()) {
                            for (File file : file1.listFiles()) {
                                add("", file.getPath(), zip);
                            }
                        } else {
                            add("", file1.getPath(), zip);
                        }
                    }
                } else {
                    for (String s : source) {
                        add("", s, zip);
                    }
                }
            } finally {
                if (zip != null) {
                    zip.close();
                }
            }
        } finally {
            if (fW != null) {
                fW.close();
            }
        }
        if (options.isTempFile()) {
            targetFile.getParentFile().mkdirs();
            if (!f.renameTo(targetFile)) {
                Files.copy(f.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    //    private static void zipDir(String dirName, String nameZipFile) throws IOException {
//        ZipOutputStream zip = null;
//        FileOutputStream fW = null;
//        fW = new FileOutputStream(nameZipFile);
//        zip = new ZipOutputStream(fW);
//        addFolderToZip("", dirName, zip);
//        zip.close();
//        fW.close();
//    }
    private static void add(String path, String srcFolder, ZipOutputStream zip) throws IOException {
        File folder = new File(srcFolder);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFolder, zip);
        } else {
            addFileToZip(path, srcFolder, zip, false);
        }
    }

    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws IOException {
        File folder = new File(srcFolder);
        if (folder.list().length == 0) {
            addFileToZip(path, srcFolder, zip, true);
        } else {
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    addFileToZip(folder.getName(), concatPath(srcFolder, fileName), zip, false);
                } else {
                    addFileToZip(concatPath(path, folder.getName()), srcFolder + "/" + fileName, zip, false);
                }
            }
        }
    }

    private static String concatPath(String a, String b) {
        if (a.endsWith("/")) {
            if (b.startsWith("/")) {
                return a + b.substring(1);
            } else {
                return a + b;
            }
        } else {
            if (b.startsWith("/")) {
                return a + b;
            } else {
                return a + "/" + b;
            }
        }
    }

    private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag) throws IOException {
        File folder = new File(srcFile);
        String pathPrefix = path;
        if (!pathPrefix.endsWith("/")) {
            pathPrefix = pathPrefix + "/";
        }
        if (!pathPrefix.startsWith("/")) {
            pathPrefix = "/" + pathPrefix;
        }

        if (flag) {
//            System.out.println("[FOLDER ]" + pathPrefix + folder.getName());
            zip.putNextEntry(new ZipEntry(pathPrefix + folder.getName() + "/"));
        } else {
            if (folder.isDirectory()) {
                addFolderToZip(pathPrefix, srcFile, zip);
            } else {
//                System.out.println("[FILE  ]" + pathPrefix + folder.getName() + " - " + srcFile);
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                zip.putNextEntry(new ZipEntry(pathPrefix + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
        }
    }

    public static boolean visitZipFile(File zipFile, Predicate<String> possiblePaths, InputStreamVisitor visitor) throws IOException {
        InputStream is = null;
        try {
            return visitZipStream(is = new FileInputStream(zipFile), possiblePaths, visitor);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Unzip it
     *
     * @param zipFile input zip file
     * @param outputFolder zip file output folder
     */
    public static void unzip(NutsWorkspace ws,String zipFile, String outputFolder, UnzipOptions options) throws IOException {
        if (options == null) {
            options = new UnzipOptions();
        }
        byte[] buffer = new byte[1024];

        //create output directory is not exists
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        //get the zip file content
        try(ZipInputStream zis
                = new ZipInputStream(new FileInputStream(new File(zipFile)))) {
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            String root = null;
            while (ze != null) {

                String fileName = ze.getName();
                if (options.isSkipRoot()) {
                    if (root == null) {
                        if (fileName.endsWith("/")) {
                            root = fileName;
                            ze = zis.getNextEntry();
                            continue;
                        } else {
                            throw new IOException("tot a single root zip");
                        }
                    }
                    if (fileName.startsWith(root)) {
                        fileName = fileName.substring(root.length());
                    } else {
                        throw new IOException("tot a single root zip");
                    }
                }
                if (fileName.endsWith("/")) {
                    File newFile = new File(outputFolder + File.separator + fileName);
                    newFile.mkdirs();
                } else {
                    File newFile = new File(outputFolder + File.separator + fileName);
                    ws.log().of(ZipUtils.class).log(Level.FINEST, NutsLogVerb.WARNING, "file unzip : " + newFile.getAbsoluteFile());
                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    newFile.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }

    public static boolean extractFirstPath(InputStream zipFile, Set<String> possiblePaths, OutputStream output, boolean closeOutput) throws IOException {
        byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(zipFile);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                if (!fileName.endsWith("/")) {
                    if (possiblePaths.contains(fileName)) {
                        int len;
                        try {
                            while ((len = zis.read(buffer)) > 0) {
                                output.write(buffer, 0, len);
                            }
                            zis.closeEntry();
                        } finally {
                            if (closeOutput) {
                                output.close();
                            }
                        }
                        return true;
                    }
                }
                ze = zis.getNextEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
        return false;
    }

//    public static void zip(final File _folder, final File _zipFilePath) {
//        final Path folder = _folder.toPath();
//        Path zipFilePath = _zipFilePath.toPath();
//        try (
//                FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
//                ZipOutputStream zos = new ZipOutputStream(fos)) {
//            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
//                    Files.copy(file, zos);
//                    zos.closeEntry();
//                    return FileVisitResult.CONTINUE;
//                }
//
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    zos.putNextEntry(new ZipEntry(folder.relativize(dir).toString() + "/"));
//                    zos.closeEntry();
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            throw new RuntimeIOException(e);
//        }
//    }
    public static boolean visitZipStream(InputStream zipFile, Predicate<String> possiblePaths, InputStreamVisitor visitor) throws IOException {
        //byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(zipFile);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            final ZipInputStream finalZis = zis;
            InputStream entryInputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    return finalZis.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return finalZis.read(b);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return finalZis.read(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    finalZis.closeEntry();
                }
            };

            while (ze != null) {

                String fileName = ze.getName();
                if (!fileName.endsWith("/")) {
                    if (possiblePaths.test(fileName)) {
                        if (!visitor.visit(fileName, entryInputStream)) {
                            break;
                        }
                    }
                }
                ze = zis.getNextEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
        }

        return false;
    }
}
