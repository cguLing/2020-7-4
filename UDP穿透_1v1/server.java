import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class server {
	public static void main(String[] args) throws IOException
	{
		System.out.println("server start");
		//绑定端口
		DatagramSocket server = new DatagramSocket(8002);
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		int count=0;
		String count_S;
        String clientA_IP = "",clientB_IP="";
        String ClientA="",ClientB="";
		int clientA_port = 0,clientB_port=0;
		InetAddress clientA_address = null,clientB_address=null;
		while(true)//循环监听
		{
			server.receive(packet);
			String requestMessage = new String(packet.getData(), 0, packet.getLength());
			System.out.println(requestMessage);
			if(requestMessage.contains("Request ID")){
                count++;//记录客户端数量
                count_S=String.valueOf(count);
                if(count%2!=0){
                    clientA_port = packet.getPort();
                    clientA_address = packet.getAddress();
                    ClientA=count_S+":" + clientA_address.getHostAddress() + ":" + clientA_port;
                    System.out.println("client "+ClientA);
                    clientA_IP = clientA_address.getHostAddress() + ":" + clientA_port;
                    sendID(count_S,clientA_port,clientA_address,server);//分配ID
                }
                else{
                    clientB_port = packet.getPort();
                    clientB_address = packet.getAddress();
                    ClientB=count_S+":" + clientB_address.getHostAddress() + ":" + clientB_port;
                    System.out.println("client "+ClientB);
                    clientB_IP = clientB_address.getHostAddress() + ":" + clientB_port;
                    sendID(count_S,clientB_port,clientB_address,server);//分配ID
                    try { //延时预防丢包问题
                        Thread.sleep(1000);//延时1秒
                     } catch (Exception e) { 
                         System.out.println("Got an exception!"); 
                     }
                    //异步给新节点与当前已读节点发送对方的的节点信息
                    sendIP(ClientA, clientB_port, clientB_address, server);
                    sendIP(ClientB, clientA_port, clientA_address, server);
                }
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
	private static void sendIP(String Client, int port, InetAddress address, DatagramSocket server)
	{//发送节点信息
		byte[] sendBuf = Client.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
		try {
			server.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
