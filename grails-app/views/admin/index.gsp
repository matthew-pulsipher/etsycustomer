<%--
  Created by IntelliJ IDEA.
  User: dwpulsip
  Date: 5/23/13
  Time: 9:30 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>
<g:if test="${google == null}">
    <oauth:connect provider="google">Connect to Google</oauth:connect>
</g:if>
<g:else>
    Google Connected
</g:else>

<g:if test="${etsy == null}">
    <oauth:connect provider="etsy">Connect to Etsy</oauth:connect>
</g:if>
<g:else>
    Etsy Connected
</g:else></body>
</html>