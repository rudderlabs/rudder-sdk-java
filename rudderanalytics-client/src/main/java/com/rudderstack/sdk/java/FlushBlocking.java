package com.rudderstack.sdk.java;
import java.util.concurrent.Phaser;
import com.rudderstack.sdk.java.RudderAnalytics;
import com.rudderstack.sdk.java.Callback;
import com.rudderstack.sdk.java.MessageTransformer;
import com.rudderstack.sdk.java.Plugin;
import com.rudderstack.sdk.java.messages.MessageBuilder;
import com.rudderstack.sdk.java.messages.Message;

public final class FlushBlocking {

	private static FlushBlocking instance = null;

    private FlushBlocking() {
    	this.phaser = new Phaser(1);
    }

    public static FlushBlocking create() {
        if (instance == null) {
            instance = new FlushBlocking();     
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
	                phaser.arrive();
	              }

	              @Override
	              public void failure(Message message, Throwable throwable) {
	                phaser.arrive();
	              }
	            });
	      }
	    };
	  }

	void block() {
	    phaser.arriveAndAwaitAdvance();
	  }
	}
