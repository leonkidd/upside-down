package cn.heroes.ud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		final HttpHost host = new HttpHost("localhost", 80);
		ServerSocket ss = new ServerSocket(80);
		Socket socket = ss.accept();
		System.out.print(socket);
	}

}
