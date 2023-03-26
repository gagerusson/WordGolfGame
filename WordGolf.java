import java.util.*;


public class WordGolf   {
   //Main method - handles the looping of all 9 holes and calls parseSentence method to calculate word total. Prints update of the hole. Also, applies multiplier
   public static void main(String[] args) { 
      //Instantiate Random Object
      Random r = new Random();
      //Instantiate Scanner
      Scanner input = new Scanner(System.in);
         
      //Initialize variables
      String nextSentence = "";
      String newWord = "";
      int yards = 0;
      int score = 0;
      int par = 0;
      int stroke = 1;
      int parTotal = 0;
      int userTotal = 0;
      String next = "";
      String parScore = "";
      String userStrokes = "";
      String printSentence = "";
      String playAgain = "yes";
      
   while (playAgain.equals("yes")) {            
      System.out.print("What's the name for your round? ");
      String name = input.nextLine();
      
      System.out.println("Type 'quit' to end the game at any time. Goodluck, enjoy your round!!!");
      
      //loop 9 times to simulate 9 holes
      for (int i = 1; i <= 9; i++) {
         int strokes = 0;
         stroke = 1;
         //get yardage for hole - random number (100-350)
         yards = r.nextInt(350 + 1 - 100) + 100;
         //determine par - 3, 4, or 5
         if (yards <= 200) {
            par = 3;
         } else if (yards >= 201) {
            par = 4;
         } 
         parTotal += par;
         
         System.out.println();
         System.out.println("Hole " + i + " | " + yards + " yards | Par " + par);
         //run until score is equal to the amount of yards - meaning run until hole is complete
         while (yards != 0) {             
            //get user input for the first shot of each hole
            if (strokes == 0) {
               System.out.print("Take your tee shot: ");
               nextSentence = input.nextLine();
               printSentence = nextSentence;
            }
            //Pass input into parseSentence()
            if (nextSentence.equals("quit")) {
               i = 10;
               break;
            } else { 
               score = parseSentence(nextSentence, yards);  
            }
            //if shot is passed the whole, subtract from the total score instead
             if (yards > 0) {
                yards -= score;
             } else { 
                yards += score;
             } 
             
             //print update on the distance the ball went and distance left to hole
             System.out.println("Stroke " + stroke + ": Your sentence: " + printSentence);
             //if the hole was overshot print message
             if (yards < 0) {
               System.out.println("The ball went " + score + " yards. Oops you overshot the hole.");
               System.out.println();
             } else {
               System.out.println("The ball went " + score + " yards");
               System.out.println();
             }
             //print yards left to the hole
             if (yards != 0) {
               System.out.println((Math.abs(yards)) + " yards left to the hole"); 
             //prompt user for next input
               System.out.print("Take your next swing: ");
               nextSentence = input.nextLine();
             }                        
          
          printSentence = nextSentence;        
          strokes++; 
          stroke++; 
          userTotal++;               
         } //close while
         
         
         System.out.println("You made it in " + strokes + " strokes, nice job!");
                  
         userStrokes += (strokes + " | ");
         
         parScore += (par + " | ");
         
         
      }//close for
      
      //call scoreCard method to print score card at finish
         scoreCard(name, parScore, userStrokes, parTotal, userTotal);
         
      System.out.println();
         
      System.out.print("Would you like to play again? Type 'yes' or 'no' ");
      playAgain = input.nextLine(); 
      System.out.println(); 
      } //close outer while
       
      System.out.println("Word Golf by Gage Russon");   
      
   } //close main 
   
     //parseWord method - loops through each individual letter of each word that is passed in from the parseSentence method, returns the total value of the word. 
     public static int parseWord(String word) {
        int score = 0;
        //make the word to lower case so computer can read easier
        word = word.toLowerCase();
        //find length of the word to set as limit in for loop
        int wordLength = word.length();
        //loop through each letter of word
        for (int i = 0; i < wordLength; i++) {
            String letter = word.substring(i, i + 1);
            if (letter.equals("a") || letter.equals("e") || letter.equals("i") || letter.equals("o") || letter.equals("l") ||
                letter.equals("n") || letter.equals("u") || letter.equals("r") || letter.equals("s") || letter.equals("t")) {
                score++;
            } else if (letter.equals("d") || letter.equals("g")) {
                     score += 2;           
            } else if (letter.equals("b") || letter.equals("c") || letter.equals("m") || letter.equals("p")) {
                     score += 3;
            } else if (letter.equals("f") || letter.equals("h") || letter.equals("v") || letter.equals("w") || letter.equals("y")) {
                     score += 4;            
            } else if (letter.equals("k")) {
                     score += 5;            
            } else if (letter.equals("j") || letter.equals("x")) {
                     score += 8;            
            } else if (letter.equals("q") || letter.equals("z")) {
                     score += 10;            
            }
        }
        return(score);
    }
         
     //parseSentence method - accepts the users input and the yards of the hole and loops until each word is calculated to be used in the main method.
     public static int parseSentence(String nextSentence, int yards) {
        //Instantiate scanner 
        Scanner input = new Scanner(System.in);                
        //initialize variables
        int totalScore = 0;
        String word = " ";
        int i = 0;
        int strokes = 0;
        int stroke = 1;
        int yardsLeft = yards;
                
              String printSentence = nextSentence;
              //find how many words are in the input
              String [] wordCount  = nextSentence.split(" ");
              int words = wordCount.length;
              int score = 0;
              int count = 1;
              //loop and seperate each word - individually call to each method
              for (int j = 0; j < words; j++) {
                //if its one word or the last word               
                if (words == 1 || j == (words - 1)) {
                   word = nextSentence;                
                } else {
                   word = nextSentence.substring(0, nextSentence.indexOf(" "));        
                   nextSentence = nextSentence.substring(nextSentence.indexOf(" ") + 1);
                } 
                
                //determine club/multiplier
                if ((yardsLeft > 200 && count % 2 == 1)) { //driver
                  score += parseWord(word) * 3;
                } else if ((yardsLeft > 200) && (count % 2 == 0)) {
                     score += parseWord(word);
                } else if (yardsLeft > 40 && yardsLeft < 200 && count % 2 == 1) { //iron
                     score += parseWord(word) * 2;
                } else if ((yardsLeft > 40) && (yardsLeft < 200) && (count % 2 == 0)) {
                     score += parseWord(word);
                } else { //putter
                     score += parseWord(word);
                }
                count++;
             } //close for  
                                   
                     
            return(score);
   }
   
   //scoreCard method - print out the final results of the round, prints a report of the hole numbers, pars, and users strokes. 
   public static int scoreCard(String name, String parStrokes, String userStrokes, int parTotal, int userTotal) {
      System.out.println();
      
      System.out.println("  NAME:  " + name);
      //print hole numbers 1-9
      System.out.println("   HOLE  | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |");      
          
      System.out.println("    PAR  | " + parStrokes);
      
      System.out.println("STROKES  | " + userStrokes);
      
      System.out.println();
      if (userTotal < parTotal) {
         System.out.println("You shot under par! You need to find your way on the PGA Tour!");
      }
      else if (userTotal == parTotal) {
         System.out.println("You shot even par! Great Job!");
      }
      else if (userTotal > parTotal) {
         System.out.println("You shot over par... go hit the range to get some practice.");
      }
      
      return(0);
   }     

} //close class
        