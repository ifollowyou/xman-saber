package org.ifollowyou.utility.remote;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadsTest {

    public static void main(String args[]) throws Exception {
//        System.setProperty("http.proxyHost", "10.3.76.8");
//        System.setProperty("http.proxyPort", "80");

        final Downloads downloads = new Downloads("http://mirror.bit.edu.cn/apache/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.zip", "maven-bin1.zip", 3);
//        final Downloads downloads = new Downloads("http://localhost:8888/zxwas_build.zip", "was.zip", 3);

//        downloads.setHttpProxy("10.3.76.8", 80);
        downloads.download();

        new Thread(() -> {
            while (downloads.getCompleteRate() < 1) {
                log.info("已完成:" + downloads.getCompleteRate());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
