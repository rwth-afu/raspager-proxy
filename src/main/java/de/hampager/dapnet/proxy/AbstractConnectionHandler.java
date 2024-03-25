package de.hampager.dapnet.proxy;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

abstract class AbstractConnectionHandler extends SimpleChannelInboundHandler<String> {

	private final String profileName;
	private final Queue<String> pendingMessages = new ConcurrentLinkedQueue<>();
	private final ChannelFutureListener writeListener = new WriteCompleteListener();

	protected AbstractConnectionHandler(String profileName) {
		this.profileName = Objects.requireNonNull(profileName, "Profile name must not be null.");
	}

	public String getProfileName() {
		return profileName;
	}

	public void sendMessage(String message) {
		if (message == null) {
			throw new NullPointerException("Message must not be null.");
		}

		if (!message.isEmpty()) {
			pendingMessages.add(message);
		}

		// sendPendingMessages();
	}

	private void sendPendingMessages(Channel channel) {
		if (!channel.isActive()) {
			return;
		}

		while (!pendingMessages.isEmpty()) {
			final String message = pendingMessages.remove();
			channel.write(message).addListener(writeListener);
		}

		channel.flush();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		sendPendingMessages(ctx.channel());
	}

	private class WriteCompleteListener implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				return;
			}
		}

	}

}
