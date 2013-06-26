<%@ page import="java.io.*" %>

<%
    String path = getServletContext().getRealPath("");

    //to get the content type information from JSP Request Header
    String contentType = request.getContentType();
    if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
        DataInputStream in = new DataInputStream(request.getInputStream());
        
        
        //we are taking the length of Content type data
        int formDataLength = request.getContentLength();
        byte dataBytes[] = new byte[formDataLength];
        int byteRead = 0;
        int totalBytesRead = 0;
        
        //this loop converting the uploaded file into byte code
        while (totalBytesRead < formDataLength) {
            byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
            totalBytesRead += byteRead;
            }
        String file = new String(dataBytes);
        //for saving the file name
        String saveFile = file.substring(file.indexOf("filename=\"") + 10);
        saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
        saveFile = saveFile.substring(saveFile.lastIndexOf("\\")
 + 1,saveFile.indexOf("\""));
        int lastIndex = contentType.lastIndexOf("=");
        String boundary = contentType.substring(lastIndex + 1,contentType.length());
        int pos;
        //extracting the index of file 
        pos = file.indexOf("filename=\"");
        pos = file.indexOf("\n", pos) + 1;
        pos = file.indexOf("\n", pos) + 1;
        pos = file.indexOf("\n", pos) + 1;
        int boundaryLocation = file.indexOf(boundary, pos) - 4;
        int startPos = ((file.substring(0, pos)).getBytes()).length;
        int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;

        // creating a new file with the same name and writing the content in new file
        FileOutputStream fileOut = new FileOutputStream(new File(path,"file.txt"));
        fileOut.write(dataBytes, startPos, (endPos - startPos));
        fileOut.flush();
        fileOut.close();
        String transmit = new String(dataBytes,startPos, (endPos - startPos));
        transmit = transmit.replaceAll("\n","%0A");
        transmit = transmit.replaceAll(" ","%20");
        String redirectURL = "edit.jsp?code=" + transmit;
        response.sendRedirect(redirectURL);

        %><Br><table border="2"><tr><td><b>You have successfully
 upload the file by the name of:</b>
        <% out.println(saveFile); %></td></tr></table> <%
        }
%>
<!DOCTYPE html>
<html>
    <head>
        <title>Edit Code</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="css/bootstrap.min.css" rel="stylesheet" media="screen">

    </head>
    <body>
        
         
    </body>
</html>
