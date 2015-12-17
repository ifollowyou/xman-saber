package org.ifollowyou.saber.remote;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 多线程断点续传  基于 (HTTP)
 *
 * @author Bin
 *         Windows NT 6.1
 */
public class TextThreadsAndPointAdd {

    public TextThreadsAndPointAdd(String webAddr, String fileDir, String fileName, int count) {
        try {
            TranBean bean = new TranBean(webAddr, fileDir, fileName, count);
            ControlFileFetch fileFetch = new ControlFileFetch(bean);
            fileFetch.start();
        } catch (Exception e) {
            System.out.println("多线程下载文件出错:" + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("http.proxyHost", "10.3.76.8");
        System.setProperty("http.proxyPort", "80");

        String webAddr = "http://mirrors.cnnic.cn/apache//httpcomponents/httpclient/binary/httpcomponents-client-4.5.1-bin.zip";
        String fileDir = "c:/";
        String fileName = "hc.zip";
        int count = 5;
        new TextThreadsAndPointAdd(webAddr, fileDir, fileName, count);


    }
}

//扩展多线程类,负责文件的抓取,控制内部线程  
class ControlFileFetch extends Thread {
    TranBean tranBean = null;             //扩展信息bean
    long[] startPosition;              //开始位置
    long[] endPosition;                //结束位置
    FileFetch[] childThread;            //子线程对象  
    long fileLength;                    //文件长度  
    boolean isFitstGet = true;            //是否第一次去文件
    boolean isStopGet = false;            //停止标志
    File fileName;                      //文件下载的临时信息  
    DataOutputStream output;            //输出到文件的输出流  

    public ControlFileFetch(TranBean tranBean) {
        this.tranBean = tranBean;
        fileName = new File(tranBean.getFileDir() + File.separator + tranBean.getFileName() + ".info");  //创建文件
        System.out.println(tranBean.getFileDir() + File.separator + tranBean.getFileName() + ".info");
        if (fileName.exists()) {
            isFitstGet = false;
            readInfo();
        } else {
            startPosition = new long[tranBean.getCount()];
            endPosition = new long[tranBean.getCount()];
        }

    }

    public void run() {
        try {
            if (isFitstGet) {                             //第一次读取文件
                fileLength = getFieldSize();              //调用方法获取文件长度

                if (fileLength == -1) {
                    System.err.println("文件长度为止");
                } else if (fileLength == -2) {
                    System.err.println("不能访问文件");
                } else {
                    System.out.println("文件的长度:" + fileLength);

                    //循环划分 每个线程要下载的文件的开始位置  
                    for (int i = 0; i < startPosition.length; i++) {
                        startPosition[i] = (long) (i * (fileLength / startPosition.length));
                    }
                    //循环划分每个线程要下载的文件的结束位置  
                    for (int i = 0; i < endPosition.length - 1; i++) {
                        endPosition[i] = startPosition[i + 1];
                    }
                    //设置最后一个 线程的下载 结束位置 文件的的长度   
                    endPosition[endPosition.length - 1] = fileLength;
                }
            }
            //创建 子线程数量的数组  
            childThread = new FileFetch[startPosition.length];

            for (int i = 0; i < startPosition.length; i++) {
                childThread[i] = new FileFetch(tranBean.getWebAddr(), tranBean.getFileDir() + File.separator + tranBean.getFileName(), startPosition[i], endPosition[i], i);
                Log.log("线程" + (i + 1) + ",的开始位置=" + startPosition[i] + ",结束位置=" + endPosition[i]);
                childThread[i].start();
            }

            boolean breakWhile = false;
            while (!isStopGet) {
                savePosition();
                Log.sleep(500);//     
                breakWhile = true;
                for (int i = 0; i < startPosition.length; i++) { //循环实现下载文件  
                    if (!childThread[i].downLoadOver) {
                        breakWhile = false;
                        break;
                    }
                }
                if (breakWhile)
                    break;

            }

            System.err.println("文件下载结束!");

        } catch (Exception e) {
            System.out.println("下载文件出错:" + e.getMessage());
        }

    }

    //保存下载信息(文件指针信息)  
    private void savePosition() {
        try {
            output = new DataOutputStream(new FileOutputStream(fileName));
            output.writeInt(startPosition.length);
            for (int i = 0; i < startPosition.length; i++) {
                output.writeLong(childThread[i].startPosition);
                output.writeLong(childThread[i].endPosition);
            }
            output.close();
        } catch (Exception e) {
            System.out.println("保存下载信息出错:" + e.getMessage());
        }


    }

    //获得文件的长度  
    public long getFieldSize() {
        int fileLength = -1;
        try {
            URL url = new URL(tranBean.getWebAddr());     //根据网址传入网址创建URL对象
            //打开连接对象  
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            //设置描述发出HTTP请求的终端信息  
            httpConnection.setRequestProperty("User-Agent", "NetFox");
            int responseCode = httpConnection.getResponseCode();
            //表示不能访问文件  
            if (responseCode >= 400) {
                errorCode(responseCode);
                return -2;
            }
            String head;
            for (int i = 1; ; i++) {
                head = httpConnection.getHeaderFieldKey(i); //获取文件头部信息
                if (head != null) {
                    if (head.equals("Content-Length")) {  //根据头部信息获取文件长度
                        fileLength = Integer.parseInt(httpConnection.getHeaderField(head));
                        break;
                    }
                } else {
                    break;
                }
            }


        } catch (Exception e) {
            System.out.println("获取文件长度出错:" + e.getMessage());
        }
        Log.log("文件长度" + fileLength);
        return fileLength;

    }

    private void errorCode(int errorCode) {
        System.out.println("错误代码:" + errorCode);
    }

    //读取文件指针位置  
    private void readInfo() {
        try {
            //创建数据输出流  
            DataInputStream input = new DataInputStream(new FileInputStream(fileName));
            int count = input.readInt();                     //读取分成的线程下载个数
            startPosition = new long[count];                  //设置开始线程
            endPosition = new long[count];                    //设置结束线程
            for (int i = 0; i < startPosition.length; i++) {
                startPosition[i] = input.readLong();          //读取每个线程的开始位置
                endPosition[i] = input.readLong();            //读取每个线程的结束位置
            }
            input.close();
        } catch (Exception e) {
            System.out.println("读取文件指针位置出错:" + e.getMessage());
        }
    }


}

//扩展线程类,实现部分文件的抓取  
class FileFetch extends Thread {
    String webAddr;                     //网址  
    long startPosition;                 //开始位置  
    long endPosition;                   //结束位置  
    int threadID;                       //线程编号  
    boolean downLoadOver = false;         //下载结束
    boolean isStopGet = false;            //是否停止请求
    FileAccess fileAccessI = null;            //存储文件的类

    public FileFetch(String surl, String sname, long startPosition, long endPosition,
                     int threadID) throws IOException {
        this.webAddr = surl;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.threadID = threadID;
        this.fileAccessI = new FileAccess(sname, startPosition);
    }

    //实现线程的方法  
    public void run() {
        while (startPosition < endPosition && !isStopGet) {
            try {
                URL url = new URL(webAddr);       //根据网络资源创建URL对象
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //创建 打开的连接对象
                //设置描述发出的HTTP请求的终端的信息  
                httpConnection.setRequestProperty("User-Agent", "NetFox");
                String sproperty = "byte=" + startPosition + "-";
                httpConnection.setRequestProperty("RANGE", sproperty);
                Log.log(sproperty);

                //获取 网络资源的输入流  
                InputStream input = httpConnection.getInputStream();

                byte[] b = new byte[1024];
                int nRead;
                //循环将文件下载制定目录  
                while ((nRead = input.read(b, 0, 1024)) > 0 && startPosition < endPosition && !isStopGet) {
                    startPosition += fileAccessI.write(b, 0, nRead); //调用方法将内容写入文件
                }
                Log.log("线程\t" + (threadID + 1) + "\t结束....");
                downLoadOver = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //打印回应的头的信息  
    public void logResponseHead(HttpURLConnection con) {
        for (int i = 1; ; i++) {
            String header = con.getHeaderFieldKey(i); //循环答应回应的头信息
            if (header != null) {
                Log.log(header + ":" + con.getHeaderField(header));
            } else
                break;
        }
    }

    public void splitterStop() {
        isStopGet = true;
    }

}

//存储文件的类  
class FileAccess implements Serializable {
    RandomAccessFile saveFile;          //要保存的文件  
    long position;

    public FileAccess() throws IOException {
        this("", 0);
    }

    public FileAccess(String sname, long position) throws IOException {
        this.saveFile = new RandomAccessFile(sname, "rw");   //创建随机读取对象, 以 读/写的方式
        this.position = position;
        saveFile.seek(position);                            //设置指针位置  
    }

    //将字符数据 写入文件  
    public synchronized int write(byte[] b, int start, int length) {
        int n = -1;
        try {
            saveFile.write(b, start, length);
            n = length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return n;
    }
}

//传输保存信息的类  
class TranBean {
    private String webAddr;             //下载地址  
    private String fileDir;             //下载到指定的目录  
    private String fileName;            //下载后文件的新名字  
    private int count;                  //文件分几个线程下载, 默认为  3个   

    public TranBean() {                 //默认的构造方法
        this("", "", "", 3);
    }                                   //带参数的构造方法  

    public TranBean(String webAddr, String fileDir, String fileName, int count) {
        this.webAddr = webAddr;
        this.fileDir = fileDir;
        this.fileName = fileName;
        this.count = count;
    }

    public String getWebAddr() {
        return webAddr;
    }

    public void setWebAddr(String webAddr) {
        this.webAddr = webAddr;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}

//线程运行信息显示的日志类  
class Log {
    public Log() {
    }

    public static void sleep(int nsecond) {
        try {
            Thread.sleep(nsecond);
        } catch (Exception e) {
            System.out.println("线程沉睡");
        }
    }

    public static void log(String message) {         //显示日志信息
        System.err.println(message);

    }

    public static void log(int message) {            //显示日志信息
        System.err.println(message);

    }
}  