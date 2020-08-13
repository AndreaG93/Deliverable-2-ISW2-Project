package utilis.external;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ExternalApplication {

    private final String name;
    private final File workingDirectory;

    public ExternalApplication(String name, String workingDirectory) {

        this.name = name;
        this.workingDirectory = new File(workingDirectory);
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
        execute(false, null, commands);
    }

    public void execute(ExternalApplicationOutputReader reader, String... commands) {
        execute(false, reader, commands);
    }

    public void executeWithOutputRedirection(ExternalApplicationOutputReader reader, String... commands) {
        execute(true, reader, commands);
    }

    private void execute(boolean outputRedirection, ExternalApplicationOutputReader reader, String... commands) {

        List<String> commandList = new ArrayList<>();
        commandList.add(this.name);
        commandList.addAll(Arrays.asList(commands));

        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.directory(this.workingDirectory);

        BufferedReader bufferedReader;
        File temporaryFile = null;

        try {

            if (outputRedirection) {

                temporaryFile = File.createTempFile("tempFile", ".tmp");
                processBuilder.redirectOutput(temporaryFile);
            }

            Process process = processBuilder.start();

            if (process.waitFor() != 0) {

                String error = readErrors(process);
                if (!error.equals(""))
                    Logger.getLogger(this.name).severe(error);
            }

            if (outputRedirection)
                bufferedReader = new BufferedReader(new FileReader(temporaryFile));
            else
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null && !reader.isOutputReadingTerminated())
                reader.readOutputLine(currentLine);

            bufferedReader.close();
            process.destroy();

            if (outputRedirection && !temporaryFile.delete()) {

                String errorString = readErrors(process);

                Logger.getLogger(this.name).severe(errorString);
                System.exit(1);
            }

        } catch (Exception e) {

            Logger.getLogger(this.name).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }
}