import model.GameBoard;
import model.Player;
import model.Square;

import java.io.*;
import java.util.*;

public class GamePlay {
    private static Map<String, Player> players = new HashMap<>();
    private static final int MAX_ROUNDNUM = 100;
    private static final int INITIAL_MONEY = 1500;

    public static Map<String, Player> getPlayers() {
        return players;
    }

    public static void addPlayers(int id,String name){ //player name retrieved from command
        Player player = new Player(id, name,1,INITIAL_MONEY,0);
        players.put(name,player);
    }


    public static void start(GameBoard gameBoard){
        String boardName = gameBoard.getBoardName();
        checkGame(gameBoard); //print out the whole game board
        Boolean saveSuccessfully = true;

        for (int roundNum = 1; roundNum <= MAX_ROUNDNUM; roundNum++){ //start the game
            System.out.println("Round "+roundNum+":");

            for (Player player : players.values()){
                System.out.printf("\n%s's turn. You have $%d.\n", player.getName(),player.getMoney());
                if (player.getJailTime() > 0){ //player is in jail
                    inJail(player);
                } else if (!player.isRetired()){
                    Scanner scanner = new Scanner(System.in);
                    String input = "";
                    boolean playerTurn = true;
                    while (playerTurn){
                        System.out.println("What would you like to do?");
                        System.out.println("1. Enter 'ROLL' to roll the dice and pass to next player.");
                        System.out.println("2. Enter 'CHECK <player name/ALL/GAME>' to check status.");
                        System.out.println("3. Enter 'QUERY' to query next player.");
                        System.out.println("4. Enter 'SAVE <filename>' to save the game.");
                        System.out.println("5. Enter 'BACK' to go back to Player Mode Page.");
                        System.out.println("6. Enter 'EXIT' to exit.");
                        input = scanner.nextLine();
                        String[] inputParts = input.split(" ");
                        String command = inputParts[0].toLowerCase();
                        switch (command){
                            case "roll":
                                saveSuccessfully = false;
                                int move = player.throwDice();
                                player.movement(move);

                                String boardType = GameBoard.getSquare(player.getCurrent()).getType();
                                switch (boardType){
                                    case "PROPERTY":
                                        Square landProperty = GameBoard.getSquare(player.getCurrent());
                                        String propertyName = landProperty.getName();
                                        int propertyPrice = landProperty.getPrice();
                                        int propertyRent = landProperty.getRent();
                                        boolean propertyState = landProperty.isOwned();
                                        Player propertyOwner = landProperty.getOwner();

                                        System.out.printf("You land on %s.\n", propertyName);
                                        if (propertyState) {
                                            System.out.printf("The property %s is owned. You have to pay $%d to %s for rent.\n", propertyName, propertyRent, propertyOwner.getName());
                                            landProperty.rentPayment(player);
                                            System.out.printf("You have $%d left.\n", player.getMoney());
                                        } else {
                                            while (true) {
                                                System.out.printf("Do you want to buy %s for $%d? (Enter yes/no): ", propertyName, propertyPrice);
                                                String answer = scanner.nextLine().toLowerCase();
                                                if (answer.equals("yes")) {
                                                    landProperty.buy(player);
                                                    System.out.printf("You have $%d left.\n", player.getMoney());
                                                    break;
                                                } else if (answer.equals("no")) {
                                                    System.out.printf("You have $%d left.\n", player.getMoney());
                                                    break;
                                                } else {
                                                    System.out.println("Invalid input. Please answer yes or no only.");
                                                }
                                            }
                                        }
                                        break;
                                    case "CHANCE":
                                        System.out.println("You land on CHANCE!");
                                        player.chance();
                                        break;
                                    case "INCOMETAX":
                                        System.out.println("Ops! You land on INCOME TAX!");
                                        player.incomeTax();
                                        break;
                                    case "FREEPARKING":
                                        System.out.println("You land on FREE PARKING. No action can be done.");
                                        break;
                                    case "GOTOJAIL":
                                        System.out.println("Ops! You land on GO TO JAIL!");
                                        player.goToJail();
                                        break;
                                    case "JAIL":
                                        System.out.println("You land on In Jail/Just Visiting! No action can be done.");
                                        break;
                                    case "GO":
                                        break;
                                    default:
                                        throw new IllegalStateException("Unexpected value: " + boardType);
                                }
                                playerTurn = false;
                                break;
                            case "check":
                                try {
                                    if (inputParts[1].toLowerCase().compareTo("all") == 0){
                                        printPlayers();
                                    } else if (inputParts[1].toLowerCase().compareTo("game") == 0){
                                        checkGame(gameBoard);
                                    } else {
                                        boolean found = false;
                                        String checkName = inputParts[1];
                                        for (String checkPlayer: players.keySet()){
                                            if (checkPlayer.toLowerCase().equals(checkName)) {
                                                found = true;
                                                checkPlayer(players.get(checkName));
                                                break;
                                            }
                                        }
                                        if (!found){
                                            System.out.printf("Player %s not found. Please enter a valid name.\n", checkName);
                                            System.out.println("You can enter CHECK ALL the review all the player name.");
                                        }
                                    }
                                    break;
                                }
                                catch (Exception e){
                                    System.out.println("Invalid command format. Please enter again.\n" +
                                            "command: CHECK <player name/ALL/GAME>");
                                    break;
                                }
                            case "query":
                                queryNext(player);
                                break;
                            case "save":
                                if (saveSuccessfully){
                                    System.out.println("Game has been saved already / No change has been made.");
                                    break;
                                }
                                else try {
                                    if(!checkFirstPlayer(player)){
                                        continue;
                                    }
                                    String fileName = inputParts[1];

                                    if (fileName.length() <= 4 || !fileName.endsWith(".txt")) {
                                        System.out.println("Invalid command format. Please enter again.\n" +
                                                "command: SAVE <file name>.txt");
                                        continue;
                                    }
                                    saveSuccessfully = true;
                                    savePlay(fileName,boardName);
                                    players.clear();
                                    break;
                                } catch (Exception e){
                                    System.out.println("Invalid command format. Please enter again.\n" +
                                            "command: SAVE <filename>");
                                    break;
                                }
                            case "back":
                                if (saveSuccessfully){
                                    players.clear();
                                    return;
                                }
                                else {
                                    System.out.println("Your game record will be lost if you have not saved.\n" + "Do you want to save the game? (Enter yes/no)\nEnter answer: ");
                                    String answer = scanner.nextLine().toLowerCase();
                                    if (answer.compareTo("yes") == 0) {
                                        if(!checkFirstPlayer(player)){
                                            continue;
                                        }
                                        System.out.println("Please enter the file name with '.txt'");
                                        String saveName = scanner.nextLine();
                                        if (saveName.length() <= 4 || !saveName.endsWith(".txt")) {
                                            System.out.println("Invalid filename. Please enter a filename with '.txt'.\n");
                                            continue;
                                        }
                                        savePlay(saveName, boardName);
                                        players.clear();
                                        return;
                                    } else if (answer.compareTo("no") == 0) {
                                        System.out.println("Your game record will not be saved.");
                                        System.out.println("Goodbye!");
                                        players.clear();
                                        return;
                                    } else {
                                        System.out.println("Invalid input. Please enter yes or no.");
                                    }
                                    break;
                                }
                            case "exit":
                                if (saveSuccessfully){
                                    System.out.println("Goodbye!");
                                    players.clear();
                                    System.exit(0);
                                }
                                else {
                                    System.out.println("Your game record will be lost if you have not saved.\nDo you want to save the game? (Enter yes/no)\nEnter answer: ");
                                    String answer2 = scanner.nextLine().toLowerCase();
                                    if (answer2.compareTo("yes") == 0) {
                                        if(!checkFirstPlayer(player)){
                                            continue;
                                        }
                                        System.out.println("Please enter the file name with '.txt'");
                                        String saveName = scanner.nextLine().toLowerCase();
                                        if (saveName.length() <= 4 || !saveName.endsWith(".txt")) {
                                            System.out.println("Invalid filename. Please enter a filename with '.txt'.\n");
                                            continue;
                                        }
                                        savePlay(saveName, boardName);
                                        players.clear();
                                        System.exit(0);
                                    } else if (answer2.compareTo("no") == 0) {
                                        System.out.println("Your game record will not be saved.");
                                        System.out.println("Goodbye!");
                                        players.clear();
                                        System.exit(0);
                                    } else {
                                        System.out.println("Invalid input. Please enter yes or no.");
                                    }
                                    break;
                                }
                            default:
                                System.out.println("Invalid input. Please enter a valid command.\n"+
                                        "command 1 (roll the dice):                 ROLL\n" +
                                        "command 2 (check status):                  CHECK <player name/ALL/GAME>\n" +
                                        "command 3 (query next file):               QUERY\n"+
                                        "command 4 (save gameplay record):          SAVE <filename>.txt\n"+
                                        "command 5 (back to Player Mode Page):      BACK\n"+
                                        "command 6 (exit):                          EXIT");
                                break;
                        }
                    }
                } else {
                    System.out.println("You are bankrupt! No action can be done.");
                }
            }
            if (endGame()){
                break;
            }
        }
        winner();
        System.out.println("The game is ended.");
        System.exit(0);
    }

