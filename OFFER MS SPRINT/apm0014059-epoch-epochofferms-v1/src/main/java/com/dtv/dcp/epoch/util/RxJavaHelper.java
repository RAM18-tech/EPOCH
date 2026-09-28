package com.dtv.dcp.epoch.util;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * This class contains common reusable helper methods related to RxJava.
 * 
 * Currently contains methods for parallel execution of apis
 *
 * @author yk442w
 *
 */

public class RxJavaHelper {

	/**
	 * Creates an Observable for a specified callable that will return either the
	 * result or Throwable thrown by the call; This call gets executed on a thread
	 * provided by Schedulers.io() scheduler.
	 *
	 * @param methodCaller
	 * @return Observable<Object>
	 */
	public static Observable<Object> createCallObservable(Callable<Object> methodCaller) {
		return Observable.fromCallable(methodCaller).onErrorReturn(t -> t).subscribeOn(Schedulers.io());
	}

	/**
	 * Executes all specified callables concurrently and returns an Observable that
	 * emits 1 element of type Object[] containing response (result or exception)
	 * for each of the callable in the SAME order (i.e. The number of callables and
	 * size of the one Object[] will be same and the order of callables and
	 * corresponding response will be exactly SAME)
	 *
	 * @param methodCalls
	 *            Callable for the methods to be called.
	 * @return Observable containing one Object[n] (where n is the number of input
	 *         callables)
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static Observable<Object[]> callConcurrently(Callable<Object>... methodCalls) {
		Observable<Object>[] callObsArr = Stream.of(methodCalls).map(RxJavaHelper::createCallObservable)
				.toArray(Observable[]::new);
		return Observable.zipArray(a -> a, false, 1, callObsArr);
	}

	/**
	 * Executes all specified callables concurrently and returns Object[] containing
	 * response (result or exception) for each of the callable in the SAME order
	 * (i.e. The number of callables and size of the one Object[] will be same and
	 * the order of callables and corresponding response will be exactly SAME)
	 *
	 * @param methodCalls
	 *            Callable for the methods to be called.
	 * @return Object[n] (where n is the number of input callables and index of
	 *         input callable response and corresponsing input callable)
	 */
	@SuppressWarnings("unchecked")
	public static Object[] callConcurrentlyGetResult(Callable<Object>... methodCalls) {
		return callConcurrently(methodCalls).blockingFirst();
	}
}
