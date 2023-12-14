package com.rudderstack.sdk.java.analytics;

import com.rudderstack.sdk.java.analytics.messages.AliasMessage;
import com.rudderstack.sdk.java.analytics.messages.GroupMessage;
import com.rudderstack.sdk.java.analytics.messages.IdentifyMessage;
import com.rudderstack.sdk.java.analytics.messages.Message;
import com.rudderstack.sdk.java.analytics.messages.PageMessage;
import com.rudderstack.sdk.java.analytics.messages.ScreenMessage;
import com.rudderstack.sdk.java.analytics.messages.TrackMessage;

/** Intercept every message after it is built to process it further. */
@Beta
public interface MessageInterceptor {
    /**
     * Called for every message. This will be called on the same thread the request was made and after
     * all {@link MessageTransformer}'s have been called. Returning {@code null} will skip processing
     * this message any further.
     */
    Message intercept(Message message);

    /**
     * A {@link MessageInterceptor} that lets you implement more strongly typed methods and add
     * transformations specific to the event type.
     */
    abstract class Typed implements MessageInterceptor {
        @Override
        public final Message intercept(Message message) {
            // todo: non final so messages can be filtered without duplicating logic?
            Message.Type type = message.type();
            return switch (type) {
                case alias -> alias((AliasMessage) message);
                case group -> group((GroupMessage) message);
                case identify -> identify((IdentifyMessage) message);
                case screen -> screen((ScreenMessage) message);
                case page -> page((PageMessage) message);
                case track -> track((TrackMessage) message);
                default -> throw new IllegalArgumentException("Unknown payload type: " + type);
            };
        }

        /** Called for every {@link AliasMessage}. */
        abstract AliasMessage alias(AliasMessage message);

        /** Called for every {@link GroupMessage}. */
        abstract GroupMessage group(GroupMessage message);

        /** Called for every {@link IdentifyMessage}. */
        abstract IdentifyMessage identify(IdentifyMessage message);

        /** Called for every {@link ScreenMessage}. */
        abstract ScreenMessage screen(ScreenMessage message);

        /** Called for every {@link PageMessage}. */
        abstract PageMessage page(PageMessage message);

        /** Called for every {@link TrackMessage}. */
        abstract TrackMessage track(TrackMessage message);
    }
}
