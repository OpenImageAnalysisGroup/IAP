/*
Copyright (c) 2007 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.rmiio;

import java.rmi.RemoteException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;


/**
 * Utility class for automatically retrying remote method calls (which might
 * fail for spurious reasons).  Pretty much any remote method call should deal
 * with RemoteExceptions because many transient failures (such as temporary
 * network connection failures) can cause these exceptions, and the call may
 * succeed when reattempted.
 * <p>
 * The major caveat for this class is the remote method call <b>must be
 * idempotent</b>.  This means, in essence, that repeated calls with the same
 * arguments should generate the exact same results.  An example of a
 * non-idempotent call would be "remove $10 from my bank account".  If this
 * call were sent twice (because the first attempt seemed to fail), you could
 * end up with $20 removed from your bank account.  In order to solve this,
 * you could add a unique sequence id to the call, "remove $10 from my bank
 * account, seqId(5)", and the server could ignore the second call (assuming
 * it was keeping track of the sequence ids that it had processed thus far).
 * In other words, remote method calls are a lot harder to make than local
 * method calls and much care should be taken when dealing with remote APIs.
 * <p>
 * Although RemoteRetry is an abstract class, there are a variety of simple
 * implementations, as well as static instances of these implementations.  See
 * the implementations for more details.
 * <p>
 * Example usage:
 * <pre>
 *
 * // use simple retry mechanism
 * RemoteRetry retry = RemoteRetry.SIMPLE;
 *
 * // since we are using anonymous inner classes, these must be final
 * final MyRemoteObject myRemoteObject;
 * final int myArgument;
 *
 * // make a call with a return value
 * Result res = retry.call(new RemoteRetry.Caller&lt;Result&gt;()
 *   {
 *     public Result call() throws RemoteException, MyException {
 *       return myRemoteObject.getResult(myArgument);
 *     }
 *   }, LOG, MyException.class, RemoteException.class);
 *
 * // make a call with no return value (use VoidCaller)
 * retry.call(new RemoteRetry.VoidCaller()
 *   {
 *     public void call() throws RemoteException, MyException {
 *       return myRemoteObject.setValue(myArgument);
 *     }
 *   }, LOG, MyException.class, RemoteException.class);
 *
 * </pre>
 * <p>
 * Note that the various call() methods use generics to create methods with
 * custom Exception signatures in addition to the custom return types.
 *
 * 
 * @author James Ahlborn
 */
public abstract class RemoteRetry
{
  protected static final Log LOG = LogFactory.getLog(RemoteRetry.class);

  /** RuntimeException class for overloading of exception types */
  protected static final Class<RuntimeException> RUNTIME_CLASS =
    RuntimeException.class;
  
  /** instance of the {@link Never} retry strategy for general use. */
  public static final RemoteRetry NEVER = new Never();

  /** instance of the {@link Simple} retry strategy for general use. */
  public static final RemoteRetry SIMPLE = new Simple();

  /** instance of the {@link SimpleAlways} retry strategy for general use. */
  public static final Always SIMPLE_ALWAYS = new SimpleAlways();


  protected RemoteRetry() {}

  
  /**
   * Implementation of a simple backoff strategy:
   * <ol>
   * <li> First retry returns immediately
   * <li> Retries 1 - 30 wait that many seconds each time before returning
   *      (after 1st retry wait 1 second, after 2nd retry wait 2 seconds...)
   * <li> Retries > 30 wait 30 seconds each time before returning
   * </ol>
   *
   * @param numRetries number of retries which have happended thus far
   * @param log debug log
   */
  protected static void simpleBackOff(int numRetries, Log log)
  {
    if(numRetries == 0) {
      // immediate retry first time
      return;
    }
    long sleepTime = numRetries;
    if(sleepTime > 30) {
      sleepTime = 30;
    }
    try {
      Thread.sleep(sleepTime * 1000);
    } catch(InterruptedException ignored) {
      // pass interrupt along
      Thread.currentThread().interrupt();
      if(log.isDebugEnabled()) {
        log.debug("Caught exception while sleeping", ignored);
      }
    }
  }

  /**
   * Implementation of the actual retry logic.  Calls the call() method of the
   * given Caller, returning results.  All Throwables will be caught and
   * shouldRetry() will be queried to see if the call should be reattempted.
   * Iff shouldRetry() returns <code>true</code>, backoff() is called in order
   * to allow the other end of the connection to have a breather and then the
   * call() is reattempted (and the cycle repeats).  Otherwise, the original
   * Throwable is thrown to the caller.
   *
   * @param caller implementation of the actual remote method call
   */
  protected final <RetType> RetType callImpl(Caller<RetType> caller, Log log)
    throws Throwable
  {
    int numTries = 0;
    do {
      try {
        // attempt actual remote call
        return caller.call();
      } catch(Throwable e) {

        // keep track of number of retries
        ++numTries;

        // determine if caller wants to retry
        if(!shouldRetry(e, numTries)) {
          // guess not...
          log.warn("Retry for caller " + caller + " giving up!");
          throw e;
        }

        if(log.isDebugEnabled()) {
          log.debug("Caller " + caller + " got exception, retrying", e);
        }

        // wait for a bit before retrying
        backOff(numTries, log);
      }
    } while(true);
  }

