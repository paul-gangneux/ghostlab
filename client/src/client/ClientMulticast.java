package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import launcher.Launcher;
import model.ChatScope;
import model.MessageInfo;
import ui.View;

public class ClientMulticast {

	private static MulticastSocket sock = null;
	private static InetAddress multicastAddress = null;
	private static Thread thread = null;

	private static final Object lock = new Object();

	private ClientMulticast() {
	}

	public static void setMulticastSocket(String ip, int port) {
		stopListening();
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

	public static void stopListening() {
		synchronized (lock) {
			if (sock != null) {
				try {
					sock.leaveGroup(multicastAddress);
				} catch (IOException e) {
					e.printStackTrace();
				}
				sock.close();
			}
			sock = null;
		}
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				thread.interrupt();
				e.printStackTrace();
			}
			thread = null;
		}
	}

	private static void listens() {
		if (Launcher.isVerbose())
			System.out.println("Multicast: starts listening");

		byte[] data = new byte[218];
		DatagramPacket paquet = new DatagramPacket(data, data.length);

		while (true) {
			try {
				sock.receive(paquet);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			String keyword = new String(paquet.getData(), 0, 5, StandardCharsets.UTF_8);
			// System.out.println(new String(paquet.getData(), 0, paquet.getLength()));

			switch (keyword) {
				case "GHOST": { // [GHOST xxx yyy+++]
					String sx = new String(paquet.getData(), 6, 3, StandardCharsets.UTF_8);
					String sy = new String(paquet.getData(), 10, 3, StandardCharsets.UTF_8);
					int x = Integer.parseInt(sx);
					int y = Integer.parseInt(sy);
					View.getInstance().ghostMoved(x, y);
					break;
				}
				case "SCORE": { // [SCORE username pppp xxx yyy+++]
					String id = ClientUdp.getPseudo(paquet.getData());
					String sp = new String(paquet.getData(), 15, 4, StandardCharsets.UTF_8);
					String sx = new String(paquet.getData(), 20, 3, StandardCharsets.UTF_8);
					String sy = new String(paquet.getData(), 24, 3, StandardCharsets.UTF_8);
					int p = Integer.parseInt(sp);
					int x = Integer.parseInt(sx);
					int y = Integer.parseInt(sy);
					View.getInstance().ghostCaptured(id, p, x, y);
					break;
				}
				case "MESSA": { // [MESSA username mess+++]
					String name = ClientUdp.getPseudo(paquet.getData());
					String st = new String(paquet.getData(), 15, paquet.getLength() - 15);
					Scanner sc = new Scanner(st);
					String msg;
					try {
						msg = sc.useDelimiter("\\+\\+\\+").next();
					} catch (Exception e) {
						System.out.println("MESSA: bad incoming string");
						sc.close();
						break;
					}
					sc.close();
					MessageInfo mi = new MessageInfo(ChatScope.GLOBAL_MSG, name, msg);
					View.getInstance().incomingMessage(mi);
					break;
				}
				case "ENDGA": { // [ENDGA username pppp+++]
					String id = ClientUdp.getPseudo(paquet.getData());
					String sp = new String(paquet.getData(), 15, 4, StandardCharsets.UTF_8);
					int p = Integer.parseInt(sp);
					View.getInstance().endGameAndShowWinner(id, p);
					break;
				}

				default:
					System.out.println("Multicast: unknown message");
					break;
			}
		}

		synchronized (lock) {
			sock.close();
			sock = null;
		}

		if (Launcher.isVerbose())
			System.out.println("Multicast: stops listening");
	}
}
