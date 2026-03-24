package client;

public class PostloginUI {

    private final ServerFacade facade;
    private final String authToken;

    public PostloginUI(ServerFacade facade, String authToken) {
        this.facade = facade;
        this.authToken = authToken;
    }

    public String eval(String input) {
        var tokens = input.split(" ");
        var cmd = tokens[0];
        return switch (cmd) {
            case "help" -> help();
            case "logout" -> "Logged out!";
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String help() {
        return """
                - create <NAME> - create a game
                - list - games
                - play <ID> <WHITE|BLACK> - a game
                - observe <ID> - a game
                - logout - when you are done
                - help - with possible commands
                """;
    }
    private boolean loggedOut = false;

    public boolean isLoggedOut() {
        return loggedOut;
    }
}