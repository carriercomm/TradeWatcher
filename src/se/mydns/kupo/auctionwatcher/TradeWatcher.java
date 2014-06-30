package se.mydns.kupo.auctionwatcher;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.List;

/**
 * Main class for TradeWatcher
 */
class TradeWatcher {
    private final ArrayList<String> lines = new ArrayList<>();
    private ArrayList<HashMap<String,String>> auctions = new ArrayList<>();
    private final AuctionMatcher matcher = new AuctionMatcher();
    private final AuctionParser parser = new AuctionParser();
    private ArrayList<HashMap<String, String>> newMatches = new ArrayList<>();
    private ArrayList<HashMap<String,String>> matches = new ArrayList<>();
    private WatchListFrame frame;

    private TradeWatcher() {
        setup();
        parse();
        match();
    }

    private void match() {
        newMatches.clear();
        boolean paus = false;
        while(!paus) {
            getAuctionFeed();
            parse();
            frame.addStatus("Matching patterns.");

//            newMatches = matcher.checkWTS(auctions, matches);
//            addMatches();

            newMatches = matcher.checkWTB(auctions);
            addMatches();

            try { Thread.sleep(30000); } catch (InterruptedException e) { e.printStackTrace(); }
        }

    }


    private void addMatches() {
        if (!newMatches.isEmpty()) {
            ArrayList<HashMap<String, String>> temp = (ArrayList<HashMap<String, String>>) matches.clone();
            for(HashMap<String, String> newMatch : newMatches) {
                boolean gotPreviousMatch = false;
                for(HashMap<String, String> oldMatch : temp) {
                    if(newMatch.get("Seller").equals(oldMatch.get("Seller")) &&
                            newMatch.get("Match").equals(oldMatch.get("Match"))) {
                        temp.remove(oldMatch);
                        temp.add(newMatch);
                        gotPreviousMatch = true;
                    }
                }
                if(!gotPreviousMatch) {
                    temp.add(newMatch);
                    frame.notify(newMatch.get("Auction"));
                }
            }
            newMatches.clear();
            matches = (ArrayList<HashMap<String, String>>) temp.clone();
            frame.updateMatches(matches);
        }
    }

    private void parse() {
        auctions.clear();
        auctions = parser.parse(lines);
    }

    private void setup() {
        frame = new WatchListFrame();
        populateMatcher();
    }

    private void populateMatcher() {
        frame.addStatus("Populating matcher patterns.");
        String slash = FileSystems.getDefault().getSeparator();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("." + slash + "res" + slash + "sell.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                frame.addSellItem(line);
                matcher.addSellingPattern(line);
            }
            br.close();

            br = new BufferedReader(new FileReader("." + slash + "res" + slash + "buy.txt"));
            while ((line = br.readLine()) != null) {
                frame.addBuyItem(line);
                matcher.addShoppingPattern(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAuctionFeed() {
        URL ahungry;
        lines.clear();
        try {
            frame.addStatus("Getting auction feed...");
            ahungry = new URL("http://ahungry.com/eqauctions/?");
            HttpURLConnection connection = (HttpURLConnection) ahungry.openConnection();

            connection.setRequestMethod("POST");

            connection.setDoInput(true); // We want to use this connection for both input and output
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.getResponseMessage();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            while((inputLine = in.readLine()) != null)  {
                lines.add(inputLine);
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String args[]) {
        new TradeWatcher();
    }
}
