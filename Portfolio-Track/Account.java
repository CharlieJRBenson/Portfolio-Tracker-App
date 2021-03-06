import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Account implements Serializable {
    private String name;
    private float balance = 0;

    protected ArrayList<Asset> assets = new ArrayList<>();

    // constructor
    public Account(String name) {
        this.name = name;

    }

    // deposits to account balance
    public void deposit(float amount) {
        this.balance += amount;
    }

    // withdraws from account balance
    public void withdraw(float amount) {
        this.balance -= amount;
    }

    // calls asset's buy method only when balance is sufficient
    public boolean buy(Asset ass, float total) {
        // if what want to buy is more than acc bal
        if (total > this.balance) {
            return false;
        }

        // check if asset doesnt already exist in list
        if (!assets.contains(ass)) {
            add(ass);
        }

        float quant = total / ass.getPrice();
        ass.buy(quant);
        this.balance -= total;
        return true;
    }

    // calls asset's sell method only when balance is sufficient
    public boolean sell(Asset ass, float total) {
        // total = quantity*price
        float quant = total / ass.getPrice();
        // returns false when asset doesnt exist
        if (!assets.contains(ass)) {
            return false;
        } else if (ass.getTotal() <= total) {
            // if assets quantity is less than or equal to quant
            // getTotal = getQuantity*getPrice
            this.balance += ass.getTotal();
            // remove the asset and credit the total sold
            this.remove(ass);
        } else {
            ass.sell(quant);
            this.balance += total;
        }
        return true;
    }

    // returns account balance
    public float getBal() {
        return this.balance;
    }

    // returns account name
    public String getName() {
        return this.name;
    }

    // returns accounts assets
    public List<Asset> getAssets() {
        return this.assets;
    }

    public float getWorth() {
        float worth = 0;
        for (Asset ass : this.assets) {
            worth += ass.getTotal();
        }
        return worth;
    }

    public Asset getAssetByName(String name) {
        for (Asset ass : assets) {
            if (name.equals(ass.getName())) {
                return ass;
            }
        }
        return null;
    }

    // adds new asset to assets
    private void add(Asset ass) {
        assets.add(ass);
    }

    // removes asset from assets
    private void remove(Asset ass) {
        assets.remove(ass);
    }
}
