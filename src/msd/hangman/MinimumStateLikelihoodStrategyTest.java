package msd.hangman;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class MinimumStateLikelihoodStrategyTest {

    @Test
    public void testCattle() throws IOException {
        MinimumStateLikelihoodStrategy strategy = new MinimumStateLikelihoodStrategy("words.txt");
        StrategyTester.testWord(strategy);
    }
    
    @Test @Ignore // This is for testing performance and correctness, not functionality.
    public void scoreSampleWords() throws IOException {
        MinimumStateLikelihoodStrategy strategy = new MinimumStateLikelihoodStrategy("words.txt");
        StrategyTester.StatBlock results = StrategyTester.scoreSampleWords(strategy);
        StrategyTester.printResults(results);
    }
}