  /**
   * Wrapper for {@link #callImpl} which only throws RuntimeException.
   */
  public <RetType> RetType call(Caller<RetType> caller)
  {
    return call(caller, LOG, RUNTIME_CLASS, RUNTIME_CLASS, RUNTIME_CLASS);
  }
  
  /**
   * Wrapper for {@link #callImpl} which only throws RuntimeException.
   */
  public <RetType> RetType call(Caller<RetType> caller, Log log)
  {
    return call(caller, log, RUNTIME_CLASS, RUNTIME_CLASS, RUNTIME_CLASS);
  }

  /**
   * Wrapper for {@link #callImpl} which throws RuntimeException and one user
   * defined Exception.
   */
  public <RetType, ExType1 extends Throwable>
  RetType call(Caller<RetType> caller,
               Class<ExType1> throwType1)
    throws ExType1
  {
    return call(caller, LOG, throwType1, RUNTIME_CLASS, RUNTIME_CLASS);
  }
  
  /**
   * Wrapper for {@link #callImpl} which throws RuntimeException and one user
   * defined Exception.
   */
  public <RetType, ExType1 extends Throwable>
  RetType call(Caller<RetType> caller,
               Log log,
               Class<ExType1> throwType1)
    throws ExType1
  {
    return call(caller, log, throwType1, RUNTIME_CLASS, RUNTIME_CLASS);
  }

  /**
   * Wrapper for {@link #callImpl} which throws RuntimeException and two user
   * defined Exceptions.
   */
  public <RetType, ExType1 extends Throwable, ExType2 extends Throwable>
  RetType call(Caller<RetType> caller,
               Class<ExType1> throwType1,
               Class<ExType2> throwType2)
    throws ExType1, ExType2
  {
    return call(caller, LOG, throwType1, throwType2, RUNTIME_CLASS);
  }
  
  /**
   * Wrapper for {@link #callImpl} which throws RuntimeException and two user
   * defined Exceptions.
   */
  public <RetType, ExType1 extends Throwable, ExType2 extends Throwable>
  RetType call(Caller<RetType> caller,
               Log log,
               Class<ExType1> throwType1,
               Class<ExType2> throwType2)
    throws ExType1, ExType2
  {
    return call(caller, log, throwType1, throwType2, RUNTIME_CLASS);
  }

  /**
   * Wrapper for {@link #callImpl} which throws RuntimeException and three
   * user defined Exceptions.
   */
  public <RetType, ExType1 extends Throwable, ExType2 extends Throwable,
          ExType3 extends Throwable>
  RetType call(Caller<RetType> caller,
               Class<ExType1> throwType1,
               Class<ExType2> throwType2,
               Class<ExType3> throwType3)
    throws ExType1, ExType2, ExType3
  {
    return call(caller, LOG, throwType1, throwType2, throwType3);
  }
  
  /**
   * Wrapper for {@link #callImpl} which throws RuntimeException and three
   * user defined Exceptions.
   */
  public <RetType, ExType1 extends Throwable, ExType2 extends Throwable,
          ExType3 extends Throwable>
  RetType call(Caller<RetType> caller,
               Log log,
               Class<ExType1> throwType1,
               Class<ExType2> throwType2,
               Class<ExType3> throwType3)
    throws ExType1, ExType2, ExType3
  {
    try {
      return callImpl(caller, log);
    } catch(Throwable e) {
      throwCommonTypes(e);
      throwIfMatchesType(e, throwType1);
      throwIfMatchesType(e, throwType2);
      throwIfMatchesType(e, throwType3);
      throw handleNoMatches(e);
    }
  }
  
  
  /**
   * Checks the given exception against the given Exception type, throwing if
   * the given exception is an instanceof the given type.  Otherwise, returns.
   */
  private static <ExType extends Throwable> void throwIfMatchesType(
      Throwable throwable, Class<ExType> throwType)
    throws ExType
  {
    if(throwType.isInstance(throwable)) {
      throw throwType.cast(throwable);
    }
  }

  /**
   * Checks the given exception against a variety of common types (Error
   * and RuntimeException), throwing if the given exception
   * matches any of those types.  Otherwise, returns.
   */
  private static void throwCommonTypes(
      Throwable throwable)
  {
    throwIfMatchesType(throwable, RuntimeException.class);
    throwIfMatchesType(throwable, Error.class);
  }

