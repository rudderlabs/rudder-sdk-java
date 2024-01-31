package com.rudderstack.sdk.java.analytics.messages;

import com.google.auto.value.AutoValue;
import com.rudderstack.sdk.java.analytics.gson.AutoGson;

import java.util.Date;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The track API call is how you record any actions your users perform, along with any properties
 * that describe the action.
 *
 * <p>Use {@link #builder} to construct your own instances.
 *
 * @see <a href="https://www.rudderstack.com/docs/sources/event-streams/sdks/rudderstack-java-sdk/#track">Track</a>
 */
@AutoValue
@AutoGson //
public abstract class TrackMessage implements Message {

    /**
     * Start building an {@link TrackMessage} instance.
     *
     * @param event The event is the name of the action that a user has performed.
     * @throws IllegalArgumentException if the event name is null or empty
     */
    public static Builder builder(String event) {
        return new Builder(event);
    }

    public abstract String event();

    @Nullable
    public abstract Map<String, ?> properties();

    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Fluent API for creating {@link TrackMessage} instances.
     */
    public static class Builder extends MessageBuilder<TrackMessage, Builder> {
        private String event;
        private Map<String, ?> properties;

        private Builder(TrackMessage track) {
            super(track);
            event = track.event();
            properties = track.properties();
        }

        private Builder(String event) {
            super(Type.track);
            if (isNullOrEmpty(event)) {
                throw new IllegalArgumentException("event cannot be null or empty.");
            }
            this.event = event;
        }

        /**
         * Set a map of information that describe the page. These can be anything you want.
         */
        public Builder properties(Map<String, ?> properties) {
            if (properties == null) {
                throw new NullPointerException("Null properties");
            }
            this.properties = ImmutableMap.copyOf(properties);
            return this;
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        protected TrackMessage realBuild(
                Type type,
                String messageId,
                Date sentAt,
                String channel,
                Date timestamp,
                Map<String, ?> context,
                String anonymousId,
                String userId,
                Map<String, Object> integrations) {
            return new AutoValue_TrackMessage(
                    type,
                    messageId,
                    sentAt,
                    channel,
                    timestamp,
                    context,
                    anonymousId,
                    userId,
                    integrations,
                    event,
                    properties);
        }
    }
}
