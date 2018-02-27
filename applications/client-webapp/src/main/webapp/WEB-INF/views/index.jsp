<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<!-- Static content -->
<link rel="stylesheet" href="/resources/css/style.css">
<script type="text/javascript" src="/resources/js/app.js"></script>

<title>Tempus Device Provisioning</title>

</head>
<body>
  <h1>Provision Device</h1>
  <hr>

  <div class="form">
    <form action="hello" method="post" onsubmit="return validate()">
      <table>
        <tr>
          <td>Enter Device Id</td>
          <td><input id="name" name="name"></td>
         </tr>
         <tr>
          <td>Enter Password</td>
          <td><input type="password" id="password" name="password"></td>

        </tr>
        <tr>
           <td>Enter server IP</td>
           <td><input id="serverIP" name="serverIP"></td>
        </tr>
        <tr>
            <td>Enter Port</td>
            <td><input  id="port" name="port"></td>
        </tr>
        <tr>
             </td><td><input type="submit" value="Submit"></td>
        </tr>
      </table>
    </form>
  </div>

</body>
</html>