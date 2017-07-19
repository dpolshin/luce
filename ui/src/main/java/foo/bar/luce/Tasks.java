package foo.bar.luce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Tasks {
    private static final Logger LOG = LoggerFactory.getLogger(Tasks.class);
    private JProgressBar progressBar;
    private Map<Long, Job> activeJobs = new ConcurrentHashMap<>();

    public Tasks(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }


    public <T> void run(Job<T> job) {
        activeJobs.put(job.getId(), job);
        job.onDispose(() -> dispose(job));
        SwingUtilities.invokeLater(() -> progressBar.setVisible(true));
        job.execute();
    }

    public void cancel(JobDescription jobDescription) {
        Long id = jobDescription.getId();
        if (activeJobs.containsKey(id)) {
            Job job = activeJobs.get(id);
            job.cancel(true);
            LOG.info("job '{}' cancelled", job.getDescription());
            activeJobs.remove(id);
        }
    }

    public void dispose(Job job) {
        activeJobs.remove(job.getId());
        if (activeJobs.isEmpty()) {
            SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
        }
    }

    public List<JobDescription> getRunningTasks() {
        return activeJobs.values().stream().map(j -> new JobDescription(j.getId(), j.getDescription())).collect(Collectors.toList());
    }

    public static class JobDescription {
        private Long id;
        private String description;

        public JobDescription(Long id, String description) {
            this.id = id;
            this.description = description;
        }

        private Long getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
