<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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
                <h1>Eliminar producto</h1>
                <p>Confirma que deseas eliminar este producto del catálogo.</p>
            </div>
            <a href="${contexto}/productos" class="btn btn-secondary">&larr; Volver al listado</a>
        </div>

        <form class="card" method="post" action="${contexto}/productos/eliminar">
            <div class="card__body">
                <c:choose>
                    <c:when test="${not empty errorGeneral}">
                        <div class="alert alert-danger" role="alert"><c:out value="${errorGeneral}" /></div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-warning" role="alert">
                            Esta acción no se puede deshacer.
                        </div>
                    </c:otherwise>
                </c:choose>

                <dl class="form-row">
                    <div>
                        <dt class="form-label">Código</dt>
                        <dd><c:out value="${producto.codigo}" /></dd>
                    </div>
                    <div>
                        <dt class="form-label">Nombre</dt>
                        <dd><c:out value="${producto.nombre}" /></dd>
                    </div>
                    <div>
                        <dt class="form-label">Categoría</dt>
                        <dd><c:out value="${producto.categoria.nombre}" /></dd>
                    </div>
                    <div>
                        <dt class="form-label">Precio</dt>
                        <dd>
                            <fmt:formatNumber value="${producto.precio}" type="currency"
                                              currencySymbol="$" maxFractionDigits="0" />
                        </dd>
                    </div>
                    <div>
                        <dt class="form-label">Stock</dt>
                        <dd>${producto.stock}</dd>
                    </div>
                    <div>
                        <dt class="form-label">Vencimiento</dt>
                        <dd>${empty producto.fechaVencimiento ? '—' : producto.fechaVencimiento}</dd>
                    </div>
                </dl>

                <input type="hidden" name="idProducto" value="${producto.idProducto}" />
            </div>
            <div class="card__footer">
                <a href="${contexto}/productos" class="btn btn-secondary">Cancelar</a>
                <c:if test="${empty errorGeneral}">
                    <button type="submit" class="btn btn-danger">Sí, eliminar producto</button>
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