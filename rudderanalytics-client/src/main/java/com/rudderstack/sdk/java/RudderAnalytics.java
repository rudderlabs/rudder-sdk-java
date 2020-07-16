package com.rudderstack.sdk.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.sdk.java.gson.AutoValueAdapterFactory;
import com.rudderstack.sdk.java.gson.ISO8601DateAdapter;
import com.rudderstack.sdk.java.http.RudderService;
import com.rudderstack.sdk.java.internal.AnalyticsClient;
import com.rudderstack.sdk.java.messages.Message;
import com.rudderstack.sdk.java.messages.MessageBuilder;

import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.GsonConverter;

/**
 * The entry point into the Rudder for Java library.
 *
 * <p>
 * The idea is simple: one pipeline for all your data. Rudder is the single hub
 * to collect, translate and route your data with the flip of a switch.
 *
 * <p>
 * Analytics for Java will automatically batch events and upload it periodically
 * to Segment's servers for you. You only need to instrument Segment once, then
 * flip a switch to install new tools.
 *
 * <p>
 * This class is the main entry point into the client API. Use {@link #builder}
 * to construct your own instances.abcded
 */
public class RudderAnalytics {
    private final AnalyticsClient client;
    private final List<MessageTransformer> messageTransformers;
    private final List<MessageInterceptor> messageInterceptors;
    private final Log log;
    final static FlushBlock flushBlock = FlushBlock.create();

    RudderAnalytics(AnalyticsClient client, List<MessageTransformer> messageTransformers,
                    List<MessageInterceptor> messageInterceptors, Log log) {
        this.client = client;
        this.messageTransformers = messageTransformers;
        this.messageInterceptors = messageInterceptors;
        this.log = log;
    }

    /**
     * Start building an {@link RudderAnalytics} instance.
     *
     * @param writeKey Your project write key available on the Rudder dashboard.
     */
    public static Builder builder(String writeKey, String dataPlaneURI) {
        return new Builder(writeKey, dataPlaneURI);
    }

    /**
     * Enqueue the given message to be uploaded to Rudder's servers.
     */
    public void enqueue(MessageBuilder builder) {
        for (MessageTransformer messageTransformer : messageTransformers) {
            boolean shouldContinue = messageTransformer.transform(builder);
            if (!shouldContinue) {
                log.print(Log.Level.VERBOSE, "Skipping message %s.", builder);
                return;
            }
        }
        Message message = builder.build();
        for (MessageInterceptor messageInterceptor : messageInterceptors) {
            message = messageInterceptor.intercept(message);
            if (message == null) {
                log.print(Log.Level.VERBOSE, "Skipping message %s.", builder);
                return;
            }
        }
        client.enqueue(message);
    }

    /**
     * Flush events in the message queue.
     */
    public void flush() {
        client.flush();
    }

    /**
     * Stops this instance from processing further requests.
     */
    public void shutdown() {
        client.shutdown();
    }
    /**
     * Block until the flush completes
     */
    public void blockFlush() {
    	flushBlock.block();
    }
    
    /**
     * Fluent API for creating {@link RudderAnalytics} instances.
     */
    public static class Builder {
        private static final Endpoint DEFAULT_ENDPOINT = Endpoints.newFixedEndpoint("https://hosted.rudderlabs.com");
        private static final String DEFAULT_USER_AGENT = "rudderstack-analytics-java/1.0.0";

        private final String writeKey;
        private Client client;
        private Log log;
        private Endpoint endpoint;
        private String userAgent = DEFAULT_USER_AGENT;
        private List<MessageTransformer> messageTransformers;
        private List<MessageInterceptor> messageInterceptors;
        private ExecutorService networkExecutor;
        private ThreadFactory threadFactory;
        private int flushQueueSize;
        private long flushIntervalInMillis;
        private List<Callback> callbacks;
        private String configURL;
        

        Builder(String writeKey, String dataPlaneURI) {
            if (writeKey == null || writeKey.trim().length() == 0 || dataPlaneURI == null
                    || dataPlaneURI.trim().length() == 0) {
                throw new NullPointerException("writeKey cannot be null or empty.");
            }
            this.writeKey = writeKey;
            this.endpoint = Endpoints.newFixedEndpoint(dataPlaneURI);
        }

        /**
         * Set a custom networking client.
         */
        public Builder client(Client client) {
            if (client == null) {
                throw new NullPointerException("Null client");
            }
            this.client = client;
            return this;
        }

        /**
         * Configure debug logging mechanism. By default, nothing is logged.
         */
        public Builder log(Log log) {
            if (log == null) {
                throw new NullPointerException("Null log");
            }
            this.log = log;
            return this;
        }

        /**
         * Set an endpoint that this client should upload events to.
         */
        public Builder endpoint(String endpoint) {
            if (endpoint == null || endpoint.trim().length() == 0) {
                throw new NullPointerException("endpoint cannot be null or empty.");
            }
            this.endpoint = Endpoints.newFixedEndpoint(endpoint + "/v1/batch");
            return this;
        }

