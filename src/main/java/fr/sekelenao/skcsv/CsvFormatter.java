package fr.sekelenao.skcsv;

import fr.sekelenao.skcsv.exception.CsvParsingException;

import java.util.Objects;

final class CsvFormatter {

    private static final class CsvBuffer {

        private final SkCsv csv;

        private SkCsvRow row;

        private StringBuilder cell;

        private CsvBuffer() {
            this.csv = new SkCsv();
            this.row = new SkCsvRow();
            this.cell = new StringBuilder();
        }

        private void appendToCell(char c) {
            cell.append(c);
        }

        private void pushCell() {
            row.add(cell.toString());
            cell = new StringBuilder();
        }

        private void pushRow(){
            csv.add(row);
            row = new SkCsvRow();
        }

        private boolean notEmpty() {
            return !cell.isEmpty();
        }

    }

    private enum QuoteState {ENCOUNTERED, IN, OUT}

    private final char quote;
    private final char delimiter;
    private QuoteState quoteState = QuoteState.OUT;

    CsvFormatter(SkCsvConfig configuration) {
        Objects.requireNonNull(configuration);
        this.quote = configuration.quote();
        this.delimiter = configuration.delimiter();
    }

    private void treatDelimiter(CsvBuffer buffer) {
        switch (quoteState) {
            case OUT -> buffer.pushCell();
            case IN -> buffer.appendToCell(delimiter);
            case ENCOUNTERED -> {
                buffer.pushCell();
                quoteState = QuoteState.OUT;
            }
        }
    }

    private void treatQuote(CsvBuffer buffer, String text) {
        switch (quoteState) {
            case OUT -> {
                if (buffer.notEmpty()) throw new CsvParsingException(text);
                quoteState = QuoteState.IN;
            }
            case IN -> quoteState = QuoteState.ENCOUNTERED;
            case ENCOUNTERED -> {
                buffer.appendToCell(quote);
                quoteState = QuoteState.IN;
            }
        }
    }

    private void treatChar(CsvBuffer buffer, char c, String text){
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(text);
        switch (quoteState){
            case OUT -> {
                SkAssertions.validChar(c);
                buffer.appendToCell(c);
            }
            case IN -> buffer.appendToCell(c);
            case ENCOUNTERED -> throw new CsvParsingException(text);
        }
    }

    SkCsv split(Iterable<String> lines){
        Objects.requireNonNull(lines);
        quoteState = QuoteState.OUT;
        var buffer = new CsvBuffer();
        for(var line : lines){
            var chars = line.toCharArray();
            for (char c : chars) {
                if (c == quote) treatQuote(buffer, line);
                else if (c == delimiter) treatDelimiter(buffer);
                else treatChar(buffer, c, line);
            }
            if(quoteState != QuoteState.IN){
                buffer.pushCell();
                buffer.pushRow();
                quoteState = QuoteState.OUT;
            } else {
                buffer.appendToCell('\n');
            }
        }
        if (quoteState == QuoteState.IN)
            throw new CsvParsingException(buffer.row.toString());
        return buffer.csv;
    }

    static boolean isEscapedChar(char character) {
        return switch (character) {
            case '\n', '\r', '\b', '\f', '\0':
                yield true;
            default: yield false;
        };
    }

    private String formatString(String value) {
        Objects.requireNonNull(value);
        var needQuotes = false;
        var formatted = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (c == quote) {
                needQuotes = true;
                formatted.append(quote).append(quote);
                continue;
            } else if (c == delimiter || isEscapedChar(c)) {
                needQuotes = true;
            }
            formatted.append(c);
        }
        if(needQuotes) return quote + formatted.toString() + quote;
        return formatted.toString();
    }

    String toCsvString(Iterable<String> values) {
        var csvString = new StringBuilder();
        var joiner = "";
        for (var value : values) {
            csvString.append(joiner).append(formatString(value));
            joiner = String.valueOf(delimiter);
        }
        return csvString.toString();
    }

}
