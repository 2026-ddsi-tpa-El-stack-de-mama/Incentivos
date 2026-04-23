```mermaid
classDiagram
class Asignacion {
-String id
-String paqueteID
-String necesidadID
-LocalDateTime fecha
-EstadoAsginacionEnum estado
-List~HistorialEstadoAsignacion~ historialEstados
+setEstado(EstadoAsginacionEnum)
}

    class HistorialEstadoAsignacion {
        -String asignacionID
        -EstadoAsginacionEnum estado
        -LocalDateTime fechaCambio
    }

    class Deposito {
        -String id
        -String nombre
        -String direccion
        -Integer capacidadMaxima
        -List~Paquete~ stockActual
        +addPaquete(Paquete): Boolean
        +getCapacidadDisponible(): Integer
    }

    class Paquete {
        -String paqueteID
        -String donacionID
        -String productoID
        -Integer cantidad
        -String entidadAsignadaID
    }

    class Donacion {
        -String id
        -String donadorID
        -String depositoID
        -String descripcion
        -String productoID
        -Integer cantidad
        -EstadoDonacionEnum estado
    }

    class Donador {
        -String id
        -String nombre
        -String apellido
        -Integer edad
        -String email
        -String nroDocumento
        -String domicilio
        -EstadoDonadorEnum estado
        -String categoria
    }

    class Entidad {
        -String id
        -String razonSocial
        -String domicilio
        -String telefono
        -String email
        -List~Necesidad~ necesidades
    }

    class Necesidad {
        -String id
        -String entidadID
        -Integer nivelDeUrgencia
        -String descripcion
        -Integer cantidadObjetivo
        -String productoID
        -Integer cantidadRecibida
    }

    Asignacion "1" --> "*" HistorialEstadoAsignacion : historialEstados
    Deposito "1" --> "*" Paquete : stockActual
    Entidad "1" --> "*" Necesidad : necesidades
    Donador "1" --* "*" Donacion : realiza
    Deposito "1" --* "*" Donacion : recibe
    Donacion "1" --* "1" Paquete : genera
    Entidad "1" --* "*" Necesidad : publica
    Asignacion "1" -- "1" Paquete : vincula
    Asignacion "1" -- "1" Necesidad : satisface
    Deposito "1" --* "*" Paquete : almacena