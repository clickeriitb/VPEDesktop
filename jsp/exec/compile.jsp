<%@ page import="org.apache.commons.io.FileUtils,com.iitb.vpeub.Compiler,java.io.FileOutputStream,java.io.*;" %>
<%

    
    String path = getServletContext().getRealPath("");
    String code = request.getParameter("code");
    String target = request.getParameter("target");
    
    FileOutputStream codeFile = new FileOutputStream(path + File.separator + "code" + File.separator + "sketch.ino");
    org.apache.commons.io.FileUtils.cleanDirectory(new File(path + File.separator + "build"));
    codeFile.write(code.getBytes());
    codeFile.close();
    
    FileOutputStream compLog = new FileOutputStream(path + File.separator + "compile.log");
    FileOutputStream errLog = new FileOutputStream(path + File.separator + "compError.log");
    //out.write(absoluteDiskPath);
    Compiler c = new Compiler(target,path,compLog,errLog);
    c.doYourJob();
%>


<!DOCTYPE html>
<html>
    <head>
        <title>Compile Log</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
        <script type = "text/javascript">
            function upload() {
                var port = document.getElementById('port').value
                window.location.href="upload.jsp?port=" + port + "&target=<%out.write(target);%>";
           
            }
        </script>
    </head>
    <body>
        
         <div class="container">
           
            <div class="alert alert-info">
                <h2>Compile Log</h2>
                <%
                    InputStream is = new FileInputStream(path + File.separator + "compile.log");
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);
                    String s = new String(buffer);
                    out.write(s.replaceAll("\n","<br/>"));
                %>
            </div>
            <div class="alert alert-error">
                <h2>Error Log</h2>
                <%
                    is = new FileInputStream(path + File.separator + "compError.log");
                    buffer = new byte[is.available()];
                    is.read(buffer);
                    s = new String(buffer);
                    out.write(s.replaceAll("\n","<br/>"));
                %>
                
            </div>
            <div class="well">
                <% 
                    File f = new File(path + File.separator + "build" + File.separator + "final.hex");
                    if(f.exists()) {
                %>
                <div class = "form-inline offset1">
                    
                    <input id="port" type="text" style="height: 40px;width:500px;font-size:20px"></input>
                    <button class="btn btn-primary btn-large" onclick="upload()" type="button">Upload</button>
                    
                </div>
                <%
                    }
                %>
            </div>
        </div>
    <script src="js/jquery.js"></script>
    <script src="js/bootstrap.min.js"></script>
    </body>
</html>
