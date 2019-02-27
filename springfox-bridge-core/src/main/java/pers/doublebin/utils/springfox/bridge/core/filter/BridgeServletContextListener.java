package pers.doublebin.utils.springfox.bridge.core.filter;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import springfox.documentation.swagger2.web.Swagger2Controller;

@WebListener
//@Component
public class BridgeServletContextListener implements  ServletContextListener
{
    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * <p>All ServletContextListeners are notified of context
     * initialization before any filters or servlets in the web
     * application are initialized.
     *
     * @param sce the ServletContextEvent containing the ServletContext
     *            that is being initialized
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext servletContext = sce.getServletContext();
        FilterRegistration.Dynamic dynamic = servletContext.addFilter("swagger2ControllerFilter", Swagger2ControllerFilter.class);
        EnumSet<DispatcherType> dispatcherTypes = EnumSet
            .allOf(DispatcherType.class);
        dispatcherTypes.add(DispatcherType.REQUEST);
        dispatcherTypes.add(DispatcherType.FORWARD);
        dispatcherTypes.add(DispatcherType.INCLUDE);

        //dynamic.addMappingForUrlPatterns(dispatcherTypes, true, Swagger2Controller.DEFAULT_URL); //TODO 有问题,如果额外指定了springfox.documentation.swagger.v2.host的话


        dynamic.addMappingForUrlPatterns(null, false, Swagger2Controller.DEFAULT_URL);
        dynamic.setAsyncSupported(true);

    }

    /**
     * Receives notification that the ServletContext is about to be
     * shut down.
     *
     * <p>All servlets and filters will have been destroyed before any
     * ServletContextListeners are notified of context
     * destruction.
     *
     * @param sce the ServletContextEvent containing the ServletContext
     *            that is being destroyed
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
