package com.rudderstack.sdk.java;
import java.util.concurrent.Phaser;
import com.rudderstack.sdk.java.RudderAnalytics;
import com.rudderstack.sdk.java.Callback;
import com.rudderstack.sdk.java.MessageTransformer;
import com.rudderstack.sdk.java.Plugin;
import com.rudderstack.sdk.java.messages.MessageBuilder;
import com.rudderstack.sdk.java.messages.Message;

public class FlushBlocking {

	  public static FlushBlocking create() {
	    return new FlushBlocking();
	  }

	  FlushBlocking() {
	    this.phaser = new Phaser(1);
	  }

	  final Phaser phaser;

	  public Plugin plugin() {
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

	  public void block() {
	    phaser.arriveAndAwaitAdvance();
	  }
	}
