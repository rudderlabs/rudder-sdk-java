package com.rudderstack.sdk.java;
import java.util.concurrent.Phaser;
import com.rudderstack.sdk.java.messages.MessageBuilder;
import com.rudderstack.sdk.java.messages.Message;

/**
 * This class provides a blocking {@link RudderAnalytics#flush()} implementation. It is built using {@link Phaser} that monitors requests and is able to block until they're uploaded
 */
public final class FlushBlocking {

	private static FlushBlocking instance = null;

    private FlushBlocking() {
    	this.phaser = new Phaser(1);
    }

    public static FlushBlocking create() {
        if (instance == null) {
			synchronized(FlushBlocking.class) {
                if (instance == null) {
                    instance = new FlushBlocking(); 
                }
            }       
        }
        return instance;
    }

	  final Phaser phaser;

	   Plugin plugin() {
	    return new Plugin() {
	      @Override
	      public void configure(RudderAnalytics.Builder builder) {
	        builder.messageTransformer(
	            new MessageTransformer() {
	              @Override
	              public boolean transform(MessageBuilder builder) {
					phaser.register();
	                return true;
	              }
	            });

	        builder.callback(
	            new Callback() {
	              @Override
	              public void success(Message message) {
					phaser.arriveAndDeregister();
	              }
	              @Override
	              public void failure(Message message, Throwable throwable) {
	                phaser.arriveAndDeregister();
	              }
	            });
	      }
	    };
	  }

	 void block() {
		phaser.arriveAndAwaitAdvance();
	  }
	}
