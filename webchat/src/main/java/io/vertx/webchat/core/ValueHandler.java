package io.vertx.webchat.core;
/**
 * This interface is an alternative version of the vert.x provided {@link io.vertx.core.Handler}
 * and may return a specific value after handling some context
 * 
 * @author Stefan
 *
 * @param <C> The specific parameter type that will be used as the context of the handler
 * @param <V> The value-type that will be returned after handling the context
 */
public interface ValueHandler<C, V>{

	public V handle(C context);
}
