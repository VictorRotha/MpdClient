package de.nif.client;

import de.nif.data.AckException;
import de.nif.data.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Connection {

    public static int TIMEOUT = 5000;

    private boolean connected;
    protected String server;
    protected int port;
    protected InetSocketAddress address;
    protected String version;
    private ConnectionListener listener;

    public interface ConnectionListener {
        void onConnectedChanged(boolean connected, String message, Exception e);

    }

    private InetSocketAddress createAddress() throws UnknownHostException, IllegalArgumentException {

        System.out.println("createAddress()" + server + " " + port);

        InetAddress iAddress = InetAddress.getByName(server);
        return new InetSocketAddress(iAddress, port);

    }

    private String ping(InetSocketAddress address) throws IOException {

        System.out.println("ping");

        String result = null;

        Socket socket = new Socket();
        socket.connect(address, TIMEOUT);

        OutputStream os = socket.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        os.write("command_list_begin\nping\ncommand_list_end\n".getBytes());
        String line;
        while ((line = br.readLine()) != null) {
            if (line.equals("OK")) break;
            else if (line.startsWith("OK")) {
                result = line.substring(7);
            } else if (line.startsWith("ACK")) {
                throw new AckException(line);
            }
        }

        if (!socket.isClosed())
           socket.close();

        return result;


    }

    public Result<String> connect(String host, int port) {

        server = host;
        this.port = port;

        System.out.println("Connection.connect(): (Try) Connect to " + server + ":" + port + " ...");

        Result<String> result = new Result<>();

        try {

            address = createAddress();

            version = ping(address);

            setConnected(true, version, null);
            result.type = Result.ResultType.OK;
            result.message = version;

        } catch (IOException | IllegalArgumentException e) {
            setConnected(false, null, e);
            result.type = Result.ResultType.ERROR;
            result.exception = e;
            e.printStackTrace();

        }

        return result;
    }


    public void setConnected(boolean connected, String message, Exception e) {
        this.connected = connected;
        if (listener != null) {
            listener.onConnectedChanged(connected, message, e);
        }

    }

    public boolean isConnected() {
        return connected;
    }

    public void setListener(ConnectionListener listener) {
        this.listener = listener;
    }
}