    public static void inJail(Player player){ //called when player is in jail
        Scanner scanner = new Scanner(System.in);
        System.out.printf("You are in jail. The remaining jail time is %d.\n",player.getJailTime());
        if (player.getJailTime() > 1) {
            System.out.print("""
                    You can try to throw doubles (i.e. both dice coming out the same face up) or pay $150 to get out.
                    (Enter 'ROLL' to try to throw doubles or 'PAY' to pay the fine.)
                    """);
            String input = scanner.nextLine().toLowerCase();
            /** Have not handled typo!!!!!!
             Question: throw dice 3 turns is in three rounds or same round?????
             * */
            if (input.compareTo("roll") == 0){
                int dice1 = Player.throwDice();
                int dice2 = Player.throwDice();
                if (dice1 == dice2){ //throw double
                    System.out.printf("You throw doubles! Moving forward by %d.\n",dice1+dice2);
                    player.setJailTime(0);
                    player.movement(dice1+dice2);
                }else {
                    System.out.println("Ops! You failed to throw double, you will remain in the Jail.");
                    player.setJailTime(player.getJailTime()- 1);
                }
            } else if (input.compareTo("pay") == 0){
                player.setJailTime(0);
                player.setMoney(player.getMoney() - 150);
                System.out.println(player.getName()+" paid $150 to get out of the jail.");
            }
        } else { //the third turn
            int dice1 = Player.throwDice();
            int dice2 = Player.throwDice();
            if (dice1 == dice2){ //throw double
                System.out.printf("You throw doubles! Moving forward by %d.\n",dice1+dice2);
            }else {
                player.setMoney(player.getMoney() - 150);
                System.out.println(player.getName()+" paid $150 to get out of the jail.");
            }
            player.setJailTime(0);
            player.movement(dice1+dice2);
        }

    }

