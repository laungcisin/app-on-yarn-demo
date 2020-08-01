package com.neoremind.app.on.yarn.demo;

import com.neoremind.app.on.yarn.demo.mode.Employee;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * SampleHttpServer
 */
public class SampleHttpServer extends BaseHttpServer {

    /**
     * WelcomeServlet
     */
    public static class WelcomeServlet extends HttpServlet {

        private String name;

        private long startTimeInMs;

        public WelcomeServlet(String name, long startTimeInMs) {
            this.name = name;
            this.startTimeInMs = startTimeInMs;
        }

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/html; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.println("<body>");
                out.println("<h2>" + name + "</h2>");
                out.println(String.format("<div>The server has started for %d secs</div>",
                        (System.currentTimeMillis() - startTimeInMs) / 1000));
                out.println("</body>");
            }
        }
    }

    /**
     * EmployeeServlet
     */
    public static class EmployeeServlet extends HttpServlet {

        private String name;

        private long startTimeInMs;

        private JdbcTemplate jdbcTemplate;

        public EmployeeServlet(String name, long startTimeInMs, JdbcTemplate jdbcTemplate) {
            this.name = name;
            this.startTimeInMs = startTimeInMs;
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            String id = request.getParameter("id");
            String sql = "SELECT ID, NAME, ROLE FROM EMPLOYEE WHERE ID = ?";
            RowMapper<Employee> rowMapper = new BeanPropertyRowMapper<>(Employee.class);
            Employee employee = jdbcTemplate.queryForObject(sql, rowMapper, id);

            response.setContentType("text/html; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.println("<body>");
                out.println("<h2>userName: " + employee.getName() + "</h2>");
                out.println(String.format("<div>The server has started for %d secs</div>",
                        (System.currentTimeMillis() - startTimeInMs) / 1000));
                out.println("</body>");
            }
        }
    }

//    @Override
//    public HttpServlet getIndexPageServlet(String name) {
//        return new WelcomeServlet(name, System.currentTimeMillis());
//    }

    @Override
    public Map<String, HttpServlet> getAllDefinedServlet(String name, JdbcTemplate jdbcTemplate) {
        Map<String, HttpServlet> nameServletMap = new HashMap<>();
        WelcomeServlet welcomeServlet = new WelcomeServlet(name, System.currentTimeMillis());
        nameServletMap.put("/", welcomeServlet);


        EmployeeServlet employeeServlet = new EmployeeServlet(name, System.currentTimeMillis(), jdbcTemplate);
        nameServletMap.put("/employee", employeeServlet);
        return nameServletMap;
    }

    public static void main(String[] args) {
        SampleHttpServer httpServer = new SampleHttpServer();
        httpServer.start("test", 8290);
    }
}
