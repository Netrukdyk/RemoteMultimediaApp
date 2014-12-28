package com.remotemultimedia;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityMain extends Activity implements OnClickListener, OnLongClickListener {

	ImageButton volUp, volDown, mute, prev, play, next, stop, media, zoom;
	ImageView serverStatus;
	Server server;
	Handler serverHandler;

	SharedPreferences preferences;
	TextView status, debug;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		serverStatus = (ImageView) findViewById(R.id.server_status);
		status = (TextView) findViewById(R.id.status);
		debug = (TextView) findViewById(R.id.debug);

		volUp = (ImageButton) findViewById(R.id.plus);
		volDown = (ImageButton) findViewById(R.id.minus);
		mute = (ImageButton) findViewById(R.id.mute);
		prev = (ImageButton) findViewById(R.id.prev);
		play = (ImageButton) findViewById(R.id.play);
		next = (ImageButton) findViewById(R.id.next);
		stop = (ImageButton) findViewById(R.id.stop);
		media = (ImageButton) findViewById(R.id.media);
		zoom = (ImageButton) findViewById(R.id.zoom);

		volUp.setOnClickListener(this);
		volUp.setOnLongClickListener(this);
		volDown.setOnClickListener(this);
		mute.setOnClickListener(this);
		prev.setOnClickListener(this);
		play.setOnClickListener(this);
		next.setOnClickListener(this);
		stop.setOnClickListener(this);
		media.setOnClickListener(this);
		zoom.setOnClickListener(this);

		startServer();
	}

	private void startServer() {
		server = new Server(uiHandler, getBroadcastIpAddress());
		server.start();
	}

	private void killServer() {
		if (server != null) {
			server.kill();
			server = null;
		}
	}

	private void reconnect() {
		killServer();
		startServer();
	}

	private InetAddress getBroadcastIpAddress() {
		try {
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (wifi == null || !wifi.isWifiEnabled()) {
				setServerStatus(0, "Wifi disabled!");
			} else if (wifi.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED) {
				setServerStatus(0, "Wifi not connected!");
			} else {
				DhcpInfo dhcp = wifi.getDhcpInfo();
				int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
				byte[] quads = new byte[4];
				for (int k = 0; k < 4; k++)
					quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

				InetAddress ip = InetAddress.getByAddress(quads);
				Log.v("BroadcastIP", ip.getHostAddress().toString());
				return ip;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Apdoroja þinutes, gautas ið serverio
	@SuppressLint("HandlerLeak")
	public Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			C.Type type = C.Type.values()[msg.what];

			switch (type) {
			case STATUS:
				if (msg.arg1 == 1)
					setServerStatus(1);
				else
					setServerStatus(0);
				break;
			case INFO:
				debug.setText(msg.getData().getString("Server"));
				break;
			case OTHER:
				if (msg.getData().get("Server") == "Disconnected")
					reconnect();
				break;
			}
		};
	};

	private void sendToServer(String text) {
		if (server != null && server.getStatus() != 0) {
			serverHandler = server.getHandler();
			Message msgObj = serverHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("CMD", text);
			msgObj.setData(b);
			serverHandler.sendMessage(msgObj);
		} else
			setServerStatus(0);

	}

	private void setServerStatus(int state) {
		switch (state) {
		case 0:
			serverStatus.setImageResource(R.drawable.status_red);
			status.setText("Not connected");
			break;
		case 1:
			serverStatus.setImageResource(R.drawable.status_green);
			status.setText("Connected");
			break;
		}
	}

	private void setServerStatus(int state, String reason) {
		switch (state) {
		case 0:
			serverStatus.setImageResource(R.drawable.status_red);
			break;
		case 1:
			serverStatus.setImageResource(R.drawable.status_green);
			break;
		}
		status.setText(reason);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			// nustatytiIP();
			Intent intent = new Intent(this, ActivityAbout.class);
			startActivity(intent);
			return true;
		case R.id.action_find:
			reconnect();
			return true;
		case R.id.action_sleep:
			sendToServer("SLEEP");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		String msg = "";
		switch (v.getId()) {
		case R.id.prev:
			msg = "PREV";
			break;
		case R.id.play:
			msg = "PLAY";
			break;
		case R.id.next:
			msg = "NEXT";
			break;
		case R.id.plus:
			msg = "VOLU";
			break;
		case R.id.minus:
			msg = "VOLD";
			break;
		case R.id.mute:
			msg = "MUTE";
			break;
		case R.id.stop:
			msg = "STOP";
			break;
		case R.id.media:
			msg = "MEDIA";
			break;
		case R.id.zoom:
			msg = "ZOOM";
			break;
		}
		sendToServer(msg);
	}

	@Override
	public boolean onLongClick(View v) {
		String msg = "";
		switch (v.getId()) {
		case R.id.plus:
			msg = "VOLU";
			break;
		case R.id.minus:
			msg = "VOLD";
			break;
		}

		for(int i=1;i<=10;i++)
			sendToServer(msg);
		
		return true;
	}

}
