// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.http.net;

import jodd.http.HttpException;
import jodd.http.ProxyInfo;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket factory for SOCKS4 proxy. This proxy does not do password authentication.
 *
 * See: http://www.openssh.com/txt/socks5.protocol for more details.
 */
public class Socks4ProxySocketFactory extends SocketFactory {

	private final ProxyInfo proxy;

	public Socks4ProxySocketFactory(ProxyInfo proxy) {
		this.proxy = proxy;
	}

	public Socket createSocket(String host, int port) throws IOException {
		return createSocks4ProxySocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		return createSocks4ProxySocket(host, port);
	}

	public Socket createSocket(InetAddress host, int port) throws IOException {
		return createSocks4ProxySocket(host.getHostAddress(), port);
	}

	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return createSocks4ProxySocket(address.getHostAddress(), port);
	}

	/**
	 * Connects to the SOCKS4 proxy and returns proxified socket.
	 */
	private Socket createSocks4ProxySocket(String host, int port) {
		Socket socket = null;
		String proxyHost = proxy.getProxyAddress();
		int proxyPort = proxy.getProxyPort();
		String user = proxy.getProxyUsername();

		try {
			socket = new Socket(proxyHost, proxyPort);
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			socket.setTcpNoDelay(true);

			byte[] buf = new byte[1024];

			// 1) CONNECT

			int index = 0;
			buf[index++] = 4;
			buf[index++] = 1;

			buf[index++] = (byte) (port >>> 8);
			buf[index++] = (byte) (port & 0xff);

			InetAddress addr = InetAddress.getByName(host);
			byte[] byteAddress = addr.getAddress();
			for (int i = 0; i < byteAddress.length; i++) {
				buf[index++] = byteAddress[i];
			}

			if (user != null) {
				System.arraycopy(user.getBytes(), 0, buf, index, user.length());
				index += user.length();
			}
			buf[index++] = 0;
			out.write(buf, 0, index);

			// 2) RESPONSE

			int len = 6;
			int s = 0;
			while (s < len) {
				int i = in.read(buf, s, len - s);
				if (i <= 0) {
					throw new HttpException(ProxyInfo.ProxyType.SOCKS4, "stream is closed");
				}
				s += i;
			}
			if (buf[0] != 0) {
				throw new HttpException(ProxyInfo.ProxyType.SOCKS4, "proxy returned VN " + buf[0]);
			}
			if (buf[1] != 90) {
				try {
					socket.close();
				} catch (Exception ignore) {
				}
				throw new HttpException(ProxyInfo.ProxyType.SOCKS4, "proxy returned CD " + buf[1]);
			}

			byte[] temp = new byte[2];
			in.read(temp, 0, 2);
			return socket;
		} catch (RuntimeException rtex) {
			closeSocket(socket);
			throw rtex;
		} catch (Exception ex) {
			closeSocket(socket);
			throw new HttpException(ProxyInfo.ProxyType.SOCKS4, ex.toString(), ex);
		}
	}

	/**
	 * Closes socket silently.
	 */
	private void closeSocket(Socket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception ignore) {
		}
	}
}