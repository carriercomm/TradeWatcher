package se.mydns.kupo.test.auctionwatcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.mydns.kupo.auctionwatcher.AuctionMatcher;
import se.mydns.kupo.auctionwatcher.AuctionParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oskurot on 2014-06-23.
 */
public class TestAuctionMatcher {
    private ArrayList<String> lines = new ArrayList<>();
    private ArrayList<HashMap<String,String>> auctionFeed = new ArrayList<>();

    @Before
    public void setUp() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("C:\\Users\\oskurot\\IdeaProjects\\P99AuctionWatchList\\res\\sample-html.html"));

            String inputLine;
            while((inputLine = in.readLine()) != null)  {
                lines.add(inputLine);
            }
        } catch (Exception e) { e.printStackTrace(); }

        AuctionParser parser = new AuctionParser();
        auctionFeed = parser.parse(lines);
    }

    @Test
    public void testWTSMatcher() {
        AuctionMatcher matcher = new AuctionMatcher();

        matcher.addShoppingPattern("Cobalt Breastplate");

        ArrayList<HashMap<String,String>> matches = matcher.checkShopping(auctionFeed);

        if(matches.size() > 0) {
            for(HashMap<String,String> match : matches) {
                System.out.println(match.get("Auction"));
            }
        } else {
            Assert.fail();
        }
    }

    @Test
    public void testWTBMatcher() {
        AuctionMatcher matcher = new AuctionMatcher();

        matcher.addSellingPattern("Impskin");

        ArrayList<HashMap<String,String>> matches = matcher.checkSelling(auctionFeed);

        if(matches.size() > 0) {
            for(HashMap<String,String> match : matches) {
                System.out.println(match.get("Auction"));
            }
        } else {
            Assert.fail();
        }
    }
}
