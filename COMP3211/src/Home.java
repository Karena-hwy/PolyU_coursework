import model.GameBoard;
import model.Square;

import java.io.*;
import java.util.Scanner;

//import static src.gameplay.players;

public class Home {
    public static void main(String[] args){
        monopoly();
    }

    public static void monopoly(){
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Welcome to The Monopoly Game!!!");

        while (running) {
            try {
                System.out.println("=====main page=====");
                System.out.println("Who are you? (Enter Player/Designer) ");
                System.out.print("\nEnter help for the available command formats.\nEnter command: ");
                String input = scanner.nextLine().toLowerCase();
                if (input.compareTo("designer") == 0){designerMode();}
                else if (input.compareTo("player") == 0) {playerMode();}
                else if (input.compareTo("help") == 0) {help();}
                else if (input.compareTo("exit") == 0) {running = false; System.out.println("=====Monopoly Closed=====");}
                else if (input.compareTo("back") == 0) {System.out.println("This is the first page.");}
                else throw new Exception("Incorrect command format.");
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        }
    }
    public static void playerMode(){
        Scanner scanner = new Scanner(System.in);
        boolean playing = true;

        while(playing) {
            try {
                System.out.println("=====player mode page=====");
                System.out.print("\nEnter help for the available command formats.\nEnter command: ");
                String input = scanner.nextLine();
                String[] commandParts = input.split(" ");
                String command = commandParts[0].toLowerCase();
                playing = readPlayerCommand(commandParts, command);

            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        }
    }

    public static boolean readPlayerCommand(String[] commandParts, String command) throws Exception {
        if (command.compareTo("help") == 0) {
            playerHelp();
            help();
        } else if (command.compareTo("start") == 0) {
            GamePlay monopoly = new GamePlay();

            int playerNum = validInt(commandParts[2]);
            if (playerNum < 2) { //less than 2 players
                System.out.println("You have to enter a number greater than or equal to 2 for number of players.");
                return true;
            }
            if (commandParts[3].toLowerCase().compareTo("random") == 0){
                for (int i = 0; i < playerNum; i++) {
                    String playerName = "Player" + i;
                    GamePlay.addPlayers(i+1, playerName);
                }
            } else {
                for (int i = 0, n = 3; i < playerNum; i++, n++) { //add players
                    //monopoly.addPlayers(commandParts[n]);
                    GamePlay.addPlayers(i+1, commandParts[n]);
                }
            }

            String boardName = commandParts[1];
            GameBoard board = new GameBoard(boardName); //load game board chosen

            boolean searching = true;
            if (boardName.toLowerCase().compareTo("default") == 0){
                GamePlay.start(board);
                return true;
            }
            boolean boardLoadable = true;
            if (searching){
                try {
                    if (validFileName(boardName)){ // the board name is valid (not ends with .txt / used defaultGameBoard.txt)
                        FileReader fileReader = new FileReader(boardName); // throw FileNotFound Exception if file does not exist in current working directory
                        if (!board.loadGameBoard(boardName)) { // file found && fail to load (the content within game board file does not provide information that the game needs)
                            boardLoadable = false;
                        }
                    }
                } catch (Exception e){
                    System.out.println("File not Found. Please try again."); //correct filename format but not in the current working directory
                }
                if (!boardLoadable) { //not loading a valid game board file
                    System.out.println("The provided game board file does not contain all the essential information to build a game board. Please try again.");
                } else {
                    GamePlay.start(board);
                }
            }
        } else if (command.compareTo("load") == 0) {
            if (commandParts.length != 2) {
                System.out.println("Invalid Command format. Please enter again.");
                return true;
            }
            String gameName = commandParts[1];
            if (!validFileName(gameName)) {
                return true;
            }
            GamePlay.loadPlay(gameName);
        } else if (command.compareTo("back") == 0) {
            return false;
        } else if (command.compareTo("exit") == 0) {
            exit();
        } else throw new Exception("Invalid command.");
        return true;
    }

    public static void designerMode(){
        Scanner scanner = new Scanner(System.in);
        Designer designer = new Designer();
        boolean designing = true;

        System.out.println("=====designer mode page=====");
        System.out.print("Please enter the file name of game board you want to modify. (Enter 'Default' if you want to modify the default game board.)\n Enter file name: ");
        String boardName = scanner.nextLine();

        boolean searching = true;
        GameBoard tempBoard = new GameBoard("tempBoard.txt"); //load game board chosen into a temporary instance
        if (boardName.toLowerCase().compareTo("back") == 0){
            return;
        } else if (boardName.toLowerCase().compareTo("exit") == 0){
            exit();
        } else if (boardName.toLowerCase().compareTo("default") == 0){
            searching = false;
        }
        boolean boardLoadable = true;
        boolean correctFileName = true;
        while(searching){
            try {
                if (boardName.toLowerCase().compareTo("back") == 0 || boardName.toLowerCase().compareTo("exit") == 0 || boardName.toLowerCase().compareTo("default") == 0){ // the board name is back/exit/default
                    throw new Exception();
                } else if (!validFileName(boardName)){ // the board name is not valid (not ends with .txt / used defaultGameBoard.txt)
                    correctFileName = false;
                    throw new Exception();
                } else { // the board name is valid
                    FileReader fileReader = new FileReader(boardName); // throw FileNotFound Exception if file does not exist in current working directory
                    if (tempBoard.loadGameBoard(boardName)) { // file found && load game board chosen into a temporary instance successfully
                        searching = false;
                    } else { // file found && fail to load (the content within game board file does not provide information that the game needs)
                        boardLoadable = false;
                        throw new Exception();
                    }
                }
            } catch (Exception e){
                if (boardName.toLowerCase().compareTo("back") == 0){
                    return;
                } else if (boardName.toLowerCase().compareTo("exit") == 0){
                    exit();
                } else if (boardName.toLowerCase().compareTo("default") == 0){
                    break;
                }
                if (!boardLoadable) { //not loading a valid game board file
                    System.out.println("The provided game board file does not contain all the essential information to build a game board. Please try again.");
                    boardLoadable = true;
                } else if (correctFileName) { //correct filename format but not in the current working directory
                    System.out.println("File not Found. Please try again.");
                } else if (!correctFileName) { //incorrect filename format, error message printed when calling validFileName(boardName)
                    correctFileName = true;
                }
                System.out.print("Please enter the file name of game board you want to modify. (Enter 'Default' if you want to modify the default game board.)\n Enter file name: ");
                boardName = scanner.nextLine();
            }
        }

        boolean saveSuccessfully = true;
        while(designing) {
            try {
                System.out.print("\nEnter help for the available command formats.\nEnter command: ");
                String input = scanner.nextLine();
                String[] commandPart = input.split(" ");
                String command = commandPart[0].toLowerCase();

                switch (command) {
                    case "modify":
                        saveSuccessfully = false;
                        if (commandPart[1].toLowerCase().compareTo("square") == 0){ //Modify Square
                            //command 1 (modify attribute of property):                                MODIFY SQUARE <square number> PROPERTY <attribute to modify> <new value>
                            //command 2 (modify all attributes of property / square as type property): MODIFY SQUARE <square number> PROPERTY <new value of name> <new value of price> <new value of rent>
                            //command 3 (modify square as other types):                                MODIFY SQUARE <square number> <new square type>

                            // check if square number is valid and within range 1-20
                            int sqNum = validInt(commandPart[2]);
                            if (sqNum < 1 || sqNum > 20){ //if user input is non-integer or integer out of range 1-20
                                System.out.println("Please enter a number between 1 and 20 (inclusively) for square number.");
                                continue;
                            }

                            String newSqType = commandPart[3].toLowerCase();
                            String sqType = GameBoard.getSquare(sqNum).getType();

                            // command 1 [modify attribute of property]
                            if (newSqType.compareTo("property") == 0 && sqType.compareTo("PROPERTY") == 0 && commandPart.length == 6){
                                String attribute = commandPart[4].toLowerCase();
                                String value = commandPart[5];
                                designer.modifyProperty(tempBoard, sqNum, attribute, value);
                                System.out.println("The current attributes of the square " + sqNum + " are:");
                                System.out.println("Name: " + tempBoard.getSquare(sqNum).getName());
                                System.out.println("Price: "+ tempBoard.getSquare(sqNum).getPrice());
                                System.out.println("Rent: "+ tempBoard.getSquare(sqNum).getRent());

                                // command 2 [modify all attributes of property / change from other type to property]
                            } else if (newSqType.compareTo("property") == 0 && commandPart.length == 7) {
                                if (!validJAILorGTJAILmodification(tempBoard, sqNum, newSqType)) {
                                    continue;
                                }
                                String value1 = commandPart[4];
                                String value2 = commandPart[5];
                                String value3 = commandPart[6];
                                String oldType = tempBoard.getSquare(sqNum).getType();

                                designer.modifyProperty(tempBoard, sqNum, "name", value1);
                                designer.modifyProperty(tempBoard, sqNum, "price", value2);
                                designer.modifyProperty(tempBoard, sqNum, "rent", value3);

                                // check whether all attributes of originally non-property square have been updated
                                if (tempBoard.getSquare(sqNum).getName() == null ||
                                        tempBoard.getSquare(sqNum).getPrice() == -1 ||
                                        tempBoard.getSquare(sqNum).getRent() == -1) {
                                    designer.modifyOtherSquareType(tempBoard, sqNum, oldType);
                                    System.err.println("The attributes of the square " + sqNum + " are not updated due to the above reason(s).");
                                    continue;
                                }
                                System.out.println("The current attributes of the square " + sqNum + " are:");
                                System.out.println("Name: " + tempBoard.getSquare(sqNum).getName());
                                System.out.println("Price: "+ tempBoard.getSquare(sqNum).getPrice());
                                System.out.println("Rent: "+ tempBoard.getSquare(sqNum).getRent());

                                // command 3 [change from property / other type to other type]
                            } else if (commandPart.length == 4) {
                                if (!validJAILorGTJAILmodification(tempBoard, sqNum, newSqType)) {
                                    continue;
                                }
                                designer.modifyOtherSquareType(tempBoard,sqNum,newSqType);

                            } else {
                                System.out.println("Invalid command format. Please enter again.\n" +
                                        "command 1 (modify one attribute of property):                            MODIFY SQUARE <square number> PROPERTY <attribute to modify> <new value>\n" +
                                        "command 2 (modify all attributes of property / square as type property): MODIFY SQUARE <square number> PROPERTY <new value of name> <new value of price> <new value of rent>\n" +
                                        "command 3 (modify square as other types):                                MODIFY SQUARE <square number> <new square type>");
                            }


                        } else if (commandPart[1].toLowerCase().compareTo("swap") == 0){ //Swapping two Squares
                            //command: MODIFY SWAP <square number 1> <square number 2>
                            if (commandPart.length != 4){
                                System.out.println("Invalid command format. Please enter again.\n" +
                                        "command: MODIFY SWAP <square number 1> <square number 2>");
                                continue;
                            }
                            int sqNum1 = validInt(commandPart[2]);
                            int sqNum2 = validInt(commandPart[3]);
                            if (sqNum1 < 1 || sqNum1 > 20 || sqNum2 < 1 || sqNum2 > 20){
                                System.out.println("Please enter a number between 1 and 20 (inclusively) for square number.");
                                continue;
                            }
                            /**else if (sqNum1 == 1 || sqNum2 == 1){
                             System.out.println("The position of GO can not be modified.");
                             System.out.println("Please enter a number between 2 and 20 (inclusively) for square number.");
                             continue;
                             }*/
                            designer.modifySwap(tempBoard,sqNum1,sqNum2);
                        } else{
                            throw new Exception("Invalid command.");
                        }
                        break;
                    case "save":
                        if (saveSuccessfully){
                            System.out.println("Game board has been saved already / No change has been made.");
                            break;
                        }
                        // check whether the number of arguments is correct
                        if (commandPart.length != 2) {
                            System.out.println("Invalid command format. Please enter again.\n" +
                                    "command: SAVE <file name>.txt");
                            continue;
                        }
                        String fileName = commandPart[1];
                        // check whether the file name ends with '.txt'
                        if (!validFileName(fileName)) {
                            System.out.println("command: SAVE <file name>.txt");
                            continue;
                        }
                        saveSuccessfully = true;
                        tempBoard.saveGameBoard(fileName);
                        break;
                    case "help":
                        if (commandPart.length != 1) {
                            System.out.println("Invalid command format. Please enter again.\n" +
                                    "command: HELP");
                            continue;
                        }
                        designerHelp();
                        help();
                        break;
                    case "print":
                        if (commandPart.length != 1) {
                            System.out.println("Invalid command format. Please enter again.\n" +
                                    "command: PRINT");
                            continue;
                        }
                        tempBoard.printGameBoard("designer");
                        break;
                    case "back":
                        if (saveSuccessfully){
                            return;
                        }
                        System.out.print("New modified game board data will be lost if you have not saved.\nDo you want to save the game? (Enter yes/no) \nEnter answer: ");
                        String answer = scanner.nextLine();
                        if (answer.compareTo("yes") == 0) {
                            System.out.println("Please enter the file name with '.txt'");
                            String saveName = scanner.nextLine();
                            // check whether the file name ends with '.txt'
                            if (!validFileName(saveName)) {
                                continue;
                            }
                            tempBoard.saveGameBoard(saveName);
                            designing = false;
                        } else if (answer.compareTo("no") == 0) {
                            System.out.println("Your modified game board data will not be saved.");
                            System.out.println("Goodbye!");
                            designing = false;
                        } else {
                            System.out.println("Invalid input, please input yes or no.");
                        }
                        break;
                    case "exit":
                        if (saveSuccessfully){
                            System.out.println("Goodbye!");
                            exit();
                        }
                        System.out.println("New modified game board data will be lost if you have not saved.\nDo you want to save the game? (Enter yes/no) \nEnter answer: ");
                        String answer2 = scanner.nextLine();
                        if (answer2.compareTo("yes") == 0) {
                            System.out.println("Please enter the file name with '.txt'");
                            String saveName = scanner.nextLine();
                            // check whether the file name ends with '.txt'
                            if (!validFileName(saveName)) {
                                continue;
                            }
                            tempBoard.saveGameBoard(saveName);
                            exit();
                        } else if (answer2.compareTo("no") == 0) {
                            System.out.println("Your modified game board data will not be saved.");
                            System.out.println("Goodbye!");
                            exit();
                        } else {
                            System.out.println("Invalid input, please input yes or no.");
                        }
                        break;
                    default:
                        System.out.println("Invalid command format. Please enter again.");
                }

            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        }
    }

    public static boolean validJAILorGTJAILmodification(GameBoard gameBoard, int sqNum, String newSqType) { // function to check 1) whether the square can be modified as type JAIL/GOTOJAIL; 2) whether the JAIL square can be modified
        newSqType = newSqType.toLowerCase();
        String originalSqType = gameBoard.getSquare(sqNum).getType();
        // Note 1: There should be at most one JAIL square if the game board contains at least one GOTOJAIL square
        // Note 2: There could be one or no JAIL square if the game board contains no GOTOJAIL square

        if (newSqType.compareTo("jail") == 0) { // user want to change square as type JAIL
            for (Square square: gameBoard.getSquares().values()) {
                if (square.getType().compareTo("GOTOJAIL") == 0 && square.getSquarenum() != sqNum) { // check whether there is an exising GOTOJAIL square
                    System.out.println("Since there is an existing GOTOJAIL square at square " + square.getSquarenum() + ", the square at square " + sqNum + " cannot be modified as type JAIL.");
                    return false;
                }
            }
        }
        if (newSqType.equals("gotojail")) { // user want to change square as type GOTOJAIL
            if (originalSqType.compareTo("JAIL") == 0) { // check whether JAIL square is being requested to be changed as type GOTOJAIL
                System.out.println("Since square " + sqNum + " is a JAIL square, it cannot be modified as type GOTOJAIL.");
                return false;
            }
            boolean jailExists = false;
            for (Square square: gameBoard.getSquares().values()) { // check whether there is an exising JAIL square
                if (square.getType().compareTo("JAIL") == 0) {
                    jailExists = true;
                    break;
                }
            }
            if (!jailExists) { // there is no JAIL square
                System.out.println("Since there is no JAIL square in the game board, you cannot modify the square as type GOTOJAIL directly.\nYou need to set any one square as type JAIL beforehand.");
                return false;
            }
        }

        if (originalSqType.compareTo("JAIL") == 0 && newSqType.compareTo("jail") != 0) { // user want to change JAIL square as other types
            for (Square square: gameBoard.getSquares().values()) {
                if (square.getType().compareTo("GOTOJAIL") == 0) {
                    System.out.println("Since there is an GOTOJAIL square at square " + square.getSquarenum() + ", JAIL square at square " + sqNum + "cannot be modified unless GOTOJAIL square no longer exist.");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean validFileName(String fileName) { // check whether the file name and its format are valid
        if (fileName.length() <= 4 || !fileName.endsWith(".txt")) { // check whether the file name ends with ".txt"
            System.out.println(  );
            return false;
        }
        if (fileName.compareTo("defaultGameBoard.txt") == 0){   // check whether the file name entered is the same as the default game board file name
            System.out.println("The file name cannot be 'defaultGameBoard.txt', please try again with another file name.");
            return false;
        }
        return true;
    }

    public static int validInt(String string) { // check whether the String can be converted as integer
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void help() {
        System.out.println("=====basic function command format=====\n" +
                "1. Enter 'BACK' to go back to previous page.\n" +
                "2. Enter 'EXIT' to close the game.\n" +
                "3. Enter 'SAVE <file name>.txt' to save the current process.\n" +
                "=======================================");
    }

    public static void playerHelp() {
        System.out.println("=====player mode command format=====\n" +
                "1.To start the game: START <filename of game board including .txt or default> <number of player> <name of player1> <name of player2> ......" +
                "2.To load previous game: LOAD <filename>.txt");
    }

    public static void designerHelp() {
        System.out.println("=====designer mode command format=====\n" +
                "1. To modify one attribute of property:                                   MODIFY SQUARE <square number> PROPERTY <attribute to modify> <new value>\n" +
                "2. To modify all attributes of property / modify square as type property: MODIFY SQUARE <square number> PROPERTY <new value of name> <new value of price> <new value of rent>\n" +
                "3. To modify square as other types:                                       MODIFY SQUARE <square number> <new square type>\n" +
                "   Available Square Types: 1. GO   2. CHANCE   3. INCOMETAX    4.FREEPARKING   5. GOTOJAIL   6. JAIL\n" +
                "4. To swap the position of two squares:                                   MODIFY SWAP <square number 1> <square number 2>\n" +
                "5. To print the currently modified game board data:                       PRINT");
    }

    protected static void exit() {
        System.out.println("=====Monopoly Closed=====");
        System.exit(0);
    }
}