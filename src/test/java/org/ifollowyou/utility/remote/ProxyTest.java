package org.ifollowyou.utility.remote;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;


public class ProxyTest {

    public static void main(String[] args) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        URL baiduURL = new URL("http://www.baidu.com");

//        InetSocketAddress isa = new InetSocketAddress("10.3.76.8", 80);
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, isa);

        System.setProperty("http.proxyHost", "10.3.76.8");
        System.setProperty("http.proxyPort", "80");

        URLConnection conn = baiduURL.openConnection();

        InputStream inputStream = conn.getInputStream();
        if (inputStream != null) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str;
            try {
                while ((str = bufferedReader.readLine()) != null) {
                    stringBuilder.append(str).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeStream(bufferedReader);
                closeStream(bufferedReader);
            }
        }

        System.out.println(stringBuilder.toString());
    }

    public static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
