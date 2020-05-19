/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
/**
 *
 * @author Asus
 */
public class LoginServlet extends HttpServlet {
    
    private Connection connectionToDb = null;
    private static final String COLUMN_ID = "id", COLUMN_LOGIN = "login",
                                COLUMN_PASSWORD = "password",
                                COLUMN_NAME = "name",
                                COLUMN_SURNAME = "surname";
    
    private static final String HTML_KEY_NAME = "#htmlName#",
                                HTML_KEY_SURNAME = "#htmlSurname#",
                                HTML_KEY_ID = "#htmlId#";
    
    private static final String PATH_TO_TEACHER_PAGE = "D:/BSU/2Degree/4SEM/PR_PS/Labs/Lab6_TestingSystem_Application/src/TestingSystemWebApp/web/teacherpage.html";
    private static final String PATH_TO_STUDENT_PAGE = "D:/BSU/2Degree/4SEM/PR_PS/Labs/Lab6_TestingSystem_Application/src/TestingSystemWebApp/web/studentpage.html";
    
    private Connection connectToDb() throws Exception{
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:D:/BSU/2Degree/4SEM/PR_PS/Labs/Lab6_TestingSystem_Application/src/TestingSystemWebApp/src/java/db/testingsystem.db";
        // create a connection to the database
        Connection conn = DriverManager.getConnection(url);
            
        System.out.println("Connection to SQLite has been established.");
        
        return conn;
    }
    
    private void printErrorPage(PrintWriter out, String message){
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LoginServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1 align='center'>Error!</h1>");
            out.println("<p>" + message +"</p>");
            out.println("</body>");
            out.println("</html>");
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        connectionToDb = null;
        try {
            connectionToDb = connectToDb();
        }
        catch(Exception ex){
            try(PrintWriter out = response.getWriter()){
                printErrorPage(out, ex.toString());
            }
        }
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        PrintWriter out = response.getWriter();
        //if successfully connected
        if (loggedStudent(login, password)){
            try{
            loadStudentPage(out, login, password);
            }
            catch(Exception ex){
                printErrorPage(out, "Server Error: there is no page for students");
            }
        }
        else if (loggedTeacher(login, password)){
            try{
                loadTeacherPage(out, login, password);
            }
            catch(Exception ex){
                printErrorPage(out, "Server Error: there is no page for teachers");
            }        
        }
        else{
            printErrorPage(out, "Error: you are neither teacher nor student");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private boolean loggedStudent(String login, String password) {
        String studentRequest = "SELECT id, name, login, password, surname FROM student";
        try (Statement stmt  = this.connectionToDb.createStatement();
             ResultSet rs    = stmt.executeQuery(studentRequest)){
            // loop through the result set
            while (rs.next()) {
                if (rs.getString(COLUMN_LOGIN).equals(login) &&
                    rs.getString(COLUMN_PASSWORD).equals(password)) {
                        return true;
                }

            }
        }
        catch(Exception ex){
            return false;
        }
        
        return false;
    }

    
    private User getUserWith(String login, String password) throws Exception{
        //try to find in teachers
        String teacherRequest = "SELECT id, name, surname, login, password FROM teacher";
        try (Statement stmt  = this.connectionToDb.createStatement();
             ResultSet rs    = stmt.executeQuery(teacherRequest)){
            // loop through the result set
            while (rs.next()) {
                if (rs.getString(COLUMN_LOGIN).equals(login) &&
                    rs.getString(COLUMN_PASSWORD).equals(password)) {
                        return new Teacher(rs.getString(COLUMN_NAME), 
                                      rs.getString(COLUMN_SURNAME), 
                                      rs.getString(COLUMN_ID)); 
                }
            }
        }
        catch(Exception ex){
            //idk
        }
        String studentRequest = "SELECT id, name, login, password, surname FROM student";
        try (Statement stmt  = this.connectionToDb.createStatement();
             ResultSet rs    = stmt.executeQuery(studentRequest)){
            // loop through the result set
            while (rs.next()) {
                if (rs.getString(COLUMN_LOGIN).equals(login) &&
                    rs.getString(COLUMN_PASSWORD).equals(password)) {
                        return new Student(rs.getString(COLUMN_NAME), 
                                      rs.getString(COLUMN_SURNAME), 
                                      rs.getString(COLUMN_ID)); 
                }

            }
        }
        throw new Exception("Error: there is no user with such login and password;");
    }
    
    private void loadStudentPage(PrintWriter out, String login, String password) throws Exception {
        Student student = (Student)getUserWith(login, password);
        File file = new File(PATH_TO_STUDENT_PAGE);
        Scanner scnr = new Scanner(file);
     
        //Reading each line of file using Scanner class
        while(scnr.hasNextLine()){
            String line = scnr.nextLine();
            line = line.replace(HTML_KEY_NAME, student.getName());
            line = line.replace(HTML_KEY_SURNAME, student.getSurname());
            line = line.replace(HTML_KEY_ID, student.getId());
            out.println(line);
        }
    }
    
    private void loadTeacherPage(PrintWriter out, Teacher teacher) throws FileNotFoundException{
        File file = new File(PATH_TO_TEACHER_PAGE);
        Scanner scnr = new Scanner(file);
     
        //Reading each line of file using Scanner class
        while(scnr.hasNextLine()){
            String line = scnr.nextLine();
            line = line.replace(HTML_KEY_NAME, teacher.getName());
            line = line.replace(HTML_KEY_SURNAME, teacher.getSurname());
            line = line.replace(HTML_KEY_ID, teacher.getId());
            out.println(line);
        }
    }

    private void loadTeacherPage(PrintWriter out, String login, String password) throws Exception{
        Teacher teacher = (Teacher)getUserWith(login, password);
        loadTeacherPage(out, teacher);
    }

    private boolean loggedTeacher(String login, String password) {
        String teacherRequest = "SELECT id, name, surname, login, password FROM teacher";
        try (Statement stmt  = this.connectionToDb.createStatement();
             ResultSet rs    = stmt.executeQuery(teacherRequest)){
            // loop through the result set
            while (rs.next()) {
                if (rs.getString(COLUMN_LOGIN).equals(login) &&
                    rs.getString(COLUMN_PASSWORD).equals(password)) {
                        return true;
                }
            }
        }
        catch(Exception ex){
            return false;
        }
       
        return false;
    }

}
