package com.rudderstack.sdk.java;
import java.util.concurrent.Phaser;
import com.rudderstack.sdk.java.messages.MessageBuilder;
import com.rudderstack.sdk.java.messages.Message;

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
			// 		System.out.printf("%-20s: %10s, registered=%s, arrived=%s, unarrived=%s phase=%s%n",
			// 		"BeforeRegister",
			// 		Thread.currentThread().getName(),
			// 		phaser.getRegisteredParties(),
			// 		phaser.getArrivedParties(),
			// 		phaser.getUnarrivedParties(),
			// 		phaser.getPhase()
			// );
					phaser.register();
				// 	System.out.printf("%-20s: %10s, registered=%s, arrived=%s, unarrived=%s phase=%s%n",
				// 		"AfterRegister",
				// 		Thread.currentThread().getName(),
				// 		phaser.getRegisteredParties(),
				// 		phaser.getArrivedParties(),
				// 		phaser.getUnarrivedParties(),
				// 		phaser.getPhase()
				// );
	                return true;
	              }
	            });

	        builder.callback(
	            new Callback() {
	              @Override
	              public void success(Message message) {
				// 	System.out.printf("%-20s: %10s, registered=%s, arrived=%s, unarrived=%s phase=%s%n",
				// 		"BeforeArrive",
				// 		Thread.currentThread().getName(),
				// 		phaser.getRegisteredParties(),
				// 		phaser.getArrivedParties(),
				// 		phaser.getUnarrivedParties(),
				// 		phaser.getPhase()
				// );
					phaser.arriveAndDeregister();
				// 	System.out.printf("%-20s: %10s, registered=%s, arrived=%s, unarrived=%s phase=%s%n",
				// 		"AfterArrive",
				// 		Thread.currentThread().getName(),
				// 		phaser.getRegisteredParties(),
				// 		phaser.getArrivedParties(),
				// 		phaser.getUnarrivedParties(),
				// 		phaser.getPhase()
				// );
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
		// System.out.println("Block Thread : " + Thread.currentThread().getName());
		// System.out.printf("%-20s: %10s, registered=%s, arrived=%s, unarrived=%s phase=%s%n",
		// 				"BeforeMainArrive",
		// 				Thread.currentThread().getName(),
		// 				phaser.getRegisteredParties(),
		// 				phaser.getArrivedParties(),
		// 				phaser.getUnarrivedParties(),
		// 				phaser.getPhase()
		// 		);
		phaser.arriveAndAwaitAdvance();
		// System.out.printf("%-20s: %10s, registered=%s, arrived=%s, unarrived=%s phase=%s%n",
		// 				"AfterMainArrive",
		// 				Thread.currentThread().getName(),
		// 				phaser.getRegisteredParties(),
		// 				phaser.getArrivedParties(),
		// 				phaser.getUnarrivedParties(),
		// 				phaser.getPhase()
		// 		);
	  }
	}
