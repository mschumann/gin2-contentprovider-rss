package sf.net.iqser.plugin.rss;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jperdian.rss2.RssClient;
import org.jperdian.rss2.RssException;
import org.jperdian.rss2.dom.RssChannel;
import org.jperdian.rss2.dom.RssItem;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;

import junit.framework.TestCase;

public class RSSContentProviderTest extends TestCase {

	private RSSContentProvider provider = null;
	
	private static Logger logger = Logger.getLogger( RSSContentProviderTest.class );

	protected void setUp() throws Exception {
				
		provider = new RSSContentProvider();
		
		provider.setId("com.iqser.training.rss.twitter");
		provider.setType("Twitter Status Update");
		
		Properties props = new Properties();
		props.setProperty("url", "http://twitter.com/statuses/user_timeline/jwurzer.rss");
		
		provider.setInitParams(props);
		
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDoSynchonization() {
		
		// Just a quick test of the RSS framework
		
		try {
			URL url = new URL(provider.getInitParams().getProperty("url"));

			RssClient rssClient = new RssClient(url);
			RssChannel rssChannel = rssClient.getData();
			
			Iterator iter = rssChannel.getItemList().iterator();
			
			while (iter.hasNext()) {
				RssItem rssItem = (RssItem) iter.next();
				String contentUrl = rssItem.getGuid().getGuid();
				
				assertNotNull(contentUrl);
				
				logger.debug("Fetched url: " + contentUrl);
			}
		} catch (MalformedURLException e) {
			logger.error("Couldn't create url: " + e.getLocalizedMessage());
		} catch (RssException e) {
			logger.error("Couldn't parse rss: " + e.getLocalizedMessage());
		} 
	}

	public void testDoHousekeeping() {
		// Nothing to do yet
	}

	public void testGetContentString() {
		
		String contentUrl = "http://twitter.com/jwurzer/statuses/63223611695185920";
		
		Content c = provider.getContent(contentUrl);
		
		assertEquals("Twitter Status Update", c.getType());
		assertEquals("com.iqser.training.rss.twitter", c.getProvider());
		assertNotNull(c.getFulltext());
		
		logger.debug("Fulltext: " + c.getFulltext());
		
		assertTrue(c.getModificationDate() < System.currentTimeMillis());
		
		Collection col = c.getAttributes();
		
		assertTrue(col.size() > 0);
		
		Iterator iter = col.iterator();
		
		while (iter.hasNext()) {
			Attribute a = (Attribute)iter.next();
			
			logger.debug(a.getName() + ": " + a.getValue());
		}
	}
}
