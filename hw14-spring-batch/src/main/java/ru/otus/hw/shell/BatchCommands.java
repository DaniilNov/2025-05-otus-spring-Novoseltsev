package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.service.TemporaryMappingService;

@ShellComponent
@RequiredArgsConstructor
@Slf4j
public class BatchCommands {

    private final JobLauncher jobLauncher;

    private final Job migrateLibraryJob;

    private final TemporaryMappingService temporaryMappingService;

    @ShellMethod(value = "Start migration from MongoDB to H2", key = {"migrate", "start-migration"})
    public String startMigration() {
        try {
            temporaryMappingService.clear();

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(migrateLibraryJob, jobParameters);

            return String.format("Migration completed successfully! Status: %s, Exit Code: %s",
                    jobExecution.getStatus(),
                    jobExecution.getExitStatus().getExitCode());

        } catch (Exception e) {
            log.error("Error during migration", e);
            return "Migration failed: " + e.getMessage();
        }
    }
}