        /**
         * Sets a user agent for HTTP requests.
         */
        public Builder userAgent(String userAgent) {
            if (userAgent == null || userAgent.trim().length() == 0) {
                throw new NullPointerException("userAgent cannot be null or empty.");
            }
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Add a {@link MessageTransformer} for transforming messages.
         */
        @Beta
        public Builder messageTransformer(MessageTransformer transformer) {
            if (transformer == null) {
                throw new NullPointerException("Null transformer");
            }
            if (messageTransformers == null) {
                messageTransformers = new ArrayList<>();
            }
            if (messageTransformers.contains(transformer)) {
                throw new IllegalStateException("MessageTransformer is already registered.");
            }
            messageTransformers.add(transformer);
            return this;
        }

        /**
         * Add a {@link MessageInterceptor} for intercepting messages.
         */
        @Beta
        public Builder messageInterceptor(MessageInterceptor interceptor) {
            if (interceptor == null) {
                throw new NullPointerException("Null interceptor");
            }
            if (messageInterceptors == null) {
                messageInterceptors = new ArrayList<>();
            }
            if (messageInterceptors.contains(interceptor)) {
                throw new IllegalStateException("MessageInterceptor is already registered.");
            }
            messageInterceptors.add(interceptor);
            return this;
        }

        /**
         * Set the queueSize at which flushes should be triggered.
         */
        @Beta
        public Builder flushQueueSize(int flushQueueSize) {
            if (flushQueueSize < 1) {
                throw new IllegalArgumentException("flushQueueSize must not be less than 1.");
            }
            this.flushQueueSize = flushQueueSize;
            return this;
        }

        /**
         * Set the interval at which the queue should be flushed.
         */
        @Beta
        public Builder flushInterval(long flushInterval, TimeUnit unit) {
            long flushIntervalInMillis = unit.toMillis(flushInterval);
            if (flushIntervalInMillis < 1000) {
                throw new IllegalArgumentException("flushInterval must not be less than 1 second.");
            }
            this.flushIntervalInMillis = flushIntervalInMillis;
            return this;
        }

        /**
         * Set the {@link ExecutorService} on which all HTTP requests will be made.
         */
        public Builder networkExecutor(ExecutorService networkExecutor) {
            if (networkExecutor == null) {
                throw new NullPointerException("Null networkExecutor");
            }
            this.networkExecutor = networkExecutor;
            return this;
        }

        /**
         * Set the {@link ThreadFactory} used to create threads.
         */
        @Beta
        public Builder threadFactory(ThreadFactory threadFactory) {
            if (threadFactory == null) {
                throw new NullPointerException("Null threadFactory");
            }
            this.threadFactory = threadFactory;
            return this;
        }

        /**
         * Add a {@link Callback} to be notified when an event is processed.
         */
        public Builder callback(Callback callback) {
            if (callback == null) {
                throw new NullPointerException("Null callback");
            }
            if (callbacks == null) {
                callbacks = new ArrayList<>();
            }
            if (callbacks.contains(callback)) {
                throw new IllegalStateException("Callback is already registered.");
            }
            callbacks.add(callback);
            return this;
        }

        /**
         * Use a {@link Plugin} to configure the builder.
         */
        @Beta
        public Builder plugin(Plugin plugin) {
            if (plugin == null) {
                throw new NullPointerException("Null plugin");
            }
            plugin.configure(this);
            return this;
        }
        
        /**
         * Use a {@link FlushBlock} to implement synchronization 
         */
       
        public Builder synchronize(boolean isSynchronize) {
        	if (isSynchronize) {
        		this.plugin(flushBlock.plugin());
        	}
        	
        	return this;
        }

        /**
         * Create a {@link RudderAnalytics} client.
         */
        public RudderAnalytics build() {
            Gson gson = new GsonBuilder() //
                    .registerTypeAdapterFactory(new AutoValueAdapterFactory()) //
                    .registerTypeAdapter(Date.class, new ISO8601DateAdapter()) //
                    .create();

            if (endpoint == null) {
                endpoint = DEFAULT_ENDPOINT;
            }

            if (client == null) {
                client = Platform.get().defaultClient();
            }
            if (log == null) {
                log = Log.NONE;
            }
            if (flushIntervalInMillis == 0) {
                flushIntervalInMillis = Platform.get().defaultFlushIntervalInMillis();
            }
            if (flushQueueSize == 0) {
                flushQueueSize = Platform.get().defaultFlushQueueSize();
            }
            if (messageTransformers == null) {
                messageTransformers = Collections.emptyList();
            } else {
                messageTransformers = Collections.unmodifiableList(messageTransformers);
            }
            if (messageInterceptors == null) {
                messageInterceptors = Collections.emptyList();
            } else {
                messageInterceptors = Collections.unmodifiableList(messageInterceptors);
            }
            if (networkExecutor == null) {
                networkExecutor = Platform.get().defaultNetworkExecutor();
            }
            if (threadFactory == null) {
                threadFactory = Platform.get().defaultThreadFactory();
            }
            if (callbacks == null) {
                callbacks = Collections.emptyList();
            } else {
                callbacks = Collections.unmodifiableList(callbacks);
            }

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setConverter(new GsonConverter(gson))
                    .setEndpoint(endpoint)
                    .setClient(client)
                    .setRequestInterceptor(new AnalyticsRequestInterceptor(writeKey, userAgent))
                    .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                        @Override
                        public void log(String message) {
                            log.print(Log.Level.VERBOSE, "%s", message);
                        }
                    })
                    .build();

            RudderService rudderService = restAdapter.create(RudderService.class);

            AnalyticsClient analyticsClient = AnalyticsClient.create(rudderService, flushQueueSize, flushIntervalInMillis,
                    log, threadFactory, networkExecutor, callbacks);
            return new RudderAnalytics(analyticsClient, messageTransformers, messageInterceptors, log);
        }
    }
}
