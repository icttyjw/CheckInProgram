import joy.aksd.coreThread.RequestBroadcast;
import joy.aksd.coreThread.coreProcess;
import joy.aksd.coreThread.creatFirstBlock;
import joy.aksd.data.Block;
import joy.aksd.data.Record;
import joy.aksd.listenAndVerifyThread.Listener;
import joy.aksd.listenAndVerifyThread.verifyThread;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.data.protocolInfo.GETIPLIST;
import static joy.aksd.tools.checkBlock.checkBlock;
import static joy.aksd.tools.recoverFromDisk.recoverFDisk;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toString.byteToString;

/** 主程序
 * Created by EnjoyD on 2017/4/20.
 */
public class Main2 {
    static {
        try {
            SHA256x= MessageDigest.getInstance("SHA-256");
            System.out.println("init success");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("init fail");
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * 核心流程：
     * first 得到上一区块的信息
     * second 根据上一块信息开始找符合条件的本次nonce与时间
     * third 找到后添加入主链
     * fouth 广播找到的区块
     * fifth 将存储的块写入硬盘
     * @param args 无
     */
    public static void main(String[] args) {
        //初始化 设置IP

        //获取本地ip
        try{
            setLocalIp();
        }catch (UnknownHostException e) {
            System.out.println("error to get local ip system exit");
            return;
        }
        try {
            recoverFDisk();
        } catch (Exception e) {
            System.out.println("error in recover");
        }

        //region print info
        printInfo();
        //endregion

        //监听线程启动
        new Listener().start();
        //验证线程启动
        new verifyThread().start();

        

        try {
            creatFirstBlock.start();

        } catch (NoSuchAlgorithmException e) {
            System.out.println("error on first");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("get IPList");
            getIPist();
        } catch (IOException e) {
            System.out.println("error in get IPList");
            System.exit(1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        startSyncBlock();
        
        //核心线程启动
        coreWork.execute(new coreProcess());

        //region interupt test
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("shut down the joy.aksd.coreThread.coreProcess");
//        interuptCoreThread();
//
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        interuptReset();
//        coreWork.execute(new joy.aksd.coreThread.coreProcess());
        //endregion
    }

    private static void setLocalIp() throws UnknownHostException {
        localIp= InetAddress.getLocalHost().getHostAddress();
        System.out.println(localIp);
    }

    private static void getIPist() throws IOException, ClassNotFoundException {
        if (ROOTIP.equals(localIp))
            return;
        Socket socket=new Socket(ROOTIP,PORT);
        OutputStream out=socket.getOutputStream();
        InputStream in=socket.getInputStream();

        out.write(GETIPLIST);

        ObjectInputStream inputStream=new ObjectInputStream(in);

        HashSet<String> tem= (HashSet<String>) inputStream.readObject();
        IPList.addAll(tem);

        IPList.forEach(System.out::println);

    }

    private static void startSyncBlock() {
    	//硬盘中没有，从其他节点下载区块后恢复
        if (blocks.size()==0){
        	RequestBroadcast request = new RequestBroadcast();
            try {
                request.start();
            }catch (Exception e){
                System.out.println("request broadcast error ");
            }       	
        }
        //检查当前区块是否正确，错误则删除全部区块，重新从其他节点下载，正确则从当前区块开始下载
        else {
            Block currentblock = blocks.getLast();
            if (checkBlock(currentblock)) {
            	System.out.println("硬盘中区块正确");
                return;
            }
            else{
                System.out.println("block error, delete all blocks and reload……");
                File errorfile = new File(location);
                //错误则删除本地错误内容并新建ip.txt
                errorfile.delete();
                File newfile = new File("blockTest");
                if(!newfile.exists()) {
                	try {
                		newfile.createNewFile();
                	}catch(IOException e) {
                		e.printStackTrace();
                	}
                }
                
                RequestBroadcast request = new RequestBroadcast();
                try {
                   request.start();
                }catch (Exception e){
                System.out.println("request broadcast error ");
                } 
            }                
        }
        //区块下载完毕重新进行区块恢复
        try {
        	File localfile = new File(location);
        	if (localfile!=null) {
        		recoverFDisk();
        	}		
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printInfo(){
        System.out.println("blocks size is "+blocks.size());
        System.out.println("verify2 \n"+ freshRecord.toString());
        System.out.println("verify1 \n"+ effectiveRecord.toString());
    }
//
}
