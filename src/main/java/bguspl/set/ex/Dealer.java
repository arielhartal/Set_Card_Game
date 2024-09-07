package bguspl.set.ex;

import bguspl.set.Env;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    protected final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    
    protected Integer[] cardsOfSet;
    protected Integer[] slotsOfSet;

    protected ConcurrentLinkedQueue<Player> playersQueue;

    protected Thread dealerThread;
    protected boolean legalSet;
    private long startTime;
    private long elapsedTime;
    private int NumOfEmptySlots;
    private boolean placingCards;
    private int waitingTime;
    private int setSize;
    private int lastSlotOfTable;
    private int lastIndexOfSet;
    private long addedTime;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        setSize = 3;
        cardsOfSet = new Integer[setSize];
        slotsOfSet = new Integer [setSize];
        playersQueue = new ConcurrentLinkedQueue<Player>();
        legalSet = false;
        startTime = System.currentTimeMillis();
        NumOfEmptySlots = 12;
        placingCards = true;
        waitingTime = 5;
        lastSlotOfTable = 11;
        addedTime = 500;
        lastIndexOfSet = 2;
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        this.dealerThread = Thread.currentThread();


         // create the player threads
        for(Player player : players)
        {
             Thread playerThread = new Thread(player, "player");
             playerThread.start();         
        }


        while (!shouldFinish()) {
            placeCardsOnTable();
            timerLoop();       
            updateTimerDisplay(false);
            playersQueue.clear();
            this.env.ui.removeTokens();
            removeAllCardsFromTable();
            
        }
        announceWinners();
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
        try {
            this.wait(env.config.endGamePauseMillies);
        } catch (InterruptedException e) {}
        env.ui.dispose();
        notifyAll();
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        resetReshuffleTime();
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            checkAllPlayersSets();             
            removeCardsFromTable();
            placeCardsOnTable();
            if(env.config.hints) table.hints();
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        terminate = true;
        synchronized(this)
        {
            this.notifyAll();
        }
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks if any cards should be removed from the table and returns them to the deck.
     */
    protected void removeCardsFromTable() {
        if(legalSet)
        {
            placingCards = true;
            if(cardsOfSet[0] != null)
            {
                for(int i = 0; i < cardsOfSet.length; i++)
                {
                    table.removeCard(table.cardToSlot[cardsOfSet[i]]);
                    for(Player Aplayer : players)
                        Aplayer.removeTokensFromTable(slotsOfSet[i]);
                    cardsOfSet[i] = null;
                    slotsOfSet[i] = null;
                    NumOfEmptySlots++;

                }
            }
            
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    protected void placeCardsOnTable() {
        if(!deck.isEmpty()){
            Collections.shuffle(deck);            
            int slot = (int) ((Math.random() * (NumOfEmptySlots-1)));
            while(!deck.isEmpty() && NumOfEmptySlots > 0 && !terminate)
            {
                while(table.slotToCard[slot] != null){
                    if(slot != lastSlotOfTable)
                        slot++;
                    else
                        slot = 0;
                }     
                table.placeCard(deck.remove(0), slot);
                NumOfEmptySlots--;
                slot = (int) ((Math.random() * (NumOfEmptySlots-1)));                               
            }
        }
        placingCards = false;
    
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        synchronized(this)
        {
            try{
                this.wait(waitingTime);
            }
            catch(InterruptedException e){}
        }   
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {

        boolean red = false;
        elapsedTime = startTime + (this.env.config.turnTimeoutMillis) - System.currentTimeMillis();
        if(elapsedTime > env.config.turnTimeoutWarningMillis){
            elapsedTime += addedTime;
        }

        else if(elapsedTime <= env.config.turnTimeoutWarningMillis){
            red = true;
        }

        if(elapsedTime <= 0)
            elapsedTime = 0;
        if(elapsedTime <= 0)
            elapsedTime = 0;
        this.env.ui.setCountdown(elapsedTime, red);
        if(elapsedTime + addedTime <= 0 || reset)
        {
            startTime = System.currentTimeMillis();          
        }

    }

    /**
     * Returns all the cards from the table to the deck.
     */
    protected void removeAllCardsFromTable() {
        placingCards = true;
        for(int i = 0; i < this.table.slotToCard.length; i++)
        {
            if(table.slotToCard[i] != null)
            {
                deck.add(this.table.slotToCard[i]);
                table.removeCard(i);
                NumOfEmptySlots++;
                for(Player Aplayer : players)
                    Aplayer.removeAllTokensFromTable();
                
            }
        }
    }


    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {

        int[] allPlayersScores = new int [players.length];
        LinkedList<Integer> allWinnersIds = new LinkedList<>();
        for(Player player : players)
        {
            allPlayersScores[player.id] = player.getScore();
        }

        int maxScore = allPlayersScores[0];
        for(int i = 0; i < allPlayersScores.length; i++)
        {
            if(allPlayersScores[i] > maxScore)
            {
                maxScore = allPlayersScores[i];
            }
        }
    
        for(int i = 0; i < allPlayersScores.length; i++)
        {
            if(allPlayersScores[i] == maxScore)
            {
                allWinnersIds.add(i);
            }
        }
    
        int[] winners = new int [allWinnersIds.size()];
    
        for(int i = 0; i < winners.length; i++)
        {
            winners[i] = allWinnersIds.get(i);
        }
    
        this.env.ui.announceWinner(winners);
            
    }
    

    public List<Integer> getDeck()
    {
        return deck;
    }


     /**
     * Checking if a set chosen by the player is a legal set.
     *
     * @param player - the player that his set is being checked.
     * @post - If set is legal the cards of the set are removed from the table.
     * @post - If set is legal we reward the player.
     * @post - If set is legal we reset the timer.
     * @post - If set is ilegal we penalize the player.
     * @post - We notify all of the player that waited for the dealer to check their set.
     * @return - True if the player's set is legal and false if illegal.
     */
    public boolean checkSet(Player player)
    {

        int[] setToCheck = new int[setSize];
        for(int i = 0; i < setToCheck.length; i++)
        {
            if(cardsOfSet[i] != null)
                setToCheck[i] = cardsOfSet[i];
            else
                setToCheck[i] = -1;
        }

        if(this.env.util.testSet(setToCheck))
        {
            legalSet = true;       
            removeCardsFromTable();
            player.point();
            resetReshuffleTime();
            resetElapsedTime();
        }

        else if(slotsOfSet[lastIndexOfSet] != null)
        {
            legalSet = false;
            player.penalty();
        }

        for(Player aPlayer: players)
            aPlayer.wakePlayer();


        return legalSet;

    }

    public void wakeDealer()
    {
        synchronized(this)
        {
            this.notifyAll();
        }
    }

    public Thread getDealerThread()
    {
        return this.dealerThread;
    }

    
     /**
     * Removing a player from the players queue and checking his set (calling the check set method).
     * @post - The player's set is checked by the dealer.
     */
    public void checkAllPlayersSets()
    {

        if(!playersQueue.isEmpty())
        {
            Player player = playersQueue.remove();
            int i = 0;
            if (player.tokens.size() == setSize) {  

                for(Integer slot : player.tokens)
                {
                    cardsOfSet[i] = table.slotToCard[slot];
                    slotsOfSet[i] = slot;
                    i++;

                }

                this.checkSet(player);
                cardsOfSet = new Integer[setSize];
                slotsOfSet = new Integer[setSize];
            }
        }
        
    }

    private void resetReshuffleTime()
    {
        startTime = System.currentTimeMillis();
        reshuffleTime = System.currentTimeMillis() + this.env.config.turnTimeoutMillis;
    }

    private void resetElapsedTime()
    {
        startTime = System.currentTimeMillis();
        elapsedTime = startTime + this.env.config.turnTimeoutMillis - System.currentTimeMillis();
    }

    public boolean PlacingCards(){
        return placingCards;
    }
}
