package com.superinter.usuario.service;

import java.time.Clock;
import java.time.Instant;

import com.superinter.common.security.JwtProperties;
import com.superinter.common.security.SeguridadConfig;
import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;
import com.superinter.usuario.dto.UsuarioResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Emite y lee los tokens JWT de la sesión (RNF-01, RNF-02).
 *
 * <p>El token lleva los datos públicos del usuario y su rol, firmados con HS256. Nunca
 * lleva la contraseña ni su hash: el contenido de un JWT solo está codificado en Base64,
 * no cifrado, y cualquiera que tenga el token puede leerlo.
 */
@Service
public class TokenService {

    static final String CLAIM_NOMBRE = "nombre";
    static final String CLAIM_CORREO = "correo";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties propiedades;
    private final Clock reloj;

    @Autowired
    public TokenService(JwtEncoder jwtEncoder, JwtProperties propiedades) {
        this(jwtEncoder, propiedades, Clock.systemUTC());
    }

    /**
     * Permite a las pruebas fijar la hora de emisión, por ejemplo para obtener un token ya
     * vencido sin esperar a que pase el tiempo de expiración.
     */
    TokenService(JwtEncoder jwtEncoder, JwtProperties propiedades, Clock reloj) {
        this.jwtEncoder = jwtEncoder;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    /**
     * Genera un token para el usuario autenticado.
     *
     * <p>Claims: {@code iss}, {@code sub} (id del usuario), {@code iat}, {@code exp},
     * {@code nombre}, {@code correo} y {@code rol}. El id va en {@code sub} y no el correo,
     * porque el id no cambia aunque el usuario cambie de correo en el futuro.
     */
    public String generar(Usuario usuario) {
        Instant emitidoEn = reloj.instant();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propiedades.emisor())
                .subject(String.valueOf(usuario.getIdUsuario()))
                .issuedAt(emitidoEn)
                .expiresAt(emitidoEn.plus(propiedades.expiracion()))
                .claim(CLAIM_NOMBRE, usuario.getNombre())
                .claim(CLAIM_CORREO, usuario.getCorreo())
                .claim(SeguridadConfig.CLAIM_ROL, usuario.getRol().name())
                .build();

        // El encabezado debe declarar el mismo algoritmo con que se firma la clave HMAC.
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(encabezado, claims)).getTokenValue();
    }

    /** Segundos de vida de cada token, para informarlos al cliente en el inicio de sesión. */
    public long segundosDeExpiracion() {
        return propiedades.expiracion().toSeconds();
    }

    /**
     * Reconstruye los datos del usuario desde un token ya validado por Spring Security.
     * No consulta la base de datos: los datos son los que tenía el usuario al iniciar sesión.
     */
    public UsuarioResponse leerUsuario(Jwt token) {
        return new UsuarioResponse(
                Integer.valueOf(token.getSubject()),
                token.getClaimAsString(CLAIM_NOMBRE),
                token.getClaimAsString(CLAIM_CORREO),
                Rol.desdeCodigo(token.getClaimAsString(SeguridadConfig.CLAIM_ROL)));
    }
}
