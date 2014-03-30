package cn.heroes.ud;

import java.net.ServerSocket;


public class Gateway {
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(80);
		ss.accept();
	}
}