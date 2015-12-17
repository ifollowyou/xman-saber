package org.ifollowyou.utility.remote;

public class TestDownloadMain {
 
    public static void main(String[] args) {
        /*DownloadInfo bean = new DownloadInfo("http://i7.meishichina.com/Health/UploadFiles/201109/2011092116224363.jpg");
        System.out.println(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();*/
        
        //DownloadUtils.download("http://i7.meishichina.com/Health/UploadFiles/201109/2011092116224363.jpg");

        System.setProperty("http.proxyHost", "10.3.76.8");
        System.setProperty("http.proxyPort", "80");

//        DownloadUtils.download("http://mirrors.hust.edu.cn/apache//httpcomponents/httpclient/binary/httpcomponents-client-4.5.1-bin.tar.gz", "httpcomponents-client-4.5.1-bin.tar.gz", "c:/", 5);
        DownloadUtils.download("http://mirror.bit.edu.cn/apache/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.zip", "maven.zip", "c:/", 1);
    }
}