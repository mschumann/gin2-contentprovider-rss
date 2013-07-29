package sf.net.iqser.plugin.rss;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.jperdian.rss2.RssClient;
import org.jperdian.rss2.RssException;
import org.jperdian.rss2.dom.RssChannel;
import org.jperdian.rss2.dom.RssItem;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;

public class YoutubeRSSContentProviderTest extends TestCase {

	private RSSContentProvider provider = null;
	
	private static Logger logger = Logger.getLogger( YoutubeRSSContentProviderTest.class );

	protected void setUp() throws Exception {
				
		provider = new RSSContentProvider();
		
		provider.setName("com.iqser.training.rss.youtube");
		
		Properties props = new Properties();
		props.setProperty("url", "http://gdata.youtube.com/feeds/base/users/MorganMcKinley01/uploads?alt=rss&v=2&orderby=published&client=ytapi-youtube-profile");
		props.setProperty("type", "Youtube Video");
		provider.setInitParams(props);
		
		super.setUp();
		provider.init();
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
		
		String contentUrl = "tag:youtube.com,2008:video:wC79vQWhAHg";
		
		Content c = provider.createContent(contentUrl);
		
		assertEquals("Youtube Video", c.getType());
		assertEquals("com.iqser.training.rss.youtube", c.getProvider());
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
