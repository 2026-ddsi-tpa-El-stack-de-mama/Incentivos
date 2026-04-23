package ar.edu.utn.dds.k3003.repositories.depositos;

import ar.edu.utn.dds.k3003.model.Deposito;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryDepositosRepository implements DepositoRepository {

    private List<Deposito> depositos;
    private AtomicLong idSecuencial = new AtomicLong(1);

    public InMemoryDepositosRepository() {
        this.depositos = new ArrayList<>();
    }

    @Override
    public List<Deposito> findAll() {
        return this.depositos;
    }

    @Override
    public Optional<Deposito> findById(String id) {
        return this.depositos.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    @Override
    public Deposito save(Deposito deposito) {
        Deposito depositoConID = deposito;
        depositoConID.setId(String.valueOf(idSecuencial.getAndIncrement()));

        this.depositos.add(depositoConID);
        return this.findById(depositoConID.getId()).get();
    }

    @Override
    public Deposito deleteById(String id) {
        val deposito = this.findById(id);
        this.depositos.remove(deposito.get());
        return deposito.get();
    }

    @Override
    public Deposito modifyById(String id, Deposito deposito) {
        Optional<Deposito> depositoToModify = this.findById(id);
        if (depositoToModify.isPresent()) {
            Deposito existingDeposito = depositoToModify.get();
            existingDeposito.setNombre(deposito.getNombre());
            existingDeposito.setDireccion(deposito.getDireccion());
            existingDeposito.setCapacidadMaxima(deposito.getCapacidadMaxima());
            existingDeposito.setStockActual(deposito.getStockActual());

            return deposito;
        }
        throw new NoSuchElementException("No Deposito found with the specified ID");
    }
}
