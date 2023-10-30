package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class FileRecoveryLog implements RecoveryLog{
    private static final Logger log = LoggerFactory.getLogger(FileRecoveryLog.class);
    private String baseFileName;
    private File file;
    private FileChannel appendChannel = null;
    private Map<String, TransactionLogRecord> existingLogs;

    /**
     * Initializes a new FileRecoveryLog instance with the given base file name.
     *
     * @param baseFileName The base name for the recovery log files.
     */
    public FileRecoveryLog(String baseFileName) {
        this.existingLogs = new HashMap<>();
        this.baseFileName = baseFileName;
        this.file = createNextVersion();
    }

    /**
     * Creates the next version of the recovery log file, cleaning up the previous one.
     *
     * @return The newly created log file.
     */
    private File createNextVersion() {
        int latestVersion = findLatestVersion();
        String newFileName = baseFileName + (latestVersion + 1) + ".log";

        // Delete the old version, if it exists
        File oldFile = new File(baseFileName + latestVersion + ".log");
        if (oldFile.exists()) {
            // Get the existing logs from the old file
            existingLogs = readLogsFromFile(oldFile);
            oldFile.delete();
        }

        // Create the new version
        File newFile = new File(newFileName);
        try {
            newFile.createNewFile();
            initAppendChannel(newFile);
            // write exisiting unfinished logs to the new file
            if (!existingLogs.isEmpty()){
                // TODO: call cleanUpFinishedLogs() here and write only the unfinished logs to the new file
                cleanUpFinishedLogs();
                for (Map.Entry<String, TransactionLogRecord> entry : existingLogs.entrySet()) {
                    put(entry.getValue());
                }
            }
        } catch (IOException e) {
            log.error("An error occurred while creating the new recovery log file.");
        }
        return newFile;
    }

    /**
     * Finds the latest version of the recovery log file.
     *
     * @return The latest version of the recovery log file.
     */
    private int findLatestVersion() {
        int latestVersion = 0;
        File directory = new File(".");
        File[] files = directory.listFiles((dir, name) -> name.matches(baseFileName + "(\\d+)\\.log"));
        for (File file : files) {
            String fileName = file.getName();
            int version = Integer.parseInt(fileName.replaceAll(baseFileName, "").replaceAll(".log", ""));
            if (version > latestVersion) {
                latestVersion = version;
            }
        }
        return latestVersion;
    }

    /**
     * Initializes the append channel for the given file.
     *
     * @param file The file to initialize the append channel for.
     */
    private void initAppendChannel(File file) {
        try {
            appendChannel = FileChannel.open(file.toPath(), StandardOpenOption.APPEND);
            FileLock lock = appendChannel.tryLock();
            if (lock == null) {
                log.error("Could not acquire lock on recovery log file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void put(TransactionLogRecord trxRecord) {
        writeToFile(trxRecord.getLogRecord());
    }

    public Map<String, TransactionLogRecord> getPendingLogs() {
        Map<String, TransactionLogRecord> pendingTransactions = new HashMap<>();
        Map<String, TransactionLogRecord> transactionLogs = readLogsFromFile(file);

        for (Map.Entry<String, TransactionLogRecord> entry : transactionLogs.entrySet()) {
            String trxId = entry.getKey();
            TransactionLogRecord trxRecord = entry.getValue();

            if (!trxRecord.isCompleted()) {
                pendingTransactions.put(trxId, trxRecord);
            }
        }
        return pendingTransactions;
    }

    /**
     * Write a transaction log entry to the recovery log file.
     *
     * @param str the log entry to write
     */
    private void writeToFile(String str) {
        byte[] bytes = str.getBytes();
        try {
            appendChannel.write(java.nio.ByteBuffer.wrap(bytes));
            log.info(String.format("Wrote to recovery log file: " + str));
        } catch (IOException e) {
            log.error("An error occurred while writing to the recovery log file.");
        }
    }

    public Map<String, TransactionLogRecord> readLogsFromFile(File file) {
        Map<String, TransactionLogRecord> logMap = new HashMap<>();
        if (!file.exists() || file.length() == 0) {
            return null;
        }

        if (appendChannel != null){
            closeEverything();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TransactionLogRecord transactionLogRecord = parseTransactionRecord(line);
                if (transactionLogRecord != null) {
                    logMap.put(transactionLogRecord.transactionId, transactionLogRecord);
                }
            }
        } catch (IOException e) {
            log.error("An error occurred while reading the recovery log file.", e);
        }
        return logMap;
    }
    private TransactionLogRecord parseTransactionRecord(String logLine) {
        String[] logBlocks = logLine.split("\\|");
        if (logBlocks.length == 2) {
            String[] combinedId = logBlocks[0].split(":");
            String transactionStatusString = logBlocks[1];
            String transactionId = combinedId[0];
            String transactionBlockId = combinedId[1];
            // match with RecoveryStatus enum
            RecoveryStatus transactionStatus = RecoveryStatus.getRecoveryStatus(transactionStatusString);
            return new TransactionLogRecord(transactionId, transactionBlockId, transactionStatus);
        }
        // If parsing fails.. TODO: handle parsing fail properly
        log.error("Error while parsing transaction log record: " + logLine + "\nLog file may be corrupted.");
        return null;
    }


    private void cleanUpFinishedLogs(){
        // TODO: logic to clean up completed transaction logs (replace with proper logic)
        // go through the existingLogs map and see the finished and unfinished logs
        // if a log is finished, remove it from the map
        // if a log is unfinished, write it to the new log file
        for (Map.Entry<String, TransactionLogRecord> entry : existingLogs.entrySet()) {
            if (entry.getValue().isCompleted()) {
                existingLogs.remove(entry.getKey());
            }
        }
    }


    public void writeCheckpoint() {
        // TODO:  write checkpoint of the log file
        // get a copy of current log file
        // cleanup finished logs. call cleanUpFinishedLogs() method.
        // discard the existing log file and create a new one with same name
        if (file.length() > 1000){
            File newFile = createNextVersion();
            file = newFile;
        }
    }

    @Override
    public void close() {

    }


    private void closeEverything() {
        try {
            appendChannel.close();
        } catch (IOException e) {
            // nothing to do. java cray cray. :)
        }
    }
}
