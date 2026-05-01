package com.pe.consultorio.clinico.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.pe.consultorio.clinico.model.ResponseDTO;
import com.pe.consultorio.clinico.model.usuario.request.LoginRequest;
import com.pe.consultorio.clinico.model.usuario.response.AuthDTO;
import com.pe.consultorio.clinico.security.CustomUserDetails;
import com.pe.consultorio.clinico.security.CustomUserDetailsService;
import com.pe.consultorio.clinico.security.JwtService;
import com.pe.consultorio.clinico.service.ServiceUsuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImplUsuario implements ServiceUsuario {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService detailsService;

    @Override
    public ResponseDTO<AuthDTO> login(LoginRequest args) {
        try {
            // 1. Spring valida credenciales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(args.username(), args.password())
            );

            // 2. Traemos la data completa del usuario
            CustomUserDetails user = (CustomUserDetails) detailsService.loadUserByUsername(args.username());

            // 3. Generamos el token
            String token = jwtService.generateToken(user, user.getIdEmpresa(), user.getTipoNegocio(), user.getIdUsuario());

            // 4. Retornamos éxito (Simplificando la creación de variables locales)
            return new ResponseDTO<>(new AuthDTO(token), false, "Login exitoso");

        } catch (BadCredentialsException e) {
            // Error controlado: Contraseña o usuario incorrecto
            log.warn("Intento de login fallido para el usuario: {}", args.username());
            // Mostramos un mensaje amigable, nunca revelamos si fue el usuario o la clave lo que falló
            return new ResponseDTO<>(null, true, "Usuario o contraseña incorrectos");

        } catch (AuthenticationException e) {
            // Error crítico: Base de datos caída, error de red, etc.
            log.error("Error inesperado durante el login del usuario {}: {}", args.username(), e.getMessage(), e);
            // Mensaje genérico para no filtrar información del servidor
            return new ResponseDTO<>(null, true, "Error interno del servidor. Por favor, intente más tarde.");
        }
    }

}
