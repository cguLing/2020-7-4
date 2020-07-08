//package p2p;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class Server {
	public static void main(String[] args) throws IOException
	{
		System.out.println("server start");
		//云服务器对应存储节点信息的列表地址
		String dir="C:/Users/Administrator/Desktop/ListFolder";
		String fileName="List.txt";
		//绑定端口
		DatagramSocket server = new DatagramSocket(8002);
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		int count=0;
		String count_S;
		String client_IP = "";
		int client_port = 0;
		InetAddress client_address = null;
		while(true)//循环监听
		{
			server.receive(packet);
			String requestMessage = new String(packet.getData(), 0, packet.getLength());
			System.out.println(requestMessage);
			if(requestMessage.contains("Request ID")){
			//获取数据包内容为对应请求则判断为新节点的加入
				File file = new File(dir,fileName);
				if(count==0)file.createNewFile();//初始服务器需要构建节点列表
				count++;//记录客户端数量
				count_S=String.valueOf(count);
				client_port = packet.getPort();
				client_address = packet.getAddress();
				String NewClient=count_S+":" + client_address.getHostAddress() + ":" + client_port;
				System.out.println("client "+NewClient);
				client_IP = client_address.getHostAddress() + ":" + client_port;
                sendID(count_S,client_port,client_address,server);//分配ID给新节点
				try { //延时预防丢包问题
					Thread.sleep(1000);//延时1秒
				 } catch (Exception e) { 
					 System.out.println("Got an exception!"); 
				 }
                FileOutputStream fos = null;
				fos = new FileOutputStream(file,true);//构建文件流，追加写
				BufferedReader br = new BufferedReader(new FileReader(dir + File.separatorChar + fileName));
                String line=null;
                while((line=br.readLine())!=null) {//按行读取已存节点信息
					String[] str = line.split(":");//根据":"分割节点信息
					InetAddress address = InetAddress.getByName(str[1]); //获取已存节点的IP
					int port=Integer.parseInt(str[2]);//获取已存节点的Port
					String OldClient = line;
					//异步给新节点与当前已读节点发送对方的的节点信息
					sendIP(OldClient, client_port, client_address, server);
					sendIP(NewClient, port, address,server);
					try { //延时3秒，保证异步
						Thread.sleep(3000);
					 } catch (Exception e) { 
						 System.out.println("Got an exception!"); 
					 }
				}
				//给新节点发送Over表示新节点已加入P2P网络
				sendIP("Over!", client_port, client_address, server);
				System.out.println("NewClient shared successful!");
                OutputStreamWriter os = new OutputStreamWriter(fos, "UTF-8");
                PrintWriter pw=new PrintWriter(os);//文件写入流
                pw.println(NewClient);//将新节点更新到节点列表中
				pw.close();
			}
		}
	}
	private static void sendID(String id, int port, InetAddress address, DatagramSocket server)
	{//发送ID
		byte[] sendBuf = id.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
		try {
			server.send(sendPacket);
			System.out.println("ID assignment successful!");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	private static void sendIP(String NewClient, int port, InetAddress address, DatagramSocket server)
	{//发送节点信息
		byte[] sendBuf = NewClient.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
		try {
			server.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
