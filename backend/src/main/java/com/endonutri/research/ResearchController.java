package com.endonutri.research;

import com.endonutri.auth.AppUser;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/research")
public class ResearchController {
    private final EntityManager entityManager;
    private final Path uploadDir;
    private final int minCohort;

    public ResearchController(EntityManager entityManager,
        @Value("${app.upload-dir}") String dir,
        @Value("${app.min-research-cohort}") int minCohort) throws Exception {
        this.entityManager = entityManager;
        this.uploadDir = Path.of(dir).toAbsolutePath().normalize();
        this.minCohort = Math.max(5, minCohort);
        Files.createDirectories(uploadDir);
    }

    @GetMapping("/consented-symptoms")
    public ResponseEntity<?> consentedAggregates() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
            "SELECT s.symptom, s.recorded_on, COUNT(*) AS entries, " +
            "COUNT(DISTINCT s.patient_id) AS cohort, ROUND(AVG(s.severity)::numeric, 2) AS mean_severity " +
            "FROM symptom_records s JOIN patient_profiles p ON p.user_id=s.patient_id " +
            "WHERE p.research_consent=TRUE GROUP BY s.symptom, s.recorded_on " +
            "HAVING COUNT(DISTINCT s.patient_id) >= :minimum ORDER BY s.recorded_on"
        ).setParameter("minimum", minCohort).getResultList();
        List<Map<String,Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(Map.of("symptom", r[0], "date", r[1].toString(),
                "entries", ((Number)r[2]).longValue(), "cohortSize", ((Number)r[3]).longValue(),
                "meanSeverity", r[4]));
        }
        return ResponseEntity.ok(Map.of("minimumCohort", minCohort, "aggregates", result,
            "privacyNote", "Only grouped aggregates meeting the minimum distinct-patient threshold are returned."));
    }

    @PostMapping(value="/datasets", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file, Authentication auth) throws Exception {
        String original = Objects.toString(file.getOriginalFilename(), "dataset.csv");
        if (file.isEmpty() || file.getSize() > 10_000_000 || !original.toLowerCase(Locale.ROOT).endsWith(".csv"))
            return ResponseEntity.badRequest().body(Map.of("error", "Upload a non-empty CSV file smaller than 10 MB."));
        String stored = UUID.randomUUID() + ".csv";
        Path target = uploadDir.resolve(stored).normalize();
        if (!target.getParent().equals(uploadDir)) return ResponseEntity.badRequest().build();
        Files.copy(file.getInputStream(), target, StandardCopyOption.CREATE_NEW);
        return ResponseEntity.status(201).body(Map.of("datasetId", stored, "originalFilename",
            original.replaceAll("[\\r\\n]", ""), "bytes", file.getSize(), "status", "UPLOADED"));
    }

    public record StatsInput(@NotEmpty List<@NotNull @DecimalMin("-1.0E100") @DecimalMax("1.0E100") Double> values) {}
    @PostMapping("/statistics/descriptive")
    public ResponseEntity<?> descriptive(@Valid @RequestBody StatsInput input) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        input.values().forEach(stats::addValue);
        return ResponseEntity.ok(Map.of("count", stats.getN(), "mean", stats.getMean(),
            "median", stats.getPercentile(50), "min", stats.getMin(), "max", stats.getMax(),
            "standardDeviation", stats.getStandardDeviation(), "variance", stats.getVariance()));
    }
}
