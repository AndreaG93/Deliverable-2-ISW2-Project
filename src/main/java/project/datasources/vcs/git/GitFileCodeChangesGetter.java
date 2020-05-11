package project.datasources.vcs.git;

import utilis.external.ExternalApplicationOutputReader;

public class GitFileCodeChangesGetter implements ExternalApplicationOutputReader {

    boolean isFirstLine;
    boolean exit;

    long insertions;
    long deletions;

    private String gitModificationIndicator;

    public GitFileCodeChangesGetter() {

        this.isFirstLine = true;
        this.exit = false;
    }

    @Override
    public void readOutputLine(String input) {

        if (isFirstLine) {

            String[] gitModificationIndicatorArray = input.split("\\s+");

            this.gitModificationIndicator = gitModificationIndicatorArray[gitModificationIndicatorArray.length - 1];
            this.isFirstLine = false;

        } else {

            String[] outputContainingInsertionAndDeletion = input.split("\\s+");

            if (this.gitModificationIndicator.contains("-") && this.gitModificationIndicator.contains("+")) {

                insertions = Long.parseLong(outputContainingInsertionAndDeletion[4]);
                deletions = Long.parseLong(outputContainingInsertionAndDeletion[6]);

            } else if (this.gitModificationIndicator.contains("-")) {

                deletions = Long.parseLong(outputContainingInsertionAndDeletion[4]);

            } else if (this.gitModificationIndicator.contains("+"))
                insertions = Long.parseLong(outputContainingInsertionAndDeletion[4]);

            this.exit = true;
        }
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return this.exit;
    }

    public void clearStatistics() {

        this.insertions = 0;
        this.deletions = 0;
        this.isFirstLine = true;
        this.exit = false;
    }
}

