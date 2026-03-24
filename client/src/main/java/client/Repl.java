package client;

import java.util.Scanner;

public class Repl {

    private final ServerFacade facade;
    private final PreloginUI preloginUI;
    private PostloginUI postloginUI;

    public Repl(int port) {
        this.facade = new ServerFacade(port);
        this.preloginUI = new PreloginUI(facade);
    }

    public void run() {
        System.out.println("Welcome to Chess! Type 'help' to get started.");
        var scanner = new Scanner(System.in);
        while (true) {
            if (preloginUI.isLoggedIn()) {
                if (postloginUI == null) {
                    postloginUI = new PostloginUI(facade, preloginUI.getAuthToken());
                }
                System.out.print("[LOGGED_IN] >>> ");
                var line = scanner.nextLine().trim().toLowerCase();
                var result = postloginUI.eval(line);
                System.out.println(result);
                if (postloginUI.isLoggedOut()) {
                    preloginUI.clearAuth();
                    postloginUI = null;
                }
            } else {
                System.out.print("[LOGGED_OUT] >>> ");
                var line = scanner.nextLine().trim().toLowerCase();
                var result = preloginUI.eval(line);
                System.out.println(result);
                if (line.equals("quit")){
                    break;
                }
            }
        }
    }
}