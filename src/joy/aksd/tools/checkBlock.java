package joy.aksd.tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jdk.nashorn.internal.ir.BlockStatement;
import joy.aksd.data.Block;
import joy.aksd.data.Record;

import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.listenAndVerifyThread.Listener.*;
import static joy.aksd.tools.checkRecord.verifyScriptRecord;
import static joy.aksd.data.dataInfo.SHA256x;
import static joy.aksd.data.dataInfo.blocks;
import static joy.aksd.data.dataInfo.identifedRecord;
import static joy.aksd.data.dataInfo.indexBlock;
import static joy.aksd.data.dataInfo.location;
import static joy.aksd.data.dataInfo.merkleTreeLimitation;
import static joy.aksd.data.dataInfo.unPackageRecord;
import static joy.aksd.tools.readRecordFromBlock.readReFromBlock;
import static joy.aksd.tools.GenMerkTree.calMerTree;
import static joy.aksd.tools.toString.byteToString;

public class checkBlock{
			Block currentblock = new Block();
			int blcokNum = currentblock.getBlockByteNum();
			static boolean isright ;
			public static boolean checkBlock(Block currentblock){
				//1.计算上一个区块头hash，检查当前区块内的LastHash与其是否相同
		        Block block=blocks.getLast();
		        byte[] lashHash=block.getLastHash();
		        byte[] merkle=block.getMerkle();
		        byte[] time=block.getTime();
		        byte difficulty=block.getDifficulty();
		        byte[]nonce=block.getNonce();
		        byte[] tem=new byte[lashHash.length+merkle.length+time.length+1+nonce.length];
		        System.arraycopy(lashHash,0,tem,0,lashHash.length);
		        System.arraycopy(merkle,0,tem,lashHash.length,merkle.length);
		        System.arraycopy(time,0,tem,lashHash.length+merkle.length,time.length);
		        tem[lashHash.length+merkle.length+time.length]=difficulty;
		        System.arraycopy(nonce,0,tem,lashHash.length+merkle.length+time.length+1,nonce.length);
		        byte []result=SHA256x.digest(tem);
		      //2.通过Merkle树检查区块内记录的有效性
		      //原本检查并验证单个记录得有效性，更改为：对记录重新生成Merkele树，比较Merkle树数值
            	if(currentblock.getLastHash()==result) {
            		//读出所有的记录放在RecordResult中
                    ArrayDeque<byte []> RecordResult=new ArrayDeque<>(readReFromBlock(block));
                    //得到根节点比较
                    byte[] MerRootNode = calMerTree(RecordResult);
                    if(currentblock.getMerkle() == MerRootNode) {
                    	isright = true;
                    }                                              
                 }  
            	
            	return isright;
         }	
																						
}

			
        /*	public static void main(String[] args) throws IOException {
        		 DataInputStream in=new DataInputStream(new FileInputStream(location));
        		 long index=0;
        		 byte tem[];
        		  while (true){
        	            //读取区块
        	            tem=new byte[2];
        	            in.read(tem);//读取区块长度
        	            int byteCount=byteToInt(tem);
        	            if (byteCount==0)
        	                break;
        	            //建立索引
        	            indexBlock.add(index);
        	            tem=new byte[byteCount];
        	            in.read(tem);
        	            index+=(2+tem.length);
        	            //复原区块
        	            Block block=new Block(tem);
        	            
        	        }
        	}*/




/* //循环读取并验证单个记录
	for(int i=0; i<byteToInt(recordCount); i++) {
		int accuLength = 0;
		byte[] recordLength = new byte[2];
		//累计记录自身长度加存储自身长度的字节
		accuLength += byteToInt(recordLength)+2;
		System.arraycopy(block, blockHeadLen+accuLength, recordLength, 0, 2);
		//创建还原记录
		Record record = new Record();
		byte tem[]=new byte[6];
     System.arraycopy(block,blockHeadLen+2,tem,0,6);
     record.setMac(tem);
     tem=new byte[4];
     System.arraycopy(block,blockHeadLen+2+6,tem,0,4);
     record.setOrderStamp(tem);
     tem=new byte[4];
     System.arraycopy(block,blockHeadLen+2+6+4,tem,0,4);
     record.setTime(tem);
     tem=new byte[32];
     System.arraycopy(block,blockHeadLen+2+6+4+4,tem,0,32);
     record.setLockScript(tem);
     tem=new byte[byteToInt(recordLength)-6-4-4-32];
     System.arraycopy(block,blockHeadLen+2+6+4+4+32,tem,0,tem.length);
     record.setUnLockScript(tem);
     
		//验证每个记录的正确性
     verifyScriptRecord(record);
	}*/