    public static boolean endGame(){ //called when end the game with only one player left
        int currentPlayers = players.size(); //number of players that have not retired
        for (Player player : players.values()){
            if (player.isRetired()){
                currentPlayers--;
            }
        }
        return currentPlayers <= 1;
    }

    public static void winner(){ //called to find the winner
        List<Player> winners = new ArrayList<>();
        int maxMoney = 0;
        for (Player player : players.values()){
            if (!player.isRetired()){
                if (player.getMoney() > maxMoney){
                    maxMoney = player.getMoney(); //update the maximum amount of money among players
                    winners.clear(); //delete all the previous winner
                    winners.add(player);
                } else if (player.getMoney() == maxMoney){ //tie
                    winners.add(player);
                }
            }
        }
        if (winners.size() > 1){ //multiple winners
            System.out.printf("Tie! There are %d winners: \n",winners.size());
            for (Player player : winners) {
                String winner = player.getName();
                System.out.println(winner);
            }
            System.out.printf("\n They have $%d.\n",maxMoney);
        } else if (winners.size() == 1){
            String winner = winners.get(0).getName();
            System.out.printf("The winner is %s with $%d! Congratulations!\n",winner,maxMoney);
        }
//
    }

    // save gameboard name, player  name in turn
    public static void savePlay(String fileName, String boardName) {
        try{
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            if (boardName.compareTo("default") == 0){
                boardName = "defaultGameBoard.txt";
            }
            writer.write(boardName); //write the game board name in the first line of gameplay file
            writer.newLine();

            for (Player player : players.values()){
                writer.write(player.getName()+" ");
                writer.write(player.getCurrent() +" ");
                writer.write(player.getMoney() +" ");
                writer.write(player.getJailTime() +" ");

                for (Square property: GameBoard.getSquares().values()){
                    if (property.isOwned() && property.getOwner().equals(player)){
                        writer.write(property.getName()+" ");
                    }
                }
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.printf("Your game record is saved to %s.\n",fileName);
    }

    public static void loadPlay(String gameName) {
        /**
         example gameplay file: gameplay1.txt
         -game board file name in the first line
         -player information: <player name> <position> <money> <jail time> <all the remaining are property>
         */
        try {
            FileReader fileReader = new FileReader(gameName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String boardName = bufferedReader.readLine();
            GameBoard board = new GameBoard(boardName);
            if (!board.loadGameBoard(boardName)) {
                System.out.println("The provided game board file does not contain all the essential information to build a game board. Please try again.");
            }
            String line;
            int playerID = 1;
            players.clear(); //clear the player information of the previous game
            while ((line = bufferedReader.readLine()) != null){
                String[] content = line.split(" ");
                String playerName = content[0];
                int position = Integer.parseInt(content[1]);
                int money = Integer.parseInt(content[2]);
                int jailTime = Integer.parseInt(content[3]);
                Player player = new Player(playerID,playerName,position,money,jailTime);
                players.put(playerName, player);
                playerID++;

                for (int i = 3; i < content.length; i++){
                    String propertyName = content[i];
                    for (Square property: board.getSquares().values()){
                        if (property.getType().compareTo("PROPERTY") == 0 && property.getName().compareTo(propertyName) == 0){
                            property.setOwned(true);
                            property.setOwner(player);
                        }
                    }
                }
            }
            fileReader.close();
            System.out.printf("Your game record %s is loaded.\n", gameName);
            start(board); //start the game
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }

    public static void queryNext(Player player) { //called when player want to query the next player
        int currentID = player.getId();
        int nextID = currentID + 1; //playerID of the next player
        if (nextID > players.size()){
            nextID = 1;
        }
        Player nextPlayer = null;
        for (Player checkNext: players.values()){
            if (checkNext.getId() == nextID){
                if (!checkNext.isRetired()){ //if the next player is retired, ignore he/she
                    nextPlayer = checkNext;
                    break;
                } else {
                    nextID = nextID + 1;
                }
            }
            if (nextID > players.size()){ //reset nextID if it loops to the end of the players list
                nextID = 1;
            }
        }
        System.out.printf("The next player is %s, the position is at square %d.\n",nextPlayer.getName(),nextPlayer.getCurrent());
    }

    public static void checkPlayer(Player player) { //called when player want to see the status of specific player
        String current = String.valueOf(player.getCurrent());
        String money = String.valueOf(player.getMoney());
        System.out.printf("Information of player %s:\n", player.getName());
        System.out.println("Current position: square "+current);
        System.out.println("Current money: $"+money);
        System.out.println("He/She has the following properties:");
        for (Square property : GameBoard.getSquares().values()){
            if (property.isOwned() && property.getOwner().equals(player)){
                System.out.printf("%-2d) %s\n",property.getSquarenum(),property.getName());
            }
        }
    }

    public static void checkGame(GameBoard gameBoard) { //called when player want to see the status of the game
        /**
         1 print the game board
         2 print the position of all players
         */
        gameBoard.printGameBoard("player");
        System.out.println("The current player information:");
        printPlayers();
    }

    public static boolean checkFirstPlayer(Player player) {
        Player firstPlayer = player;
        if (player.getId() != 1) { // check if the player who access this command is the first player
            for (Player checkPlayer: players.values()){
                if (checkPlayer.getId() == 1){
                    firstPlayer = checkPlayer;
                }
            }
            System.out.println("Only the first player of the game can save the game. Please wait until first player's turn.\n" +
                    "(Note: The first player is " + firstPlayer.getName() + ".)\n");
            return false;
        }
        return true;
    }

    public static void printPlayers() { //called when checking the status of all players
        for (Player player : players.values()){
            if (!player.isRetired()){
                if (player.getJailTime() > 0){
                    System.out.printf("Player %s is in jail.\n", player.getName());
                } else System.out.printf("Player %s is at square %d.\n", player.getName(), player.getCurrent());
            } else System.out.printf("Player %s is retired.\n",player.getName());
        }
    }
}
