package sample;

import com.rudderstack.sdk.java.analytics.Callback;
import com.rudderstack.sdk.java.analytics.Plugin;
import com.rudderstack.sdk.java.analytics.messages.Message;

import java.util.*;
import java.util.concurrent.Phaser;

/**
 * Blocking flush implementor for cases where parties exceed 65535
 */
public class TierBlockingFlush {

    private static final int MAX_PARTIES_PER_PHASER = (1 << 16) - 1; // max a phaser can accommodate

    public static TierBlockingFlush create() {
        return new TierBlockingFlush(MAX_PARTIES_PER_PHASER);
    }

    private TierBlockingFlush(int maxPartiesPerPhaser) {
        this.parentPhaser = new Phaser(0);
        this.maxPartiesPerPhaser = maxPartiesPerPhaser;
        //adding first child
        childPhasers.add(new Phaser(1));
        this.parentPhaser.register();
    }

    private final Phaser parentPhaser;
    private final int maxPartiesPerPhaser;

    private final List<Phaser> childPhasers = new ArrayList<>();


    public Plugin plugin() {
        return builder -> {
            builder.messageTransformer(
                    messageTransformationBuilder -> {
                        //get the latest phaser
                        Phaser currentPhaser = childPhasers.get(childPhasers.size() - 1);
                        //in case the phaser cannot accept anymore parties, we create new one
                        if (currentPhaser.getRegisteredParties() >= maxPartiesPerPhaser) {
                            currentPhaser = new Phaser(parentPhaser, 1);
                            childPhasers.add(currentPhaser);
                            System.out.println("Number of phasers increased to " + childPhasers.size());

                        }
                        currentPhaser.register();
                        return true;
                    });

            builder.callback(
                    new Callback() {
                        @Override
                        public void success(Message message) {
                            parentPhaser.arrive();

                        }

                        @Override
                        public void failure(Message message, Throwable throwable) {
                            parentPhaser.arrive();
                        }
                    });
        };
    }

    public void block() {
        parentPhaser.arriveAndAwaitAdvance();
    }
}
