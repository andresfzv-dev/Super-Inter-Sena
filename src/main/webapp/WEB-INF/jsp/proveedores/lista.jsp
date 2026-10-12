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
                <h1>Gestión de proveedores</h1>
                <p>Administra los proveedores que abastecen a Super Inter.</p>
            </div>
            <a href="${contexto}/proveedores/nuevo" class="btn btn-primary">+ Registrar proveedor</a>
        </div>

        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success" role="status"><c:out value="${mensajeExito}" /></div>
        </c:if>

        <div class="card">
            <div class="card__body">
                <form class="table-toolbar" method="get" action="${contexto}/proveedores" role="search">
                    <div class="search-field">
                        <span class="search-field__icon" aria-hidden="true">&#128269;</span>
                        <input type="search" class="form-control" name="nombre"
                               value="${fn:escapeXml(nombreBuscado)}"
                               placeholder="Buscar por nombre..." aria-label="Buscar proveedor por nombre" />
                    </div>
                    <div class="table-toolbar__filters">
                        <button type="submit" class="btn btn-primary">Buscar</button>
                        <a href="${contexto}/proveedores" class="btn btn-secondary">Limpiar</a>
                    </div>
                </form>

                <c:choose>
                    <c:when test="${empty proveedores}">
                        <div class="empty-state">
                            <div class="empty-state__icon" aria-hidden="true">&#129309;</div>
                            <p>No se encontraron proveedores.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrapper">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th scope="col">Proveedor</th>
                                    <th scope="col">Contacto</th>
                                    <th scope="col">Teléfono</th>
                                    <th scope="col">Correo</th>
                                    <th scope="col">Acciones</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="proveedor" items="${proveedores}">
                                    <tr>
                                        <td><c:out value="${proveedor.nombre}" /></td>
                                        <td><c:out value="${proveedor.contacto}" default="—" /></td>
                                        <td><c:out value="${proveedor.telefono}" default="—" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty proveedor.correo}">
                                                    <a href="mailto:${fn:escapeXml(proveedor.correo)}"><c:out value="${proveedor.correo}" /></a>
                                                </c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="flex flex-gap-2">
                                                <a class="btn btn-secondary btn-sm"
                                                   href="${contexto}/proveedores/editar?id=${proveedor.idProveedor}">Editar</a>
                                                <a class="btn btn-danger btn-sm"
                                                   href="${contexto}/proveedores/eliminar?id=${proveedor.idProveedor}">Eliminar</a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <p class="form-hint">${fn:length(proveedores)} proveedor(es) encontrado(s).</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </main>

    <footer class="app-footer" data-role="footer">
        <jsp:include page="/WEB-INF/jsp/fragmentos/footer.jsp" />
    </footer>
</div>
<script src="${contexto}/js/menu.js" defer></script>
</body>
</html>