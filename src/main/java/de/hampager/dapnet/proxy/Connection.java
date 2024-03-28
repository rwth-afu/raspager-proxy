package de.hampager.dapnet.proxy;

public interface Connection<T> {

	String getProfileName();

	/**
	 * Whether the connection is currently active.
	 * 
	 * @return {@code true} if the connection is active, {@code false} otherwise.
	 */
	boolean isActive();

	/**
	 * Handles an incoming message.
	 * 
	 * @param message Message to process
	 */
	void handleMessage(T message);

	/**
	 * Sends a message.
	 * 
	 * @param message Message to send.
	 */
	void sendMessage(T message);

	/**
	 * Handles an exception related to the underlying connection.
	 * 
	 * @param cause Exception to process
	 */
	void handleException(Throwable cause);

}
