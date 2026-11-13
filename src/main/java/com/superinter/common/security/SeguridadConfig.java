package com.superinter.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security: cadena de filtros sin sesión, cifrado de contraseñas
 * con BCrypt y emisión y validación de tokens JWT firmados con HS256.
 *
 * <p>Atiende RNF-01 (autenticación con usuario y contraseña), RNF-02 (el rol viaja en el
 * token) y RNF-03 (contraseñas cifradas).
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SeguridadConfig {

    /** Nombre del claim del token que lleva el código del rol del usuario. */
    public static final String CLAIM_ROL = "rol";

    /** Prefijo que Spring Security espera para que hasRole("CAJERO") funcione. */
    private static final String PREFIJO_ROL = "ROLE_";

    private static final MacAlgorithm ALGORITMO_FIRMA = MacAlgorithm.HS256;

    /** Único endpoint protegido en esta evidencia: demuestra el uso del token. */
    public static final String RUTA_PERFIL = "/api/auth/perfil";

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http,
                                                 RespuestaErrorSeguridad respuestaError) throws Exception {
        http
                // CSRF se desactiva porque la autenticación no usa cookies ni sesión: el token
                // viaja en la cabecera Authorization, que un sitio externo no puede agregar a
                // una petición que el navegador envíe por su cuenta. Además, los formularios
                // JSP envían POST sin token CSRF y con CSRF activo responderían 403.
                // Debe activarse si se agrega un inicio de sesión basado en sesión o cookies.
                .csrf(AbstractHttpConfigurer::disable)
                // Sin sesión: cada petición se autentica solo con su token, así el servidor
                // no guarda estado de inicio de sesión ni emite la cookie JSESSIONID para ello.
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // El inicio de sesión es el endpoint REST; los formularios y la autenticación
                // básica de Spring Security abrirían otra forma de entrar que no se usa.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Solo el perfil exige token. Lo demás sigue público para no romper las JSP ni
                // el front-end en React, que aún no tienen pantalla de inicio de sesión;
                // proteger los demás módulos queda pendiente para una evidencia futura.
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers(RUTA_PERFIL).authenticated()
                        .anyRequest().permitAll())
                // El 401 y el 403 se responden en JSON (ProblemDetail) y no con un cuerpo vacío.
                // Se configuran en los dos lugares: exceptionHandling atiende las peticiones
                // sin token y oauth2ResourceServer las que traen un token inválido o vencido.
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(respuestaError)
                        .accessDeniedHandler(respuestaError))
                .oauth2ResourceServer(servidorRecursos -> servidorRecursos
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorAutenticacion()))
                        .authenticationEntryPoint(respuestaError)
                        .accessDeniedHandler(respuestaError));
        return http.build();
    }

    /**
     * BCrypt incluye una sal aleatoria en cada hash y es lento a propósito, lo que encarece
     * los ataques de diccionario si la tabla de usuarios llegara a filtrarse.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties propiedades) {
        return NimbusJwtEncoder.withSecretKey(propiedades.claveSecreta())
                .algorithm(ALGORITMO_FIRMA)
                .build();
    }

    /**
     * Además de la firma, valida las fechas del token ({@code exp} y {@code nbf}) y que el
     * emisor sea esta aplicación. Fijar el algoritmo impide que se acepte un token firmado
     * con otro distinto al que se usa para emitirlos.
     */
    @Bean
    public JwtDecoder jwtDecoder(JwtProperties propiedades) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(propiedades.claveSecreta())
                .macAlgorithm(ALGORITMO_FIRMA)
                .build();
        OAuth2TokenValidator<Jwt> validador = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(propiedades.emisor()));
        decodificador.setJwtValidator(validador);
        return decodificador;
    }

    /**
     * Convierte el claim {@code rol} (por ejemplo, {@code CAJERO}) en la autoridad
     * {@code ROLE_CAJERO}. Sin esto, Spring Security buscaría los permisos en el claim
     * {@code scope}, que estos tokens no tienen.
     */
    private JwtAuthenticationConverter convertidorAutenticacion() {
        JwtGrantedAuthoritiesConverter convertidorRoles = new JwtGrantedAuthoritiesConverter();
        convertidorRoles.setAuthoritiesClaimName(CLAIM_ROL);
        convertidorRoles.setAuthorityPrefix(PREFIJO_ROL);

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(convertidorRoles);
        return convertidor;
    }
}
