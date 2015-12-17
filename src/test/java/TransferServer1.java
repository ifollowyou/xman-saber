import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class TransferServer1 {
    private int defaultBindPort = Constants.DEFAULT_BIND_PORT;    //默认监听端口号为10000
    private int tryBindTimes = 0;           //初始的绑定端口的次数设定为0  
    private ServerSocket serverSocket;      //服务套接字等待对方的连接和文件发送  
    private ExecutorService executorService;    //线程池  
    private final int POOL_SIZE = 4;            //单个CPU的线程池大小   

    /**
     * 不带参数的构造器，选用默认的端口号 
     * @throws Exception
     */
    public TransferServer1() throws Exception{
        try {
            this.bingToServerPort(defaultBindPort);
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
            System.out.println("开辟线程数 ： " + Runtime.getRuntime().availableProcessors() * POOL_SIZE);
        } catch (Exception e) {
            throw new Exception("绑定端口不成功!");
        }
    }

    /**
     * 带参数的构造器，选用用户指定的端口号 
     * @param port
     * @throws Exception
     */
    public TransferServer1(int port) throws Exception{
        try {
            this.bingToServerPort(port);
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
        } catch (Exception e) {
            throw new Exception("绑定端口不成功!");
        }
    }

    private void bingToServerPort(int port) throws Exception{
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(port);
            System.out.println("服务启动!");
        } catch (Exception e) {
            this.tryBindTimes = this.tryBindTimes + 1;
            port = port + this.tryBindTimes;
            if(this.tryBindTimes >= 20){
                throw new Exception("您已经尝试很多次了，但是仍无法绑定到指定的端口!请重新选择绑定的默认端口号");
            }
            //递归绑定端口  
            this.bingToServerPort(port);
            //TODO
        }
    }

    public void service(){
        Socket socket = null;
        while (true) {
            try {
                socket = serverSocket.accept();
                executorService.execute(new Handler(socket));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class Handler implements Runnable{
        private Socket socket;

        public Handler(Socket socket){
            this.socket = socket;
        }

        public void run() {

            System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

            DataInputStream dis = null;
            DataOutputStream dos = null;
            DataOutputStream dsm = null;

            int bufferSize = 8192;
            byte[] buf = new byte[bufferSize];

            try {
                dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dsm = new DataOutputStream(socket.getOutputStream());
                String savePath = Constants.RECEIVE_FILE_PATH + dis.readUTF();
                long length = dis.readLong();
                File file = new File(savePath);
                if(file.exists()){
                    System.out.println("文件["+file.getName()+"]进入续传！");
                    long fileLength = file.length();

                    dsm.writeUTF(fileLength+"");
                    dsm.flush();
                    RandomAccessFile raf = new RandomAccessFile(savePath,"rw");
//                	System.out.println("文件存放位置:"+savePath);
//                	System.out.println("续传长度标记:"+fileLength);

                    int start = (int)fileLength;
                    int amount = 0;
                    raf.seek(fileLength);
                    while((amount = dis.read(buf))!=-1){
                        raf.write(buf,0,amount);
                        start = start + amount;
//                		System.out.println("文件["+file.getName()+"]已接收:"+start*100L/length+"%");
                    }
//                	System.out.println("文件接收结束!");
                    raf.close();
                    this.socket.shutdownInput();
                    dsm.writeUTF("90000");
                    System.out.println("文件["+file.getName()+"]接收成功2 : 90000");
                }else{
                    System.out.println("文件["+file.getName()+"]不存在直接传送!");
                    dsm.writeUTF("noexsits");
                    dsm.flush();
                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath)));
                    int read = 0;
                    long passedlen = 0;
                    while ((read = dis.read(buf)) != -1) {
                        passedlen += read;
                        dos.write(buf, 0, read);
//                        System.out.println("文件[" + savePath + "]已经接收: " + passedlen * 100L/ length + "%");  
                    }
//                    
                    this.socket.shutdownInput();
                    dsm.writeUTF("90000");
                    System.out.println("文件["+file.getName()+"]接收成功1 : 90000");
                }



            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("接收文件失败!");
            }finally{
                try {
                    if(dos != null){
                        dos.close();
                    }
                    if(dis != null){
                        dis.close();
                    }
                    if(dsm !=null){
                        dsm.close();
                    }
                    if(socket != null){
                        socket.close();
                    }
                    System.gc();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception{
        new TransferServer1().service();
    }

}
