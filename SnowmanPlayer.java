/**
 * @author Sung, Alfred
 */

import java.util.*;
  
public class SnowmanPlayer {
  
    // Global variables
    //In the format of <Length, Words[]>
    static ArrayList<ArrayList<String>> Dictionary = new ArrayList<ArrayList<String>>();
    static ArrayList<String> PossibleWords = new ArrayList<String>();
    static ArrayList<String> PreviousGuess = new ArrayList<String>();
     
    //In the format of <Letter, <Pattern, Count>>
    static HashMap<Character, HashMap<Integer, Integer>> PatternFuckery = new HashMap<Character, HashMap<Integer, Integer>>();
    //In the format of <Letter, <Pattern, Entropy Calculations>>
    static HashMap<Character, HashMap<Integer, Double>> Entropy = new HashMap<Character, HashMap<Integer, Double>>();
    //In the format of <Letter, Summation>
    static HashMap<Character, Double> LetterSummation = new HashMap<Character, Double>();
    //In the format of <Letter, Occurrence>
    static HashMap<Character, Integer> LetterOccurrence = new HashMap<Character, Integer>();
    //In the format of <Letter, Weight>
    static HashMap<Character, Double> LetterWeight = new HashMap<Character, Double>();
    
    static String AllowedChars = "";
    /**
     * Is called only once when the game starts
     * @param words - Possible words that the secret words can be
     * @param minLength - The minimum length that the secret word can be
     * @param maxLength - The maximum length that the secret word can be
     * @param allowedChars - The alphabet
     */
    public static void startGame(String[] words, int minLength, int maxLength, String allowedChars) {
        /* 
        Sort the possible words by length in a 2D array list such that
        word lengths are in rows and words are in columns:
            1   |
            2   |my
            3   |
            4   |This, life
            5   |
            6   |ruined
            ...
            10  |assignment
         */
        for (int i = 0; i < maxLength + 1; i++) {
            // Initialize rows as string word length up to the max length
            Dictionary.add(new ArrayList<String>());
            // Populate columns with words from the given words
            for (int j = 0; j < words.length; j++) {
                if (words[j].length() == i) {
                    Dictionary.get(i).add(words[j]);
                }
            }
        }
        
        AllowedChars = allowedChars;
    }
    
    /**
     * Is called when the Runner starts a new word
     * @param length - The length of the secret word
     */
    public static void startNewWord(int length) {
        // Filter Dictionary into words that have the same length as the secret word
        // using the rows that represent word length
        // Copy the list of possible words into PossibleWords array list
        PossibleWords = new ArrayList<>(Dictionary.get(length));
        //Reset PreviousGuesses
        PreviousGuess = new ArrayList<String>();
    }
    
    /**
     * Is called by the Runner to guess the next letter
     * @param pattern - Gives the positions of already guessed characters
     * @param previousGuesses - A string of previously guessed characters
     * @return - The best possible guess
     */
    public static char guessLetter(String pattern, String previousGuesses) {
        PreviousGuess.add(pattern);
        
        // Ensures that the program is at least on it's second attempt
        if (previousGuesses != "") {
                // Checks if the last attempt was a miss
                if (PreviousGuess.get(PreviousGuess.size() - 1).equals(PreviousGuess.get(PreviousGuess.size() - 2))) {
                        // If the last attempt was a miss, then the word must not include the guessed letter
                        // We can narrow down the list size by filtering out all words that have the guessed letter
                        FilterByExclusion(pattern, previousGuesses);
                } else {
                        // The last guess was a hit, then we can filter out words that do not have the guessed letter
                        // We can increase accuracy by checking for the exact position of the letter in the words
                        FilterByPosition(pattern, previousGuesses);
                }
        }
        
        //Reset tables
        PatternFuckery = new HashMap<Character, HashMap<Integer, Integer>>();
        Entropy = new HashMap<Character, HashMap<Integer, Double>>();
        LetterSummation = new HashMap<Character, Double>();
        LetterOccurrence = new HashMap<Character, Integer>();
        LetterWeight = new HashMap<Character, Double>();

        for (int i = 0; i < AllowedChars.length(); i++){
            //Sort Patterns
            PopulatePatternCount(AllowedChars.charAt(i), previousGuesses);
        }
        CalculatePatternEntropy();
        CalculateLetterWeight();
        CalculateLetterOccurrence(previousGuesses);
        
        //We can find the best possible guess using the equation
        //Summation / Occurrence * totalOfPossibleWords
        for (int i = 0; i < AllowedChars.length(); i++){
            double Weight = (LetterSummation.get(AllowedChars.charAt(i)) * LetterOccurrence.get(AllowedChars.charAt(i)) / PossibleWords.size())
             / (PossibleWords.size() - LetterOccurrence.get(AllowedChars.charAt(i)))
              / PossibleWords.size();
            LetterWeight.put(AllowedChars.charAt(i), Weight);
        }
        
        //Whatever has the highest number is our best bet
        return FindMostEntropy();
    }
    
