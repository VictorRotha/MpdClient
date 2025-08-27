package de.nif.client;

import de.nif.data.AckException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;



public class Channel {

    private final String LOGTAG = getClass().getSimpleName().toUpperCase();

    private Socket socket;
    private InputStream is;
    private OutputStream os;

    private final Connection connection;


    private final boolean hasTimeOut;

    public Channel(Connection _connection) {

        this(_connection, true);
    }

    public Channel(Connection _connection, boolean hasTimeOut) {

        this.hasTimeOut = hasTimeOut;
        this.connection = _connection;

    }

    public void initSocket() throws IOException {

        socket = new Socket();
        if (hasTimeOut)
            socket.connect(connection.address, Connection.TIMEOUT);
        else
            socket.connect(connection.address);

        os = socket.getOutputStream();
        is = socket.getInputStream();

    }

    public void send(String... commands) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("command_list_begin\n");
        for (String command : commands) {
            sb.append(command);
            sb.append("\n");
        }
        sb.append("command_list_end\n");

        if (os != null)
            os.write(sb.toString().getBytes());

//        System.out.println(LOGTAG + " Send:\n" + sb);

    }

    public ArrayList<String> receive() throws IOException {
        ArrayList<String> result = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {

            result.add(line);
            if (line.equals("OK")) break;
            if (line.startsWith("ACK")) {
                System.out.println(LOGTAG + "MPD Error: " + result);
                throw new AckException(line, result);
            }

        }

//        System.out.println(LOGTAG + "Receive:\n" + result);

        return result;

    }


    public void close() {

        System.out.println("channel close()");
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public Socket getSocket() {
        return socket;
    }

}
