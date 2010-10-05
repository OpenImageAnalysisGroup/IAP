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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import java.lang.reflect.Proxy;


/**
 * Base class for implementing remote stub wrappers with builtin retry
 * policies.  Providers of remote interfaces can generate simple wrappers for
 * use by a remote client which handle the retry functionality under the hood,
 * simplifying client code dramatically.  Note that this should only be done
 * for <b>idempotent</b> method calls. See {@link RemoteInputStreamWrapper}
 * and {@link RemoteOutputStreamWrapper} for example usage.
 * <p>
 * While subclassing presents an ability to fine tune the wrapper
 * implementation, it may be difficult and/or unnecessary for many interfaces.
 * In that case, a much simpler wrapper can be created using the {@link #wrap}
 * method, which uses the java {@code Proxy} functionality to generate the
 * wrapper implementation at run time.  This may be slightly less efficient
 * than a custom implementation since reflection is used for the actual method
 * invocations, but for remote method calls that overhead is probably
 * meaningless.
 *
 * @author James Ahlborn
 */
public class RemoteWrapper<RemoteType>
  implements InvocationHandler, RemoteClient
{

  /** the handle to the remote interface which will do the real work of the
      remote method calls */
  protected final RemoteType _stub;
  /** the retry policy we are using for our remote calls */
  protected RemoteRetry _retry;
  /** the log which will be used by the retry facility when making the remote
      calls */
  protected final Log _log;
  
  public RemoteWrapper(RemoteType stub, RemoteRetry retry, Log log) {
    _stub = stub;
    _retry = retry;
    _log = log;
  }

  /**
   * Simple wrapper generator which creates a Proxy for the given remote
   * interface.  This proxy will make all the remote calls through the
   * {@link #invoke} method which makes the actual method calls on the
   * underlying stub within the retry logic.
   * 
   * @param iface the remote interface to be implemented
   * @param stub the underlying implementation of the remote interface which
   *             will actually make the remote calls
   * @param retry the retry policy to use for the remote calls
   * @param log the log to use during retry handling
   *
   * @return a proxy for the given interface using the given retry strategy
   */
  public static <R> R wrap(Class<R> iface, R stub,
                           RemoteRetry retry, Log log)
  {
    RemoteWrapper<R> wrapper = new RemoteWrapper<R>(stub, retry, log);
    return iface.cast(Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        new Class<?>[]{iface}, wrapper));
  }

  /**
   * Gets the wrapper underlying a proxy created by a call to {@link #wrap}.
   */
  public static RemoteWrapper<?> getWrapper(Object proxy)
  {
    return (RemoteWrapper<?>)Proxy.getInvocationHandler(proxy);
  }

  public RemoteType getStub() {
    return _stub;
  }

  public Log getLog() {
    return _log;
  }
  
  public RemoteRetry getRemoteRetry() {
    return _retry;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This may be useful for temporarily changing the retry policy for a
   * specific set of calls (e.g. a startup/discovery sequence may be more
   * forgiving than normal usage).
   * <p>
   * Note, this method is not thread-safe as this should only be used on a
   * wrapper for which the caller has exclusive ownership (the retry policy
   * will be changed for all users of the wrapper).
   */
  public void setRemoteRetry(RemoteRetry retry) {
    _retry = ((retry != null) ? retry : DEFAULT_RETRY);
  }

  public Object invoke(Object proxy, final Method method, final Object[] args)
    throws Throwable
  {
    // make the method call on the actual remote stub within the retry handler
    return _retry.call(new RemoteRetry.Caller<Object>()
      {
        @Override
        public Object call() throws Exception {
          return method.invoke(_stub, method, args);
        }
      }, _log, Exception.class);
  }
  
}
