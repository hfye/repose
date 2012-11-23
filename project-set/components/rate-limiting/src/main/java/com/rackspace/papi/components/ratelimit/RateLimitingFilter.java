package com.rackspace.papi.components.ratelimit;

import com.rackspace.repose.service.ratelimit.config.RateLimitingConfiguration;

import com.rackspace.papi.filter.FilterConfigHelper;
import com.rackspace.papi.filter.logic.impl.FilterLogicHandlerDelegate;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.context.ContextAdapter;
import com.rackspace.papi.service.context.ServletContextHelper;
import org.slf4j.Logger;

import javax.servlet.*;
import java.io.IOException;

/**
 *
 * @author jhopper
 */
public class RateLimitingFilter implements Filter {

    private static final String DEFAULT_CONFIG = "rate-limiting.cfg.xml";
    private String config;
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RateLimitingFilter.class);
    private RateLimitingHandlerFactory handlerFactory;
    private ConfigurationService configurationManager;

    @Override
    public void destroy() {
        configurationManager.unsubscribeFrom(config, handlerFactory);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        new FilterLogicHandlerDelegate(request, response, chain).doFilter(handlerFactory.newHandler());
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final ContextAdapter ctx = ServletContextHelper.getInstance().getPowerApiContext(filterConfig.getServletContext());
        config = new FilterConfigHelper(filterConfig).getFilterConfig(DEFAULT_CONFIG);
        LOG.info("Initializing filter using config " + config);

        handlerFactory = new RateLimitingHandlerFactory(ctx.datastoreService());
        configurationManager = ctx.configurationService();
        configurationManager.subscribeTo(config, handlerFactory, RateLimitingConfiguration.class);
    }
}
