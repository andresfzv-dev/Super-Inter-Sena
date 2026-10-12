<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
                <h1><c:out value="${tituloPagina}" /></h1>
                <p>
                    <c:choose>
                        <c:when test="${esEdicion}">Modifica la información del proveedor.</c:when>
                        <c:otherwise>Completa la información del proveedor. Solo el nombre es obligatorio.</c:otherwise>
                    </c:choose>
                </p>
            </div>
            <a href="${contexto}/proveedores" class="btn btn-secondary">&larr; Volver al listado</a>
        </div>

        <form class="card" method="post" action="${accionFormulario}" novalidate>
            <div class="card__body">
                <c:if test="${not empty errorGeneral}">
                    <div class="alert alert-danger" role="alert"><c:out value="${errorGeneral}" /></div>
                </c:if>
                <c:if test="${not empty errores}">
                    <div class="alert alert-warning" role="alert">Revisa los campos marcados.</div>
                </c:if>

                <c:if test="${esEdicion}">
                    <input type="hidden" name="idProveedor" value="${idProveedor}" />
                </c:if>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="nombre">Nombre del proveedor<span class="required">*</span></label>
                        <input class="form-control" type="text" id="nombre" name="nombre" maxlength="100"
                               value="${fn:escapeXml(formulario.nombre)}" placeholder="Ej: Postobón S.A." />
                        <p class="form-error"><c:out value="${errores.nombre}" /></p>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="contacto">Persona de contacto</label>
                        <input class="form-control" type="text" id="contacto" name="contacto" maxlength="100"
                               value="${fn:escapeXml(formulario.contacto)}" placeholder="Ej: Laura Gómez" />
                        <p class="form-error"><c:out value="${errores.contacto}" /></p>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="telefono">Teléfono</label>
                        <input class="form-control" type="tel" id="telefono" name="telefono" maxlength="20"
                               value="${fn:escapeXml(formulario.telefono)}" placeholder="Ej: +57 300 123 4567" />
                        <p class="form-error"><c:out value="${errores.telefono}" /></p>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="correo">Correo electrónico</label>
                        <input class="form-control" type="email" id="correo" name="correo" maxlength="254"
                               value="${fn:escapeXml(formulario.correo)}" placeholder="Ej: ventas@proveedor.com" />
                        <p class="form-error"><c:out value="${errores.correo}" /></p>
                    </div>
                </div>
            </div>
            <div class="card__footer">
                <a href="${contexto}/proveedores" class="btn btn-secondary">Cancelar</a>
                <button type="submit" class="btn btn-primary">
                    ${esEdicion ? 'Guardar cambios' : 'Guardar proveedor'}
                </button>
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