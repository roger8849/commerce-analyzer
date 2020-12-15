package co.edu.unal.migration.commerceanalyzer.services.impl;

import co.edu.unal.migration.commerceanalyzer.utils.DatabaseConfigUtils;
import co.edu.unal.migration.commerceanalyzer.services.WordCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultWordReplacerService {
    @Autowired
    private DatabaseConfigUtils databaseConfig;
    @Autowired
    private WordCrawlerService wordCrawlerService;




}
