package org.ifollowyou.saber;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Slf4j
public class PathUtil {
    public static String replaceDot(String path) {
        return path.replace(".", "/");
    }

    public static String replaceAntiSlash(String path) {
        return path.replace("\\", "/");
    }

    public static String replaceSlash(String path) {
        return path.replace("/", "\\");
    }


    public static String getFilePhyPath(String proj, String src, String pkg, String name, String ext) {
        String rst = proj + "/" + src + "/";

        if (Objects.isNotEmpty(pkg)) {
            rst += replaceDot(pkg);
        }
        rst = replaceAntiSlash(rst);
        new File(rst).mkdirs();

        return rst + "/" + name + "." + ext;
    }

    public static String getFilePhyPath(String proj, String src, String pkg, String name) {
        return getFilePhyPath(proj, src, pkg, name, "java");
    }


    public static String getDirPhyPath(String pathBeforePkg, String pkg) {
        pathBeforePkg = replaceAntiSlash(pathBeforePkg);
        return pathBeforePkg + "/" + replaceDot(pkg);
    }

    public static String getDirPath(String filePath) {
        filePath = replaceAntiSlash(filePath);
        if (isFile(filePath)) {
            String fname = getFileNameFromPath(filePath);
            return filePath.substring(0, filePath.lastIndexOf(fname));
        } else {
            return filePath;
        }
    }

    public static String trimEndingSlash(String path) {
        if (path.endsWith("\\") || path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        } else {
            return path;
        }
    }

    public static String wellformPath4Java(String path) {
        path = PathUtil.replaceAntiSlash(path);
        path = PathUtil.trimEndingSlash(path);

        return path;
    }

    public static String getFileNameFromPath(String fullPath) {
        fullPath = replaceAntiSlash(fullPath);
        if (fullPath.endsWith("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }

        int lastIdx = fullPath.lastIndexOf("/");
        return fullPath.substring(lastIdx + 1);
    }

    public static String getExtName(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String getPathFromURL(URL url) {
        try {
            return new File(url.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static URL getFileUrlOfJar(String jarPath, String relativePath) {
        String fPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        URL url = null;
        try {
            url = new URL("jar:file:/" + jarPath + "!" + fPath);
        } catch (MalformedURLException e) {
            log.error("", e);
            return null;
        }

        return url;
    }

    public static String convertQNamePhyPath(String qName) {
        return replaceDot(qName) + ".java";
    }

    public static String getPkgFromQName(String qName) {
        return qName.substring(0, qName.lastIndexOf("."));
    }

    public static String getClsNameFromQName(String qName) {
        return qName.substring(qName.lastIndexOf(".") + 1);
    }

    public static boolean isFile(String path) {
        String tempPath = replaceAntiSlash(path);
        if (tempPath.lastIndexOf(".") > tempPath.lastIndexOf("/")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDirectory(String path) {
        return !isFile(path);
    }

    public static String remove(String path, String remove) {
        path = replaceSlash(path);
        remove = replaceSlash(remove);
        return StringUtils.remove(path, remove);
    }

    public static String getRelativePath(String path, String base) {
        base = replaceAntiSlash(base);
        path = replaceAntiSlash(path);
        if (!path.contains(base)) {
            return path;
        }

        path = path.replace(base, "");
        return path.substring(1);
    }

    public static String removeExtName(String path) {
        return path.substring(0, path.lastIndexOf("."));
    }
}