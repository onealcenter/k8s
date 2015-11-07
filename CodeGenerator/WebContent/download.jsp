<%@ page language="java" contentType="application/x-msdownload" pageEncoding="utf-8"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.*"%>
<%
response.setContentType("application/x-download");
String file = request.getParameter("file");
String[] str = file.split("\\\\");
String filedisplay = str[str.length-1];
filedisplay = URLEncoder.encode(filedisplay,"utf-8");  
response.addHeader("Content-Disposition","attachment;filename=" + filedisplay); 
OutputStream ops = null;  
FileInputStream fis = null;  
try  
{  
	ops = response.getOutputStream();  
	fis = new FileInputStream(file);  
	byte[] b = new byte[1024];  
	int i = 0;  
	while((i = fis.read(b)) > 0)  
	{  
		ops.write(b, 0, i);  
		ops.flush();
	}  	
	out.clear();  
	out = pageContext.pushBody(); 
}catch(Exception e)  
{  
	System.out.println("Error!");  
	e.printStackTrace();  
}  
finally  
{  
	if(fis != null)  
	{  
		fis.close();  
		fis = null;  
	} 
	if(ops != null)  
	{  
		ops.close();  
		ops = null;  
	} 
}
%>