package client;

public class PreloginUI {

    private final ServerFacade facade;
    private String authToken = null;

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public String eval(String input) {
        var tokens = input.split(" ");
        var cmd = tokens[0].toLowerCase();
        return switch (cmd) {
            case "help" -> help();
            case "quit" -> "Goodbye!";
            case "register" -> register(tokens);
            case "login" -> login(tokens);
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String help() {
        return """
                - register <USERNAME> <PASSWORD> <EMAIL> - create an account
                - login <USERNAME> <PASSWORD> - start playing
                - quit - exit the application
                - help - show possible commands
                """;
    }

    private String register(String[] tokens) {
        if (tokens.length < 4) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        }
        try {
            var authData = facade.register(tokens[1], tokens[2], tokens[3]);
            authToken = authData.authToken();
            return "Registered and logged in as " + authData.username();
        } catch (Exception e) {
            return "Error: Registration failed.";
        }
    }

    private String login(String[] tokens) {
        if (tokens.length < 3) {
            return "Usage: login <USERNAME> <PASSWORD>";
        }
        try {
            var authData = facade.login(tokens[1], tokens[2]);
            authToken = authData.authToken();
            return "Logged in as " + authData.username();
        } catch (Exception e) {
            return "Error: Login failed. Check your username and password.";
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }
    public void clearAuth() {
        authToken = null;
    }
}