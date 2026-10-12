<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contexto" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <jsp:include page="/WEB-INF/jsp/fragmentos/head.jsp" />
</head>
<body>
<div class="sidebar-overlay" data-role="overlay"></div>
<div class="app-shell">
  <aside class="sidebar" data-role="sidebar">
    <jsp:include page="/WEB-INF/jsp/fragmentos/sidebar.jsp" />
  </aside>
  <header class="app-header" data-role="header">
    <jsp:include page="/WEB-INF/jsp/fragmentos/header.jsp" />
  </header>

  <main class="app-main">
    <div class="page-heading">
      <div>
        <h1>Eliminar proveedor</h1>
        <p>Confirma que deseas eliminar este proveedor.</p>
      </div>
      <a href="${contexto}/proveedores" class="btn btn-secondary">&larr; Volver al listado</a>
    </div>

    <form class="card" method="post" action="${contexto}/proveedores/eliminar">
      <div class="card__body">
        <c:choose>
          <c:when test="${not empty errorGeneral}">
            <div class="alert alert-danger" role="alert"><c:out value="${errorGeneral}" /></div>
          </c:when>
          <c:otherwise>
            <div class="alert alert-warning" role="alert">Esta acción no se puede deshacer.</div>
          </c:otherwise>
        </c:choose>

        <dl class="form-row">
          <div>
            <dt class="form-label">Nombre</dt>
            <dd><c:out value="${proveedor.nombre}" /></dd>
          </div>
          <div>
            <dt class="form-label">Contacto</dt>
            <dd><c:out value="${proveedor.contacto}" default="—" /></dd>
          </div>
          <div>
            <dt class="form-label">Teléfono</dt>
            <dd><c:out value="${proveedor.telefono}" default="—" /></dd>
          </div>
          <div>
            <dt class="form-label">Correo</dt>
            <dd><c:out value="${proveedor.correo}" default="—" /></dd>
          </div>
        </dl>

        <input type="hidden" name="idProveedor" value="${proveedor.idProveedor}" />
      </div>
      <div class="card__footer">
        <a href="${contexto}/proveedores" class="btn btn-secondary">Cancelar</a>
        <%-- Si el proveedor tiene compras, reintentar daría el mismo error: se oculta el botón. --%>
        <c:if test="${empty errorGeneral}">
          <button type="submit" class="btn btn-danger">Sí, eliminar proveedor</button>
        </c:if>
      </div>
    </form>
  </main>

  <footer class="app-footer" data-role="footer">
    <jsp:include page="/WEB-INF/jsp/fragmentos/footer.jsp" />
  </footer>
</div>
<script src="${contexto}/js/menu.js" defer></script>
</body>
</html>