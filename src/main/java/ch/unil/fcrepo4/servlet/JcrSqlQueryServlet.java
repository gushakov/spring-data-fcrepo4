package ch.unil.fcrepo4.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fcrepo.kernel.api.exception.RepositoryRuntimeException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.query.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author gushakov
 */
@WebServlet("/query")
public class JcrSqlQueryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queryString = req.getHeader("queryString");

        if (queryString == null){
            // will throw 500 error
            throw new RepositoryRuntimeException("Query string is null");
        }

        Session jcrSession = null;
        try {
            WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(req.getServletContext());
            Repository repository = context.getBean(Repository.class);
            jcrSession = repository.login();
            ArrayList<String> paths = new ArrayList<>();
            QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);
            QueryResult result = query.execute();
            RowIterator iter = result.getRows();
            while (iter.hasNext()) {
                Row row = (Row) iter.next();
                paths.add(row.getPath());
            }
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(writer, paths);
            writer.flush();
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        } finally {
            if (jcrSession != null) {
                jcrSession.logout();
            }
        }

    }
}
