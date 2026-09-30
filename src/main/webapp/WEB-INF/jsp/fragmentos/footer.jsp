<%@ page pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="fechaActual" class="java.util.Date" />
&copy; <fmt:formatDate value="${fechaActual}" pattern="yyyy" /> Super Inter — Sistema de Gestión Integral. Proyecto académico SENA ADSO.