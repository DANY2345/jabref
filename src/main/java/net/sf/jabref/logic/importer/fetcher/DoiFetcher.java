package net.sf.jabref.logic.importer.fetcher;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import net.sf.jabref.logic.formatter.bibtexfields.NormalizePagesFormatter;
import net.sf.jabref.logic.help.HelpFile;
import net.sf.jabref.logic.importer.FetcherException;
import net.sf.jabref.logic.importer.IdBasedFetcher;
import net.sf.jabref.logic.importer.ImportFormatPreferences;
import net.sf.jabref.logic.importer.fileformat.BibtexParser;
import net.sf.jabref.logic.net.URLDownload;
import net.sf.jabref.logic.util.DOI;
import net.sf.jabref.model.entry.BibEntry;

public class DoiFetcher implements IdBasedFetcher {

    private ImportFormatPreferences preferences;

    public DoiFetcher(ImportFormatPreferences preferences) {
        this.preferences = preferences;
    }

    private String cleanupEncoding(String bibtex) {
        return new NormalizePagesFormatter().format(bibtex);
    }

    @Override
    public String getName() {
        return "DOI to BibTeX";
    }

    @Override
    public HelpFile getHelpPage() {
        return HelpFile.FETCHER_DOI_TO_BIBTEX;
    }

    @Override
    public Optional<BibEntry> performSearchById(String identifier) throws FetcherException {
        Optional<DOI> doi = DOI.build(identifier);

        try {
            if (doi.isPresent()) {
                URL doiURL = new URL(doi.get().getURIAsASCIIString());

                // BibTeX data
                URLDownload download = new URLDownload(doiURL);
                download.addParameters("Accept", "application/x-bibtex");
                String bibtexString = download.downloadToString(StandardCharsets.UTF_8);

                // BibTeX entry
                return BibtexParser.singleFromString(cleanupEncoding(bibtexString), preferences);
            } else {
                throw new FetcherException("Invalid DOI: " + identifier);
            }
        } catch (IOException e) {
            throw new FetcherException("Bad URL when fetching DOI info", e);
        }
    }
}
