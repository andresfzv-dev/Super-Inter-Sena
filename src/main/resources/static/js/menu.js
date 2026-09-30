/**
 * Abre y cierra el menú lateral en pantallas pequeñas.
 * Es la única interacción del prototipo que requiere JavaScript;
 * el contenido de las páginas se genera en el servidor.
 */
(function () {
    "use strict";

    var botonMenu = document.querySelector('[data-role="menu-toggle"]');
    var menuLateral = document.querySelector('[data-role="sidebar"]');
    var fondo = document.querySelector('[data-role="overlay"]');

    if (!botonMenu || !menuLateral || !fondo) {
        return;
    }

    botonMenu.addEventListener("click", function () {
        menuLateral.classList.add("is-open");
        fondo.classList.add("is-visible");
    });

    fondo.addEventListener("click", function () {
        menuLateral.classList.remove("is-open");
        fondo.classList.remove("is-visible");
    });
})();