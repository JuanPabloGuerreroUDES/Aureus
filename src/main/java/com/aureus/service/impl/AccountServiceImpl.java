package com.aureus.service.impl;

import com.aureus.dto.account.CuentaDto;
import com.aureus.exception.AccesoDenegadoException;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.exception.ValidacionException;
import com.aureus.model.Account;
import com.aureus.model.User;
import com.aureus.repository.AccountRepository;
import com.aureus.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<Account> listar(User usuario) {
        // Principal primero, luego las demás por nombre
        return accountRepository.findByUserOrderByEsPrincipalDescNameAsc(usuario);
    }

    @Override
    public Account obtenerPrincipal(User usuario) {
        // Siempre existe — UserServiceImpl la crea al registrar
        return accountRepository.findByUserAndEsPrincipalTrue(usuario)
                .orElseGet(() -> {
                    // Fallback: si por alguna razón no hay principal, tomar la primera
                    List<Account> cuentas = accountRepository.findByUser(usuario);
                    if (cuentas.isEmpty()) {
                        throw new RecursoNoEncontradoException("No hay cuentas para el usuario");
                    }
                    Account primera = cuentas.get(0);
                    primera.setEsPrincipal(true);
                    return accountRepository.save(primera);
                });
    }

    @Override
    @Transactional
    public Account crear(CuentaDto dto, User usuario) {
        if (accountRepository.existsByNameAndUser(dto.getName(), usuario)) {
            throw new ValidacionException("Ya tienes una cuenta con el nombre '" + dto.getName() + "'");
        }
        Account cuenta = new Account(dto.getName(), dto.getIcono(), dto.getDescripcion(), usuario);
        // Las cuentas adicionales NO son principales
        cuenta.setEsPrincipal(false);
        Account guardada = accountRepository.save(cuenta);
        log.info("Cuenta '{}' creada para usuario id={}", dto.getName(), usuario.getId());
        return guardada;
    }

    @Override
    @Transactional
    public Account editar(Long id, CuentaDto dto, User usuario) {
        Account cuenta = obtenerCuentaDelUsuario(id, usuario);
        cuenta.setName(dto.getName());
        cuenta.setIcono(dto.getIcono());
        cuenta.setDescripcion(dto.getDescripcion());
        return accountRepository.save(cuenta);
    }

    @Override
    @Transactional
    public void marcarComoPrincipal(Long id, User usuario) {
        Account nuevaPrincipal = obtenerCuentaDelUsuario(id, usuario);
        if (nuevaPrincipal.isEsPrincipal()) return; // ya es principal, nada que hacer

        // Quitar el flag de la actual principal
        accountRepository.findByUserAndEsPrincipalTrue(usuario).ifPresent(anterior -> {
            anterior.setEsPrincipal(false);
            accountRepository.save(anterior);
        });

        nuevaPrincipal.setEsPrincipal(true);
        accountRepository.save(nuevaPrincipal);
        log.info("Cuenta '{}' (id={}) marcada como principal", nuevaPrincipal.getName(), id);
    }

    @Override
    @Transactional
    public void eliminar(Long id, User usuario) {
        Account cuenta = obtenerCuentaDelUsuario(id, usuario);

        if (cuenta.isEsPrincipal()) {
            throw new ValidacionException(
                "No puedes eliminar tu cuenta principal. " +
                "Primero establece otra cuenta como principal y luego intenta de nuevo.");
        }
        accountRepository.delete(cuenta);
        log.info("Cuenta '{}' (id={}) eliminada", cuenta.getName(), id);
    }

    private Account obtenerCuentaDelUsuario(Long id, User usuario) {
        return accountRepository.findByIdAndUser(id, usuario)
                .orElseThrow(AccesoDenegadoException::new);
    }
}
