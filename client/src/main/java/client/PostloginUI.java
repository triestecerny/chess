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
                - list - list all games
                - play <ID> <WHITE|BLACK> - join a game
                - observe <ID> - observe a game
                - logout - logout
                - help - with possible commands
                """;
    }
}