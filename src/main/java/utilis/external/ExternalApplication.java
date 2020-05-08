package utilis.external;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ExternalApplication {

    private final List<String> operatingSystemProgramAndArguments;
    private final File workingDirectory;
    private final Logger logger;
    private final ExternalApplicationOutputReader nullReader;

    public ExternalApplication(String name, String workingDirectory) {

        this.workingDirectory = new File(workingDirectory);
        this.operatingSystemProgramAndArguments = new ArrayList<>();
        this.operatingSystemProgramAndArguments.add(name);

        this.logger = Logger.getLogger(ExternalApplication.class.getName());
        this.nullReader = new ExternalApplicationOutputReader() {
            @Override
            public void readOutputLine(String input) {
            }

            @Override
            public boolean isOutputReadingTerminated() {
                return false;
            }
        };
    }

    private static String readErrors(Process process) throws IOException {

        StringBuilder output = new StringBuilder();
        String currentErrorLine;

        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        while ((currentErrorLine = stdError.readLine()) != null)
            output.append(currentErrorLine);

        return output.toString();
    }

    public void execute(String... commands) {
        execute(false, this.nullReader, commands);
    }

    public void execute(ExternalApplicationOutputReader reader, String... commands) {
        execute(false, reader, commands);
    }

    public void executeWithOutputRedirection(ExternalApplicationOutputReader reader, String... commands) {
        execute(true, reader, commands);
    }

    private void execute(boolean outputRedirection, ExternalApplicationOutputReader reader, String... commands) {

        this.operatingSystemProgramAndArguments.addAll(Arrays.asList(commands));

        ProcessBuilder processBuilder = new ProcessBuilder(this.operatingSystemProgramAndArguments);
        processBuilder.directory(this.workingDirectory);

        BufferedReader bufferedReader;
        File temporaryFile = null;

        try {

            if (outputRedirection) {

                temporaryFile = File.createTempFile("tempFile", ".tmp");
                processBuilder.redirectOutput(temporaryFile);
            }

            Process process = processBuilder.start();

            if (process.waitFor() != 0)
                this.logger.severe(readErrors(process));

            if (outputRedirection)
                bufferedReader = new BufferedReader(new FileReader(temporaryFile));
            else
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null && !reader.isOutputReadingTerminated())
                reader.readOutputLine(currentLine);

            bufferedReader.close();
            process.destroy();

            if (outputRedirection)
                if (!temporaryFile.delete()) {

                    this.logger.severe(readErrors(process));
                    System.exit(1);
                }

        } catch (Exception e) {

            e.printStackTrace();
            System.exit(e.hashCode());
        }

        this.operatingSystemProgramAndArguments.removeAll(Arrays.asList(commands));
    }
}