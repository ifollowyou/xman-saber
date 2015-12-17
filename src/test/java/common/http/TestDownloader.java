/**
 * 专注互联网,分享创造价值
 *  maoxiang@gmail.com
 *  2010-3-31下午10:55:16
 */
package common.http;

public class TestDownloader {

	public static void main(String[] args) throws Exception {
//		String url = "http://apache.etoak.com/tomcat/tomcat-6/v6.0.26/bin/apache-tomcat-6.0.26.zip";
		String url = "http://mirrors.hust.edu.cn/apache/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.zip";
		String saveFile = "c:/maven3.zip";
		HttpDownloader downloader = new HttpDownloader(url, saveFile);
		downloader.setHttpProxy("10.3.76.8", 80);

		downloader.download();
		System.out.println("finsihed");

	}
}
