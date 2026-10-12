<div>
  <p class="sidebar__group-label">Operaciones</p>
  <ul>
    <li>
      <a class="sidebar__link ${paginaActiva == 'proveedores' ? 'is-active' : ''}"
         href="${contexto}/proveedores"
         <c:if test="${paginaActiva == 'proveedores'}">aria-current="page"</c:if>>
                    <span class="sidebar__link-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <path d="m11 13-2.5-2.5a2 2 0 0 0-2.8 0L3 13.2 8.8 19l1.8-1.8m.4-4.2 2.6 2.6a1.8 1.8 0 0 0 2.6 0l3.8-3.8-5.6-5.6-2 2"/>
                        </svg>
                    </span>
        <span>Proveedores</span>
      </a>
    </li>
  </ul>
</div>