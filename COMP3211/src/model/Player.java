package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Player {
    private int id;
    private String name;
    private int current; //current position of the src.player, start from 1
    private int money;
    private int jailTime;

    public Player (int id, String name, int current, int initialMoney, int jailTime) {
        this.id = id;
        this.name = name;
        this.current = current;
        this.money = initialMoney;
        this.jailTime = jailTime;
    }

    public int getId() {return id;}
    public String getName() {return this.name;}
    public int getCurrent() {return this.current;}
    public int getMoney() {return this.money;}
    public int getJailTime() {return this.jailTime;}

    public void setName(String newName) {this.name = newName;}
    public void setCurrent(int newPosition) {this.current = newPosition;}
    public void setMoney(int newMoney) {this.money = newMoney;}
    public void setJailTime(int newTime) {this.jailTime = newTime;}

    public void movement(int dice){
        int cur = getCurrent();
        int goTo = cur + dice; //new position
        int passGo = 0; //number of GO passed
        List<Integer> goSqNum = new ArrayList<>(); //list of square number of GO

        for (Square square: GameBoard.getSquares().values()){
            if (square.getType().compareTo("GO") == 0){
                goSqNum.add(square.getSquarenum());
            }
        }
        if (goTo > 20){
            goTo-= 20;
        }
        for (int i = cur+1; i != goTo+1; i++){ //count the number of GO passed
            if (i > 20){
                i = 1;
            }
            if (goSqNum.isEmpty()) break;
            int index = 0;
            for (int num: goSqNum){
                if (i == num){
                    passGo++;
                    goSqNum.remove(index);
                    break;
                }
                if (goSqNum.isEmpty()) break;
                index++;
            }
        }
        this.setMoney(this.getMoney() + 1500*passGo);
        this.setCurrent(goTo);

        if (passGo > 0){
            System.out.printf("You have passed %d GO and get $%d\n",passGo,1500*passGo);
            System.out.println("Your current position is "+ goTo);
            System.out.println("You now have $"+this.getMoney());
        } else System.out.println("Your current position is "+ goTo);
    }

    public static int throwDice(){ //called when player want to throw a dice
        try{
            int dice = (int)(Math.random() * 4) + 1; //give a random number of 1 to 4
            System.out.println("The result is "+ dice);
            return dice;
        }catch(Exception e){
            System.out.println("An error occurred throwing the dice: " + e.getMessage());
            throw e;
        }
    }

    public boolean isRetired(){ //function to check if the player is retired
        try {
            if (this.money < 0) { //the player have negative amount of money
                System.out.println("The player " + getName() + " is bankrupted!");

                Map<Integer, Square> properties = GameBoard.getSquares();
                for (Square property : properties.values()) { //release the properties owned by the bankrupted player
                    Player owner = property.getOwner();
                    if (owner == this) {
                        property.setOwner(null);
                        property.setOwned(false);
                    }
                }
                return true;
            } else return false;
        }catch (Exception e){
            System.out.println("An error occurred while checking if the player is retired: " + e.getMessage());
            throw e;
        }
    }

    public void chance() {    //called when player lands on chance
        try {
            double i = Math.random() * 2;
            int chance;
            if (i > 1) {
                chance = (int) (Math.random() * 200) + 1;
                System.out.println("You get $" + chance);
            } else {
                chance = (int) (Math.random() * -300) + 1;
                System.out.println("You are charged $" + chance);
            }
            setMoney(getMoney() + chance);
            System.out.println("You have $" + getMoney() + " left.");
        }catch (Exception e) {
            System.out.println("An error occurred during chance: " + e.getMessage());
            throw e;
        }
    }

    public void goToJail () {  //called when player lands on Go to Jail
        try {
            int jailNum = 6; //6 is default In Jail square, can be changed
            for (Square property : GameBoard.getSquares().values()) {
                if (property.getType().equals("JAIL")) {
                    jailNum = property.getSquarenum();
                }
            }
            this.setCurrent(jailNum);
            this.setJailTime(3);
            System.out.printf("You are at square %d, which is the jail.\n", jailNum);
            System.out.println("For the following 3 rounds, you can choose to pay $150 or throwing doubles for getting out of the jail. Good Luck!");
        } catch (Exception e) {
            System.out.println("An error occurred while sending player to jail: " + e.getMessage());
            throw e;
        }
    }

    public void incomeTax() {   //called when player lands on Income Tax
        try {
            int tax = (int) (this.money * 0.1);
            tax = (tax / 10) * 10; //round down to a multiple of 10
            this.money -= tax;
            System.out.println("You have been taxed for $" + tax + ".\nYou have $" + this.money + " left.");
        } catch(Exception e) {
            System.out.println("An error occurred during income tax: " + e.getMessage());
            throw e;
        }
    }
}
