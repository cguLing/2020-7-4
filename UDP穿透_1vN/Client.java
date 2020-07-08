//package p2p;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
	public static void main(String[] args){
		System.out.println("Client Start");
		server server_start = new server();
		server_start.start();
	}
}
class server extends Thread{
	String content ="";
	String NewClient ="";
	String Name = null;
	String dir = "/Users/ling/Desktop";/** 要改*/
	String FileName = "List.txt";
	File file = new File(dir,FileName);
	public void run(){
		try {
			//发送请求
			SocketAddress target = new InetSocketAddress("120.53.16.130", 8002);
			DatagramSocket client = new DatagramSocket();
			String message = "Request ID";
			byte[] sendBuf = message.getBytes();
			DatagramPacket pack = new DatagramPacket(sendBuf, sendBuf.length, target);
			client.send(pack);
			//获取分配ID
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			client.receive(packet);
			String ID = new String(packet.getData(), 0, packet.getLength());
			Name = ID;
			System.out.println("本机被分配ID为："+Name);

			//与全部客户端进行UDP打洞
			String flag="0";
			while(true){
				while(true){
					client.receive(packet);
					content = new String(packet.getData(), 0, packet.getLength());
					if(content.contains(":"))break;
					else if(content.equals("Over!")) {
						flag="1";
						break;
					}
				}
				if(flag.equals("1"))break;
				else{
					String[] str = content.split(":");//字符串转行为字符串数组
					String Client_name = str[0];
					String Client_address = str[1];
					int Client_port = Integer.parseInt(str[2]);
					String requestClient = "Hello, client"+Client_name+". I'm client"+Name;
					sendBuf = requestClient.getBytes();
					SocketAddress ToClient_address = new InetSocketAddress(Client_address, Client_port);
					DatagramPacket sendPacket0 = new DatagramPacket(sendBuf, sendBuf.length, ToClient_address);
					client.send(sendPacket0);
					client.receive(packet);
					String Client_Message = new String(packet.getData(), 0, packet.getLength());
					System.out.println("接收消息："+Client_Message);
					client.send(sendPacket0);
					try {
						FileOutputStream fos = null;
						if(!file.exists()){
							file.createNewFile();//如果文件不存在，就创建该文件
							fos = new FileOutputStream(file);//首次写入获取
						}else{
							//如果文件已存在，那么就在文件末尾追加写入
							fos = new FileOutputStream(file,true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
						}
						OutputStreamWriter os = new OutputStreamWriter(fos, "UTF-8");//指定以UTF-8格式写入文件
						PrintWriter pw=new PrintWriter(os);
						pw.println(content);//每输入一个数据，自动换行，便于我们每一行每一行地进行读取
						pw.close();
						os.close();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}   
				}
			}
			System.out.println("成功接入P2P网络！"); 
			
			//循环监听是否有新节点加入并进行UDP打洞
			while (true) {
				try {
					Thread.sleep(1); 
				} catch (Exception e) {
					e.printStackTrace();
				}
				byte[] buf0 = new byte[1024];
				DatagramPacket packet0 = new DatagramPacket(buf0, buf0.length);
				while(true){
					client.receive(packet0);
					NewClient = new String(packet0.getData(), 0, packet0.getLength());
					if(NewClient.contains(":"))break;
				}
				String[] NewInfo = NewClient.split(":");
				String NewClient_name = NewInfo[0];
				String NewClient_address = NewInfo[1];
				int NewClient_port = Integer.parseInt(NewInfo[2]);
				//应答
				String requestNewClient = "Welcome, client"+NewClient_name+". I'm client"+Name;
				sendBuf = requestNewClient.getBytes();
				SocketAddress ToNewClient_address = new InetSocketAddress(NewClient_address, NewClient_port);
				DatagramPacket sendPacket1 = new DatagramPacket(sendBuf, sendBuf.length, ToNewClient_address);
				client.send(sendPacket1);//丢失
				//接收回复
				client.receive(packet);
				String NewClient_Message = new String(packet.getData(), 0, packet.getLength());
				System.out.println("接收消息："+NewClient_Message);
				client.send(sendPacket1);
				System.out.println("与Client"+NewClient_name+"NAT穿透成功");
				try {
					FileOutputStream fos = null;
					fos = new FileOutputStream(file,true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
					OutputStreamWriter os = new OutputStreamWriter(fos, "UTF-8");//指定以UTF-8格式写入文件
					PrintWriter pw=new PrintWriter(os);
					pw.println(NewClient);//每输入一个数据，自动换行，便于我们每一行每一行地进行读取
					pw.close();
					os.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}   
			}

		} catch (IOException e) {
			System.out.println(e);
		}
	}
}