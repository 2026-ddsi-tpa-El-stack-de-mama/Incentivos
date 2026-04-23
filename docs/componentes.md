```mermaid
graph TD
subgraph "Servicio de Logística"
Core[Logística Core]
Repo[(Repositorios Memoria)]
FL(interface FachadaLogística)
end

    Donaciones[Servicio de Donaciones Simulado]
    DyE[Servicio de Donadores y Entidades Simulado]

    %% Relaciones
    FL --- Core
    Core --> Repo
    Core -.->|consulta necesidades| DyE
    Core -.->|actualiza estado| Donaciones