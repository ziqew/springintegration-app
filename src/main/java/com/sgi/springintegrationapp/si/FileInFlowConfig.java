package com.sgi.springintegrationapp.si;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.LastModifiedFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.messaging.MessageChannel;

import java.io.File;

@Configuration
public class FileInFlowConfig {

    @Bean
    @InboundChannelAdapter(value = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> fileReadingMessageSource() {
        CompositeFileListFilter<File> filters = new CompositeFileListFilter<>();
        filters.addFilter(new SimplePatternFileListFilter("*.txt"));
        filters.addFilter(new LastModifiedFileListFilter());

        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setAutoCreateDirectory(true);
        source.setDirectory(new File("~/share/"));
        source.setFilter(filters);

        return source;
    }

    @Bean
    public FileToStringTransformer fileToStringTransformer() {
        return new FileToStringTransformer();
    }

    @Bean
    public IntegrationFlow processFileFlow() {
        return IntegrationFlows
                .from("fileInputChannel")
                .transform(fileToStringTransformer())
                .handle("fileProcessor", "process").get();
    }

    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }
}