package bguspl.set.ex;

import java.lang.Thread.State;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    /* */


    private Dealer dealer;
    private BlockingQueue<Integer> actionsQueue;
    protected BlockingQueue<Integer> tokens;
    private boolean tired;
    private boolean tooTired;
    private long panTime;
    private long celebrationTime;
    private int numOfTokens;
    private long addedTime;
    private long sleepTimePenalty;
    private long sleepTimeTired;
 

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        
        /* */
        this.dealer = dealer;
        numOfTokens = 3;
        actionsQueue = new ArrayBlockingQueue<>(numOfTokens);
        tokens = new ArrayBlockingQueue<>(numOfTokens);
        tired = false;
        tooTired = false;
        panTime = 0;
        celebrationTime = 0;
        addedTime = 1000;
        sleepTimePenalty = 100;
        sleepTimeTired = 500;


    }


    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {

    
        playerThread = Thread.currentThread();
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        if (!human) createArtificialIntelligence();
       
        while (!terminate) {
            PlayerPenalty();
            PlayerCelebrate();
            while(!actionsQueue.isEmpty())
            {       
                Integer currentSlot = actionsQueue.remove();
                if(currentSlot != null){                                    
                    if(table.slotToCard[currentSlot] != null){ // Checking if theres a card in the chosen slot on table.
                        if(!tired && !tooTired && !dealer.PlacingCards()){
                                if(tokens.contains(currentSlot)) 
                                { // If the slot was already chosen and is already on table we remove it.
                                    tokens.remove(currentSlot);
                                    this.table.removeToken(this.id, currentSlot);
                                }
                                else if(tokens.size() < numOfTokens){
                                    if(tokens.size() == numOfTokens - 1)
                                    {                                   
                                        tokens.add(currentSlot); 
                                        this.table.placeToken(this.id, currentSlot);
                                        // Player is declearing a set.
                                        // Sending the player to the dealer and waking him up to check the set of the player.
                                        // The player is waiting for dealer to check his set.
                                        dealer.playersQueue.add(this);
                                        synchronized(this)
                                        {
                                            dealer.wakeDealer();
                                            try {
                                                this.wait();
                                            } catch (InterruptedException e) {}
                                        }
                                        
                                    }
                                    else
                                    {
                                        // If its not the last chosen than set is not decleared yet, we place it on the table.
                                        tokens.add(currentSlot);
                                        this.table.placeToken(this.id, currentSlot);
                                    }
                                
                                
                                }
                            }
                    }
                }
            }
        }    
            

        

        synchronized(this)
        {
            this.notifyAll();
        }

        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
            while (!terminate) {
                int tableSize = env.config.tableSize;
                int chosenSlot = (int)(Math.random()*tableSize);
                if(table.slotToCard[chosenSlot] != null)
                    keyPressed(chosenSlot);

            }
            System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
        }, "computer-" + id);
        aiThread.start();
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
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
       
        if(actionsQueue.size() < numOfTokens && table.slotToCard[slot] != null  && Thread.currentThread().getState() != State.WAITING)
        {
            actionsQueue.add(slot);
        }           
       
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {

        if(!tired){
            celebrationTime = System.currentTimeMillis();
            tired = true;
        } 

        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        env.ui.setScore(id, ++score);
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {

        if(!tooTired){
            panTime = System.currentTimeMillis();
            tooTired = true;
        }   

        env.ui.setScore(id, score);
    }
    // Puts the player in the penalty box instead of letting him continue playing
    public void PlayerPenalty() {

        while(tooTired && !terminate){

            long elapsedTime = panTime + this.env.config.penaltyFreezeMillis  - System.currentTimeMillis(); 
            this.env.ui.setFreeze(this.id, elapsedTime);
            try {
                Thread.sleep(sleepTimePenalty);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(elapsedTime <= 0)
            {
                tooTired = false;
            }

            actionsQueue.clear();
        }
      
    }
    //The player celebrates because he found a set, although it is not so smart for him, but until he is done celebrating,
    //he will stay here instead of continuing to play
    public void PlayerCelebrate() {

        while(tired && !terminate)
        {
            long elapsedTime = celebrationTime + this.env.config.pointFreezeMillis + addedTime - System.currentTimeMillis(); 
            this.env.ui.setFreeze(this.id, elapsedTime);
            tired = true;
            if(elapsedTime <= 0)
            {
                tired = false;
            }
            actionsQueue.clear();
        }
        
    }

    
    public void wakePlayer()
    {
        synchronized(this)
        {
           this.notifyAll();
        }
    }

    public int getScore() {
        return score;
    }
    
    public Thread getPlayerThread()
    {
        return playerThread;
    }

    // Put Player Thread to sleep
    public void PlayerTired()
    {

        try {
            Thread.sleep(sleepTimeTired);
            
        } catch (InterruptedException e) {
        
            e.printStackTrace();
        
        }
    }

  
    /**
     * Clearing the tokens queue of player.
     * 
     * @post - tokens queue of player is empty.
     * */
    public void removeAllTokensFromTable()
    {
        tokens.clear();      
    }

    /**
     * Removing the tokens of a selected set from tokens queue of player.
     * 
     * @post - tokens of selected set are removed from the tokens queue of player.
     * @post - the tokens of the chosen set are removed from the table.
     * */
    public void removeTokensFromTable(Integer slotsOfSet)
    {
        if(tokens.contains(slotsOfSet))
        {
            tokens.remove(slotsOfSet);
            this.table.removeToken(this.id, slotsOfSet);
        }    
    }


                
}
