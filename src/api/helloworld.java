package api;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.json.JSONObject;

public class helloworld extends HttpServlet{
    public void init(){

    }
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();
        StringWriter out_writer = new StringWriter();
        JSONObject obj1 = new JSONObject();
        obj1.put("message", "Hello, you find me!");
        obj1.put("time", new java.util.Date());
        obj1.write(out_writer);
        System.out.println(out_writer.toString());
        out.print(out_writer.toString());
    }
    public void destroy(){

    }
}
