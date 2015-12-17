package org.ifollowyou.saber;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ifollowyou.saber.model.JarFileFilter;
import org.ifollowyou.saber.model.JarFileItem;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileUtil {
    private final static Log log = LogFactory.getLog(FileUtil.class);

    public static boolean isExist(String path) {
        return new File(path).exists();
    }

    public static boolean isNotExist(String path) {
        return !isExist(path);
    }

    public static boolean makeDirs(String path) {
        return new File(path).mkdirs();
    }

    public static void makeDirsWhenNeeded(String path) {
        if (PathUtil.isFile(path)) {
            String dir = PathUtil.getDirPath(path);
            if (isNotExist(dir)) {
                new File(dir).mkdirs();
            }

            return;
        } else if (isNotExist(path)) {
            new File(path).mkdirs();
        }
    }

    public static void makeDirsOfFileWhenNeeded(String filePath) {
        String dir = PathUtil.getDirPath(filePath);
        if (isNotExist(dir)) {
            makeDirs(dir);
        }
    }

    public static File find(String path, String name) {
        Collection<File> files = FileUtils.listFiles(new File(path), new String[]{PathUtil.getExtName(name)}, true);
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(name)) {
                return file;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static File findFileByPtn(String path, String ptn, String ext) {
        List<File> files = findFilesByKeyword(path, ptn, ext);
        if (files.isEmpty()) {
            return null;
        } else {
            return files.get(0);
        }
    }

    /**
     * @param path
     * @param keyword keyword which should be contained in the desired file names
     * @param ext
     * @param include if the keyword should be exist in the file name
     * @return
     */
    public static List<File> findFilesByKeyword(String path, String keyword, boolean include, String ext) {
        ArrayList<File> rst = new ArrayList<File>();
        String[] ptns = keyword.split(",");
        Collection<File> files = FileUtils.listFiles(new File(path), new String[]{PathUtil.getExtName(ext)}, true);
        for (File file : files) {
            for (String p : ptns) {
                if (include) {
                    if (file.getName().contains(p.trim())) {
                        rst.add(file);
                    }
                } else {
                    if (!file.getName().contains(p.trim())) {
                        rst.add(file);
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            System.out.println(rst.size() + " files found=============");
            for (File file : rst) {
                System.out.println(file.getAbsolutePath());
            }
        }
        return rst;
    }

    public static List<File> findFilesByKeyword(String path, String keyword, String ext) {
        return findFilesByKeyword(path, keyword, true, ext);
    }


    /**
     * find files from jar,
     *
     * @param jarPath
     * @param keyword
     * @param ext     not contains dot.
     * @return
     */
    public static List<JarFileItem> findFilesFromJarByKeyword(String jarPath, String keyword, String ext) throws IOException {
        return findFilesFromJarByKeyword(jarPath, keyword, true, ext);
    }

    public static List<JarFileItem> findFilesFromJarByKeyword(String jarPath, final String keyword, boolean include, String ext) throws IOException {
        if (StringUtils.isBlank(keyword)) {
            return listJarFile(jarPath, null, ext);
        } else {
            JarFileFilter filter;
            if (include) {
                filter = new JarFileFilter() {
                    public boolean accept(JarEntry entry) {
                        return entry.getName().contains(keyword);
                    }
                };
            } else {
                filter = new JarFileFilter() {
                    public boolean accept(JarEntry entry) {
                        return !entry.getName().contains(keyword);
                    }
                };
            }

            return listJarFile(jarPath, filter, ext);
        }
    }


    public static List<JarFileItem> listJarFile(String jarPath, JarFileFilter filter, String ext) throws IOException {
        List<JarFileItem> result = new ArrayList<JarFileItem>();
        JarFile jar = null;
        try {
            jar = new JarFile(jarPath);
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith("." + ext)) {
                    continue;
                }

                if (filter == null) {
                    continue;
                } else if (filter.accept(entry)) {
                    result.add(new JarFileItem(jarPath, entry.getName()));
                }
            }

            jar.close();
        } catch (IOException e) {
            log.error(e, e);
            throw e;
        }

        return result;
    }

    public static List<JarFileItem> listJarFile(String jarPath, String ext) throws IOException {
        return listJarFile(jarPath, null, ext);
    }

    public static String getContentFromJar(String jarPath, String filePath) {
        String fPath = filePath.startsWith("/") ? filePath : "/" + filePath;
        URL url = null;
        try {
            url = new URL("jar:file:/" + jarPath + "!" + fPath);
            return readInputStream(url.openStream());
        } catch (MalformedURLException e) {
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getContentFromJar(String jarPath, String filePath, String encoding) {
        String fPath = filePath.startsWith("/") ? filePath : "/" + filePath;
        URL url = null;
        try {
            url = new URL("jar:file:/" + jarPath + "!" + fPath);
            return readInputStream(url.openStream(), encoding);
        } catch (MalformedURLException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    public static String readInputStream(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String str = null;
        try {
            while ((str = reader.readLine()) != null) {
                sb.append(str + "\n");
            }
        } catch (IOException e) {
            return "";
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static String readInputStream(InputStream is, String encoding) {
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        String str = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, encoding));
            while ((str = reader.readLine()) != null) {
                sb.append(str + "\n");
            }
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (IOException e) {
            return "";
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static List<String> head(File file, int n) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String str = null;
        int row = 0;
        ArrayList<String> lines = new ArrayList<String>();
        while ((str = reader.readLine()) != null) {
            row++;
            if (row > n) {
                IOUtils.closeQuietly(reader);
                return lines;
            } else {
                lines.add(str);
            }
        }

        IOUtils.closeQuietly(reader);
        return lines;
    }


    public static long getLineNum(String filePath) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filePath));
        long cnt = 0;
        int len = 0;
        byte[] buffer = new byte[1000];
        while ((len = is.read(buffer)) != -1) {
            for (int i = 0; i < len; i++) {
                if (buffer[i] == '\n') {
                    cnt++;
                }
            }
        }

        IOUtils.closeQuietly(is);
        return ++cnt;
    }

    public static void write(File out, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(out);
            fw.write(content);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    log.error(e);
                }

            }
        }
    }

    public static long writePart(OutputStream os, InputStream is, long size, long start, long len, FileWriteProgress fileWriteProgress) throws IOException {
        int bufferSize = 64 * 1024;
        byte[] buffer = new byte[bufferSize];
        is.skip(start);
        long pos = start;
        long end;
        if (pos + len - 1 > size - 1) {
            end = size - 1;
        } else {
            end = start + len - 1;
        }
        int cnt = 0;
        long total = 0;
        while (true) {
            if (pos + bufferSize - 1 > end - 1) {
                cnt = is.read(buffer, 0, (int) (end - pos + 1));
            } else {
                cnt = is.read(buffer);
            }

            os.write(buffer, 0, cnt);

            total += cnt;
            fileWriteProgress.changed(total);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error(e);
            }
            if (pos + bufferSize - 1 >= end - 1) {
                break;
            } else {
                pos += cnt;
            }
        }

        os.flush();
        os.close();

        return total;
    }

    public static File mergePartialFilesMT(File[] partials, final String finalFullPath) {
//        ArrayList<Callable> callables=new ArrayList<Callable>();
//        for (int i = 0; i < partials.length; i++) {
//              File partial=partials[i];
//              callables.add(new Callable() {
//                  public Object call() throws Exception {
//                      RandomAccessFile raf=new RandomAccessFile(new File(finalFullPath),"rws");
//                      raf.skipBytes(pos);
//                      raf.seek();
//                      return null;
//                  }
//              })
//              long bytes=getFileByteNum(partial);
//
//
//        }

        return null;
    }

    public static File mergePartialFilesLinear(File[] partials, final String finalFullPath) {
        File file = new File(finalFullPath);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            for (File partial : partials) {
                IOUtils.copy(new FileInputStream(partial), os);
            }
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }

        return file;
    }

    public static long getFileByteNum(File file) {
        try {
            return new RandomAccessFile(file, "r").length();
        } catch (IOException e) {
            log.error(e);
            return -1;
        }
    }

}