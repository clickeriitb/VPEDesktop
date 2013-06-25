<%@ page import="org.apache.commons.io.FileUtils,com.iitb.vpeub.Compiler,java.io.FileOutputStream,java.io.*;" %>
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
           
            <div class="well">
                <h2>Edit Generated Code</h2>
                <form id="formid" method="post" action="compile.jsp">
                    <textarea id="code" name="code"style="width: 100%; height: 500px;">
<%
    String path = getServletContext().getRealPath("");
    String code = request.getParameter("code");
    out.write(code);
%>
                    </textarea>
                
                
                <div class="form-inline offset1">
                    <select id="board" name="target" style="height: 40px;width:500px;font-size:20px">
                        Board
                        <option value="uno">Arduino Uno</option>
                        <option value="atmega328">Arduino Duemilanove w/ ATmega328</option>
                    </select> 
                    <button  onclick="submitCode()"type="button" class="btn btn-primary btn-large">Compile</button>
                </div>
                
                <br/>
                </form>
            </div>
        </div>
    <script src="js/jquery.js"></script>
    <script src="js/bootstrap.min.js"></script>
    </body>
</html>
