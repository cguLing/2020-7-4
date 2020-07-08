import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
 
/**
 * 内网客户端，要进行穿透的内网服务
 */
public class Client {
	
	//输入scanner
	private Scanner scanner = new Scanner(System.in);
	//是否等待输入
	private boolean isWaitInput = true;
	//首次与外网主机通信的连接
	private Socket socket;
	//首次与外网主机通信的本地端口
	private int localPort;
 
	private PrintWriter pw;
	private BufferedReader br;
	
	public static void main(String[] args) {
		new Client().start();
	}
	
	public void start() {
		try {
			// 新建一个socket通道
			socket = new Socket();
			// 设置reuseAddress为true
			socket.setReuseAddress(true);
 
			//TODO在此输入外网地址和端口
			String ip = "120.53.16.130";
			int port = 8002;
			socket.connect(new InetSocketAddress(ip, port));
			
			//首次与外网服务器通信的端口
			//这就意味着我们内网服务要与其他内网主机通信，就可以利用这个通道
			localPort = socket.getLocalPort();
 
			System.out.println("本地端口：" + localPort);
			System.out.println("请输入命令 notwait等待穿透，或者输入conn进行穿透");
 
			pw = new PrintWriter(socket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			try {
				while (true) {
					if(process()) {
						break;
					}
				}
			} finally {
				// 关闭资源
				try {
					if(pw != null) {
						pw.close();
					}
					if (br != null) {
						br.close();
					}
					if (socket != null) {
						socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	/*
	 * 处理与服务器连接的交互，返回是否退出
	 */
	private boolean process() throws IOException {
		
		String in = null;
		
		if (isWaitInput) {
			//把输入的命令发往服务端
			in = scanner.next();
			pw.write(in + "\n");
			
			//调用flush()方法将缓冲输出
			pw.flush();
			
			if ("notwait".equals(in)) {
				isWaitInput = false;
			}
		}
		//获取服务器的响应信息
		String info = br.readLine();
		if(info != null) {
			System.out.println("我是客户端，服务器说：" + info);
		}
		//处理本地命令
		processLocalCommand(in);
		
		//处理服务器命令
		processRemoteCommand(info);
		
		return "exit".equals(in);
	}
 
	private void processRemoteCommand(String info) throws IOException {
		if (info != null && info.startsWith("autoConn_")) {
			
			System.out.println("服务器端返回的打洞命令，自动连接目标");
			
			String[] infos = info.split("_");
			//目标外网地址
			String ip = infos[1];
			//目标外网端口
			String port = infos[2];
			
			doPenetration(ip, Integer.parseInt(port));
		}
	}
 
	private void processLocalCommand(String in) throws IOException {
		if ("conn".equals(in)) {
			System.out.println("请输入要连接的目标外网ip:");
			String ip = scanner.next();
			System.out.println("请输入要连接的目标外网端口:");
			int port = scanner.nextInt();
 
			pw.write("newConn_" + ip + "_" + port + "\n");
			pw.flush();
 
			doPenetration(ip, port);
			
			isWaitInput = false;
		}
	}
 
	/*
	 * 对目标服务器进行穿透
	 */
	private void doPenetration(String ip, int port) {
		try {
			//异步对目标发起连接
			new Thread() {
				public void run() {
					try {
 
						Socket newsocket = new Socket();
 
						newsocket.setReuseAddress(true);
						newsocket.bind(new InetSocketAddress(
								InetAddress.getLocalHost().getHostAddress(), localPort));
 
						System.out.println("connect to " + new InetSocketAddress(ip, port));
						
						for(int i=0;i<5;i++){
							newsocket.connect(new InetSocketAddress(ip, port));
						}
						newsocket.connect(new InetSocketAddress(ip, port));
						
						System.out.println("connect success");
 
						BufferedReader b = new BufferedReader(
								new InputStreamReader(newsocket.getInputStream()));
						PrintWriter p = new PrintWriter(newsocket.getOutputStream());
						
						while (true) {
							
							p.write("hello " + System.currentTimeMillis() + "\n");
							p.flush();
							
							String message = b.readLine();
							
							System.out.println(message);
							
							pw.write(message + "\n");
							pw.flush();
							
							if("exit".equals(message)) {
								break;
							}
							
							Thread.sleep(1000l);
						}
						
						b.close();
						p.close();
						newsocket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
			
//			//监听本地端口
//			ServerSocket serverSocket = new ServerSocket();
//			serverSocket.setReuseAddress(true);
//			serverSocket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), localPort));
//
//			// 记录客户端的数量
//			System.out.println("******开始监听端口：" + localPort);
//			// 循环监听等待客户端的连接
//			// 调用accept()方法开始监听，等待客户端的连接
//			Socket st = serverSocket.accept();
//			
//			System.out.println("成功了，哈哈，新的连接：" + st.getInetAddress().getHostAddress() + ":" + st.getPort());
//			
//			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("监听端口 " + socket.getLocalPort() + " 出错");
		}
	}
}