package co.edu.unal.migration.commerceanalyzer.services;

import co.edu.unal.migration.commerceanalyzer.dto.WordCrawlerInputParams;
import co.edu.unal.migration.commerceanalyzer.dto.WordFound;

import java.io.IOException;
import java.util.Set;

public interface WordCrawlerService {
    Set<WordFound> findOracleKeysInJavaFiles(WordCrawlerInputParams wordCrawlerInputParams) throws IOException;

    Set<WordFound> searchWordInDirectory(Set<WordFound> wordFoundSet, String searchWord);

    Set<String> listJavaFilesFromRootDirectory();
}
