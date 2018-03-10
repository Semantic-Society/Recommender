package de.rwth.dbis.neologism.recommender.partialProvider;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;

/**
 * CLASS UNTESTED!!!!
 * 
 * This class implements a strategy for giving partial answers when a request is
 * passed on to multiple subtasks.
 * 
 * Usage:
 * 
 * <ol>
 * <li>create a {@link PartialAnswerProvider}
 * <ul>
 * <li>as parameters, specify a list of functions representing the subtasts and
 * an {@link Executor}</li>
 * </ul>
 * </li>
 * <li>make a call to startTask, with the input for the subtasks. An ID is
 * returned which can be used to get results/</li>
 * <ul>
 * <li>All subtasks start running in the background</li>
 * </ul>
 * <li>Call getMore with the obtained ID and optionally a timeout</li>
 * <ul>
 * <li>Returns a value upon completion of a subtask or nothing in case a) the
 * (overall)task has not been asked about in the last 1 minute or b) the
 * specified timeout is reached.</li>
 * </ul>
 * <li>If it is know that no more requests will come for an ID, the cancel
 * method can be called. This is optional.</li>
 * 
 * @author cochez
 *
 * @param <INPUT>
 *            The input type of the subtasks
 * @param <OUTPUT>
 *            The output type of the subtasks
 */
public class PartialAnswerProvider<INPUT, OUTPUT> {

	private static final Logger l = Logger.getLogger(PartialAnswerProvider.class);

	private final Executor pool;
	private final ImmutableList<Function<INPUT, OUTPUT>> providers;
	// private final ConcurrentHashMap<String, State<OUTPUT>> outstandingValues;
	// TODO a smaller time should be okay here. could also be a parameter.
	private final Cache<String, State<OUTPUT>> outstandingValues = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.MINUTES).concurrencyLevel(200).build();

	// random is thread-safe, or at least each call to next is. That is sufficient
	// for this use case.
	private final Random r = new Random();

	public PartialAnswerProvider(List<Function<INPUT, OUTPUT>> providers, Executor pool) {
		this.providers = ImmutableList.copyOf(providers);
		this.pool = pool;
		// this.outstandingValues = new ConcurrentHashMap<>();
	}

	public String startTasks(INPUT callParam) {

		String reqID;
		byte[] array = new byte[16]; // Means 128 bit
		do {
			r.nextBytes(array);
			reqID = BaseEncoding.base16().encode(array);
		} while (outstandingValues.getIfPresent(reqID) != null);

		State<OUTPUT> state = new State<>(providers.size());
		outstandingValues.put(reqID, state);

		for (Function<INPUT, OUTPUT> provider : providers) {
			pool.execute(new Runnable() {

				@Override
				public void run() {
					OUTPUT result = provider.apply(callParam);
					state.add(result);
				}
			});
		}
		return reqID;
	}

	public Optional<OUTPUT> getMore(String ID) {

		State<OUTPUT> outstanding = outstandingValues.getIfPresent(ID);
		if (outstanding == null) {
			l.debug("outstanding ID was not found " + ID);
			return Optional.empty();
		}
		Optional<OUTPUT> result = outstanding.tryMore();
		if (result.isPresent()) {
			// after getting something back, put back to make sure more can be gotten on the
			// next call (resets timeout for this cache element!)
			outstandingValues.put(ID, outstanding);
		}
		return result;
	}

	public Optional<OUTPUT> getMore(String ID, long timeout, TimeUnit unit) {

		State<OUTPUT> outstanding = outstandingValues.getIfPresent(ID);
		if (outstanding == null) {
			l.debug("outstanding ID was not found " + ID);
			return Optional.empty();
		}
		Optional<OUTPUT> result = outstanding.tryMore(timeout, unit);
		if (result.isPresent()) {
			// after getting something back, put back to make sure more can be gotten on the
			// next call (resets timeout for this cache element!)
			outstandingValues.put(ID, outstanding);
		}
		return result;
	}

	public void cancel(String ID) {
		State<OUTPUT> outstanding = outstandingValues.getIfPresent(ID);
		if (outstanding == null) {
			l.debug("outstanding ID was not found " + ID);
			return;
		}
		outstandingValues.invalidate(ID);
		outstanding.cancel();
	}

	private static class State<OUTPUT> {
		private final BlockingQueue<OUTPUT> outputChannel;
		private final Semaphore waiters;

		State(int waitingFor) {
			this.outputChannel = new ArrayBlockingQueue<>(waitingFor);
			this.waiters = new Semaphore(waitingFor);
		}

		void add(OUTPUT o) {
			try {
				outputChannel.put(o);
			} catch (InterruptedException e) {
				throw new Error(e);
			}
		}

		public Optional<OUTPUT> tryMore() {
			if (waiters.tryAcquire()) {
				// there is more to get
				OUTPUT result;
				try {
					result = outputChannel.take();
				} catch (InterruptedException e) {
					waiters.release();
					throw new Error(e);
				}
				return Optional.of(result);
			} else {
				return Optional.empty();
			}
		}

		public Optional<OUTPUT> tryMore(long timeout, TimeUnit unit) {
			if (waiters.tryAcquire()) {
				// there is more to get
				OUTPUT result;
				try {
					result = outputChannel.poll(timeout, unit);
				} catch (InterruptedException e) {
					waiters.release();
					throw new Error(e);
				}
				return Optional.ofNullable(result);
			} else {
				return Optional.empty();
			}
		}

		/**
		 * Canceling is optional. But, it makes sure that future calls to tryMore will
		 * not give a result.
		 */
		public void cancel() {
			waiters.drainPermits();
		}

	}

}
