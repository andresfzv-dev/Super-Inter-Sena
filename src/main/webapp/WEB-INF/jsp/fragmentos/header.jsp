<%@ page pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<button type="button" class="btn btn-icon btn-secondary app-header__menu-toggle"
        data-role="menu-toggle" aria-label="Abrir menú">
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         stroke-width="2" stroke-linecap="round" aria-hidden="true">
        <path d="M4 6h16M4 12h16M4 18h16"/>
    </svg>
</button>
<div class="app-header__title-group">
    <h1><c:out value="${tituloPagina}" /></h1>
</div>