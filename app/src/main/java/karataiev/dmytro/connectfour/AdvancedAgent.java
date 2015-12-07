package karataiev.dmytro.connectfour;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.Random;

public class AdvancedAgent extends Agent {
    Random r = new Random();

    public AdvancedAgent(Connect4Game var1, boolean var2) {
        super(var1, var2);
    }

    public void move() {
        boolean var1 = false;
        int var2 = this.canWin(this.iAmRed);
        int var3 = this.canWin(!this.iAmRed);
        int var4;
        if(var2 >= 0) {
            var4 = var2;
        } else if(var3 >= 0) {
            var4 = var3;
        } else {
            var4 = this.randomMove();
        }

        this.moveOnColumn(var4);
    }

    public int randomMove() {
        int var1;
        for(var1 = this.r.nextInt(this.myGame.getColumnCount()); this.getTopEmptySlot(this.myGame.getColumn(var1)) == null; var1 = this.r.nextInt(this.myGame.getColumnCount())) {
            ;
        }

        return var1;
    }

    public void moveOnColumn(int var1) {
        Connect4Slot var2 = this.getTopEmptySlot(this.myGame.getColumn(var1));
        if(var2 != null) {
            if(this.iAmRed) {
                var2.addRed();
            } else {
                var2.addYellow();
            }
        }

    }

    public Connect4Slot getTopEmptySlot(Connect4Column var1) {
        int var2 = -1;

        for(int var3 = 0; var3 < var1.getRowCount(); ++var3) {
            if(!var1.getSlot(var3).getIsFilled()) {
                var2 = var3;
            }
        }

        if(var2 < 0) {
            return null;
        } else {
            return var1.getSlot(var2);
        }
    }

    public int canWin(boolean var1) {
        for(int var2 = 0; var2 < this.myGame.getColumnCount(); ++var2) {
            int var3 = this.getTopEmptyIndex(this.myGame.getColumn(var2));
            if(var3 > -1) {
                if(var3 < this.myGame.getRowCount() - 3 && this.myGame.getColumn(var2).getSlot(var3 + 1).getIsRed() == var1 && this.myGame.getColumn(var2).getSlot(var3 + 2).getIsRed() == var1 && this.myGame.getColumn(var2).getSlot(var3 + 3).getIsRed() == var1) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 3 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 1).getSlot(var3), this.myGame.getColumn(var2 + 2).getSlot(var3), this.myGame.getColumn(var2 + 3).getSlot(var3))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 2 && var2 > 0 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3), this.myGame.getColumn(var2 + 1).getSlot(var3), this.myGame.getColumn(var2 + 2).getSlot(var3))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 1 && var2 > 1 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3), this.myGame.getColumn(var2 + 1).getSlot(var3), this.myGame.getColumn(var2 - 2).getSlot(var3))) {
                    return var2;
                }

                if(var2 > 2 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3), this.myGame.getColumn(var2 - 3).getSlot(var3), this.myGame.getColumn(var2 - 2).getSlot(var3))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 3 && var3 < this.myGame.getRowCount() - 3 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 1).getSlot(var3 + 1), this.myGame.getColumn(var2 + 3).getSlot(var3 + 3), this.myGame.getColumn(var2 + 2).getSlot(var3 + 2))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() && var2 > 2 && var3 < this.myGame.getRowCount() && var3 > 2 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3 - 1), this.myGame.getColumn(var2 - 2).getSlot(var3 - 2), this.myGame.getColumn(var2 - 3).getSlot(var3 - 3))) {
                    return var2;
                }

                if(var2 > 2 && var2 < this.myGame.getColumnCount() && var3 < this.myGame.getRowCount() - 3 && var3 >= 0 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3 + 1), this.myGame.getColumn(var2 - 2).getSlot(var3 + 2), this.myGame.getColumn(var2 - 3).getSlot(var3 + 3))) {
                    return var2;
                }

                if(var2 >= 0 && var2 < this.myGame.getColumnCount() - 3 && var3 < this.myGame.getRowCount() && var3 > 2 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 3).getSlot(var3 - 3), this.myGame.getColumn(var2 + 2).getSlot(var3 - 2), this.myGame.getColumn(var2 + 1).getSlot(var3 - 1))) {
                    return var2;
                }
            }
        }

        return -1;
    }

    public boolean checkIfEqual(boolean var1, Connect4Slot var2, Connect4Slot var3, Connect4Slot var4) {
        return var2.getIsFilled() && var3.getIsFilled() && var4.getIsFilled() && var2.getIsRed() == var1 && var3.getIsRed() == var1 && var4.getIsRed() == var1;
    }

    public int getTopEmptyIndex(Connect4Column var1) {
        int var2 = -1;

        for(int var3 = 0; var3 < var1.getRowCount(); ++var3) {
            if(!var1.getSlot(var3).getIsFilled()) {
                var2 = var3;
            }
        }

        return var2;
    }

    public String getName() {
        return "Advanced Agent";
    }
}
