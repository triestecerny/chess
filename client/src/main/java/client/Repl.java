package client;

import java.util.Scanner;

public class Repl {

    private final PreloginUI preloginUI;

    public Repl(int port) {
        var facade = new ServerFacade(port);
        this.preloginUI = new PreloginUI(facade);
    }

    public void run() {
        System.out.println("Welcome to Chess! Type 'Help' to get started.");
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");
            var line = scanner.nextLine().trim().toLowerCase();
            var result = preloginUI.eval(line);
            System.out.println(result);
            if (line.equals("quit")) break;
        }
    }
}