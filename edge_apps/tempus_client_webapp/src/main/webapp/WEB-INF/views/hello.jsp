<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Tempus Device Provisioning</title>
</head>
<body>
  <h1>Provision Device</h1>
  <hr>

  <h2>Device provisioned for: ${name}</h2>

   <h2>Device Status :${isConnected}</h2>

 <form action="reconfigure" method="post" onsubmit="return validate()">
 <input id="reconfigure" type="submit" value="Reconfigure" ></input>
 </form>
</body>
</html>