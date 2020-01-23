import java.util.*;
import java.io.*;

public class SnowmanRunner {

    /* You are allowed to modify the following three class variables. */
    private static int SEED = 54321;
    private static final int ROUNDS = 1;
    private static final boolean VERBOSE = true;
    
    /* DO NOT MODIFY ANYTHING BELOW THIS LINE !!!! */
    public static final int MINLENGTH = 5;
    public static final int MAXLENGTH = 15;
    private static final String allowed = "abcdefghijklmnopqrstuvwxyz";
    public static final char BLANK = '*';
    private static List<String> wordList = new ArrayList<String>();
    private static String[] wordArray;
      
    public static void main(String[] args) {
        Scanner s;
        try {
            s = new Scanner(new File("words.txt"));
        } catch(Exception e) {
            System.out.println("Error opening file words.txt: " + e);
            return;
        }
        int totalCount = 0;
        outer:
        while(s.hasNext()) {
            String word = s.next();
            totalCount++;
            if(word.length() < MINLENGTH || word.length() > MAXLENGTH) { continue; }
            for(int i = 0; i < word.length(); i++) {
                if(allowed.indexOf(word.charAt(i)) == -1) { continue outer; }
            }
            wordList.add(word);
        }
        if(VERBOSE) {
            System.out.printf("Read in %d words, of which %d remain after filtering.\n",
            totalCount, wordList.size());
            System.out.printf("Using seed %d, with %d rounds.\n", SEED, ROUNDS); 
        }
        wordArray = new String[wordList.size()];
        for(int i = 0; i < wordList.size(); i++) {
            wordArray[i] = wordList.get(i);
        }
            
        Random rng = new Random(SEED);
        SnowmanPlayer.startGame(wordArray, MINLENGTH, MAXLENGTH, allowed);
        
        int totalMisses = 0;
        for(int i = 0; i < ROUNDS; i++) {
            int currentMisses = 0;
            String secretWord = wordList.get(rng.nextInt(wordList.size()));
            SnowmanPlayer.startNewWord(secretWord.length());
            String previousGuesses = "";
            if(VERBOSE) { System.out.print(secretWord + ": "); }
            int previousCorrect = -1;
            do {
                String pattern = "";
                int correct = 0;
                for(int j = 0; j < secretWord.length(); j++) {
                    char c = secretWord.charAt(j);
                    if(previousGuesses.indexOf(c) > -1) { pattern += c; correct++; }
                    else { pattern += BLANK; }
                }
                if(correct == secretWord.length()) { break; }
                if(correct == previousCorrect) { 
                    currentMisses++; 
                    if(VERBOSE) { System.out.print('!'); }
                }
                char guess = SnowmanPlayer.guessLetter(pattern, previousGuesses);
                if(VERBOSE) { System.out.print(guess); }
                previousCorrect = correct;
                previousGuesses += guess;
            } while(currentMisses < allowed.length());
            totalMisses += currentMisses;
            if(VERBOSE) { System.out.println(" (" + currentMisses + ")"); }
        }
        System.out.println("Code by '" + SnowmanPlayer.getAuthor() + "' made a total of " 
            + totalMisses + " misses over " + ROUNDS + " words.");
    }
}

