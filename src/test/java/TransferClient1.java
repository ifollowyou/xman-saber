import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TransferClient1 {
    //socket 多线程网络传输多个文件

    private static ArrayList<String> fileList = new ArrayList<String>();

    private String sendFilePath = Constants.SEND_FILE_PATH;

    //
    private final String serverPath = Constants.SERVER_FILE_PATH;

    /**
     * 带参数的构造器，用户设定需要传送文件的文件夹 
     * @param filePath
     */
    public TransferClient1(String filePath){
        getFilePath(filePath);
    }

    /**
     * 不带参数的构造器。使用默认的传送文件的文件夹 
     */
    public TransferClient1(){
        getFilePath(sendFilePath);
    }

    public void service(){
        ExecutorService executorService = Executors.newCachedThreadPool();
        Vector<Integer> vector = getRandom(fileList.size());
        for(Integer integer : vector){
            String filePath = fileList.get(integer.intValue());
            executorService.execute(sendFile(filePath));
        }
    }


    private void getFilePath(String dirPath){
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for(int i = 0; i < files.length; i++){
            if(files[i].isDirectory()){
                getFilePath(files[i].getAbsolutePath());
            }
            else {
                fileList.add(files[i].getAbsolutePath());
            }
        }
    }

    private Vector<Integer> getRandom(int size){
        Vector<Integer> v = new Vector<Integer>();
        Random r = new Random();
        boolean b = true;
        while(b){
            int i = r.nextInt(size);
            if(!v.contains(i))
                v.add(i);
            if(v.size() == size)
                b = false;
        }
        return v;
    }

    private static Runnable sendFile(final String filePath){
        return new Runnable(){

            private Socket socket = null;
            private String ip ="localhost";
            private int port = 10000;
            public void run() {
                System.out.println("开始发送文件:" + filePath);
                File file = new File(filePath);

                if(createConnection()){
                    int bufferSize = 8192;
                    byte[] buf = new byte[bufferSize];
                    try {
                        DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                        DataInputStream dis = new DataInputStream(socket.getInputStream());

                        dos.writeUTF(file.getName());
                        dos.flush();
                        dos.writeLong(file.length());
                        dos.flush();

                        String marklength= dis.readUTF();
                        if("noexsits".equals(marklength)){
                            System.out.println("文件["+file.getName()+"]直接传输!");
                            int num = 0;
                            long numlth =0;
                            while((num=fis.read(buf))!=-1){
                                dos.write(buf,0,num);
                                dos.flush();
                                numlth+=num;
//                        		System.out.println("文件["+file.getName()+"]已传送"+numlth*100L/file.length()+"%");
                            }
                            dos.flush();
                            this.socket.shutdownOutput();
                            String record = dis.readUTF();
                            System.out.println("文件["+file.getName()+"]传输结束1:"+record);
                        }else{
                            System.out.println("读取服务器端["+file.getName()+"]文件长度!");
                            long serlth = Long.parseLong(marklength);
                            RandomAccessFile raf = new RandomAccessFile(filePath,"r");
                            raf.skipBytes((int)serlth);
                            int serstart =0;

                            while((serstart=raf.read(buf))!=-1){
                                dos.write(buf,0,serstart);
                                dos.flush();
                                serlth+=serstart;
//                        		System.out.println("文件["+file.getName()+"]已传送"+serlth*100L/file.length()+"%");
                            }
                            dos.flush();
                            raf.close();
                            this.socket.shutdownOutput();
                            String record = dis.readUTF();
                            System.out.println("文件["+file.getName()+"]传输结束2:"+record);
                        }

                        fis.close();
                        dos.close();
                        dis.close();
                        socket.close();
//                       System.out.println("文件 " + filePath + "传输完成!");  
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            private boolean createConnection() {
                try {
                    socket = new Socket(ip, port);
                    System.out.println("连接服务器成功！");
                    return true;
                } catch (Exception e) {
                    System.out.println("连接服务器失败！");
                    return false;
                }
            }

        };
    }

    public static void main(String[] args){
        new TransferClient1().service();
    }
}
