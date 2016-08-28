package org.droidplanner.services.android.impl.core.MAVLink.connection;

import android.content.Context;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Provides support for mavlink connection via TCP.
 */
public abstract class TcpConnection extends MavLinkConnection {

    private static final int CONNECTION_TIMEOUT = 20 * 1000; // 20 secs in ms

    private Socket socket;
    private BufferedOutputStream mavOut;
    private BufferedInputStream mavIn;

    private String serverIP;
    private int serverPort;

    protected TcpConnection(Context context) {
        super(context);
    }

    @Override
    public final void openConnection(Bundle connectionExtras) throws IOException {
        getTCPStream(connectionExtras);
        onConnectionOpened();
    }

    @Override
    public final int readDataBlock(byte[] buffer) throws IOException {
        return mavIn.read(buffer);
    }

    @Override
    public final void sendBuffer(byte[] buffer) throws IOException {
        if (mavOut != null) {
            mavOut.write(buffer);
            mavOut.flush();
        }
    }

    @Override
    public final void loadPreferences() {
        serverIP = loadServerIP();
        serverPort = loadServerPort();
    }

    protected abstract int loadServerPort();

    protected abstract String loadServerIP();

    @Override
    public final void closeConnection() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    private void getTCPStream(Bundle extras) throws IOException {
        InetAddress serverAddr = InetAddress.getByName(serverIP);
        socket = new Socket();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network network = extras == null
                ? null
                : (Network) extras.getParcelable(MavLinkConnection.EXTRA_NETWORK);
            if (network != null) {
                network.bindSocket(socket);
            }
        }

        socket.connect(new InetSocketAddress(serverAddr, serverPort), CONNECTION_TIMEOUT);
        mavOut = new BufferedOutputStream((socket.getOutputStream()));
        mavIn = new BufferedInputStream(socket.getInputStream());
    }

    @Override
    public final int getConnectionType() {
        return MavLinkConnectionTypes.MAVLINK_CONNECTION_TCP;
    }
}
