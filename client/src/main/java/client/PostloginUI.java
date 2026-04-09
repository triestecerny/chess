package client;

public class PostloginUI {

    private final ServerFacade facade;
    private final String authToken;
    private ServerFacade.GameData[] lastGames = null;
    private boolean loggedOut = false;

    public PostloginUI(ServerFacade facade, String authToken) {
        this.facade = facade;
        this.authToken = authToken;
    }

    public String eval(String input) {
        var tokens = input.split(" ");
        var cmd = tokens[0].toLowerCase();
        return switch (cmd) {
            case "help" -> help();
            case "logout" -> logout();
            case "create" -> createGame(tokens);
            case "list" -> listGames();
            case "play" -> playGame(tokens);
            case "observe" -> observeGame(tokens);
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
            return "Error: Could not create game.";
        }
    }

    private String listGames() {
        try {
            lastGames = facade.listGames(authToken);
            if (lastGames.length == 0) {
                return "No games available.";
            }
            var sb = new StringBuilder();
            for (int i = 0; i < lastGames.length; i++) {
                var game = lastGames[i];
                sb.append(i + 1).append(". ").append(game.gameName());
                sb.append(" | White: ").append(game.whiteUsername() != null ? game.whiteUsername() : "open");
                sb.append(" | Black: ").append(game.blackUsername() != null ? game.blackUsername() : "open");
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: Could not get game list";
        }
    }

    private String playGame(String[] tokens) {
        if (tokens.length < 3) {
            return "Usage: play <ID> <WHITE|BLACK>";
        }
        try {
            if (lastGames == null) {
                lastGames = facade.listGames(authToken);
            }
            int gameNumber = Integer.parseInt(tokens[1]);
            if (gameNumber < 1 || gameNumber > lastGames.length) {
                return "Invalid game number.";
            }
            int gameID = lastGames[gameNumber - 1].gameID();
            String color = tokens[2].toUpperCase();
            facade.joinGame(authToken, gameID, color);
            new GameplayUI(facade.getServerUrl(), authToken, gameID, color).run(new java.util.Scanner(System.in));
            return null;
        } catch (NumberFormatException e) {
            return "Please enter a valid game number.";
        } catch (Exception e) {
            return "Error: Could not join game";
        }
    }

    private String observeGame(String[] tokens) {
        if (tokens.length < 2) {
            return "Usage: observe <ID>";
        }
        try {
            if (lastGames == null) {
                lastGames = facade.listGames(authToken);
            }
            int gameNumber = Integer.parseInt(tokens[1]);
            if (gameNumber < 1 || gameNumber > lastGames.length) {
                return "Invalid game number.";
            }
            int gameID = lastGames[gameNumber - 1].gameID();
            new GameplayUI(facade.getServerUrl(), authToken, gameID, null).run(new java.util.Scanner(System.in));
            return null;
        } catch (NumberFormatException e) {
            return "Please enter a valid game number.";
        } catch (Exception e) {
            return "Error: Could not observe game.";
        }
    }

    public boolean isLoggedOut() {
        return loggedOut;
    }

    private String logout() {
        try {
            facade.logout(authToken);
            loggedOut = true;
            return "Logged out";
        } catch (Exception e) {
            return "Error: Logout failed";
        }
    }
}