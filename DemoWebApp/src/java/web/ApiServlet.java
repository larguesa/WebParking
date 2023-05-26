/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package web;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import model.User;
import model.VehicleStay;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.Duration;
import java.time.Instant;

/**
 *
 * @author rlarg
 */
@WebServlet(name = "ApiServlet", urlPatterns = {"/api/*"})
public class ApiServlet extends HttpServlet {

    private JSONObject getJSONBody(BufferedReader reader) throws IOException{
        StringBuilder buffer = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return new JSONObject(buffer.toString());
    }
    
    private void processSession(JSONObject file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(request.getMethod().toLowerCase().equals("put")){
            JSONObject body = getJSONBody(request.getReader());
            String login = body.getString("login");
            String password = body.getString("password");
            User u = User.getUser(login, password);
            if(u==null){
                response.sendError(403, "Login or password incorrects");
                file.put("error", "Login or password incorrects");
            }else{
                request.getSession().setAttribute("user", u);
                file.put("id", u.getRowId());
                file.put("login", u.getLogin());
                file.put("name", u.getName());
                file.put("role", u.getRole());
                file.put("passwordHash", u.getPasswordHash());
                file.put("parkingHourPrice", VehicleStay.HOUR_PRICE);
                file.put("message", "Logged in");
            }
        }else if(request.getMethod().toLowerCase().equals("delete")){
            request.getSession().removeAttribute("user");
            file.put("message", "Logged out");
        }else if(request.getMethod().toLowerCase().equals("get")){
            if(request.getSession().getAttribute("user") == null){
                response.sendError(403, "No session");
                file.put("error", "No session");
            }else{
                User u = (User) request.getSession().getAttribute("user");
                file.put("id", u.getRowId());
                file.put("login", u.getLogin());
                file.put("name", u.getName());
                file.put("role", u.getRole());
                file.put("passwordHash", u.getPasswordHash());
                file.put("parkingHourPrice", VehicleStay.HOUR_PRICE);
            }
        }else{
            response.sendError(405, "Method not allowed");
            file.put("error", "Method not allowed");
        }
    }
    private void processUsers(JSONObject file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(request.getSession().getAttribute("user")==null){
            response.sendError(401, "Unauthorized: no session");
            file.put("error", "Unauthorized: no session");
        }else if(!((User)request.getSession().getAttribute("user")).getRole().equals("ADMIN")){
            response.sendError(401, "Unauthorized: only admin can manage users");
            file.put("error", "Unauthorized: only admin can manage users");
        }else if(request.getMethod().toLowerCase().equals("get")){
            file.put("list", new JSONArray(User.getUsers()));
        }else if(request.getMethod().toLowerCase().equals("post")){
            JSONObject body = getJSONBody(request.getReader());
            String login = body.getString("login");
            String name = body.getString("name");
            String role = body.getString("role");
            String password = body.getString("password");
            User.insertUser(login, name, role, password);
        }else if(request.getMethod().toLowerCase().equals("put")){
            JSONObject body = getJSONBody(request.getReader());
            String login = body.getString("login");
            String name = body.getString("name");
            String role = body.getString("role");
            String password = body.getString("password");
            User.updateUser(login, name, role, password);
        }else if(request.getMethod().toLowerCase().equals("delete")){
            Long id = Long.parseLong(request.getParameter("id"));
            User.deleteUser(id);
        }else{
            response.sendError(405, "Method not allowed");
            file.put("error", "Method not allowed");
        }
    }
    private void processParking(JSONObject file, HttpServletRequest request, HttpServletResponse response) throws Exception{
        if(request.getSession().getAttribute("user")==null){
            response.sendError(401, "Unauthorized: no session");
            file.put("error", "Unauthorized: no session");
        }else if(request.getMethod().toLowerCase().equals("get")){
            file.put("hourPrice", VehicleStay.HOUR_PRICE);
            if(request.getParameter("history") != null){
                file.put("list", new JSONArray(VehicleStay.getHistoryList()));
            }else{
                file.put("list", new JSONArray(VehicleStay.getList()));
            }
        }else if(request.getMethod().toLowerCase().equals("post")){
            JSONObject body = getJSONBody(request.getReader());
            String model = body.getString("model");
            String color = body.getString("color");
            String plate = body.getString("plate");
            VehicleStay.addVehicleStay(model, color, plate);
        }else if(request.getMethod().toLowerCase().equals("put")){
            JSONObject body = getJSONBody(request.getReader());
            Long id = body.getLong("id");
            VehicleStay vStay = VehicleStay.getStay(id);
            Instant beginInstant = vStay.getBeginStay().toInstant();
            Instant now = Instant.now();
            long ms = Duration.between(beginInstant, now).toMillis();
            double price = VehicleStay.HOUR_PRICE * ms / 1000 / 60 / 60;
            VehicleStay.finishVehicleStay(id, price);
        }else{
            response.sendError(405, "Method not allowed");
            file.put("error", "Method not allowed");
        }
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject file = new JSONObject();
        try{
            if(request.getRequestURI().endsWith("/api/session")){
                processSession(file, request, response);
            }else if(request.getRequestURI().endsWith("/api/users")){
                processUsers(file, request, response);
            }else if(request.getRequestURI().endsWith("/api/parking")){
                processParking(file, request, response);
            }else{
                response.sendError(400, "Invalid URL");
                file.put("error", "Invalid URL");
            }
        }catch(Exception ex){
            response.sendError(500, "Internal error: "+ex.getLocalizedMessage());
            file.put("error", "Internal error: "+ex.getLocalizedMessage());
        }
        response.getWriter().print(file.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
