package nl.inl.blacklab.server.lib.results;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import nl.inl.blacklab.index.DocIndexerFactory.Format;
import nl.inl.blacklab.index.DocumentFormats;
import nl.inl.blacklab.indexers.config.ConfigInputFormat;
import nl.inl.blacklab.server.exceptions.NotFound;

public class ResultInputFormat {
    private ConfigInputFormat config;

    ResultInputFormat(String formatName) {
        Format format = DocumentFormats.getFormat(formatName);
        if (format == null)
            throw new NotFound("NOT_FOUND", "The format '" + formatName + "' does not exist.");
        if (!format.isConfigurationBased())
            throw new NotFound("NOT_FOUND", "The format '" + formatName
                    + "' is not configuration-based, and therefore cannot be displayed.");
        config = format.getConfig();
    }

    public ConfigInputFormat getConfig() {
        return config;
    }

    public String getXslt() {
        return XslGenerator.generateXsltFromConfig(config);
    }

    public String getFileContents() {
        // Read the format file
        try (BufferedReader reader = config.getFormatFile()) {
            return IOUtils.toString(reader);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }
}
