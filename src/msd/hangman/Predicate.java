package msd.hangman;

/**
 * Predicate to be used to filter out data.
 * 
 * @author Michael S. Daines
 * 
 * @see {@code Utilities}
 */
interface Predicate {

    boolean apply(String term);
}
