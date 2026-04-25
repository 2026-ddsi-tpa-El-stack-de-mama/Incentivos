package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.controllers.DepositoInputDTO;
import ar.edu.utn.dds.k3003.controllers.DonacionInputDTO;
import ar.edu.utn.dds.k3003.exceptions.DepositoNoEncotradoException;
import ar.edu.utn.dds.k3003.repositories.DonadoresYEntidadesDataMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/depositos")
public class DepositoController {

    private final Fachada fachada;
    private final DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper;


    public DepositoController(Fachada fachada, DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper) {
        this.fachada = fachada;
        this.donadoresYEntidadesDataMapper = donadoresYEntidadesDataMapper;
    }

    @GetMapping
    public ResponseEntity<?> getAllDepositos() {
        try {
            List<DepositoDTO> depositos = fachada.obtenerDepositos();
            return depositos != null && !depositos.isEmpty()
                    ? ResponseEntity.ok(depositos)
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving depositos: " + ex.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDeposito(@RequestBody DepositoInputDTO deposito) {
        try {
            DepositoDTO depositoDTO = new DepositoDTO("", null, deposito.nombre(), deposito.direccion(),
                    deposito.capacidadMaxima(), null);
            fachada.agregarDeposito(depositoDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(depositoDTO);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating deposito: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepositoById(@PathVariable("id") String depositoID) {
        try {
            DepositoDTO deposito = fachada.buscarDepositoPorID(depositoID);
            return ResponseEntity.ok(deposito);
        } catch (NoSuchElementException | DepositoNoEncotradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deposito no encontrado");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving deposito: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepositoByID(@PathVariable("id") String depositoID) {
        try {
            DepositoDTO depositoDTO = fachada.deleteDepositoByID(depositoID);
            return depositoDTO != null ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() :
                    ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting deposito: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}/donacion")
    public ResponseEntity<?> gestionarDonacion(@PathVariable("id") String depositoID, @RequestBody DonacionInputDTO donacionInputDTO) {
        try {
            DepositoDTO deposito = fachada.gestionarDonacion(donacionInputDTO.depositoID(), donacionInputDTO.donacionID(), donacionInputDTO.productoID(), donacionInputDTO.cantidad());
            return deposito != null ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error managing donacion: " + ex.getMessage());
        }
    }

}