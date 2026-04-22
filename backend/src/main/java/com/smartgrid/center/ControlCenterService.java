package com.smartgrid.center;

import com.smartgrid.audit.AuditLogger;
import com.smartgrid.crypto.DataPacker;
import com.smartgrid.dto.AggregationRequest;
import com.smartgrid.dto.StatisticsResponse;
import com.smartgrid.entity.Aggregation;
import com.smartgrid.repository.AggregationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ControlCenterService {

    @Autowired
    private AggregationRepository aggregationRepository;

    @Autowired
    private StatisticsCalculator statisticsCalculator;

    @Autowired
    private AuditLogger auditLogger;

    private final DataPacker dataPacker = new DataPacker(DataPacker.generateDefaultMaxValues());

    public StatisticsResponse processAggregation(AggregationRequest request) {
        try {
            BigInteger sumCiphertext = new BigInteger(request.getSumCiphertext(), 16);
            BigInteger sumSqCiphertext = new BigInteger(request.getSumSqCiphertext(), 16);

            BigInteger packedSum = statisticsCalculator.getPaillier().decrypt(sumCiphertext);
            BigInteger packedSqSum = statisticsCalculator.getPaillier().decrypt(sumSqCiphertext);

            int[] sumValues = dataPacker.unpack(packedSum);
            int[] sumSqValues = dataPacker.unpack(packedSqSum);

            List<String> participants = parseParticipantList(request.getParticipantList());

            BigDecimal[] stats = statisticsCalculator.computeMeanAndVariance(
                    sumValues, sumSqValues, request.getParticipantCount());

            Aggregation aggregation = new Aggregation();
            aggregation.setWindowStart(request.getWindowStart());
            aggregation.setWindowEnd(request.getWindowEnd());
            aggregation.setParticipantCount(request.getParticipantCount());
            aggregation.setParticipantList(request.getParticipantList());
            aggregation.setSumPower(BigDecimal.valueOf(sumValues[2]));
            aggregation.setSumVoltage(BigDecimal.valueOf(sumValues[0]));
            aggregation.setSumCurrent(BigDecimal.valueOf(sumValues[1]));
            aggregation.setFogSignature(request.getFogSignature());
            aggregation.setVerifyResult(true);
            aggregationRepository.save(aggregation);

            auditLogger.logSecurityEvent("CONTROL_CENTER", null, "AGGREGATION_RECEIVED",
                    "Aggregation completed with " + request.getParticipantCount() + " participants");

            StatisticsResponse response = new StatisticsResponse();
            response.setWindowStart(request.getWindowStart());
            response.setWindowEnd(request.getWindowEnd());
            response.setParticipantCount(request.getParticipantCount());
            response.setParticipantList(participants);
            response.setAvgVoltage(stats[0]);
            response.setAvgCurrent(stats[1]);
            response.setAvgPower(stats[2]);
            response.setVarVoltage(stats[6]);
            response.setVarCurrent(stats[7]);
            response.setVarPower(stats[8]);
            response.setSignatureVerified(true);

            return response;
        } catch (Exception e) {
            auditLogger.logSecurityEvent("CONTROL_CENTER", null, "AGGREGATION_FAILED",
                    "Failed to process aggregation: " + e.getMessage());
            throw new RuntimeException("Failed to process aggregation", e);
        }
    }

    public List<StatisticsResponse> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Aggregation> aggregations = aggregationRepository.findByWindowStartBetween(startTime, endTime);
        List<StatisticsResponse> result = new ArrayList<>();

        for (Aggregation agg : aggregations) {
            StatisticsResponse stats = new StatisticsResponse();
            stats.setWindowStart(agg.getWindowStart());
            stats.setWindowEnd(agg.getWindowEnd());
            stats.setParticipantCount(agg.getParticipantCount());
            stats.setParticipantList(parseParticipantList(agg.getParticipantList()));
            stats.setAvgVoltage(agg.getSumVoltage().divide(
                    BigDecimal.valueOf(agg.getParticipantCount()), 2, java.math.RoundingMode.HALF_UP));
            stats.setAvgCurrent(agg.getSumCurrent().divide(
                    BigDecimal.valueOf(agg.getParticipantCount()), 2, java.math.RoundingMode.HALF_UP));
            stats.setAvgPower(agg.getSumPower().divide(
                    BigDecimal.valueOf(agg.getParticipantCount()), 2, java.math.RoundingMode.HALF_UP));
            stats.setSignatureVerified(agg.getVerifyResult());
            result.add(stats);
        }

        return result;
    }

    private List<String> parseParticipantList(String participantListJson) {
        List<String> participants = new ArrayList<>();
        if (participantListJson != null && !participantListJson.isEmpty()) {
            String[] parts = participantListJson.replace("[", "").replace("]", "")
                    .replace("\"", "").split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    participants.add(trimmed);
                }
            }
        }
        return participants;
    }
}
