package com.segment.analytics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.segment.analytics.messages.*;



public class TestAnalytics {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final Analytics analytics =
		        Analytics.builder("1dgURTY4fptGJK0c0RA8SXr7l9z","https://hosted.rudderlabs.com")
		            .build();
		
		final String userId = System.getProperty("user.name");
	    final String anonymousId = UUID.randomUUID().toString();

	    final AtomicInteger count = new AtomicInteger();
	    for (int i = 0; i < 10; i++) {
	      for (int j = 0; j < 10; j++) {
	        Map<String, Object> properties = new LinkedHashMap<>();
	        properties.put("count", count.incrementAndGet());
	        analytics.enqueue(
	            TrackMessage.builder("Java Test")
	                .properties(properties)
	                .anonymousId(anonymousId)
	                .userId(userId));
	      }
	    }
	   
	    analytics.enqueue(IdentifyMessage.builder()
	    	    .userId("f4ca124298")
//	    	    .traits(ImmutableMap.builder()
//	    	        .put("name", "Michael Bolton")
//	    	        .put("email", "mbolton@example.com")
//	    	        .build()
//	    	    )
	    	);
	    
	    analytics.enqueue(ScreenMessage.builder("Schedule")
	    	    .userId("f4ca124298")
//	    	    .properties(ImmutableMap.builder()
//	    	        .put("category", "Sports")
//	    	        .put("path", "/sports/schedule")
//	    	        .build()
//	    	    )
	    	);
	    
	    analytics.enqueue(PageMessage.builder("Schedule")
	    	    .userId("f4ca124298")
//	    	    .properties(ImmutableMap.builder()
//	    	        .put("category", "Sports")
//	    	        .put("path", "/sports/schedule")
//	    	        .build()
//	    	    )
	    	);
	    
	    analytics.enqueue(GroupMessage.builder("some-group-id")
	    	    .userId("f4ca124298")
//	    	    .traits(ImmutableMap.builder()
//	    	        .put("name", "Segment")
//	    	        .put("size", 50)
//	    	        .build()
//	    	    )
	    	);
	    
	    analytics.enqueue(AliasMessage.builder("previousId")
	    	    .userId("f4ca124298")
	    	);

	}

}
