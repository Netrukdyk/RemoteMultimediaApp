package com.remotemultimedia;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.remotemultimedia.C.Type;

class Server extends Thread {

	private InetAddress broadcastIp;

	private OutputStream out;
	private PrintWriter output;
	private Socket s = null;
	private int server_state = 0;
	// Serverio Handleris, apdoroja þinutes ið UI
	@SuppressLint("HandlerLeak")
	private Handler serverHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			String cmd = msg.getData().getString("CMD");
			Log.v("Sending", cmd);
			output.println(cmd);
			// output.flush();
			if (output.checkError()) {
				Log.v("Socket", "Error");
				setStatus(0);
				disconnect();
				sendToUI(C.Type.OTHER,"Disconnected");
			}
		}
	};
	private String serverIP = C.IP;
	private int serverPORT = C.PORT;

	private Handler uiHandler;

	public Server(Handler handler, InetAddress ip) {
		super("Worker");
		Log.v("Server", "Create");
		this.uiHandler = handler;
		this.broadcastIp = ip;
	}

	private void connect() {
		do {
			try {
				Log.v("Server", serverIP + " connecting...");
				s = new Socket();
				s.connect(new InetSocketAddress(serverIP, serverPORT), 1000);

				if (s.isConnected()) {
					setStatus(1);
					Log.v("Server", "Socket connected");
					sendToUI(C.Type.INFO, s.getRemoteSocketAddress().toString());
					out = s.getOutputStream();
					output = new PrintWriter(out, true);
				}

			} catch (SocketTimeoutException e2) {
				Log.v("Server", "Socket timeout " + serverIP + ":" + serverPORT);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} while (getStatus() != 1);
	}

	private void disconnect() {
		if (getStatus() != 0) {
			try {
				if (output != null)
					output.close();
				if (out != null)
					out.close();
				if (s != null)
					s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setStatus(0);
		}
	}

	private String findServer(InetAddress broadcastIp) {
		Boolean found = false;
		int key = (int) Math.round(Math.random() * 10000);
		int newkey = key+1234;
		String data = "RMS_"+key;
		String ip = null;

		byte[] outdata = Base64.encode(data.getBytes(), Base64.DEFAULT);
		byte[] buf = new byte[64];
		DatagramSocket socket = null;
		DatagramPacket outPacket = new DatagramPacket(outdata, outdata.length, broadcastIp, serverPORT);
		DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
		try {
			socket = new DatagramSocket(serverPORT);
			socket.setBroadcast(true);
			do {
				
				socket.send(outPacket);
				//Log.v("UDP",new String(outPacket.getData(),Charset.forName("UTF8")));
				socket.receive(inPacket);
				String info = new String(Base64.decode(inPacket.getData(), Base64.DEFAULT), Charset.forName("UTF8"));
				// String source = inPacket.getAddress().getHostAddress().toString();
				// Log.v("UDP from " + source, info);
				StringTokenizer tokens = new StringTokenizer(info, "#");
				if (tokens.nextToken().contains("RMS_"+newkey)) {
					found = true;
					ip = tokens.nextToken();
				} else
					Thread.sleep(1000);
			} while (!found);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (socket != null)
			socket.close();
		return ip;
	}

	// getter for local Handler
	public Handler getHandler() {
		return this.serverHandler;
	}

	public int getStatus() {
		return this.server_state;
	}

	public void kill() {
		disconnect();
		Log.v("Server", "Killed");
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void run() {
		Log.v("Server", "Run");
		serverIP = findServer(broadcastIp);		
		sendStatusToUI();
		connect();
	} // End of Run

	// Suformuoja þinutæ ir iðsiunèia UI
	private void sendStatusToUI() {
		Message msg = new Message();
		msg.what = 0;
		msg.arg1 = server_state;
		uiHandler.sendMessage(msg);
	}

	private void sendToUI(Type type, String message) {
		Bundle data = new Bundle();
		data.putString("Server", message);
		Message msg = new Message();
		msg.what = type.ordinal(); // MsgType
		msg.setData(data);
		uiHandler.sendMessage(msg);
	}

	public void setIp(String ip) {
		this.serverIP = ip;
	}

	public void setStatus(int state) {
		this.server_state = state;
		sendStatusToUI();
	}

} // End of Server
