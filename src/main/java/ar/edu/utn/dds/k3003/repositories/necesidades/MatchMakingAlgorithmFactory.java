package ar.edu.utn.dds.k3003.repositories.necesidades;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;

import java.util.Map;
import java.util.EnumMap;

public class MatchMakingAlgorithmFactory {
    private static final Map<TipoAlgoritmoEnum, MatchmakingAlgorithm> ALGORITHMS = new EnumMap<>(TipoAlgoritmoEnum.class);

    static {
        ALGORITHMS.put(TipoAlgoritmoEnum.SUB_ATENDIDOS, new PrioridadSubAtendidos());
        ALGORITHMS.put(TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE, new PrioridadPorScore());
    }

    public static MatchmakingAlgorithm getAlgorithm(TipoAlgoritmoEnum tipo) {
        return ALGORITHMS.get(tipo);
    }
}
