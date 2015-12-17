package org.ifollowyou.saber.remote;


/**
 * <b>function:</b> 分块多线程下载工具类
 */
public abstract class DownloadUtils {

    public static void download(String url) {
        DownloadInfo bean = new DownloadInfo(url);
        LogUtils.info(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();
    }

    public static void download(String url, int threadNum) {
        DownloadInfo bean = new DownloadInfo(url, threadNum);
        LogUtils.info(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();
    }

    public static void download(String url, String fileName, String filePath, int threadNum) {
        DownloadInfo bean = new DownloadInfo(url, fileName, filePath, threadNum);
        LogUtils.info(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();
    }
}