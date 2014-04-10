package cn.heroes.ud;

import java.net.InetAddress;

public class Main2 {

	private static final String HTTP_IN_CONN = "http.proxy.in-conn";
	private static final String HTTP_OUT_CONN = "http.proxy.out-conn";
	private static final String HTTP_CONN_KEEPALIVE = "http.proxy.conn-keepalive";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// String uri = "http://heroes.cn:9000/";
		// HttpGet get = new HttpGet(uri);
		// CloseableHttpClient client = HttpClients.createDefault();
		// CloseableHttpResponse response = client.execute(get);
		// HttpEntity entity = response.getEntity();
		// System.out.println(EntityUtils.toString(entity));

		InetAddress ia = InetAddress.getByName("heroes.cn");
		System.out.println(ia);
	}

}
