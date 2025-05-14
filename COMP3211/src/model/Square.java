package model;

public class Square {
    private int squarenum;
    private String name;
    private String type; //type of square (eg property, chance go,etc)
    private int price;
    private int rent;
    private boolean owned;
    private Player owner;

    public Square (int squarenum, String type, String name, int price, int rent){
        this.squarenum =squarenum;
        this.name = name;
        this.type = type;
        this.price = price;
        this.rent = rent;
        this.owned = false;
        this.owner = null;
    }

    //Scanner scanner = new Scanner(System.in);

    public int getSquarenum() {return this.squarenum;}
    public String getName() {return this.name;}
    public String getType() {return this.type;}
    public int getPrice() {return this.price;}
    public int getRent() {return this.rent;}
    public boolean isOwned() {return this.owned;}
    public Player getOwner() {return this.owner;}

    public void setSquareNum(int newSquareNum){this.squarenum = newSquareNum;}
    public void setName(String newName) {this.name = newName;}
    public void setType(String newType) {this.type = newType;}
    public void setPrice(String newPrice) { this.price = Integer.parseInt(newPrice);}
    public void setRent(String newRent) {this.rent = Integer.parseInt(newRent);}
    public void setOwned(boolean owned){this.owned = owned;}
    public void setOwner(Player owner){this.owner = owner;}

    public void buy(Player buyer){ //called when player want to buy the property
        if (buyer.getMoney() > getPrice()){ //check whether buyer have enough money on hand
            this.owned = true;
            this.owner = buyer;
            buyer.setMoney(buyer.getMoney() - this.price);
            System.out.println("You successfully buy "+ getName());
        } else System.out.println("You do not have enough money to buy this property.");
    }

    public void rentPayment(Player player){ //called when player step on property that owned by other player
        if (this.owner != player){
            player.setMoney(player.getMoney() - this.rent);
            this.owner.setMoney(this.owner.getMoney() + this.rent);
        }
    }
}