  /**
   * Cleanup method which returns an InternalError.  This is necessary if the
   * given exception did not match any of the given types (which should never
   * happen).
   */
  private static Error handleNoMatches(
      Throwable throwable)
  {
    return (InternalError)
      (new InternalError("Impossible exception thrown"))
      .initCause(throwable);
  }

  /**
   * Returns <code>true</code> if the caller should attempt to repeat the
   * current remote method call given the number of previous reattempts.
   *
   * @param t throwable thrown
   * @param numRetries number of previous reattempts
   * @return <code>true</code> iff call should be repeated, <code>true</code>
   *         otherwise
   */
  public abstract boolean shouldRetry(Throwable t, int numRetries);

  /**
   * Should delay for some implementation defined amount of time (to give the
   * callee, network, etc. time to recover) given the number of previous
   * reattempts.  Will be called iff shouldRetry() returned <code>true</code>.
   * Good implementations should implement some sort of increased delay
   * based on the number of reattempts.
   *
   * @param numRetries number of previous reattempts
   * @param log debug log
   */
  public abstract void backOff(int numRetries, Log log);

  
  /**
   * Utility type implemented by those atttempting to make remote method calls
   * using this retry mechanism.  The call() method should implement the
   * desired remote call.  Easiest implementation is using an anonymous inner
   * class instantiated on the fly, per call (see main example above).
   */
  public static abstract class Caller<RetType>
  {
    /**
     * Makes a remote method call which returns a value.  Users should change
     * the exception signature to be that of the actual method call.
     */
    public abstract RetType call()
      throws Exception;
  }

  /**
   * Simple subclass of Caller for use by remote method calls which do not
   * need to return values.  User should implement voidCall() instead of
   * call().
   */
  public static abstract class VoidCaller extends Caller<Object>
  {
    @Override
    public final Object call()
      throws Exception
    {
      voidCall();
      return null;
    }
    
    /**
     * Makes a remote method call which returns no value.  Users should change
     * the exception signature to be that of the actual method call.
     */
    public abstract void voidCall()
      throws Exception;
  }


  /**
   * Simple implementation of RemoteRetry which retries RemoteExceptions some
   * number of times and uses the backoff strategy from
   * {@link RemoteRetry#simpleBackOff}.
   */
  public static class Simple extends RemoteRetry
  {
    /** default number of times to retry */
    public static final int DEFAULT_NUM_MAX_RETRIES = 5;

    /** user defined number of times to retry a remote method call which
        throws a RemoteException */
    private int _maxNumRetries;

    public Simple() {
      this(DEFAULT_NUM_MAX_RETRIES);
    }

    public Simple(int maxNumRetries) {
      _maxNumRetries = maxNumRetries;
    }

    public int getMaxNumRetries() {
      return _maxNumRetries;
    }
    
    @Override
    public boolean shouldRetry(Throwable t, int numRetries)
    {
      if(t instanceof RemoteException) {
        return(numRetries < getMaxNumRetries());
      }
      return false;
    }

    @Override
    public void backOff(int numRetries, Log log)
    {
      simpleBackOff(numRetries, log);
    }
    
  }

  /**
   * Simple implementation of RemoteRetry which <b>always</b> retries
   * RemoteExceptions thrown from the remote method call.  This will make a
   * remote method call behave similarly to a local method call in that it
   * will not fail for any network related reason (can still fail if callee
   * throws a normal exception).  This should be used with <b>extreme care</b>
   * as it can hang a program which is talking to a dead remote callee, and
   * should generally not be used in any sort of robust, enterprise grade
   * software (which should always have the ability to handle remote
   * failures).
   */
  public static abstract class Always extends RemoteRetry
  {
    protected Always() {}

    @Override
    public final boolean shouldRetry(Throwable t, int numRetries)
    {
      return(t instanceof RemoteException);
    }    
  }
  
  /**
   * Simple implementation of Always retry strategy which uses the backoff
   * strategy from {@link RemoteRetry#simpleBackOff}.  Please read warning in
   * {@link Always} before using.
   */
  public static class SimpleAlways extends Always
  {
    public SimpleAlways() {}

    @Override
    public void backOff(int numRetries, Log log)
    {
      simpleBackOff(numRetries, log);
    }    
  }

  /**
   * Simple implementation of RemoteRetry which never retries.  This is useful
   * for users of utilities which parameterize the RemoteRetry type used by
   * the utility where the user does not want any retry attempts.
   */
  public static final class Never extends RemoteRetry
  {
    public Never() {}

    @Override
    public boolean shouldRetry(Throwable t, int numRetries)
    {
      return false;
    }
    
    @Override
    public void backOff(int numRetries, Log log)
    {
      throw new UnsupportedOperationException("Should never be called");
    }    
  }
  
}
