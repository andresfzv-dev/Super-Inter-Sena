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
                        <c:when test="${esEdicion}">Modifica la información del producto.</c:when>
                        <c:otherwise>Completa la información del producto para agregarlo al catálogo.</c:otherwise>
                    </c:choose>
                </p>
            </div>
            <a href="${contexto}/productos" class="btn btn-secondary">&larr; Volver al listado</a>
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
                    <input type="hidden" name="idProducto" value="${formulario.idProducto}" />
                </c:if>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="codigo">Código<span class="required">*</span></label>
                        <input class="form-control" type="text" id="codigo" name="codigo" maxlength="50"
                               value="${fn:escapeXml(formulario.codigo)}" placeholder="Ej: 7701234567890" />
                        <p class="form-error"><c:out value="${errores.codigo}" /></p>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="nombre">Nombre del producto<span class="required">*</span></label>
                        <input class="form-control" type="text" id="nombre" name="nombre" maxlength="150"
                               value="${fn:escapeXml(formulario.nombre)}" placeholder="Ej: Arroz Diana x 500g" />
                        <p class="form-error"><c:out value="${errores.nombre}" /></p>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="idCategoria">Categoría<span class="required">*</span></label>
                        <select class="form-control" id="idCategoria" name="idCategoria">
                            <option value="">Seleccione una categoría</option>
                            <c:forEach var="categoria" items="${categorias}">
                                <option value="${categoria.idCategoria}"
                                    ${formulario.idCategoria == categoria.idCategoria ? 'selected' : ''}>
                                    <c:out value="${categoria.nombre}" />
                                </option>
                            </c:forEach>
                        </select>
                        <p class="form-error"><c:out value="${errores.idCategoria}" /></p>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="fechaVencimiento">Fecha de vencimiento</label>
                        <input class="form-control" type="date" id="fechaVencimiento" name="fechaVencimiento"
                               value="${fn:escapeXml(formulario.fechaVencimiento)}" />
                        <p class="form-error"><c:out value="${errores.fechaVencimiento}" /></p>
                        <p class="form-hint">Déjala vacía si el producto no vence.</p>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="precio">Precio de venta (COP)<span class="required">*</span></label>
                        <input class="form-control" type="number" min="0.01" step="0.01" id="precio" name="precio"
                               value="${fn:escapeXml(formulario.precio)}" placeholder="0" />
                        <p class="form-error"><c:out value="${errores.precio}" /></p>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="stock">Stock<span class="required">*</span></label>
                        <input class="form-control" type="number" min="0" step="1" id="stock" name="stock"
                               value="${fn:escapeXml(formulario.stock)}" placeholder="0" />
                        <p class="form-error"><c:out value="${errores.stock}" /></p>
                    </div>
                </div>
            </div>
            <div class="card__footer">
                <a href="${contexto}/productos" class="btn btn-secondary">Cancelar</a>
                <button type="submit" class="btn btn-primary">
                    ${esEdicion ? 'Guardar cambios' : 'Guardar producto'}
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