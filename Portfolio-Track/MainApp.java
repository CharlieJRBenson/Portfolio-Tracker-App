import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MainApp {

    public static void main(String[] args) {

        // setup
        JFrame frame = new JFrame("Portfolio");
        MainPanel app = new MainPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(app);

        // sets up all button event listeners
        addAllBtnListeners(app);

        // packs and sets as visible
        frame.pack();
        frame.setVisible(true);

    }

    public static void buyAsset(MainPanel app) {
        float total = Float.parseFloat(app.amountTf.getText());
        String name = app.shareTf.getText();
        Account acc = getSelected(app);

        Asset ass = acc.getAssetByName(name);
        if (ass != null) {
            // buys more of existing share
            acc.buy(ass, total);
            // THROW PROMPT BOUGHT SHARE
        } else {
            Account fu = getAccByName(app, name);
            if (fu != null) {
                // check if buying fund and if fund exists
                acc.buy((Asset) fu, total);
                // THOW PROMPT BOUGHT FUND
            } else {
                // creates new share
                Share sh = new Share(name);
                sh.getAPIResp();
                acc.buy(sh, total);

                // THROW PROMPT BOUGHT SHARE
            }

        }

        refreshAccountInfo(app);
    }

    public static void sellAsset(MainPanel app) {
        float total = Float.parseFloat(app.amountTf.getText());
        String name = app.shareTf.getText();
        Account acc = getSelected(app);

        Asset ass = acc.getAssetByName(name);
        if (ass != null) {
            acc.sell(ass, total);

            // THROW PROMPT SOLD ASSET
        } else {
            // THROW PROMPT ASSET NOT OWNED
        }

        refreshAccountInfo(app);

    }

    public static Fund createFund(String name) {
        // creates new fund
        return new Fund(name);
    }

    public static User createUser(String name) {
        // creates new user
        return new User(name);
    }

    public static void deposit(MainPanel app) {
        Account acc = getSelected(app);
        float amount = Float.parseFloat(app.depositTf.getText());
        if (acc == null)
            return;
        acc.deposit(amount);
        // update balance label
        app.balanceLbl.setText("Balance: $" + Float.toString(acc.getBal()));
        app.depositTf.setText("");
    }

    public static void withdraw(MainPanel app) {
        Account acc = getSelected(app);
        float amount = Float.parseFloat(app.depositTf.getText());

        if (acc == null)
            return;
        acc.withdraw(amount);

        // update balance labels
        app.balanceLbl.setText("Balance: $" + Float.toString(acc.getBal()));
        app.depositTf.setText("");
    }

    public static Account getAccByName(MainPanel app, String name) {
        // returns account by name if it exists in existing list
        for (Account acc : app.accList) {
            if (name.equals(acc.getName())) {
                return acc;
            }
        }
        return null;
    }

    public static Account getSelected(MainPanel app) {
        // gets account string from menu and returns its concurrent Account
        String accName = app.accountsMenu.getSelectedItem().toString();
        return getAccByName(app, accName);
    }

    public static void deleteSelected(MainPanel app) {
        // deletes selected user

        // THROW PROMPT ARE YOU SURE

        // get account currently selected by menu
        Account acc = getSelected(app);

        if (app.accModel.getSize() <= 1) {
            return;
        }

        // remove this from accounts arraylist & menu drop down
        app.accModel.removeElement(acc.getName());
        app.accList.remove(acc);

        refreshAccountInfo(app);

    }

    public static void createAcc(MainPanel app, boolean isFund) {
        // method runs when new fund or user btn is pressed

        String txt = app.userNameTf.getText();
        if (txt.equals("") || getAccByName(app, txt) != null) {
            // THROW PROMPT ERROR
            return;
        }

        // creates new fund or user
        Account acc;
        if (isFund) {
            acc = createFund(txt);
        } else {
            acc = createUser(txt);
        }

        // adds new account to account list and to menu model
        addAccount(app, acc);

        // resets textbox
        app.userNameTf.setText("");
    }

    public static void addAccount(MainPanel app, Account acc) {
        // adds new account to account list and to menu model

        app.accList.add(acc);
        app.accModel.addElement(acc.getName());
        // sets selected to added account
        app.accountsMenu.setSelectedItem(acc.getName());

    }

    public static void refreshAccountInfo(MainPanel app) {
        // get account currently selected by menu
        Account acc = getSelected(app);

        if (acc == null) {
            // THROW PROMPT ERROR
            return;
        }

        app.accTypeLbl.setText("Account Type: " + acc);
        app.balanceLbl.setText("Balance: $" + Float.toString(acc.getBal()));
        app.userNameTf.setText("");
        app.depositTf.setText("");
        app.pathTf.setText("");
        app.historyTA.setText("");

        // refreshes portfolio lists and values etc
        refreshPortfolio(app, acc);

    }

    public static void fileReadWrite(MainPanel app, boolean importing) {
        String path = app.pathTf.getText();

        if (importing) {
            try {
                // opens new file stream with append
                FileInputStream fi = new FileInputStream(new File(path));
                ObjectInputStream accIn = new ObjectInputStream(fi);

                // reading account file
                Account a = (Account) accIn.readObject();
                Account exist = getAccByName(app, a.getName());

                // if account exists - update that account
                if (exist == null) {
                    addAccount(app, a);
                    // THROW PROMPT ADDED ACCOUNT
                }

                accIn.close();

            } catch (Exception e) {
                // THROWS PROMPT ERROR e
                System.out.println(e);
            }

        } else {
            try {
                // opens new file stream for account data
                FileOutputStream f = new FileOutputStream(new File(path));
                ObjectOutputStream accOut = new ObjectOutputStream(f);

                // getting selected account
                Account acc = getSelected(app);
                // writing account to file
                accOut.writeObject(acc);

                accOut.close();

                // THROW PROMPT SAVED ACCOUNT

            } catch (Exception e) {
                // THROWS PROMPT ERROR e
            }
        }
    }

    // called when share is selected from list
    public static void displayShare(MainPanel app) {
        Account acc = getSelected(app);
        String name = app.portfolioLst.getSelectedValue();
        name = name.substring(0, name.indexOf('-'));

        Asset ass = acc.getAssetByName(name);
        if (ass == null) {
            return;
        }

        app.selectedLbl.setText("Selected: " + ass.getName());
        app.currPriceLbl.setText("$/Share: $" + ass.getPrice());

        // checks if "real data" toggle selected
        if (app.apiTgl.isSelected()) {
            app.historyTA.setText(ass.getAPIResp());
        } else {
            updateAllPrices(app);
            printHistory(app, ass);
        }

        app.portfolioLbl.setText("Portfolio Worth: $" + acc.getWorth());

    }

    public static void printHistory(MainPanel app, Asset ass) {
        app.historyTA.setText("");
        for (Pair<Date, Float> pair : app.priceHistMap.get(ass)) {
            app.historyTA.append("\n" + pair.getFirst() + "      $" + pair.getSecond());
        }
    }

    public static void refreshPortfolio(MainPanel app, Account acc) {

        // clears old portfolio
        app.portfolioModel.removeAllElements();

        // refreshes new portfolio list
        for (Asset ass : acc.assets) {
            // prints share data
            app.portfolioModel.addElement(ass.getName() + "--" + ass.getQuantity() + "shares");
        }
        app.portfolioLbl.setText("Portfolio Worth: $" + acc.getWorth());
    }

    public static void updateAllPrices(MainPanel app) {

        try {
            // acc can be null
            Account acc = getSelected(app);
            // gets current time as Date type
            Date time = Calendar.getInstance().getTime();

            // write current price to history

            // iterate each asset in acocunt
            for (int i = 0; i < app.portfolioModel.size(); i++) {
                String name = app.portfolioModel.getElementAt(i);
                name = name.substring(0, name.indexOf("--"));

                Asset ass = acc.getAssetByName(name);
                if (ass == null) {
                    continue;
                }
                // gets current arraylist of price data
                ArrayList<Pair<Date, Float>> prices = app.priceHistMap.get(ass);
                if (prices == null) {
                    prices = new ArrayList<>();
                }

                // append to start of price data array
                prices.add(0, new Pair<>(time, ass.getPrice())); // new instance of pair class generic types

                // updates price arraylist entry for asset
                app.priceHistMap.put(ass, prices);

                // create new prices
                newPrices(acc);
            }
        } catch (NullPointerException e) {
            // THROW PROMPT NO ACCOUNT

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // create new prices method
    public static void newPrices(Account acc) {
        for (Asset ass : acc.assets) {
            ass.update();
        }
    }

    // FOLLOWING ARE ALL THE BUTTON LISTENERS CALLING RESPECTIVE METHOD
    public static void addAllBtnListeners(MainPanel app) {

        // create user button
        app.addBtnListener(app.createUserBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAcc(app, false);
            }
        });

        // create fund button
        app.addBtnListener(app.createFundBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAcc(app, true);
            }
        });

        // delete account button
        app.addBtnListener(app.deleteBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected(app);
            }
        });

        // new account selected
        app.accountsMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAccountInfo(app);
            }
        });

        // save account button
        app.addBtnListener(app.saveBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileReadWrite(app, false);

            }
        });

        // import account button
        app.addBtnListener(app.importBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileReadWrite(app, true);

            }
        });

        // buy share button
        app.addBtnListener(app.buyShareBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buyAsset(app);

            }
        });

        // sell button
        app.addBtnListener(app.sellBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sellAsset(app);

            }
        });

        // deposit button
        app.addBtnListener(app.depositBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deposit(app);

            }
        });

        // withdraw button
        app.addBtnListener(app.withdrawBtn, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                withdraw(app);
            }
        });

        // new share selected
        app.portfolioLst.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                displayShare(app);
            }
        });

        // toggle real data
        app.addBtnListener(app.apiTgl, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayShare(app);
            }
        });
    }

}
