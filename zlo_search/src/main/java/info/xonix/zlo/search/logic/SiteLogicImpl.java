package info.xonix.zlo.search.logic;

import info.xonix.zlo.search.config.Config;

import info.xonix.zlo.search.config.forums.ForumDescriptor;
import info.xonix.zlo.search.config.forums.GetForum;
import info.xonix.zlo.search.logic.forum_adapters.ForumAccessException;
import info.xonix.zlo.search.logic.site.MessageRetriever;
import info.xonix.zlo.search.model.Message;
import info.xonix.zlo.search.utils.Check;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * User: Vovan
 * Date: 12.06.2010
 * Time: 22:00:33
 */
public class SiteLogicImpl implements SiteLogic, InitializingBean {
    private static final Logger log = Logger.getLogger(SiteLogicImpl.class);

    @Autowired
    private Config config;

    @Autowired
    private MessageRetriever messageRetriever;

    @Override
    public void afterPropertiesSet() throws Exception {
        Check.isSet(messageRetriever, "messageRetriever");
        Check.isSet(config, "config");
    }

    @Override
    public Message getMessageByNumber(String forumId, int num) throws ForumAccessException {
        log.debug(forumId + " - Receiving from site: " + num);
//        return messageRetriever.getMessage(forumId, num);
        return GetForum.adapter(forumId).getMessage(forumId, num);
    }

    @Override
    public List<Message> getMessages(String forumId, int from, int to) throws ForumAccessException {
        log.info(forumId + " - Downloading messages from " + from + " to " + to + "...");
        long begin = System.currentTimeMillis();

//        List<Message> msgs = messageRetriever.getMessages(forumId, from, to);
        List<Message> msgs = GetForum.adapter(forumId).getMessages(forumId, (long) from, (long) to);

        float durationSecs = (System.currentTimeMillis() - begin) / 1000f;
        log.info(forumId + " - Downloaded " + msgs.size() + " messages in " + (int) durationSecs + "secs. Rate: " + ((float) msgs.size()) / durationSecs + "mps.");

        return msgs;
    }

    @Override
    public int getLastMessageNumber(String forumId) throws ForumAccessException {
//        return messageRetriever.getLastMessageNumber(forumId);
        return (int) GetForum.adapter(forumId).getLastMessageNumber(forumId);
    }

//    private List<Site> sites;

//    @Override
//    public List<Site> getSites() {
//        if (sites == null) {
//            sites = new LinkedList<Site>();
//
//            final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//
//            final Resource[] resources;
//            try {
//                resources = resolver.getResources(Config.FORUMS_CONF_PATH + "**/*.properties");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            for (Resource siteFileResource : resources) {
//                sites.add(Site.forName(FilenameUtils.removeExtension(siteFileResource.getFilename())));
//            }
//
//            Collections.sort(sites, new Comparator<Site>() {
//                public int compare(Site o1, Site o2) {
//                    return new Integer(o1.getWeight()).compareTo(o2.getWeight());
//                }
//            });
//        }
//        return sites;
//    }

//    @Override
//    TODO: rm
    public String[] getSiteUrls() {
//        final Collection<String> ids = GetForum.ids();
//        return ids.toArray(new String[ids.size()]);

        final List<ForumDescriptor> descriptors = GetForum.descriptors();

        String[] res = new String[descriptors.size()];

        int i=0;
        for (ForumDescriptor descriptor : descriptors) {
            res[i++] = descriptor.getForumAdapter().getForumUrl();
        }

        return res;


/*        List<Site> allSites = getSites();
        String[] sites = new String[allSites.size()];
        for (int i = 0; i < allSites.size(); i++) {
            sites[i] = allSites.get(i).getSiteUrl();
        }
        return sites;*/
    }

/*    @Override
    public Site getSite(int num) {
        for (String forumId : getSites()) {
            if (forumId.getSiteNumber() == num) {
                return site;
            }
        }
        return null;
    }*/
}
