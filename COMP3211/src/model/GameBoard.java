package model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GameBoard {
    /**
     * property,Go,chance,income tax,just visiting/jail, free parking
     * for loop to load new board
     */
    private static Map<Integer, Square> squares = new HashMap<>();
    private String boardName = "defaultGameBoard";

    public GameBoard(String boardName) {
        defaultGameBoard();
        this.boardName = boardName;
    }

    public String getBoardName() {return this.boardName;}
    public void setBoardName(String boardName) {this.boardName = boardName;}

    private void defaultGameBoard(){
        squares.put(1, new Square(1, "GO", null, -1, -1));
        squares.put(2, new Square(2, "PROPERTY", "Central", 800, 90));
        squares.put(3, new Square(3, "PROPERTY", "WanChai", 700, 65));
        squares.put(4, new Square(4, "INCOMETAX", null, -1, -1));
        squares.put(5, new Square(5, "PROPERTY", "Stanley", 600, 60));
        squares.put(6, new Square(6, "JAIL", null, -1, -1));
        squares.put(7, new Square(7, "PROPERTY", "ShekO", 400, 10));
        squares.put(8, new Square(8, "PROPERTY", "MongKok", 500, 40));
        squares.put(9, new Square(9, "CHANCE", null, -1, -1));
        squares.put(10, new Square(10, "PROPERTY", "TsingYi", 400, 15));
        squares.put(11, new Square(11, "FREEPARKING", null, -1, -1));
        squares.put(12, new Square(12, "PROPERTY", "Shatin", 700, 75));
        squares.put(13, new Square(13, "CHANCE", null, -1, -1));
        squares.put(14, new Square(14, "PROPERTY", "TuenMun", 400, 20));
        squares.put(15, new Square(15, "PROPERTY", "TaiPo", 500, 25));
        squares.put(16, new Square(16, "GOTOJAIL", null, -1, -1));
        squares.put(17, new Square(17, "PROPERTY", "SaiKung", 400, 10));
        squares.put(18, new Square(18, "PROPERTY", "YuenLong", 400, 25));
        squares.put(19, new Square(19, "CHANCE", null, -1, -1));
        squares.put(20, new Square(20, "PROPERTY", "TaiO", 600, 25));
    }

    public static Square getSquare(int i){return squares.get(i);} //get a specific square

    public static Map<Integer, Square> getSquares() {return squares;} //get the whole squares map

    public static void updateSquare(int sqNum, Square newSquare){
        squares.put(sqNum, newSquare); // it can update value of existing key, old value will be replaced with new one
    }

    public void printGameBoard(String mode){
        System.out.println("The current game board:"); //print out the whole game board
        if (mode.equals("player")) {
            System.out.println("    Type          Name       Price  Rent  Owner");
        } else if (mode.equals("designer")) {
            System.out.println("    Type          Name       Price  Rent");
        }
        for (Square square: getSquares().values()) { //print out the whole game board
            String type = square.getType();
            switch (type) {
                case "GO":
                    System.out.printf("%-2d) GO\n", square.getSquarenum());
                    break;
                case "PROPERTY":
                    if (mode.equals("player")){
                        if (square.getOwner() != null){
                            System.out.printf("%-2d) %-13s %-10s %-5d  %-4d  %s\n", square.getSquarenum(), square.getType(), square.getName(), square.getPrice(), square.getRent(),square.getOwner().getName());
                        } else System.out.printf("%-2d) %-13s %-10s %-5d  %-4d  %s\n", square.getSquarenum(), square.getType(), square.getName(), square.getPrice(), square.getRent(),square.getOwner());
                    } else if (mode.equals("designer")) {
                        System.out.printf("%-2d) %-13s %-10s %-5d  %-4d\n", square.getSquarenum(), square.getType(), square.getName(), square.getPrice(), square.getRent());
                    }
                    break;
                case "INCOMETAX":
                    System.out.printf("%-2d) INCOME TAX\n", square.getSquarenum());
                    break;
                case "JAIL":
                    System.out.printf("%-2d) IN JAIL/JUST VISITING\n", square.getSquarenum());
                    break;
                case "CHANCE":
                    System.out.printf("%-2d) CHANCE\n", square.getSquarenum());
                    break;
                case "FREEPARKING":
                    System.out.printf("%-2d) FREE PARKING\n", square.getSquarenum());
                    break;
                case "GOTOJAIL":
                    System.out.printf("%-2d) GO TO JAIL\n", square.getSquarenum());
                    break;
            }

        }
    }

    public boolean loadGameBoard(String filename) {
        try {
            squares.clear();
            FileReader fileReader = new FileReader(filename);
            BufferedReader buffer = new BufferedReader(fileReader);
            String line;
            int sqNum = 1;

            while ((line = buffer.readLine()) != null) {
                String[] content = line.split(" ");
                String sqType = content[0];
                if (sqType.compareTo("PROPERTY") == 0) {
                    String name = content[1];
                    int price = Integer.parseInt(content[2]);
                    int rent = Integer.parseInt(content[3]);
                    squares.put(sqNum, new Square(sqNum, "PROPERTY", name, price, rent));
                } else if (sqType.equals("GO") || sqType.equals("INCOMETAX") || sqType.equals("JAIL") || sqType.equals("GOTOJAIL") || sqType.equals("CHANCE") || sqType.equals("FREEPARKING")) {
                    squares.put(sqNum, new Square(sqNum, sqType, null, -1, -1));
                } else {
                    System.out.printf("Error: Unknown type of square %s at square %d. Please check the file again.\n", sqType, sqNum);
                    return false;
                }
                sqNum++;
            }
            fileReader.close();
            System.out.printf("The game board %s is loaded.\n", filename);
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("File Not Found! Error loading game board from file: " + filename);
            return false;
        } catch (IOException e) {
            System.err.println("Error reading game board from file: " + e.getMessage());
            return false;
        }
    }

    public void saveGameBoard(String filename){
        try{
            FileWriter fileWriter = new FileWriter(filename);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            for (Square square: squares.values()) {
                if (square.getType().compareTo("PROPERTY") == 0) {
                    writer.write(square.getType()+" ");
                    writer.write(square.getName()+" ");
                    writer.write(square.getPrice()+" ");
                    writer.write(square.getRent()+" ");
                } else writer.write(square.getType());
                writer.newLine();
            }
            writer.close();
            System.out.printf("The game board %s is saved.\n", filename);
        } catch (IOException e) {
            System.err.println("Error saving game board to file: " + e.getMessage());
        }
    }
}
