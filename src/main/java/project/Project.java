package project;

import core.ApplicationEntryPoint;
import core.ProjectReleases;
import core.vcs.Git;
import core.vcs.VersionControlSystem;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Project {

    public final String name;
    private final VersionControlSystem versionControlSystem;
    private final Logger logger;

    public Release[] releases;

    public AbstractMap<LocalDate, Commit> commits;

    public Project(String name, String repositoryURL) {
        this.name = name;
        this.versionControlSystem = new Git(repositoryURL, "C://" + name);

        this.logger = Logger.getLogger(ApplicationEntryPoint.class.getName());
    }

    public static <T> List<List<T>> chunks(List<T> input, int n) {

        List<List<T>> chunks = new ArrayList<>();

        int lowerBound = 0;
        int upperBound;

        do {
            upperBound = Math.min(input.size(), lowerBound + n);

            List<T> chunk = input.subList(lowerBound, upperBound);
            chunks.add(chunk);

            lowerBound += n;

        } while (lowerBound != input.size());


        return chunks;
    }

    public void buildDataset() {

        versionControlSystem.cloneRepositoryLocally();

        this.releases = ProjectReleases.downloadMetadata(this.name);

        for (Release currentRelease : this.releases) {

            Commit releaseCommit = this.versionControlSystem.getReleaseCommit(currentRelease.releaseDate);

            currentRelease.files = this.versionControlSystem.getFiles(releaseCommit.hash, releaseCommit.hash);

            List<List<ProjectFile>> portions = chunks(currentRelease.files, currentRelease.files.size() / 4);
            List<Thread> threadList = new ArrayList<>();

            for (List<ProjectFile> subList : portions) {

                Runnable runnable = new DatasetBuilderThread(subList, versionControlSystem, releaseCommit);
                Thread thread = new Thread(runnable);

                thread.start();
                threadList.add(thread);
            }

            try {

                for (Thread currentThread : threadList)
                    currentThread.join();

            } catch (InterruptedException e) {

                logger.severe(e.getMessage());
                System.exit(e.hashCode());
            }

            break;
        }
    }
}