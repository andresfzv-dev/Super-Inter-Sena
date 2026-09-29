<%@ page pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contexto" value="${pageContext.request.contextPath}" />
<div class="sidebar__brand">
    <span class="sidebar__brand-logo">SI</span>
    <span class="sidebar__brand-text">Super Inter</span>
</div>
<nav class="sidebar__nav" aria-label="Navegación principal">
    <div>
        <p class="sidebar__group-label">Inventario</p>
        <ul>
            <li>
                <a class="sidebar__link ${paginaActiva == 'productos' ? 'is-active' : ''}"
                   href="${contexto}/productos"
                   <c:if test="${paginaActiva == 'productos'}">aria-current="page"</c:if>>
                    <span class="sidebar__link-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <path d="M21 8 12 3 3 8v8l9 5 9-5V8Zm-9-3.3 6.3 3.5L12 12l-6.3-3.8L12 4.7ZM5 9.6l6 3.4v6.9l-6-3.4V9.6Zm8 10.3v-6.9l6-3.4v6.9l-6 3.4Z"/>
                        </svg>
                    </span>
                    <span>Productos</span>
                </a>
            </li>
        </ul>
    </div>
</nav>