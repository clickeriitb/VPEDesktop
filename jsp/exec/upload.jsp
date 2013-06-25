<%@ page import="org.apache.commons.io.FileUtils,com.iitb.vpeub.Uploader,java.io.FileOutputStream,java.io.*;" %>
<%
    String path = getServletContext().getRealPath("");
    String port = request.getParameter("port");
    OutputStream output = new FileOutputStream(new File(path,"upload.log"));
    OutputStream err = new FileOutputStream(new File(path,"uploadError.log"));
    Uploader u = new Uploader(path,"uno",port,output,err);
    u.upload();


%>

<!DOCTYPE html>
<html>
    <head>
        <title>Edit Code</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
        <script type="text/javascript" >
            function submitCode(){
            var form = document.getElementById('formid');
            form.submit();

            }
        </script>
    </head>
    <body>
        
         <div class="container">
           
            <div class="alert alert-info">
            <h2> Avrdude Log </h2>
            <%
                InputStream is = new FileInputStream(path + File.separator + "dude.log");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                String s = new String(buffer);
                out.write(s.replaceAll("\n","<br/>"));
                out.write("hello");
            %>
              
            </div>
        </div>
    <script src="js/jquery.js"></script>
    <script src="js/bootstrap.min.js"></script>
    </body>
</html>
