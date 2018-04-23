package user;

import joy.aksd.data.Record;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static joy.aksd.data.protocolInfo.SELFQUERY;
import static joy.aksd.tools.readAndPrintData.printRecord;
import static joy.aksd.tools.toByte.hexStringToByteArray;
import static joy.aksd.tools.toInt.byteToInt;

/**
 * Created by EnjoyD on 2017/5/15.
 */
public class querySelfRecord {
	
	 private static String ip;
	    private static int port=49999;
	    static {
	        Scanner sc= null;
	        try {
	            sc = new Scanner(new File("./ip.txt"));
	        } catch (FileNotFoundException e) {
	            System.out.println("error in set ip");
	        }
	        String tem=sc.nextLine().trim();
	        sc.close();
	        ip=tem;
	        System.out.println(ip);
	    }

    public void querySelfRecord(){
        //get lockScript
        byte[] lockScript = null;
        try {
            lockScript=getLockScript();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //start query
        try {
            startQuery(lockScript);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startQuery(byte[] lockScript) throws IOException {
        //查询个人记录
    	Socket socket=new Socket(ip,port);
        InputStream in=socket.getInputStream();
        OutputStream out=socket.getOutputStream();
        out.write(SELFQUERY);
        out.write(lockScript);
        try {
            while (true) {
                byte[] tem = new byte[2];
                in.read(tem);
                tem = new byte[byteToInt(tem)];
                in.read(tem);
                Record record=new Record(tem);
                printRecord(record);
            }
        }catch (Exception e){
            ;
        }
        return;
    }

    private byte[] getLockScript() throws IOException, NoSuchAlgorithmException {
        DataInputStream in = new DataInputStream(new FileInputStream("./key"));
        in.readUTF();
        String second = in.readUTF();
        String third = in.readUTF();
        in.close();
        byte []x=hexStringToByteArray(second);
        byte []y=hexStringToByteArray(third);
        byte[] result = new byte[x.length + y.length];
        System.arraycopy(x, 0, result, 0, x.length);
        System.arraycopy(y, 0, result, x.length, y.length);
        return MessageDigest.getInstance("SHA-256").digest(result);
    }

    public static void main(String[] args) {
        new querySelfRecord().querySelfRecord();
    }


}
