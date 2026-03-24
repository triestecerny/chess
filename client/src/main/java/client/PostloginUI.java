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
            case "logout" -> logout();
            case "create" -> createGame(tokens);
            case "list" -> listGames();
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
    private String createGame(String[] tokens) {
        if (tokens.length < 2) {
            return "Usage: create <NAME>";
        }
        try {
            facade.createGame(authToken, tokens[1]);
            return "Game created: " + tokens[1];
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String listGames() {
        try {
            var games = facade.listGames(authToken);
            if (games.length == 0) {
                return "No games available.";
            }
            var sb = new StringBuilder();
            for (int i = 0; i < games.length; i++) {
                var game = games[i];
                sb.append(i + 1).append(". ").append(game.gameName());
                sb.append(" | White: ").append(game.whiteUsername() != null ? game.whiteUsername() : "open");
                sb.append(" | Black: ").append(game.blackUsername() != null ? game.blackUsername() : "open");
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    private boolean loggedOut = false;

    public boolean isLoggedOut() {
        return loggedOut;
    }
    private String logout() {
        try {
            facade.logout(authToken);
            loggedOut = true;
            return "logged out";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}