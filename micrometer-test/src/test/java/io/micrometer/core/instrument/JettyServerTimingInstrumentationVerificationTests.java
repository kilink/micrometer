/*
 * Copyright 2022 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument;

import io.micrometer.core.instrument.binder.jetty.TimedHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.jupiter.api.Disabled;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Disabled("URI is not tagged in generic Jetty servlet instrumentation")
class JettyServerTimingInstrumentationVerificationTests extends HttpServerTimingInstrumentationVerificationTests {

    private Server server;

    @Override
    protected String timerName() {
        return "jetty.server.requests";
    }

    @Override
    protected URI startInstrumentedServer() throws Exception {
        server = new Server(0);
        TimedHandler timedHandler = new TimedHandler(getRegistry(), Tags.empty());
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(ServletHandler.Default404Servlet.class, "/notFound");
        servletContextHandler.addServlet(MyWebServlet.class, "/*");
        server.setHandler(servletContextHandler);
        server.insertHandler(timedHandler);
        server.start();
        return server.getURI();
    }

    @Override
    protected void stopInstrumentedServer() throws Exception {
        server.stop();
    }

    public static class MyWebServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (req.getPathInfo().contentEquals("/")) {
                resp.setStatus(200);
                resp.getWriter().print("hello");
            }
            else if (req.getPathInfo().startsWith("/home/")) {
                resp.setStatus(200);
                resp.getWriter().print("hello " + "world");
            }
            else if (req.getPathInfo().contentEquals("/foundRedirect")) {
                resp.sendRedirect("/");
            }
            else {
                resp.sendError(400);
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doPost(req, resp);
        }

    }

}
