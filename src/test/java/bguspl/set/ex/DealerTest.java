package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DealerTest {

    Table table;
    Dealer dealer;
    private Integer[] slotToCard;
    private Integer[] cardToSlot;
    Player[] players;


    @BeforeEach
    void setUp() {

        Properties properties = new Properties();
        properties.put("Rows", "2");
        properties.put("Columns", "2");
        properties.put("FeatureSize", "3");
        properties.put("FeatureCount", "4");
        properties.put("TableDelaySeconds", "0");
        properties.put("PlayerKeys1", "81,87,69,82");
        properties.put("PlayerKeys2", "85,73,79,80");
        MockLogger logger = new MockLogger();
        Config config = new Config(logger, properties);
        slotToCard = new Integer[12];
        cardToSlot = new Integer[config.deckSize];

        Env env = new Env(logger, config, new MockUserInterface(), new MockUtil());
        table = new Table(env, slotToCard, cardToSlot);
        Player[] players = new Player[config.players];
        for(int i = 0; i < players.length; i++)
        {
            players[i] = new Player(env, dealer, table, i, false);
        }
        dealer = new Dealer(env, table, players);
    }


    private void fillAllSlots() {
        for (int i = 0; i < slotToCard.length; ++i) {
            table.slotToCard[i] = i;
            table.cardToSlot[i] = i;
        }
    }

    private void removeAllSlots() {
        for (int i = 0; i < slotToCard.length; ++i) {
            table.slotToCard[i] = null;
            table.cardToSlot[i] = null;
        }
    }

    /* Our tests */

    @Test
    void removeCardsFromTable() {
        dealer.legalSet = true;
        fillAllSlots();
        dealer.cardsOfSet[0] = 20;
        dealer.cardsOfSet[1] = 30;
        dealer.cardsOfSet[2] = 2;
        dealer.slotsOfSet[0] = 2;
        dealer.slotsOfSet[1] = 3;
        dealer.slotsOfSet[2] = 0;
        table.cardToSlot[dealer.cardsOfSet[0]] = 2;
        table.cardToSlot[dealer.cardsOfSet[1]] = 3;
        table.cardToSlot[dealer.cardsOfSet[2]] = 0;
        for(Player player : dealer.players)
        {
            player.tokens.add(3);
        }
        dealer.removeCardsFromTable();
        assertEquals(null, dealer.cardsOfSet[0]);
        assertEquals(null, dealer.cardsOfSet[1]);
        assertEquals(null, dealer.cardsOfSet[2]);
        assertEquals(null, dealer.slotsOfSet[0]);
        assertEquals(null, dealer.slotsOfSet[1]);
        assertEquals(null, dealer.slotsOfSet[2]);
        assertEquals(null, table.slotToCard[2]);
        assertEquals(null, table.slotToCard[3]);
        assertEquals(null, table.slotToCard[0]);
    }

    @Test
    void removeAllCardsFromTable() {
        fillAllSlots();
        for(Player player : dealer.players)
        {
            player.tokens.add(3);
        }
        dealer.removeAllCardsFromTable();
        assertEquals(null, table.slotToCard[2]);
        assertEquals(null, table.slotToCard[3]);
        assertEquals(null, table.slotToCard[0]);
        assertEquals(true, dealer.getDeck().size() > 0);

    }

    @Test
    void placeCardsOnTable() {
        removeAllSlots();
        assertEquals(false, table.slotToCard[2] != null);
        assertEquals(false, table.slotToCard[1] != null);
        assertEquals(false, table.slotToCard[3] != null);
        assertEquals(false, table.slotToCard[0] != null);
        dealer.placeCardsOnTable();
        assertEquals(true, table.slotToCard[2] != null);
        assertEquals(true, table.slotToCard[1] != null);
        assertEquals(true, table.slotToCard[3] != null);
        assertEquals(true, table.slotToCard[0] != null);


    }


 

    static class MockUserInterface implements UserInterface {
        @Override
        public void dispose() {}
        @Override
        public void placeCard(int card, int slot) {}
        @Override
        public void removeCard(int slot) {}
        @Override
        public void setCountdown(long millies, boolean warn) {}
        @Override
        public void setElapsed(long millies) {}
        @Override
        public void setScore(int player, int score) {}
        @Override
        public void setFreeze(int player, long millies) {}
        @Override
        public void placeToken(int player, int slot) {}
        @Override
        public void removeTokens() {}
        @Override
        public void removeTokens(int slot) {}
        @Override
        public void removeToken(int player, int slot) {}
        @Override
        public void announceWinner(int[] players) {}
    };

    static class MockUtil implements Util {
        @Override
        public int[] cardToFeatures(int card) {
            return new int[0];
        }

        @Override
        public int[][] cardsToFeatures(int[] cards) {
            return new int[0][];
        }

        @Override
        public boolean testSet(int[] cards) {
            return false;
        }

        @Override
        public List<int[]> findSets(List<Integer> deck, int count) {
            return null;
        }

        @Override
        public void spin() {}
    }

    static class MockLogger extends Logger {
        protected MockLogger() {
            super("", null);
        }
    }
}
