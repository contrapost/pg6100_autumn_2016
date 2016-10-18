package org.pg6100.utils.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Objects;

public class HttpUtil {

    public static String executeHttpCommand(String host, int port, String request) throws Exception {
        Objects.requireNonNull(host);
        Objects.requireNonNull(request);

        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(request.getBytes());
            socket.shutdownOutput();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String response = "";
            String line = in.readLine();

            while (line != null) {
                response += line + "\n";
                line = in.readLine();
            }
            return response;
        }
    }

    public static String getHeaderBlock(String message){
        Objects.requireNonNull(message);

        String[] lines = message.split("\n");
        String headers = "";
        for(int i=0; i<lines.length; i++){
            if(lines[i].isEmpty()){
                break;
            }
            headers += lines[i] + "\n";
        }
        return headers;
    }

    public static String getBodyBlock(String message){
        Objects.requireNonNull(message);

        String[] lines = message.split("\n");
        String body = "";
        boolean isHeader = true;
        for(int i=0; i<lines.length; i++){
            if(isHeader && lines[i].isEmpty()){
                isHeader = false;
                continue;
            }
            if(!isHeader) {
                body += lines[i] + "\n";
            }
        }
        return body;
    }

    public static String getHeaderValue(String name, String message){
        Objects.requireNonNull(name);
        Objects.requireNonNull(message);

        String[] lines = message.split("\n");

        for(int i=0; i<lines.length; i++){
            String h = lines[i];
            if(h.isEmpty()){
                break;
            }
            if(h.toLowerCase().startsWith(name.toLowerCase())){
                int splitPoint = h.indexOf(':');
                return h.substring(splitPoint+1, h.length()).trim();
            }
        }
        return null;
    }
}
