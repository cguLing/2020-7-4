import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;

public class client {
    public static void main(String[] args){
		System.out.println("Client Start");
		server server_start = new server();
		server_start.start();
	}
}
class server extends Thread{
	String content ="";
	String Name = null;
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

			//与客户端进行UDP打洞
            while(true){
                client.receive(packet);
                content = new String(packet.getData(), 0, packet.getLength());
                if(content.contains(":"))break;
            }
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
			System.out.println("成功打洞！"); 
			client.receive(packet);//清空上一个包
			//创建Scanner对象
        	//System.in表示标准化输出，也就是键盘输出
			Scanner sc = new Scanner(System.in);
			sc.useDelimiter("\n");//获取包含空格的字符串
			while(true){
				if(sc.hasNext()){//利用hasNextXXX()判断是否还有下一输入项
					String Message = sc.next();//利用nextXXX()方法输出内容
					sendBuf = Message.getBytes();
					SocketAddress To_address = new InetSocketAddress(Client_address, Client_port);
					DatagramPacket sendMessage = new DatagramPacket(sendBuf, sendBuf.length, To_address);
					client.send(sendMessage);
				}
				client.receive(packet);
            	String R_Message = new String(packet.getData(), 0, packet.getLength());
            	System.out.println("接收消息："+R_Message);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}