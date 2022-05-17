package client;

import java.io.*;
import java.net.*;

public class ClientMulticast {

	private static MulticastSocket sock = null;
	private static InetAddress multicastAddress = null;
	private static Thread thread = null;

	private ClientMulticast() {}

	public static void setMulticastSocket(String ip, int port) {
		if (sock != null) {
			try {
				sock.leaveGroup(multicastAddress);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sock.close();
		}
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				thread.interrupt();
				e.printStackTrace();
			}
		}
		sock = null;
		thread = null;
		multicastAddress = null;
		try {
			sock = new MulticastSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			multicastAddress = InetAddress.getByName(ip);
			sock.joinGroup(multicastAddress);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void startListening() {
		if (thread != null) {
			System.out.println("Multicast: Already listening, aborting.");
			return;
		}
		if (sock == null || multicastAddress == null) {
			System.out.println("Multicast: Attributes not initialized, aborting.");
			return;
		}
		thread = new Thread(ClientMulticast::listens);
		thread.start();
	}

	private static void listens() {
		System.out.println("Multicast: starts listening");
		// TODO
	}
}
