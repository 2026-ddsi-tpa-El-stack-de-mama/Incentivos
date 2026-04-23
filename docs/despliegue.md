```mermaid
graph TB
subgraph DevPC [PC del Desarrollador / JVM]
subgraph Jar [logistica-service.jar]
Componente[Lógica de Negocio]
Storage[(In-Memory Storage)]
end
end

    subgraph CI [GitHub Actions]
        Tests[Test de Cátedra + Propios]
    end

    DevPC <-->|push / verify| CI