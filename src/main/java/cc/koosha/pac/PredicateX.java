package cc.koosha.pac;


/**
 * Similar to java 8's java.util.Predicate
 *
 * @author Koosha Hosseiny, Copyright 2017
 */
public interface PredicateX<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     *
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    boolean test(T t);

}