    /**
     * Counts the amount of times a unique letter pattern happens for an individual letter
     * @param Letter - The letter we are trying to find unique patterns for
     * @param previousGuesses - The previous guesses that the Player made
     */
     public static void PopulatePatternCount(char Letter, String previousGuesses){
        PatternFuckery.put(Letter, new HashMap<Integer, Integer>());
        //Goes through all words in Possible Words
        for (String Word: PossibleWords){
            //Goes through all Letters in word
            int Sum = 0;
            Integer Count = 0;
            //Goes through letters of the word
            for (int i = 0; i < Word.length(); i++){
                if (!previousGuesses.contains(Character.toString(Word.charAt(i)))){
                    //Stores the unique pattern as an integer
                    if (Word.charAt(i) == Letter){
                        Sum += 1 << i;
                    }
                }
                Count = PatternFuckery.get(Letter).get(Sum) == null ? 0 : PatternFuckery.get(Letter).get(Sum) + 1;
            }
            PatternFuckery.get(Letter).put(Sum, Count); 
        }
     }
     
     /**
      * Calculates the entropy of a specific letter pattern: Entropy = P(i) * log(2)Pi
      */
     public static void CalculatePatternEntropy(){
         for (Map.Entry<Character, HashMap<Integer, Integer>> Letter: PatternFuckery.entrySet()) {
              Entropy.put(Letter.getKey(), new HashMap<Integer, Double>());
              for (Map.Entry<Integer, Integer> Pattern: Letter.getValue().entrySet()){
                  double Pi = (Pattern.getValue() * 1.0) / PossibleWords.size();
                  double H = Pi * (Math.log(Pi) / Math.log(2));
                  Entropy.get(Letter.getKey()).put(Pattern.getKey(), H);
              }
         }
     }
     
     /**
      * Sums up all the entropy values of a specific letter
      */
     public static void CalculateLetterWeight(){
         //Iterate through Letters
         for (Map.Entry<Character, HashMap<Integer, Double>> Letter: Entropy.entrySet()){
             double Summation = 0;
             //Iterate through unique patterns
             for (Map.Entry<Integer, Double> Entropy: Letter.getValue().entrySet()){
                 //Sum up all the counters
                 Summation += Entropy.getKey();
             }
             //Store sum in a hashmap with letter keys
             LetterSummation.put(Letter.getKey(), Summation);
         }
     }
     //Summation * Occurrence / PossibleWords.size()
     
     /**
      * Calculates the occurrence of a letter in the word list
      * @param previousGuesses - The previous guesses that the player made
      */
     public static void CalculateLetterOccurrence(String previousGuesses) {
         for (char Letter: AllowedChars.toCharArray()){
             LetterOccurrence.put(Letter, 0);
         }
        // Iterate through all words
        for (String Word: PossibleWords) {
            // Iterate through all letters in word
            String Exclude = previousGuesses;
            for (char Letter: Word.toCharArray()) {
                // Check for past guesses and exclude counting
                if (!Exclude.contains(Character.toString(Letter))) {
                    //Increment the letter counter
                    LetterOccurrence.put(Letter, LetterOccurrence.get(Letter) + 1);
                    //Add the letter to Exclude to prevent double counting
                    Exclude += Letter;
                }
            }
        }
    }
     
     /**
      * 
      * @return Best chance of getting the right character
      */
     public static char FindMostEntropy(){
         //Initialize highest to 0
         Double Highest = 0.0;
         Character Key = ' ';
         //Find the highest entropy value
         for (Map.Entry<Character, Double> Letter: LetterWeight.entrySet()){
             if (Letter.getValue() > Highest){
                 Highest = Letter.getValue();
                 Key = Letter.getKey();
             }
         } 
         return Key;
     }
     
     /**
      * If the guess was a miss, we remove all words with that previous guess
      * @param pattern - The revealed pattern of the secret word so far
      * @param previousGuesses - The previous guesses that the Player made
      */
    public static void FilterByExclusion(String pattern, String previousGuesses) {
        //Initialize temporary array
            ArrayList<String> Temp = new ArrayList<String>();
            //Get last guess
            char LastChar = previousGuesses.charAt(previousGuesses.length() - 1);
            //Iterate through words
            for (String Word: PossibleWords) {
                //If word does not have the letter, we add it to the temp array
                    if (!Word.contains(Character.toString(LastChar))) {
                            Temp.add(Word);
                    }
            }
            //Copy the temp aray
            PossibleWords = new ArrayList<String>(Temp);
    }
    
    /**
     * If the last guess was a hit, we can filter words based on position
     * @param pattern - The revealed pattern of the secret word
     * @param previousGuesses - The previous guesses that the Player made
     */
    public static void FilterByPosition(String pattern, String previousGuesses) {
            // Filter by position
            //Initialize position and temporary arrays
            ArrayList<Integer> Position = new ArrayList<Integer>();
            ArrayList<String> Temp = new ArrayList<String>();
            char LastChar = previousGuesses.charAt(previousGuesses.length() - 1);
            // Get letter positions
            for (int r = 0; r < pattern.length(); r++) {
                    if (pattern.charAt(r) == LastChar) {
                            Position.add(r);
                    }
            }

            // Filter PossibleWords by letter positions
            // Iterate through words in PossibleWords
            for (String Words: PossibleWords) {
                    int PassCount = 0;
                    // Iterate through letters				
                    for (int q = 0; q < Words.length(); q++) {
                            // Check all positions of previous letter
                            if (Words.charAt(q) == LastChar) {
                                    if (Position.contains(q)) {
                                            PassCount++;
                                    } else {
                                            PassCount--;
                                    }
                            }
                    }
                    if (PassCount == Position.size()) {
                            Temp.add(Words);
                    }
            }
            // Copy words from temporary array into PossibleWords
            PossibleWords = new ArrayList<String>(Temp);	
    }
    
    public static String getAuthor() { return "Sung, Alfred"; }
}