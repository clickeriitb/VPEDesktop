
<%@ page import="java.io.FileOutputStream,java.io.*;" %>
<%
    String path = getServletContext().getRealPath("");
    String code = request.getParameter("code");
    OutputStream output = new FileOutputStream(new File(path,"new_sketch.ino"));
    output.write(code.getBytes());
    output.close();
    response.sendRedirect("new_sketch.ino");
%>
