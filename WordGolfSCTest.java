/**
 * WordGolf Project Unit Tests
 * 
 * License: public domain
 *
 * 2019-11-04 | Joel Simpson | Changed test for scoring of Empty Word = 0
 * 2018-12-01 | Joel Simpson | Muted Scoring of Empty Word
 * 2018-11-07 | Joel Simpson | First Version
 */ 
import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.*;

import java.lang.reflect.Method;
import java.io.*;
import java.util.concurrent.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        WordGolfSCTest.Checkpoint1.class,
        WordGolfSCTest.Checkpoint2.class,
        WordGolfSCTest.ImprovedOps.class,
        // WordGoldSCTest.StrokeReportAndGameOutcome.class,
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WordGolfSCTest {

    /**
     * Fixture initialization (common initialization
     * for all tests).
     */
    @Before
    public void setUp() {

    }

    /**
     * ImprovedOps TESTS
     */
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class ImprovedOps {

        WordGolf wordGolf;

        //We will be accessing these methods through reflection so that the tests will still run/compile
        //even though the methods may not be defined yet.
        Method parseWordMethod;
        Method parseSentenceMethod;

        char[] consonants = "bcdfghjklmnpqrstvwxz".toCharArray();
        char[] vowels = "aeiouy".toCharArray();

        /**
         * Used to shortcut tests that require parseWord to Exist
         * returns true if the method has been defined AND has a string parameter
         */
        private void assertParseWordExists() {
            if (this.parseWordMethod != null) return;
            try {
                this.parseWordMethod = WordGolf.class.getMethod("parseWord", String.class);
                this.parseWordMethod.setAccessible(true); //make it available for invocation through reflection
            } catch (Exception e) {
                fail("The parseWord() method is missing or has an incorrect definition.");
                return;
            }
        }


        private void assertParseSentenceExists() {
            //have we already checked?
            if (this.parseSentenceMethod != null) return;
            try {
                this.parseSentenceMethod = WordGolf.class.getMethod("parseSentence", String.class, int.class);
                this.parseSentenceMethod.setAccessible(true); //make it available for invocation through reflection
            } catch (Exception e) {
                fail("The parseSentence() method is missing or has an incorrect definition.");
                return;
            }
        }


        @Before
        public void initializeGame() {
            wordGolf = new WordGolf();
        }

        @Test
        public void test1_2pts_UpperAndLowerCaseLettersEqual() throws Exception {
            assertParseSentenceExists();

            String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            assertEquals("Upper and lower case characters must be treated equally.",
                    87, this.parseSentenceMethod.invoke(wordGolf, letters,0));  //equivalent to WordGolf.paseWord(letters);
            assertEquals("Upper and lower case characters must be treated equally.",
                    87, this.parseSentenceMethod.invoke(wordGolf, letters.toLowerCase(),0));
        }

        @Test
        public void test2_2pts_AllPunctuationAccepted() throws Exception {
            assertParseSentenceExists();
//9+11+15+8+13+9+22
            String lettersWithPunctuation = "ABCD ,EFG.H I&JK*L MN_OP QR|ST UV'W XYZ";
            assertEquals("All kinds of punctuation must be accepted from user input. Punctuation should not affect the value of any of the words in any way."
                    , 87, this.parseSentenceMethod.invoke(wordGolf, lettersWithPunctuation,0));
        }

    }

    /**
     * StrokeReportAndGameOutcome TESTS
     */
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class StrokeReportAndGameOutcome {

        WordGolf wordGolf;

        /**
         * Input/Output Stream Handling Vars
         */
        PrintStream consoleOutput;
        PipedOutputStream testInput;
        PipedOutputStream out;
        PipedInputStream testOutput;

        /**
         * These are used so we can run one test for efficiency and report back via
         * multiple "tests" for grading/reporting consistency and convenience
         */
        boolean showsStrokeCount = false;
        boolean showsOriginalSentence = false;
        boolean showsCurrentTotal = false;
        boolean showsDistanceToGoal = false;
        boolean showsReportsOnEachStroke = false;

        @Before
        public void initializeGame() throws Exception {

            //Setup input/output capture
            consoleOutput = System.out;
            testInput = new PipedOutputStream();
            out = new PipedOutputStream();
            testOutput = new PipedInputStream(out);
            System.setIn(new PipedInputStream(testInput));
            System.setOut(new PrintStream(out));

            wordGolf = new WordGolf();
        }


        @Test
        public void test1_15pts_StrokeReportAndGameOutcome() throws IOException, InterruptedException {

            /**
             * Sentence, Expected Score, Expected Total Score, Expected Distance From 100
             */
            final String[][] testData = {  // updated for Scrabble 12/4/2021 KW
                    {"REALTESTS0", "10", "10", "90"},
                    {"REALTESTS1", "10", "20", "80"},
                    {"REALTESTS2", "10", "30", "70"},
                    {"REALTESTS3", "10", "40", "60"},
                    {"REALTESTS4", "10", "50", "50"},
                    {"REALTESTS5", "10", "60", "40"},
                    {"REALTESTS6", "10", "70", "30"},
                    {"REALTESTS7", "10", "80", "20"},
                    {"REALTESTS8", "10", "90", "10"},
                    {"REALTESTS9", "10", "100", "0"},
            };

            final String[] error = new String[1];


            Thread uiThread = new Thread(new Runnable() {

                private boolean validate(String haystack, String needle) {
                    if (haystack.indexOf(needle) == -1) {
                        error[0] = "'" + needle + "' should have appeared in the output received '" + haystack + "'";
                        return false;
                    }
                    return true;
                }

                @Override
                public void run() {
                    try {
                        PrintStream testPrint = new PrintStream(testInput);
                        Scanner scanner = new Scanner(testOutput);
                        if (!scanner.hasNext()) {
                            error[0] = "Was not able to communicate with the game.";
                            return;
                        }


                        int i = 0;

                        //Advance to the prompt
                        //while (scanner.nextLine().indexOf("Enter") == -1) ;
                        while (i < testData.length) {
                            String output = "";
                            String sentence = testData[i][0];
                            String points = testData[i][1];
                            String total = testData[i][2];
                            String distance = testData[i][3];
                            i++;
                            //Send our sentence
                            testPrint.println(sentence);//a 10 point single word sentence, after 10 times it should quit.
                            testPrint.flush();

                            //capture all lines until we see "Enter" again

                            String nextLine;

                            do {
                                if (scanner.hasNext()) {
                                    nextLine = scanner.nextLine();//testReader.readLine();
                                } else {
                                    error[0] = "Incorrect output formatting";
                                    return;
                                }
                                output += "\n" + nextLine;

                            } while (output.indexOf("goal.") == -1 && output.indexOf("Congratulations, you won") == -1);
                            consoleOutput.println(output);

                            //Enter a sentence: QUITT0
                            //Stroke 1: "QUITT0" = 10 points
                            //Your total is 10 points, 90 from the goal.
                            //
                            //Enter a sentence: QUITT1
                            //Stroke 2: "QUITT1" = 10 points
                            //Your total is 20 points, 80 from the goal.
                            //
                            //   ....
                            //
                            //Enter a sentence: Stroke 10: "JAVA9" = 10 points
                            //Congratulations, you won with only 10 strokes!

                            //parse through lines verifying presence of:
                            if (!this.validate(output, "Stroke " + i)) return;
                            showsStrokeCount = true;

                            if (!this.validate(output, sentence)) return;
                            showsOriginalSentence = true;

                            if (!this.validate(output, points + " points")) return;

                            //If it's a win line, we don't want to check for the things that shouldn't show.
                            if (output.indexOf("Congratulations, you won") == -1) {

                                if (!this.validate(output, "Your total is " + total + " points")) return;
                                showsCurrentTotal = true;

                                if (!this.validate(output, distance + " from the goal")) return;
                                showsDistanceToGoal = true;
                            }


                            //      |overshot|
                            //      |won|
                        }

                    } catch (Exception e) {
                        error[0] = "An error occurred while testing the game process";
                        return;
                    }
                }
            });

            try {
                uiThread.start();
                wordGolf.main(null);
                uiThread.join();
            } catch (Exception e) {

            }
            //If there is an error message then fail the test with it
            if (error[0] != null) fail(error[0]);

        }


    }


    /**
     * CHECKPOINT 2 TESTS
     */
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class Checkpoint2 {

        WordGolf wordGolf;

        //We will be accessing these methods through reflection so that the tests will still run/compile
        //even though the methods may not be defined yet.
        Method parseWordMethod;
        Method parseSentenceMethod;

        char[] consonants = "bcdfghjklmnpqrstvwxz".toCharArray();
        char[] vowels = "aeiouy".toCharArray();

        /**
         * Used to shortcut tests that require parseWord to Exist
         * returns true if the method has been defined AND has a string parameter
         */
        private void assertParseWordExists() {
            if (this.parseWordMethod != null) return;
            try {
                this.parseWordMethod = WordGolf.class.getMethod("parseWord", String.class);
                this.parseWordMethod.setAccessible(true); //make it available for invocation through reflection
            } catch (Exception e) {
                fail("The parseWord() method is missing or has an incorrect definition.");
                return;
            }
        }


        private void assertParseSentenceExists() {
            //have we already checked?
            if (this.parseSentenceMethod != null) return;
            try {
                this.parseSentenceMethod = WordGolf.class.getMethod("parseSentence", String.class, int.class);
                this.parseSentenceMethod.setAccessible(true); //make it available for invocation through reflection
            } catch (Exception e) {
                fail("The parseSentence() method is missing or has an incorrect definition.");
                return;
            }
        }


        @Before
        public void initializeGame() {
            wordGolf = new WordGolf();
        }

        @Test
        public void test1_2pts_ParseSentenceExists() {
            //Checking explicitly for the existance of a named function regardless of the rest of it's signature
            for (Method method : WordGolf.class.getDeclaredMethods())
                if (method.getName().equals("parseSentence")) return;

            fail("The parseSentence() method is missing");
        }

        @Test
        public void test2_2pts_ParseSentenceExistsWithParameter() {
            this.assertParseSentenceExists();
        }

        @Test
        public void test3_4pts_ParseSentenceReturnsInt() throws Exception {
            assertParseSentenceExists();

            Object result = this.parseSentenceMethod.invoke(wordGolf, "java", 0);
            assertEquals("The parseSentence() return type is not an int", "java.lang.Integer", result.getClass().getName());
        }

        @Test
        public void test4_2pts_ParseSpaces() throws Exception {
            assertParseSentenceExists();

//            assertEquals("Multiple concurrent spaces should count as separate words if using spaces to identify words."
//                    , 18
//                    , this.parseSentenceMethod.invoke(wordGolf, "this   is     a       test"));
            assertNotEquals("Only spaces should be counted as delimeters."
                    , this.parseSentenceMethod.invoke(wordGolf, "this is a test",60)
                    , this.parseSentenceMethod.invoke(wordGolf, "this,is,a,test",60));
        }

        @Test
        public void test5_15pts_ParseSentenceWorksCorrectly() throws Exception {
            assertParseSentenceExists();

            String[][] tests = {
                    {"14", "this is a test"},
                    {"17", "Hello world"},
                    {"33", "I love Computer Science"},
                    {"17", "but not Apple"},
                    {"9", "help"},
                    {"45", "The owls are not what they seem"},
                    {"87", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"}
            };

            for (int i = 0; i < tests.length; i++) {
                assertEquals("Test phrase '" + tests[i][1] + "' failed."
                        , Integer.parseInt(tests[i][0])
                        , this.parseSentenceMethod.invoke(wordGolf, tests[i][1],0));
            }
        }
        
        // Added by Kevin Winter 12/6/2021 to test for correct double and triple word scores
        @Test
        public void test6_4pts_ParseSentenceDoubleAndTripleWordScoreWorks() throws Exception {
            assertParseSentenceExists();

            String letters = "Hello World Again Repeat"; // 8 + 9 + 6 + 8
            assertEquals("Alternate words are doubled when yardage is over 40.",
                    45, this.parseSentenceMethod.invoke(wordGolf, letters,41));
            assertEquals("Alternate words are tripled when yardage is over 200.",
                    59, this.parseSentenceMethod.invoke(wordGolf, letters,201));
        }

/*        public void test7_2pts_ParseSentenceTripleWordScoreWorks() throws Exception {
            assertParseSentenceExists();

            String letters = "Hello World Again Repeat"; // 8 + 9 + 6 + 8
            assertEquals("Alternate words are tripled when yardage is over 100.",
                    59, this.parseSentenceMethod.invoke(wordGolf, letters,201));
        }
*/


    }

    /**
     * CHECKPOINT 1 TESTS
     */
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class Checkpoint1 {

        WordGolf wordGolf;

        //We will be accessing these methods through reflection so that the tests will still run/compile
        //even though the methods may not be defined yet.
        Method parseWordMethod;
        Method parseSentenceMethod;

        char[] consonants = "bcdfghjklmnpqrstvwxz".toCharArray();
        char[] vowels = "aeiouy".toCharArray();

        /**
         * Used to shortcut tests that require parseWord to Exist
         */
        private void assertParseWordExists() {
            assertNotNull("The parseWord() method is missing or has an incorrect definition.", this.parseWordMethod);
        }


        @Before
        public void initializeGame() {
            wordGolf = new WordGolf();


            //Setup virtual/reflection based access to the main methods we will test.
            try {
                this.parseWordMethod = WordGolf.class.getMethod("parseWord", String.class);
                this.parseWordMethod.setAccessible(true);
            } catch (Exception e) {
                return;
            }

            try {
                this.parseSentenceMethod = WordGolf.class.getMethod("parseSentence", String.class, int.class);
                this.parseSentenceMethod.setAccessible(true);
            } catch (Exception e) {
                return;
            }
        }

        
        @Test
        public void test1_2pts_ParseWordExists() {
            //Checking explicitly for the existance of a named function regardless of the rest of it's signature
            for (Method method : WordGolf.class.getDeclaredMethods())
                if (method.getName().equals("parseWord")) return;

            fail("The parseWord() method is missing");
        }

        @Test
        public void test2_2pts_ParseWordExistsWithParameter() {
            assertParseWordExists();
        }


        @Test
        public void test3_4pts_ParseWordReturnsInt() throws Exception {
            assertParseWordExists();

            Object result = this.parseWordMethod.invoke(wordGolf, "java");
            assertEquals("The parseWord() return type is not an int", "java.lang.Integer", result.getClass().getName());
        }

        @Test
        public void test4_2pts_ParseWordEmptyCounts0() throws Exception {
            assertParseWordExists();
            assertEquals("parseWord() should return 0 pts.", 0, this.parseWordMethod.invoke(wordGolf, ""));
        }


        //@Test
        //public void test6_3pts_ParseWordVowels() throws Exception {
        //    assertParseWordExists();

        //     for (char letter : this.vowels) {
        //        assertEquals("Vowels like '" + letter + "'  should multiple score by 2", 2, this.parseWordMethod.invoke(wordGolf, "" + letter));
        //        assertEquals("Vowels like '" + letter + "'  should multiple score by 2", 4, this.parseWordMethod.invoke(wordGolf, "" + letter + letter));
        //    }
        //}

        //@Test
        //public void test7_4pts_ParseWordConsonants() throws Exception {
        //    assertParseWordExists();

        //    for (char letter : this.consonants) {
        //        assertEquals("Consonants like '" + letter + "' should increment score by 1.", 2, this.parseWordMethod.invoke(wordGolf, "" + letter));
        //        assertEquals("Consonants like '" + letter + "' should increment score by 1.", 3, this.parseWordMethod.invoke(wordGolf, "" + letter + letter));
        //    }
        //}


        //@Test
        //public void test8_1pts_ParseWordTreatYasVowel() throws Exception {
        //    assertParseWordExists();
        //    assertEquals("Y should be treated like vowels and should multiple score by 2", 2, this.parseWordMethod.invoke(wordGolf, "y"));
        //   assertEquals("Y should be treated like vowels and should multiple score by 2", 4, this.parseWordMethod.invoke(wordGolf, "yy"));
        //}


        @Test
        public void test5_7pts_ParseWordWorkdCorrectly() throws Exception {
            assertParseWordExists();

            assertEquals("Incorrect Result", 14, this.parseWordMethod.invoke(wordGolf, "java"));
            assertEquals("Incorrect Result", 8, this.parseWordMethod.invoke(wordGolf, "google"));
            // Added 12/4/2021 KW
            assertEquals("Incorrect Result", 87, this.parseWordMethod.invoke(wordGolf, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        }

    }
}
