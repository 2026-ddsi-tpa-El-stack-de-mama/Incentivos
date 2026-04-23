package ar.edu.utn.dds.k3003.repositories.depositos;

import ar.edu.utn.dds.k3003.model.Deposito;

import java.util.List;
import java.util.Optional;

public interface DepositoRepository {
    List<Deposito> findAll();

    Optional<Deposito> findById(String id);

    Deposito save(Deposito deposito);

    Deposito deleteById(String id);

    Deposito modifyById(String id, Deposito deposito);
}
