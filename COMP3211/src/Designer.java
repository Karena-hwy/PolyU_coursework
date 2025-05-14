import model.GameBoard;
import model.Square;

import java.util.Scanner;

public class Designer {
    /**
     when modify
     1 load all info of chosen game board into a temporary GameBoard
     2 ask user to input command to execute different function
     3 save all updated info into the temporary GameBoard
     */
    private Scanner scanner;

    public Designer() {
        this.scanner = new Scanner(System.in);
    }

    public void modifyProperty(GameBoard gameBoard, int sqNum, String attribute, String newValue){ //called when designer want to modify one attribute of a property
        Square propertyToModify = gameBoard.getSquare(sqNum);

        // check whether the new name for property is identical to that of any other property
        if (attribute.compareTo("name") == 0) {
            for (Square square: gameBoard.getSquares().values()) {
                String name = square.getName();
                if (name != null && name.compareTo(newValue) == 0) {
                    System.out.printf("Square %d has identical name: %s\n This attribute will not be updated.\n", square.getSquarenum(), name);
                    return;
                }
            }
        }

        // check whether the new price/rent is a valid integer
        if (attribute.compareTo("price") == 0 || attribute.compareTo("rent") == 0) {
            try{
                if (Integer.parseInt(newValue) < 1) {   //newValue is a negative integer
                    System.out.printf("Value of attribute %s should be a positive integer.\n This attribute will not be updated.\n", attribute);
                    return;
                }
            } catch (Exception e) { //newValue is not an integer
                System.out.printf("Value of attribute %s should be a positive integer.\n This attribute will not be updated.\n", attribute);
                return;
            }
        }

        switch (attribute){
            case "name":
                propertyToModify.setName(newValue);
                break;
            case "price":
                propertyToModify.setPrice(newValue);
                break;
            case "rent":
                propertyToModify.setRent(newValue);
                break;
            default: // check whether the attribute is valid
                System.out.println("Invalid attribute. Please enter a valid attribute.");
                return;
        }

        // check whether the type of squareToModify needs to be updated
        if (propertyToModify.getType().compareTo("PROPERTY") == 0) { // original type of squareToModify is not PROPERTY
            System.out.printf("Square %d Property has been modified successfully.\n", sqNum);
        } else {
            propertyToModify.setType("PROPERTY");
            System.out.printf("Square %d is now modified as Property.\n", sqNum);
        }
        System.out.println("The attribute " + attribute + " is now modified as " + newValue + ".");
        gameBoard.updateSquare(sqNum, propertyToModify);
    }

    public void modifyOtherSquareType(GameBoard gameBoard, int sqNum, String newSqType) { //change square to other type
        newSqType = newSqType.toUpperCase();
        if (newSqType.equals("GO") || newSqType.equals("INCOMETAX") || newSqType.equals("JAIL") || newSqType.equals("GOTOJAIL") || newSqType.equals("CHANCE") || newSqType.equals("FREEPARKING")) {
            Square squareToModify = new Square(sqNum, newSqType, null, -1, -1);
            gameBoard.updateSquare(sqNum, squareToModify);
            System.out.println("Square " + sqNum + " has been modified successfully.");
            System.out.printf("The type is now modified as %s.\n", newSqType);
        } else if (newSqType.equals("PROPERTY")) {
            System.out.println("Please provide the values of property attributes with reference to the following command format if you would like to modify the square type as PROPERTY:\n" +
                    "command: MODIFY SQUARE <square number> PROPERTY <new value of name> <new value of price> <new value of rent>");
        } else {
            System.out.println("Invalid square type. Please enter a valid square type.\n" +
                               "Available Square Types: 1. GO   2. CHANCE   3. INCOMETAX    4.FREEPARKING   5. GOTOJAIL   6. JAIL");
        }
    }

    public void modifySwap(GameBoard gameBoard, int sqNum1, int sqNum2){ // swap the position of two square
        Square squareToSwap = gameBoard.getSquare(sqNum1);
        gameBoard.updateSquare(sqNum1, gameBoard.getSquare(sqNum2));
        gameBoard.updateSquare(sqNum2, squareToSwap);
    }

}
