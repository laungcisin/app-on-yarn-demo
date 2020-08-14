package com.neoremind.app.on.yarn.demo;

import com.google.common.base.Throwables;
import com.neoremind.app.on.yarn.demo.utils.JdbcTemplateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.http.HtmlQuoting;
import org.apache.hadoop.util.ReflectionUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.util.Map;

/**
 * BaseHttpServer
 */
public abstract class BaseHttpServer implements Closeable {

    private static final Log LOG = LogFactory.getLog(BaseHttpServer.class);

    private Server server;

    private String name;

    private JdbcTemplate jdbcTemplate;

    public void start(String name, int port) {
        this.name = name;
        this.jdbcTemplate = JdbcTemplateUtils.getJdbcTemplate();
        int retryCount = 10;
        for (int i = 0; i < retryCount; i++) {
            try {
                server = new Server(port);
                Context context = new Context();
                context.setContextPath("/");

                Map<String, HttpServlet> nameServletMap = getAllDefinedServlet(name, this.jdbcTemplate);
                nameServletMap.forEach((pathSpec, servlet) -> {
                    context.addServlet(new ServletHolder(servlet), pathSpec);
                });
//                context.addServlet(new ServletHolder(getIndexPageServlet(name)), "/");
                context.addServlet(StackServlet.class, "/stack");
                server.setHandler(context);
                server.getConnectors()[0].setHost(NetworkUtils.getLocalHostIP());
                server.start();
                LOG.info("Embedded Jetty has successfully started on port " + port);
                break;
            } catch (BindException e) {
                LOG.warn("Jetty server port conflicts on " + port);
                port++;
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
    }

    public int getHttpPort() {
        return server.getConnectors()[0].getLocalPort();
    }

    public String getName() {
        return name;
    }

    @Override
    public void close() throws IOException {
        try {
            server.stop();
        } catch (Exception e) {
            LOG.error("Failed to shut down server due to " + e.getMessage(), e);
        }
    }

//    public abstract HttpServlet getIndexPageServlet(String name);

    /**
     * 抽象方法，具体业务由子类实现
     * @param name
     * @param jdbcTemplate
     * @return
     */
    public abstract Map<String, HttpServlet> getAllDefinedServlet(String name, JdbcTemplate jdbcTemplate);

    /**
     * StackServlet
     */
    public static class StackServlet extends HttpServlet {
        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            try (PrintStream out = new PrintStream(
                    HtmlQuoting.quoteOutputStream(response.getOutputStream()))) {
                ReflectionUtils.printThreadInfo(out, "");
                out.close();
            }
            ReflectionUtils.logThreadInfo(LOG, "jsp requested", 1);
        }
    }
}
