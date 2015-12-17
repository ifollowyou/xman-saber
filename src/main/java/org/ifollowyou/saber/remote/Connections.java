package org.ifollowyou.saber.remote;


import lombok.extern.slf4j.Slf4j;
import org.ifollowyou.saber.Objects;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

@Slf4j
public class Connections {
    private static Proxy proxy;

    static {
        System.getProperty("http.proxy.host");
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.3.76.8", 80));
    }

    public static HttpURLConnection getHttpConnection(URL url) {
        log.info("Get connection for URL=" + url);
        HttpURLConnection connection = null;
        try {
            if (Objects.isNotNull(proxy)) {
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
        } catch (IOException e) {
            log.error("Failed to open connection for URL=" + url, e);
        }

        return connection;
    }
}
