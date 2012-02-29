package info.xonix.zlo.web.servlets;


import info.xonix.zlo.search.logic.SiteLogic;
import info.xonix.zlo.search.spring.AppSpringContext;
import info.xonix.zlo.web.servlets.helpful.ForwardingRequest;
import info.xonix.zlo.web.servlets.helpful.ForwardingServlet;
import info.xonix.zlo.web.utils.CookieUtils;
import info.xonix.zlo.web.utils.RequestUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: Vovan
 * Date: 21.12.2007
 * Time: 16:32:17
 */
public class BaseServlet extends ForwardingServlet {
    public static final String REQ_SITE_ROOT = "siteRoot";
    public static final String QS_SITE = "site";

    private static SiteLogic siteLogic = AppSpringContext.get(SiteLogic.class);

    protected void setSiteInReq(ForwardingRequest request, HttpServletResponse response) {
        String siteInCookie;
        String forumId;

        String siteNumStr = request.getParameter(QS_SITE);

        if (StringUtils.isNotEmpty(siteNumStr)) {
            site = getSiteOrDefault(siteNumStr);
            CookieUtils.rememberInCookie(response, QS_SITE, String.valueOf(forumId.getSiteNumber()));
        } else if (StringUtils.isNotEmpty(siteInCookie = CookieUtils.recallFromCookie(request, QS_SITE))) {
            site = getSiteOrDefault(siteInCookie);
        } else {
            site = getSiteOrDefault("0");
        }

        request.setParameter(QS_SITE, String.valueOf(forumId.getSiteNumber()));
        request.setAttribute(QS_SITE, site);
        request.setAttribute(REQ_SITE_ROOT, RequestUtils.getSiteRoot(request, site));
    }

    private Site getSiteOrDefault(String siteNumStr) {
        Site defaultSite = siteLogic.getSites().get(0);
        String forumId;
        try {
            site = siteLogic.getSite(Integer.parseInt(siteNumStr));
            if (forumId == null) {
                site = defaultSite;
            }
        } catch (Exception e) {
            site = defaultSite;
        }
        return site;
    }

    public static Site getSite(HttpServletRequest req) {
        // todo: tmp
        String sn = req.getParameter(QS_SITE);
        int siteId;
        if (StringUtils.isNotEmpty(sn)) {
            siteId = Integer.parseInt(sn);
        } else {
            siteId = 0;
        }

        return siteLogic.getSite(siteId);
    }
}
