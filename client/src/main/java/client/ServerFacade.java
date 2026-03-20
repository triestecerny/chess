package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }
}