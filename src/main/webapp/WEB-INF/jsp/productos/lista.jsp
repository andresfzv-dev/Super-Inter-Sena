<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contexto" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="es_CO" />
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
                <h1>Gestión de productos</h1>
                <p>Consulta, busca y administra el catálogo de productos.</p>
            </div>
            <a href="${contexto}/productos/nuevo" class="btn btn-primary">+ Registrar producto</a>
        </div>

        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success" role="status">
                <c:out value="${mensajeExito}" />
            </div>
        </c:if>

        <div class="card">
            <div class="card__body">
                <form class="table-toolbar" method="get" action="${contexto}/productos" role="search">
                    <div class="search-field">
                        <span class="search-field__icon" aria-hidden="true">&#128269;</span>
                        <input type="search" class="form-control" name="nombre"
                               value="${fn:escapeXml(nombreBuscado)}"
                               placeholder="Buscar producto por nombre..."
                               aria-label="Buscar producto por nombre" />
                    </div>
                    <div class="table-toolbar__filters">
                        <select class="form-control" name="idCategoria" aria-label="Filtrar por categoría">
                            <option value="">Todas las categorías</option>
                            <c:forEach var="categoria" items="${categorias}">
                                <option value="${categoria.idCategoria}"
                                    ${categoria.idCategoria == idCategoriaSeleccionada ? 'selected' : ''}>
                                    <c:out value="${categoria.nombre}" />
                                </option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn btn-primary">Buscar</button>
                        <a href="${contexto}/productos" class="btn btn-secondary">Limpiar</a>
                    </div>
                </form>

                <c:choose>
                    <c:when test="${empty productos}">
                        <div class="empty-state">
                            <div class="empty-state__icon" aria-hidden="true">&#128230;</div>
                            <p>No se encontraron productos.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrapper">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th scope="col">Código</th>
                                    <th scope="col">Producto</th>
                                    <th scope="col">Categoría</th>
                                    <th scope="col">Precio</th>
                                    <th scope="col">Stock</th>
                                    <th scope="col">Vencimiento</th>
                                    <th scope="col">Acciones</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="producto" items="${productos}">
                                    <tr>
                                        <td><c:out value="${producto.codigo}" /></td>
                                        <td><c:out value="${producto.nombre}" /></td>
                                        <td><c:out value="${producto.categoria.nombre}" /></td>
                                        <td>
                                            <fmt:formatNumber value="${producto.precio}" type="currency"
                                                              currencySymbol="$" maxFractionDigits="0" />
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${producto.stock == 0}">
                                                    <span class="badge badge-danger">Agotado</span>
                                                </c:when>
                                                <c:otherwise>${producto.stock}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${empty producto.fechaVencimiento ? '—' : producto.fechaVencimiento}</td>
                                        <td>
                                            <div class="flex flex-gap-2">
                                                <a class="btn btn-secondary btn-sm"
                                                   href="${contexto}/productos/editar?id=${producto.idProducto}">Editar</a>
                                                <a class="btn btn-danger btn-sm"
                                                   href="${contexto}/productos/eliminar?id=${producto.idProducto}">Eliminar</a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <p class="form-hint">${fn:length(productos)} producto(s) encontrado(s).</p>
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