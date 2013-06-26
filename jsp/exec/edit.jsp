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
            function saveCode(){
                var form = document.getElementById('formid');
                form.action = "save.jsp";
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
    /**
        Fetch the code and show it in textbox 
    */
    String path = getServletContext().getRealPath("");
    String code = request.getParameter("code");
    out.write(code);
%>
                    </textarea>
                
                
                <div class="form-inline offset2">
                    <select id="board" name="target" style="height: 40px;width:500px;font-size:20px">
                        Board
                        <option value="uno">Arduino Uno</option>
                        <option value="atmega328">Arduino Duemilanove w/ ATmega328</option>
                    </select> 
                    
                </div>
                <br/>
</form>
                <div class="offset4 span2">

                
                    
                    
                    <button  onclick="saveCode()"type="button" class="btn btn-primary btn-block">Save</button>
                    <br/>
                    <button  onclick="submitCode()"type="button" class="btn btn-primary btn-block">Compile</button>
                    <br/>
                    
                </div>
                <form action = "load.jsp" method="post" enctype="multipart/form-data">
                <br/><br/><br/><br/><br/><br/>
                <div class="offset3">
                    <input type="file" name="file" id="file">
                </div>
                <br/>
                <div class="span2 offset4">
                    <button  type="submit" type="button" class="btn btn-primary btn-block">Load</button>
                </div>
                </form>
                <br/>
                <br/>
                
            </div>
        </div>
    <script src="js/jquery.js"></script>
    <script src="js/bootstrap.min.js"></script>
    </body>
</html>
