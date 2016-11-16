<%@page session="false"
            contentType="text/html"
            pageEncoding="utf-8"
            import="com.day.cq.i18n.I18n"%><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %><%
%><cq:defineObjects/>
<%@include file="/libs/cq/cloudserviceconfigs/components/configpage/hideeditok.jsp"%>
<%@include file="/libs/cq/cloudserviceconfigs/components/configpage/init.jsp"%>
<%
    I18n i18n = new I18n(request);
    String resPath = resource.getPath().replace("/jcr:content", "");
%>
<div>
    <h3><%= i18n.get("Gcell Export XLIFF Connector Translator Settings") %></h3>
    <img src="<%= xssAPI.encodeForHTMLAttr(thumbnailPath) %>" alt="<%= xssAPI.encodeForHTMLAttr(serviceName) %>" style="float: left;" />
    <ul style="float: left; margin: 0px;">
        <li><div class="li-bullet"><strong><%=
        i18n.get("This is a Gcell Export XLIFF connector service.") %>
        </strong></div></li>
        <li><div class="li-bullet"><strong><%= i18n.get("Source Export XML Filepath") %>: </strong><%=
        xssAPI.encodeForHTML(properties.get("xmlstorepath", "")) %></div></li>
        <li><div class="li-bullet"><strong><%= i18n.get("Target Import XML Filepath") %>: </strong><%=
        xssAPI.encodeForHTML(properties.get("xmlloadpath", "")) %></div></li>
        <li><div class="li-bullet"><strong><%= i18n.get("XLIFF version") %>: </strong><%=
        xssAPI.encodeForHTML(properties.get("xliffdocumentversion", "")) %></div></li>
        <li class="config-successful-message when-config-successful" style="display: none">
            <%=
            i18n.get("Gcell Export XLIFF Connector configuration is successful.") %></li>
    </ul>
</div>