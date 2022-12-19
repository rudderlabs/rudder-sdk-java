package sample;

import com.rudderstack.sdk.java.analytics.Callback;
import com.rudderstack.sdk.java.analytics.Plugin;
import com.rudderstack.sdk.java.analytics.messages.Message;

import java.util.concurrent.Phaser;

/**
 * Blocking flush implementor for cases where parties exceed 65535
 */
public class TierBlockingFlush {

    private static final int MAX_PARTIES_PER_PHASER = (1 << 16) - 2; // max a phaser can accommodate

    public static TierBlockingFlush create() {
        return new TierBlockingFlush(MAX_PARTIES_PER_PHASER);
    }

    private TierBlockingFlush(int maxPartiesPerPhaser) {
        this.currentPhaser = new Phaser(1);
        this.maxPartiesPerPhaser = maxPartiesPerPhaser;
    }

    private Phaser currentPhaser;
    private final int maxPartiesPerPhaser;

    public Plugin plugin() {
        return builder -> {
            builder.messageTransformer(
                    messageTransformationBuilder -> {
                        currentPhaser = currentPhaser.getRegisteredParties() == maxPartiesPerPhaser ? new Phaser(currentPhaser) : currentPhaser;
                        currentPhaser.register();
                        return true;
                    });

            builder.callback(
                    new Callback() {
                        @Override
                        public void success(Message message) {
                            onResult();
                        }

                        @Override
                        public void failure(Message message, Throwable throwable) {
                            onResult();
                        }

                        private void onResult() {
                            if (currentPhaser.getUnarrivedParties() == 0) {
                                currentPhaser = currentPhaser.getParent();
                            }
                            currentPhaser.arrive();
                        }
                    });
        };
    }

    public void block() {
        currentPhaser.arriveAndAwaitAdvance();
    }
}
